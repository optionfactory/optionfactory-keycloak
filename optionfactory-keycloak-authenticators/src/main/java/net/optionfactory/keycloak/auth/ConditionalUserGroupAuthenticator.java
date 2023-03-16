package net.optionfactory.keycloak.auth;

import java.util.List;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.authentication.authenticators.conditional.ConditionalUserAttributeValueFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

public class ConditionalUserGroupAuthenticator implements ConditionalAuthenticator {

    public static ConditionalUserGroupAuthenticator SINGLETON = new ConditionalUserGroupAuthenticator();

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {

        final var user = context.getUser();
        if (user == null) {
            throw new AuthenticationFlowException("Cannot find user for obtaining particular user attributes. Authenticator: ", AuthenticationFlowError.UNKNOWN_USER);
        }
        final var authConfig = context.getAuthenticatorConfig().getConfig();
        final var expectedGroup = authConfig.get("group");
        final var match = user.getGroupsStream().anyMatch(g -> g.getName().equals(expectedGroup));
        final var negate = Boolean.parseBoolean(authConfig.get("negate"));
        return negate ? !match : match;
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

    public class Factory implements ConditionalAuthenticatorFactory {

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
            return "Flow is executed only if user is in the given group.";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {

            final var groupProp = new ProviderConfigProperty();
            groupProp.setType(ProviderConfigProperty.GROUP_TYPE);
            groupProp.setName("group");
            groupProp.setLabel("Group");
            groupProp.setHelpText("Group");

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
