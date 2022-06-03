package net.optionfactory.keycloak.validation;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class RequestValidatorSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "opfa-request-validator";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return RequestValidator.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return RequestValidatorProviderFactory.class;
    }

}
