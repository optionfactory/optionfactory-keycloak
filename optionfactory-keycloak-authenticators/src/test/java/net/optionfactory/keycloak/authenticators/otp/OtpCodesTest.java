package net.optionfactory.keycloak.authenticators.otp;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.models.SingleUseObjectProvider;

public class OtpCodesTest {

    private static final String KEY = "opfa-test:user";

    @Test
    public void sha256HexMatchesKnownVector() {
        Assertions.assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", OtpCodes.sha256Hex("123456"));
    }

    @Test
    public void codeMatchesAcceptsOnlyTheIssuedCode() {
        final var hash = OtpCodes.sha256Hex("424242");
        Assertions.assertTrue(OtpCodes.codeMatches(hash, "424242"));
        Assertions.assertFalse(OtpCodes.codeMatches(hash, "424243"));
        Assertions.assertFalse(OtpCodes.codeMatches(hash, ""));
    }

    @Test
    public void digitsOnlyStripsEverythingButDigits() {
        Assertions.assertNull(OtpCodes.digitsOnly(null));
        Assertions.assertEquals("424242", OtpCodes.digitsOnly(" 424 242 "));
        Assertions.assertEquals("424242", OtpCodes.digitsOnly("42-42 42"));
        Assertions.assertEquals("", OtpCodes.digitsOnly("not-a-code"));
    }

    @Test
    public void aValidatedCodeIsSingleUse() {
        final var store = new FakeSingleUseStore();
        OtpCodes.put(store, KEY, "424242", 900, 1_000);
        Assertions.assertTrue(OtpCodes.exists(store, KEY, 1_050));
        Assertions.assertEquals(OtpCodes.Attempt.VALIDATED, OtpCodes.tryValidate(store, KEY, "424242", 5, 1_050));
        Assertions.assertFalse(OtpCodes.exists(store, KEY, 1_050));
        Assertions.assertEquals(OtpCodes.Attempt.NO_CODE, OtpCodes.tryValidate(store, KEY, "424242", 5, 1_050));
    }

    @Test
    public void wrongCodesAreCountedAndCapped() {
        final var store = new FakeSingleUseStore();
        OtpCodes.put(store, KEY, "424242", 900, 1_000);
        for (int i = 0; i < 5; i++) {
            Assertions.assertEquals(OtpCodes.Attempt.INVALID, OtpCodes.tryValidate(store, KEY, "99" + i, 5, 1_050));
        }
        Assertions.assertEquals(OtpCodes.Attempt.TOO_MANY_ATTEMPTS, OtpCodes.tryValidate(store, KEY, "424242", 5, 1_050));
        Assertions.assertEquals(OtpCodes.Attempt.NO_CODE, OtpCodes.tryValidate(store, KEY, "424242", 5, 1_050));
    }

    @Test
    public void expiredCodesAreRejectedWithoutConsumingAttempts() {
        final var store = new FakeSingleUseStore();
        OtpCodes.put(store, KEY, "424242", 60, 1_000);
        Assertions.assertFalse(OtpCodes.exists(store, KEY, 1_100));
        Assertions.assertEquals(OtpCodes.Attempt.NO_CODE, OtpCodes.tryValidate(store, KEY, "424242", 5, 1_100));
    }

    @Test
    public void emptySubmissionsDoNotConsumeTheCode() {
        final var store = new FakeSingleUseStore();
        OtpCodes.put(store, KEY, "424242", 900, 1_000);
        Assertions.assertEquals(OtpCodes.Attempt.INVALID, OtpCodes.tryValidate(store, KEY, "", 5, 1_050));
        Assertions.assertEquals(OtpCodes.Attempt.INVALID, OtpCodes.tryValidate(store, KEY, null, 5, 1_050));
        Assertions.assertEquals(OtpCodes.Attempt.VALIDATED, OtpCodes.tryValidate(store, KEY, "424242", 5, 1_050));
    }

    @Test
    public void onlyOneSenderClaimsTheSendSlot() {
        final var store = new FakeSingleUseStore();
        Assertions.assertTrue(OtpCodes.claimSendSlot(store, "cooldown", 30, 1_000));
        Assertions.assertFalse(OtpCodes.claimSendSlot(store, "cooldown", 30, 1_010));
        final var remaining = OtpCodes.remainingCooldown(store, "cooldown", 1_010);
        Assertions.assertNotNull(remaining);
        Assertions.assertEquals(20, remaining);
        Assertions.assertNull(OtpCodes.remainingCooldown(store, "cooldown", 1_040));
    }

    @Test
    public void aZeroCooldownNeverBlocksSends() {
        final var store = new FakeSingleUseStore();
        Assertions.assertTrue(OtpCodes.claimSendSlot(store, "cooldown", 0, 1_000));
        Assertions.assertTrue(OtpCodes.claimSendSlot(store, "cooldown", 0, 1_001));
        Assertions.assertFalse(store.contains("cooldown"));
    }

    @Test
    public void boundedIntIgnoresGarbageAndClamps() {
        Assertions.assertEquals(6, OtpCodes.boundedInt(null, 6, 4, 12));
        Assertions.assertEquals(6, OtpCodes.boundedInt(" ", 6, 4, 12));
        Assertions.assertEquals(6, OtpCodes.boundedInt("not-a-number", 6, 4, 12));
        Assertions.assertEquals(8, OtpCodes.boundedInt("8", 6, 4, 12));
        Assertions.assertEquals(12, OtpCodes.boundedInt("99", 6, 4, 12));
        Assertions.assertEquals(4, OtpCodes.boundedInt("-3", 6, 4, 12));
    }

    private static final class FakeSingleUseStore implements SingleUseObjectProvider {

        private final Map<String, Map<String, String>> values = new HashMap<>();

        @Override
        public void put(String key, long lifespanSeconds, Map<String, String> notes) {
            values.put(key, notes);
        }

        @Override
        public Map<String, String> get(String key) {
            return values.get(key);
        }

        @Override
        public Map<String, String> remove(String key) {
            return values.remove(key);
        }

        @Override
        public boolean replace(String key, Map<String, String> notes) {
            if (!values.containsKey(key)) {
                return false;
            }
            values.put(key, notes);
            return true;
        }

        @Override
        public boolean putIfAbsent(String key, long lifespanInSeconds) {
            if (values.containsKey(key)) {
                return false;
            }
            values.put(key, Map.of());
            return true;
        }

        @Override
        public boolean contains(String key) {
            return values.containsKey(key);
        }

        @Override
        public void close() {
        }
    }
}
