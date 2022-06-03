package net.optionfactory.keycloak.resources.auth;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DefaultResourceAuthenticatorFactory implements ResourceAuthenticatorFactory {

    @Override
    public ResourceAuthenticator create(KeycloakSession session) {
        return new DefaultResourceAuthenticator(session);
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
        return "opfa-default-resource-authenticator";
    }

}
