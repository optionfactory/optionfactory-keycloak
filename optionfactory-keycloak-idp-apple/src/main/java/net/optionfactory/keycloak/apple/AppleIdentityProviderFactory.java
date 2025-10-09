package net.optionfactory.keycloak.apple;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

public class AppleIdentityProviderFactory extends AbstractIdentityProviderFactory<AppleIdentityProvider> implements SocialIdentityProviderFactory<AppleIdentityProvider> {

    @Override
    public String getName() {
        return "Sign in with Apple";
    }

    @Override
    public AppleIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new AppleIdentityProvider(session, new AppleIdentityProviderConfig(model));
    }

    @Override
    public AppleIdentityProviderConfig createConfig() {
        return new AppleIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return "opfa-apple";
    }
}
