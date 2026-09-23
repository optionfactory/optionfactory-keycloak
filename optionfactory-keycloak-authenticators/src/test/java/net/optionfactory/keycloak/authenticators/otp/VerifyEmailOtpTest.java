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
    @Test
    public void theCodeKeyIsBoundToTheAddressItWasSentTo() {
        final var forOld = VerifyEmailOtp.codeKey("u1", "old@corp.example");
        final var forNew = VerifyEmailOtp.codeKey("u1", "victim@corp.example");

        // a code issued for one address must not be usable to verify another
        Assertions.assertNotEquals(forOld, forNew);
    }

    @Test
    public void theCodeKeySurvivesCasingAndPadding() {
        // an admin retyping the same address must not silently invalidate an outstanding code
        Assertions.assertEquals(
                VerifyEmailOtp.codeKey("u1", "john@corp.example"),
                VerifyEmailOtp.codeKey("u1", "  John@Corp.Example  "));
    }

    @Test
    public void theCodeKeySeparatesUsersSharingAnAddress() {
        Assertions.assertNotEquals(
                VerifyEmailOtp.codeKey("u1", "shared@corp.example"),
                VerifyEmailOtp.codeKey("u2", "shared@corp.example"));
    }

    @Test
    public void theCodeKeyDoesNotCarryTheAddressInClear() {
        // the single-use store is not a place to leave addresses lying around
        Assertions.assertFalse(VerifyEmailOtp.codeKey("u1", "john@corp.example").contains("john@corp.example"));
    }

    @Test
    public void aMissingAddressDoesNotBlowUp() {
        Assertions.assertDoesNotThrow(() -> VerifyEmailOtp.codeKey("u1", null));
    }
}
