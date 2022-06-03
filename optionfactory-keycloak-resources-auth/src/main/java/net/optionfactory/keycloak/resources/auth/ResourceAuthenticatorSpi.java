package net.optionfactory.keycloak.resources.auth;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class ResourceAuthenticatorSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "resource-auth";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ResourceAuthenticator.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ResourceAuthenticatorFactory.class;
    }

}
