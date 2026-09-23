package net.optionfactory.keycloak.authenticators.otp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.AuthorizationEndpointBase;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;

/// Replaces the built-in `VERIFY_EMAIL` required action. To switch a realm over:
///
/// - disable the built-in "Verify Email" action and turn the realm's "Verify email" login toggle off: the toggle
///   makes `RegistrationPassword` defer the password until the email is verified, a mode that expects the built-in
///   link flow and would leave users verified here without a password
/// - enable this action and set it as a default action so it is attached to users at registration;
///   `evaluateTriggers` re-attaches it at every login while the email is unverified, brokered users included
/// - strip any pending `VERIFY_EMAIL` from existing unverified users first: a required action without a provider
///   fails the login
public class VerifyEmailOtp implements RequiredActionProvider {

    public static VerifyEmailOtp SINGLETON = new VerifyEmailOtp();

    public static final String PROVIDER_ID = "opfa-verify-email-otp";

    private static final Logger logger = Logger.getLogger(VerifyEmailOtp.class);

    private static final String FORM_TEMPLATE = "opfa-email-otp.ftl";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        final var user = context.getUser();
        if (user.isEmailVerified()) {
            return;
        }
        // a user with no address can never answer the challenge - requiredActionChallenge ignores them -
        // and only SUCCESS clears a required action, so attaching it here would pin them as permanently
        // pending: invisible to them, but enough to make them unimpersonable and to show up in the admin
        // console forever. Removing it also heals the users that were pinned before this guard existed.
        if (Validation.isBlank(user.getEmail())) {
            user.removeRequiredAction(PROVIDER_ID);
            return;
        }
        // UPDATE_EMAIL re-adds verification by itself once the new address is confirmed
        if (user.getRequiredActionsStream().noneMatch(action -> UserModel.RequiredAction.UPDATE_EMAIL.name().equals(action))) {
            user.addRequiredAction(PROVIDER_ID);
        }
    }

    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        if (context.getUser().isEmailVerified()) {
            context.success();
            return;
        }
        if (Validation.isBlank(context.getUser().getEmail())) {
            context.ignore();
            return;
        }
        final AuthenticationSessionModel authSession = context.getAuthenticationSession();
        // When triggered during registration, the required action must also be tracked by the authentication session
        if ("true".equals(authSession.getAuthNote(AuthenticationManager.NEW_USER_REGISTERED))) {
            authSession.addRequiredAction(PROVIDER_ID);
        }
        authSession.setClientNote(AuthorizationEndpointBase.APP_INITIATED_FLOW, null);
        if (!OtpCodes.exists(context.getSession().singleUseObjects(), codeKey(context.getUser().getId(), context.getUser().getEmail()), Time.currentTimeSeconds())) {
            final var challenge = sendCode(context);
            if (challenge != null) {
                context.challenge(challenge);
                return;
            }
        }
        context.challenge(context.form().createForm(FORM_TEMPLATE));
    }

    @Override
    public void processAction(RequiredActionContext context) {
        if (context.getUser().isEmailVerified()) {
            context.success();
            return;
        }
        final var formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.getFirst("resend") != null) {
            resendCode(context);
            return;
        }
        verifyCode(context, OtpCodes.digitsOnly(formData.getFirst("otp")));
    }

    private void resendCode(RequiredActionContext context) {
        final var challenge = sendCode(context);
        if (challenge != null) {
            context.challenge(challenge);
            return;
        }
        context.challenge(context.form().setInfo("opfaEmailOtpResent").createForm(FORM_TEMPLATE));
    }

    private Response sendCode(RequiredActionContext context) {
        final var settings = Settings.of(context.getConfig());
        final SingleUseObjectProvider store = context.getSession().singleUseObjects();
        final long now = Time.currentTimeSeconds();
        if (!OtpCodes.claimSendSlot(store, cooldownKey(context.getUser().getId()), settings.resendCooldownSeconds(), now)) {
            final var remaining = OtpCodes.remainingCooldown(store, cooldownKey(context.getUser().getId()), now);
            return context.form()
                    .setError(Messages.COOLDOWN_VERIFICATION_EMAIL, remaining != null ? remaining : settings.resendCooldownSeconds())
                    .createForm(FORM_TEMPLATE);
        }
        final var code = OtpGenerator.of(OtpGenerator.Mode.RANDOM, null, settings.codeLength()).generate();
        OtpCodes.put(store, codeKey(context.getUser().getId(), context.getUser().getEmail()), code, settings.codeLifespanSeconds(), now);
        final var event = context.getEvent().clone().event(EventType.SEND_VERIFY_EMAIL).detail(Details.EMAIL, context.getUser().getEmail());
        try {
            context.getSession().getProvider(EmailTemplateProvider.class)
                    .setAuthenticationSession(context.getAuthenticationSession())
                    .setRealm(context.getRealm())
                    .setUser(context.getUser())
                    .send("emailVerificationSubject", "opfa-email-verification.ftl", Map.of("code", code));
            event.success();
            return null;
        } catch (EmailException e) {
            event.clone().detail(Details.REASON, e.getMessage()).user(context.getUser()).error(Errors.EMAIL_SEND_FAILED);
            logger.error("failed to send email verification code", e);
            context.failure(Messages.EMAIL_SENT_ERROR);
            return context.form()
                    .setError(Messages.EMAIL_SENT_ERROR)
                    .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private void verifyCode(RequiredActionContext context, String submitted) {
        final var settings = Settings.of(context.getConfig());
        if (submitted == null || submitted.isEmpty()) {
            context.challenge(context.form().setError("opfaEmailOtpMissing").createForm(FORM_TEMPLATE));
            return;
        }
        final var event = context.getEvent().clone().event(EventType.VERIFY_EMAIL).detail(Details.EMAIL, context.getUser().getEmail());
        final var attempt = OtpCodes.tryValidate(context.getSession().singleUseObjects(), codeKey(context.getUser().getId(), context.getUser().getEmail()), submitted, settings.maxAttempts(), Time.currentTimeSeconds());
        switch (attempt) {
            case VALIDATED: {
                context.getUser().setEmailVerified(true);
                event.success();
                context.success();
                return;
            }
            case NO_CODE: {
                event.detail(Details.REASON, "missing-or-expired-code").error(Errors.INVALID_USER_CREDENTIALS);
                context.challenge(context.form().setError("opfaEmailOtpInvalid").createForm(FORM_TEMPLATE));
                return;
            }
            case TOO_MANY_ATTEMPTS: {
                event.detail(Details.REASON, "too-many-attempts").error(Errors.INVALID_USER_CREDENTIALS);
                context.challenge(context.form().setError("opfaEmailOtpTooManyAttempts").createForm(FORM_TEMPLATE));
                return;
            }
            case INVALID: {
                event.detail(Details.REASON, "invalid-code").error(Errors.INVALID_USER_CREDENTIALS);
                context.challenge(context.form().setError("opfaEmailOtpInvalid").createForm(FORM_TEMPLATE));
                return;
            }
        }
    }

    /// The code proves control of one address, so it is stored under one: an address that changes while a
    /// code is outstanding gets a code of its own, and the one sent to the previous address can no longer
    /// verify it. The next challenge sees no code for the new address and sends one, which is also what a
    /// person would expect after changing their email mid-verification.
    static String codeKey(String userId, String email) {
        return "opfa-verify-email-otp:" + userId + ":" + OtpCodes.sha256Hex(email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }

    /// The cooldown stays keyed by user: it is there to stop a mailbox being flooded, and scoping it to the
    /// address would hand back a fresh send on every change.
    private static String cooldownKey(String userId) {
        return "opfa-verify-email-otp-cooldown:" + userId;
    }

    public record Settings(int codeLength, int codeLifespanSeconds, int maxAttempts, int resendCooldownSeconds) {

        public static final Settings DEFAULTS = new Settings(6, 900, 5, 30);

        public static Settings of(RequiredActionConfigModel config) {
            if (config == null) {
                return DEFAULTS;
            }
            return new Settings(
                    OtpCodes.boundedInt(config.getConfigValue("codeLength"), DEFAULTS.codeLength(), 4, 12),
                    OtpCodes.boundedInt(config.getConfigValue("codeLifespanSeconds"), DEFAULTS.codeLifespanSeconds(), 60, 86_400),
                    OtpCodes.boundedInt(config.getConfigValue("maxAttempts"), DEFAULTS.maxAttempts(), 1, 100),
                    OtpCodes.boundedInt(config.getConfigValue("resendCooldownSeconds"), DEFAULTS.resendCooldownSeconds(), 0, 3_600));
        }
    }

    @Override
    public void close() {
    }

    public static class Factory implements RequiredActionFactory {

        @Override
        public String getDisplayText() {
            return "Verify Email (code)";
        }

        @Override
        public String getId() {
            return PROVIDER_ID;
        }

        @Override
        public List<ProviderConfigProperty> getConfigMetadata() {
            final var props = new ArrayList<ProviderConfigProperty>(MAX_AUTH_AGE_CONFIG_PROPERTIES);

            final var codeLength = new ProviderConfigProperty();
            codeLength.setType(ProviderConfigProperty.STRING_TYPE);
            codeLength.setName("codeLength");
            codeLength.setLabel("Code length");
            codeLength.setHelpText("Number of digits of the verification code.");
            codeLength.setDefaultValue(String.valueOf(Settings.DEFAULTS.codeLength()));
            props.add(codeLength);

            final var codeLifespan = new ProviderConfigProperty();
            codeLifespan.setType(ProviderConfigProperty.STRING_TYPE);
            codeLifespan.setName("codeLifespanSeconds");
            codeLifespan.setLabel("Code lifespan (seconds)");
            codeLifespan.setHelpText("Seconds before an unused verification code expires.");
            codeLifespan.setDefaultValue(String.valueOf(Settings.DEFAULTS.codeLifespanSeconds()));
            props.add(codeLifespan);

            final var maxAttempts = new ProviderConfigProperty();
            maxAttempts.setType(ProviderConfigProperty.STRING_TYPE);
            maxAttempts.setName("maxAttempts");
            maxAttempts.setLabel("Max attempts");
            maxAttempts.setHelpText("Failed submissions allowed before the code is invalidated and a new one must be requested.");
            maxAttempts.setDefaultValue(String.valueOf(Settings.DEFAULTS.maxAttempts()));
            props.add(maxAttempts);

            final var resendCooldown = new ProviderConfigProperty();
            resendCooldown.setType(ProviderConfigProperty.STRING_TYPE);
            resendCooldown.setName("resendCooldownSeconds");
            resendCooldown.setLabel("Resend cooldown (seconds)");
            resendCooldown.setHelpText("Minimum delay between two verification code emails.");
            resendCooldown.setDefaultValue(String.valueOf(Settings.DEFAULTS.resendCooldownSeconds()));
            props.add(resendCooldown);

            return props;
        }

        @Override
        public RequiredActionProvider create(KeycloakSession session) {
            return SINGLETON;
        }

        @Override
        public void init(Scope config) {
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
        }

        @Override
        public void close() {
        }
    }
}
