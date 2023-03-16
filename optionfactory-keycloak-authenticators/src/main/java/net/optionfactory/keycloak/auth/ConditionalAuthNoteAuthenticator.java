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

public class ConditionalAuthNoteAuthenticator implements ConditionalAuthenticator {

    public static ConditionalAuthNoteAuthenticator SINGLETON = new ConditionalAuthNoteAuthenticator();

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        final var authSession = context.getAuthenticationSession();
        final var authConfig = context.getAuthenticatorConfig().getConfig();
        final var authNoteKey = authConfig.get("key");
        final var expectedAuthNoteValue = authConfig.get("value");
        final var match = expectedAuthNoteValue.equals(authSession.getAuthNote(authNoteKey));
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
            return "conditional-auth-note";
        }

        @Override
        public String getDisplayType() {
            return "Condition - Authentication note";
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
            return "Flow is executed only if session has the given auth note.";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {

            final var authNoteKeyProp = new ProviderConfigProperty();
            authNoteKeyProp.setType(ProviderConfigProperty.STRING_TYPE);
            authNoteKeyProp.setName("key");
            authNoteKeyProp.setLabel("Key");
            authNoteKeyProp.setHelpText("Auth note key");

            final var authNoteValueProp = new ProviderConfigProperty();
            authNoteValueProp.setType(ProviderConfigProperty.STRING_TYPE);
            authNoteValueProp.setName("value");
            authNoteValueProp.setLabel("Expected value");
            authNoteValueProp.setHelpText("Auth note value");

            final var negateProp = new ProviderConfigProperty();
            negateProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            negateProp.setName("negate");
            negateProp.setLabel("Negate output");
            negateProp.setHelpText("Apply a NOT to the check result.");

            return List.of(authNoteKeyProp, authNoteValueProp, negateProp);
        }

        @Override
        public ConditionalAuthenticator getSingleton() {
            return ConditionalAuthNoteAuthenticator.SINGLETON;
        }
    }

}
