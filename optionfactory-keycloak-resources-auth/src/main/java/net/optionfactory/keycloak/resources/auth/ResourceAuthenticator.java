package net.optionfactory.keycloak.resources.auth;

import org.keycloak.provider.Provider;

public interface ResourceAuthenticator extends Provider {

    void enforceScope(String scope);

    @Override
    default void close() {

    }

}
