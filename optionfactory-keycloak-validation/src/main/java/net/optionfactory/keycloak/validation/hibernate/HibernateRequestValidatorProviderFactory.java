package net.optionfactory.keycloak.validation.hibernate;

import net.optionfactory.keycloak.validation.RequestValidatorProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class HibernateRequestValidatorProviderFactory implements RequestValidatorProviderFactory {

    private final HibernateRequestValidator validator;

    public HibernateRequestValidatorProviderFactory() {
        this.validator = new HibernateRequestValidator();
    }

    @Override
    public HibernateRequestValidator create(KeycloakSession session) {
        return validator;
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
        return "opfa-hibernate-request-validator";
    }

}
