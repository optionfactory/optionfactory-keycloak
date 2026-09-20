package net.optionfactory.keycloak.authenticators.otp;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.models.RequiredActionConfigModel;

public class VerifyEmailOtpTest {

    @Test
    public void settingsFallBackToDefaultsOnMissingConfig() {
        Assertions.assertEquals(VerifyEmailOtp.Settings.DEFAULTS, VerifyEmailOtp.Settings.of(null));
        Assertions.assertEquals(VerifyEmailOtp.Settings.DEFAULTS, VerifyEmailOtp.Settings.of(new RequiredActionConfigModel()));
    }

    @Test
    public void settingsIgnoreGarbageAndReadValues() {
        final var config = new RequiredActionConfigModel();
        config.setConfig(Map.of(
                "codeLength", "not-a-number",
                "maxAttempts", "3"));
        Assertions.assertEquals(new VerifyEmailOtp.Settings(6, 900, 3, 30), VerifyEmailOtp.Settings.of(config));
    }

    @Test
    public void settingsAreBounded() {
        final var config = new RequiredActionConfigModel();
        config.setConfig(Map.of(
                "codeLength", "99",
                "codeLifespanSeconds", "1",
                "resendCooldownSeconds", "0"));
        Assertions.assertEquals(new VerifyEmailOtp.Settings(12, 60, 5, 0), VerifyEmailOtp.Settings.of(config));
    }
}
