package net.optionfactory.keycloak.authenticators;

import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

public class ClearUserAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(ClearUserAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.clearUser();
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        logger.warn("Unexpected call to action");
        context.failure(AuthenticationFlowError.ACCESS_DENIED);
    }

    @Override
    public boolean requiresUser() {
        return false;
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

    public static class Factory implements AuthenticatorFactory {

        @Override
        public Authenticator create(KeycloakSession session) {
            return new ClearUserAuthenticator();
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
            return "clear-user";
        }

        @Override
        public String getDisplayType() {
            return "Clears the user from the authentication flow";
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
                AuthenticationExecutionModel.Requirement.DISABLED
            };
        }

        @Override
        public boolean isUserSetupAllowed() {
            return false;
        }

        @Override
        public String getHelpText() {
            return "Clears the user from the authentication flow";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {
            return List.of();
        }
    }
}
