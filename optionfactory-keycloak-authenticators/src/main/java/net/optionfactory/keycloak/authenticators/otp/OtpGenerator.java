package net.optionfactory.keycloak.authenticators.otp;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public interface OtpGenerator {

    public String generate();

    public enum Mode {
        RANDOM, PRESET;
    }

    public static OtpGenerator of(Mode mode, String preset, int digits) {
        if (mode == Mode.PRESET) {
            return new PresetOtpGenerator(preset);
        }
        return new RandomOtpGenerator(digits);
    }

    public static class RandomOtpGenerator implements OtpGenerator {

        private final SecureRandom random = new SecureRandom();
        private final int otpDigits;

        public RandomOtpGenerator(int otpDigits) {
            this.otpDigits = otpDigits;
        }

        @Override
        public String generate() {
            return random.ints(otpDigits, 0, 10).mapToObj(String::valueOf).collect(Collectors.joining());
        }

    }

    public static class PresetOtpGenerator implements OtpGenerator {

        private final String preset;

        public PresetOtpGenerator(String preset) {
            this.preset = preset;
        }

        @Override
        public String generate() {
            return preset;
        }

    }
}
