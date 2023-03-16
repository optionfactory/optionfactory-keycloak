package net.optionfactory.keycloak.sms.otp;

import org.keycloak.provider.Provider;

public interface SmsOtpService extends Provider {

    SmsOtp send(String opKey, String mobileNumber, String template);

    void validate(String opKey, String userOtp);

}
