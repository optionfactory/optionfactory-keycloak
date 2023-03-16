package net.optionfactory.keycloak.sms.otp;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class SmsOtpSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "opfa-sms-otp";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return SmsOtpService.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return SmsOtpServiceFactory.class;
    }

}
