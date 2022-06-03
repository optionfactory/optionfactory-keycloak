package net.optionfactory.keycloak.provisioning.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.optionfactory.keycloak.validation.RequestValidator;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import net.optionfactory.keycloak.resources.auth.ResourceAuthenticator;

public class ProvisioningEndpointsFactory implements RealmResourceProviderFactory {

    private ObjectMapper om;
    private String requiredScopeName;

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        session.getProvider(ResourceAuthenticator.class).enforceScope(requiredScopeName);
        final var validator = session.getProvider(RequestValidator.class);
        return new RealmResourceProvider() {
            @Override
            public Object getResource() {
                return new ProvisioningEndpoints(om, validator, session);
            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public void init(Config.Scope config) {
        this.om = new ObjectMapper();
        this.requiredScopeName = config.get("scope", "provisioning");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "provisioning";
    }

}
