package net.optionfactory.keycloak.authenticators.otp;

import org.junit.jupiter.api.Assertions;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class OtpGeneratorTest {

    @Test
    public void randomGeneratorHonorsRequestedLength() {
        for (int digits = 1; digits <= 12; digits++) {
            Assertions.assertEquals(digits, OtpGenerator.of(OtpGenerator.Mode.RANDOM, null, digits).generate().length());
        }
    }

    @Test
    public void randomGeneratorOnlyEmitsDigits() {
        final var generator = OtpGenerator.of(OtpGenerator.Mode.RANDOM, null, 6);
        for (int i = 0; i < 1_000; i++) {
            Assertions.assertTrue(generator.generate().matches("[0-9]+"));
        }
    }

    @Test
    public void randomGeneratorEmitsAllDigitsAcrossManyDraws() {
        // a modulo-biased or misconfigured generator would miss some digits
        final var generator = OtpGenerator.of(OtpGenerator.Mode.RANDOM, null, 1);
        final Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1_000; i++) {
            seen.add(generator.generate());
        }
        Assertions.assertEquals(Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), seen);
    }

    @Test
    public void randomGeneratorDoesNotRepeatTheSameCodeForever() {
        final var generator = OtpGenerator.of(OtpGenerator.Mode.RANDOM, null, 6);
        final var first = generator.generate();
        for (int i = 0; i < 100; i++) {
            if (!generator.generate().equals(first)) {
                return;
            }
        }
        Assertions.assertTrue(false, "generator emitted the same code 101 times in a row");
    }

    @Test
    public void presetGeneratorReturnsTheConfiguredCode() {
        Assertions.assertEquals("4321", OtpGenerator.of(OtpGenerator.Mode.PRESET, "4321", 6).generate());
    }

    @Test
    public void presetModeIgnoresRequestedDigits() {
        Assertions.assertEquals("42", OtpGenerator.of(OtpGenerator.Mode.PRESET, "42", 8).generate());
    }

    @Test
    public void differentRandomInstancesYieldDifferentCodes() {
        final var a = OtpGenerator.of(OtpGenerator.Mode.RANDOM, null, 8);
        final var b = OtpGenerator.of(OtpGenerator.Mode.RANDOM, null, 8);
        Assertions.assertNotEquals(a.generate(), b.generate());
    }
}
