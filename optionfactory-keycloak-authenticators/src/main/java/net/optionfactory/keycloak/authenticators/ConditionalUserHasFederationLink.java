package net.optionfactory.keycloak.authenticators;

import java.util.List;
import java.util.Objects;
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

public class ConditionalUserHasFederationLink implements ConditionalAuthenticator {

    public static ConditionalUserHasFederationLink INSTANCE = new ConditionalUserHasFederationLink();

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        final var authenticatorConfig = context.getAuthenticatorConfig();
        if (authenticatorConfig == null || authenticatorConfig.getConfig() == null) {
            return false;
        }
        final var authConfig = authenticatorConfig.getConfig();
        final var expectedFederationLink = authConfig.get("federationLink");
        final var negate = Boolean.parseBoolean(authConfig.get("negate"));
        final var user = context.getUser();
        if (user == null) {
            return negate;
        }
        final var fedLinkId = user.getFederationLink();
        if (fedLinkId == null) {
            return negate;
        }
        final var federation = context.getRealm().getComponent(fedLinkId);
        if (federation == null) {
            return negate;
        }
        final var federationName = federation.getName();
        final var match = Objects.equals(expectedFederationLink, federationName);
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
            return "conditional-user-has-federation-link";
        }

        @Override
        public String getDisplayType() {
            return "Condition - User Has Federation Link";
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
            return "Flow is executed only if user has the configured federation link.";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {

            final var groupProp = new ProviderConfigProperty();
            groupProp.setType(ProviderConfigProperty.STRING_TYPE);
            groupProp.setName("federationLink");
            groupProp.setLabel("Federation Link");
            groupProp.setHelpText("Federation Link");

            final var negateProp = new ProviderConfigProperty();
            negateProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            negateProp.setName("negate");
            negateProp.setLabel("Negate output");
            negateProp.setHelpText("Apply a NOT to the check result.");

            return List.of(groupProp, negateProp);
        }

        @Override
        public ConditionalAuthenticator getSingleton() {
            return ConditionalUserHasFederationLink.INSTANCE;
        }
    }

}
