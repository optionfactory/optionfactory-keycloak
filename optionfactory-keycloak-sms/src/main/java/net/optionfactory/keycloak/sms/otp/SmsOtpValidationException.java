package net.optionfactory.keycloak.sms.otp;

public class SmsOtpValidationException extends RuntimeException {

    public SmsOtpValidationException(String message) {
        super(message);
    }
}
