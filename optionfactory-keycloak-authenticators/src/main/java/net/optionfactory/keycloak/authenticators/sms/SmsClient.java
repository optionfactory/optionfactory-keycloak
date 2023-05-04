package net.optionfactory.keycloak.authenticators.sms;

import org.keycloak.provider.Provider;

public interface SmsClient extends Provider {

    String send(String phoneNumber, String message);

}
