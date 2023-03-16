package net.optionfactory.keycloak.sms.client;

import org.keycloak.provider.Provider;

public interface SmsClient extends Provider {

    String send(String phoneNumber, String message);
}
