package net.optionfactory.keycloak.authenticators.impersonation;

import java.util.List;
import net.optionfactory.keycloak.authenticators.Authenticators;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ImpersonationSessionNote;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.validation.Validation;

/// Swaps the browser identity when the authorization request carries `impersonate=<userId>`, and restores the
/// operator when it carries `deimpersonate`. Configure as the first `ALTERNATIVE` of a cloned browser flow bound
/// to the target client: with neither parameter the execution is `attempted()` and the normal flow is untouched.
///
/// The operator is proven by the identity cookie, validated independently of flow ordering, so a parameter
/// without a valid session is a no-op rather than a bypass; a role-less holder falls through to a normal login
/// instead of dead-ending, which is what a lingering parameter after a refresh looks like.
///
/// While impersonating, the browser's SSO cookie belongs to the target: any other tab doing a silent SSO check
/// resolves to the target until `deimpersonate` runs. The swap hands the cookie's session to the target in place
/// rather than replacing it - see `restart` for why the obvious removal cannot work - so nothing is orphaned and
/// the previous holder's refresh tokens die with it, while its access tokens live out their expiry.
public class ImpersonationAuthenticator implements Authenticator {

    public static ImpersonationAuthenticator SINGLETON = new ImpersonationAuthenticator();

    private static final Logger logger = Logger.getLogger(ImpersonationAuthenticator.class);

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        final var queryParams = context.getUriInfo().getQueryParameters();
        final var impersonate = queryParams.getFirst("impersonate");
        final var deimpersonate = queryParams.getFirst("deimpersonate");
        if (impersonate == null && deimpersonate == null) {
            context.attempted();
            return;
        }
        final var cookie = AuthenticationManager.authenticateIdentityCookie(context.getSession(), context.getRealm(), true);
        if (cookie == null) {
            // a no-op, but the one attempt an outsider can make at will: unaudited, probing the
            // impersonation surface from an unauthenticated browser would leave nothing behind
            audit(context).event(EventType.IMPERSONATE_ERROR)
                    .detail(Details.REASON, impersonate != null ? "no-session-to-impersonate-from" : "no-session-to-restore")
                    .error(Errors.NOT_LOGGED_IN);
            context.attempted();
            return;
        }
        if (impersonate != null) {
            impersonate(context, cookie, impersonate);
        } else {
            deimpersonate(context, cookie);
        }
    }

    private void impersonate(AuthenticationFlowContext context, AuthenticationManager.AuthResult cookie, String targetId) {
        final var settings = Settings.of(context.getAuthenticatorConfig());
        final var operator = cookie.user();
        if (!holdsRole(context.getRealm(), operator, settings.role())) {
            audit(context).event(EventType.IMPERSONATE_ERROR)
                    .detail(Details.REASON, "operator-lacks-role")
                    .detail(Details.IMPERSONATOR, operator.getUsername())
                    .error(Errors.ACCESS_DENIED);
            context.attempted();
            return;
        }
        final var target = context.getSession().users().getUserById(context.getRealm(), targetId);
        // a holder of the operator role is never impersonable: the role marks the impersonation surface itself, and an operator as target would allow chained swaps and muddy the audit trail
        if (target == null || !target.isEnabled() || target.getServiceAccountClientLink() != null
                || holdsRole(context.getRealm(), target, settings.role())
                || (settings.forbiddenRole() != null && holdsRole(context.getRealm(), target, settings.forbiddenRole()))) {
            audit(context).event(EventType.IMPERSONATE_ERROR)
                    .detail(Details.REASON, "invalid-target")
                    .detail(Details.IMPERSONATOR, operator.getUsername())
                    .error(Errors.ACCESS_DENIED);
            Authenticators.accessDenied(context, "opfaImpersonationInvalidTarget");
            return;
        }
        // lazy triggers (password expiry, otp policy, terms) attach at flow completion, not on the user model: pre-evaluate them for the target so the operator is never challenged; the actions land on the user exactly as they would at the target's own next login
        AuthenticationManager.evaluateRequiredActionTriggers(context.getSession(), context.getAuthenticationSession(), context.getHttpRequest(), context.getEvent(), context.getRealm(), target);
        // a blank email is never verification-challenged (the verify action ignores such users), so it does not count as pending
        if (target.getRequiredActionsStream().findAny().isPresent()
                || !context.getAuthenticationSession().getRequiredActions().isEmpty()
                || (!Validation.isBlank(target.getEmail()) && !target.isEmailVerified())) {
            audit(context).event(EventType.IMPERSONATE_ERROR)
                    .detail(Details.REASON, "target-has-required-actions")
                    .detail(Details.IMPERSONATOR, operator.getUsername())
                    .user(target)
                    .error(Errors.ACCESS_DENIED);
            context.failure(AuthenticationFlowError.ACCESS_DENIED, context.form()
                    .setAttribute("impersonationTarget", target.getUsername())
                    .setAttribute("backUrl", settings.backUrl())
                    .createForm("opfa-impersonation-blocked.ftl"));
            return;
        }
        // backchannelLogout cannot run here: the authorize endpoint re-creates the root authentication session with the same id as the cookie user session, and the logout's cleanup removes exactly that root session, deleting the flow's own state mid-flight. Restarting the session still kills its refresh tokens (they are rejected once issued before the new start time); access tokens live out their expiry and clients get no backchannel notification.
        restart(context, cookie.session(), target);
        final var authSession = context.getAuthenticationSession();
        authSession.setUserSessionNote(ImpersonationSessionNote.IMPERSONATOR_ID.toString(), operator.getId());
        authSession.setUserSessionNote(ImpersonationSessionNote.IMPERSONATOR_USERNAME.toString(), operator.getUsername());
        audit(context).event(EventType.IMPERSONATE)
                .detail(Details.IMPERSONATOR_REALM, context.getRealm().getName())
                .detail(Details.IMPERSONATOR, operator.getUsername())
                .user(target)
                .success();
        context.setUser(target);
        context.success();
    }

    private void deimpersonate(AuthenticationFlowContext context, AuthenticationManager.AuthResult cookie) {
        final var settings = Settings.of(context.getAuthenticatorConfig());
        final UserSessionModel impersonatedSession = cookie.session();
        final var operatorId = impersonatedSession.getNote(ImpersonationSessionNote.IMPERSONATOR_ID.toString());
        if (operatorId == null) {
            // the current session is not an impersonation: the parameter is a no-op
            context.attempted();
            return;
        }
        final var operator = context.getSession().users().getUserById(context.getRealm(), operatorId);
        if (operator == null || !operator.isEnabled() || !holdsRole(context.getRealm(), operator, settings.role())) {
            // a demoted, disabled or deleted operator must not be restored
            audit(context).event(EventType.IMPERSONATE_ERROR)
                    .detail(Details.REASON, "operator-no-longer-eligible")
                    .error(Errors.ACCESS_DENIED);
            context.getSession().sessions().removeUserSession(context.getRealm(), impersonatedSession);
            Authenticators.accessDenied(context, "opfaImpersonationRestoreFailed");
            return;
        }
        restart(context, impersonatedSession, operator);
        audit(context).event(EventType.IMPERSONATE)
                .detail(Details.REASON, "deimpersonation")
                .detail(Details.IMPERSONATOR, operator.getUsername())
                .user(operator)
                .success();
        context.setUser(operator);
        context.success();
    }

    /// Hands the cookie's user session to another user, in place. Removing it and letting the flow create the
    /// replacement cannot work: the root authentication session carries the same id, so `attachSession` re-creates
    /// under it in this very transaction and the create discards the pending removal. `restartSession` is what
    /// `attachSession` itself uses here: one update, new start time, notes and client sessions cleared.
    private static void restart(AuthenticationFlowContext context, UserSessionModel session, UserModel user) {
        final var authSession = context.getAuthenticationSession();
        final var rememberMe = authSession.getAuthNote(Details.REMEMBER_ME);
        session.restartSession(context.getRealm(), user, user.getUsername(),
                context.getConnection().getRemoteHost(), authSession.getProtocol(),
                "true".equalsIgnoreCase(rememberMe),
                authSession.getAuthNote(AuthenticationProcessor.BROKER_SESSION_ID),
                authSession.getAuthNote(AuthenticationProcessor.BROKER_USER_ID));
    }

    private static EventBuilder audit(AuthenticationFlowContext context) {
        return context.getEvent().clone();
    }

    private static boolean holdsRole(RealmModel realm, UserModel user, String roleName) {
        final RoleModel role = realm.getRole(roleName);
        if (role == null) {
            // typically a config typo: without this signal the guard silently denies (operator) or disables (target) with no trace
            logger.warnf("role '%s' does not exist in realm '%s'", roleName, realm.getName());
            return false;
        }
        return user.hasRole(role);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        logger.warn("Unexpected call to action");
        context.failure(AuthenticationFlowError.ACCESS_DENIED);
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }

    public record Settings(String role, String backUrl, String forbiddenRole) {

        public static final Settings DEFAULTS = new Settings("opfa-impersonate", null, null);

        public static Settings of(AuthenticatorConfigModel config) {
            if (config == null || config.getConfig() == null) {
                return DEFAULTS;
            }
            final var values = config.getConfig();
            return new Settings(
                    valueOr(values.get("role"), DEFAULTS.role()),
                    blankToNull(values.get("backUrl")),
                    blankToNull(values.get("forbiddenRole")));
        }

        private static String valueOr(String value, String fallback) {
            return value == null || value.isBlank() ? fallback : value.trim();
        }

        private static String blankToNull(String value) {
            return value == null || value.isBlank() ? null : value.trim();
        }
    }

    public static class Factory implements AuthenticatorFactory {

        @Override
        public Authenticator create(KeycloakSession session) {
            return SINGLETON;
        }

        @Override
        public void init(Config.Scope config) {
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
        }

        @Override
        public void close() {
        }

        @Override
        public String getId() {
            return "opfa-impersonation";
        }

        @Override
        public String getDisplayType() {
            return "Impersonation";
        }

        @Override
        public String getReferenceCategory() {
            return null;
        }

        @Override
        public boolean isConfigurable() {
            return true;
        }

        @Override
        public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
            return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
            };
        }

        @Override
        public boolean isUserSetupAllowed() {
            return false;
        }

        @Override
        public String getHelpText() {
            return "Swaps the identity when the request carries impersonate=<userId> and restores the operator on deimpersonate. Configure as the first ALTERNATIVE of a cloned browser flow bound to the target client.";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {
            final var role = new ProviderConfigProperty();
            role.setType(ProviderConfigProperty.STRING_TYPE);
            role.setName("role");
            role.setLabel("Operator role");
            role.setHelpText("Realm role the operator must hold to impersonate.");
            role.setDefaultValue(Settings.DEFAULTS.role());

            final var backUrl = new ProviderConfigProperty();
            backUrl.setType(ProviderConfigProperty.STRING_TYPE);
            backUrl.setName("backUrl");
            backUrl.setLabel("Back URL");
            backUrl.setHelpText("Where the warning page's back button navigates when a target cannot be impersonated. Falls back to the client's base URL when unset.");

            final var forbiddenRole = new ProviderConfigProperty();
            forbiddenRole.setType(ProviderConfigProperty.STRING_TYPE);
            forbiddenRole.setName("forbiddenRole");
            forbiddenRole.setLabel("Forbidden target role");
            forbiddenRole.setHelpText("Optional realm role that targets must not hold, e.g. to keep administrators out of the impersonation surface.");

            return List.of(role, backUrl, forbiddenRole);
        }
    }
}
