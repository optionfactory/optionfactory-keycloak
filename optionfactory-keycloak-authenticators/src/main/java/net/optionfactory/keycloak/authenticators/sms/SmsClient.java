package net.optionfactory.keycloak.authenticators.sms;

import org.keycloak.provider.Provider;

public interface SmsClient extends Provider {

    String send(String phoneNumber, String message);

    public static void ensure(boolean test, String format, Object... args) {
        if (test) {
            return;
        }
        throw new IllegalStateException(String.format(format, args));
    }
}
