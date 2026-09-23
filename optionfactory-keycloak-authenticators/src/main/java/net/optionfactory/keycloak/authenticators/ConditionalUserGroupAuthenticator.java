package net.optionfactory.keycloak.authenticators;

import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.RoleUtils;
import org.keycloak.provider.ProviderConfigProperty;

public class ConditionalUserGroupAuthenticator implements ConditionalAuthenticator {

    public static ConditionalUserGroupAuthenticator SINGLETON = new ConditionalUserGroupAuthenticator();

    private static final Logger logger = Logger.getLogger(ConditionalUserGroupAuthenticator.class);

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {

        final var user = context.getUser();
        if (user == null) {
            throw new AuthenticationFlowException("Cannot find user for obtaining particular user attributes. Authenticator: ", AuthenticationFlowError.UNKNOWN_USER);
        }
        final var authConfig = context.getAuthenticatorConfig().getConfig();
        final var configured = authConfig.get("group");
        final var group = group(context.getSession(), context.getRealm(), configured);
        if (group == null) {
            // negating a condition that cannot be answered must not turn it into "always true", which is
            // how a renamed or deleted group would otherwise open a sub-flow to everyone
            logger.warnf("conditional-user-group: no group at '%s', the condition cannot apply", configured);
            return false;
        }
        try (final var ugs = user.getGroupsStream()) {
            // keycloak's own notion of membership: the group itself, or anything below it
            final var match = RoleUtils.isMember(ugs, group);
            final var negate = Boolean.parseBoolean(authConfig.get("negate"));
            return negate ? !match : match;
        }
    }

    /// The console's group picker stores the group's path, not its name. A configuration written by hand
    /// may leave the leading slash off, so a bare value is tried as a top level path too.
    private static GroupModel group(KeycloakSession session, RealmModel realm, String configured) {
        for (final var path : candidatePaths(configured)) {
            final var group = KeycloakModelUtils.findGroupByPath(session, realm, path);
            if (group != null) {
                return group;
            }
        }
        return null;
    }

    static List<String> candidatePaths(String configured) {
        if (configured == null || configured.isBlank()) {
            return List.of();
        }
        final var trimmed = configured.trim();
        return trimmed.startsWith("/") ? List.of(trimmed) : List.of(trimmed, "/" + trimmed);
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {

    }

    public static class Factory implements ConditionalAuthenticatorFactory {

        @Override
        public void init(Scope config) {
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
        }

        @Override
        public void close() {
        }

        @Override
        public String getId() {
            return "conditional-user-group";
        }

        @Override
        public String getDisplayType() {
            return "Condition - User Group";
        }

        @Override
        public boolean isConfigurable() {
            return true;
        }

        @Override
        public Requirement[] getRequirementChoices() {
            return new Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
            };
        }

        @Override
        public boolean isUserSetupAllowed() {
            return false;
        }

        @Override
        public String getHelpText() {
            return "Flow is executed only if the user is in the given group, or in one below it.";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {

            final var groupProp = new ProviderConfigProperty();
            groupProp.setType(ProviderConfigProperty.GROUP_TYPE);
            groupProp.setName("group");
            groupProp.setLabel("Group");
            groupProp.setHelpText("The group whose members the flow applies to. Membership of any group below it counts.");

            final var negateProp = new ProviderConfigProperty();
            negateProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            negateProp.setName("negate");
            negateProp.setLabel("Negate output");
            negateProp.setHelpText("Apply a NOT to the check result.");

            return List.of(groupProp, negateProp);
        }

        @Override
        public ConditionalAuthenticator getSingleton() {
            return ConditionalUserGroupAuthenticator.SINGLETON;
        }
    }

}
