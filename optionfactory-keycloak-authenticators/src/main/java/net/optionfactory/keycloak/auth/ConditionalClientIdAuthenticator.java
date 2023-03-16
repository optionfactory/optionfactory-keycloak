package net.optionfactory.keycloak.auth;

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

public class ConditionalClientIdAuthenticator implements ConditionalAuthenticator {

    public static ConditionalClientIdAuthenticator SINGLETON = new ConditionalClientIdAuthenticator();

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        final var authSession = context.getAuthenticationSession();
        final var clientIdOrNull = authSession.getClient() == null ? null : authSession.getClient().getClientId();
        final var authConfig = context.getAuthenticatorConfig().getConfig();
        final var expectedClientId = authConfig.get("clientId");
        final var match = expectedClientId.equals(clientIdOrNull);
        final var negate = Boolean.parseBoolean(authConfig.get("negate"));
        return negate ? !match : match;
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
            return "conditional-client-id";
        }

        @Override
        public String getDisplayType() {
            return "Condition - Client id";
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
            return "Flow is executed only if client has the given id.";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {
            final var clientIdProp = new ProviderConfigProperty();
            clientIdProp.setType(ProviderConfigProperty.STRING_TYPE);
            clientIdProp.setName("clientId");
            clientIdProp.setLabel("Client id");
            clientIdProp.setHelpText("Client Id to match");

            final var negateProp = new ProviderConfigProperty();
            negateProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            negateProp.setName("negate");
            negateProp.setLabel("Negate output");
            negateProp.setHelpText("Apply a NOT to the check result.");

            return List.of(clientIdProp, negateProp);
        }

        @Override
        public ConditionalAuthenticator getSingleton() {
            return ConditionalClientIdAuthenticator.SINGLETON;
        }
    }

}
