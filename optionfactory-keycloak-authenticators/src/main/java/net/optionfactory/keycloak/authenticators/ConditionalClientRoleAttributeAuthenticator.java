package net.optionfactory.keycloak.authenticators;

import java.util.List;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

public class ConditionalClientRoleAttributeAuthenticator implements ConditionalAuthenticator {

    public static ConditionalClientRoleAttributeAuthenticator INSTANCE = new ConditionalClientRoleAttributeAuthenticator();

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        final var authenticatorConfig = context.getAuthenticatorConfig();
        if (authenticatorConfig == null || authenticatorConfig.getConfig() == null) {
            return false;
        }
        final var authConfig = authenticatorConfig.getConfig();
        final var expectedClientRole = authConfig.get("clientRole");
        final var expectedAttribute = authConfig.get("attribute");
        final var expectedValue = authConfig.get("value");
        final var negate = Boolean.parseBoolean(authConfig.get("negate"));

        final var authSession = context.getAuthenticationSession();
        final var client = authSession.getClient();

        if (client == null) {
            return negate;
        }

        final var role = client.getRole(expectedClientRole);
        if (role == null) {
            return negate;
        }

        final var attribute = role.getFirstAttribute(expectedAttribute);
        if (attribute == null) {
            return negate;
        }

        return negate ^ attribute.equals(expectedValue);
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return false;
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
            return "conditional-client-role-attribute";
        }

        @Override
        public String getDisplayType() {
            return "Condition - Client Role Attribute";
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
            return "Flow is executed only if client has a role attribute with the given value.";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {
            final var roleProp = new ProviderConfigProperty();
            roleProp.setType(ProviderConfigProperty.STRING_TYPE);
            roleProp.setName("clientRole");
            roleProp.setLabel("Client role");
            roleProp.setHelpText("Client role to inspect");

            final var attributeProp = new ProviderConfigProperty();
            attributeProp.setType(ProviderConfigProperty.STRING_TYPE);
            attributeProp.setName("attribute");
            attributeProp.setLabel("Attribute");
            attributeProp.setHelpText("Client role attribute to check");

            final var valueProp = new ProviderConfigProperty();
            valueProp.setType(ProviderConfigProperty.STRING_TYPE);
            valueProp.setName("value");
            valueProp.setLabel("Value");
            valueProp.setHelpText("Client role attribute value to check");

            final var negateProp = new ProviderConfigProperty();
            negateProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            negateProp.setName("negate");
            negateProp.setLabel("Negate output");
            negateProp.setHelpText("Negates the check result.");
            return List.of(roleProp, attributeProp, valueProp, negateProp);
        }

        @Override
        public ConditionalAuthenticator getSingleton() {
            return ConditionalClientRoleAttributeAuthenticator.INSTANCE;
        }
    }

}
