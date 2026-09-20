package net.optionfactory.keycloak.authenticators.otp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import org.keycloak.models.SingleUseObjectProvider;

public class OtpCodes {

    private static final String STORED_CODE = "code";
    private static final String STORED_EXPIRATION = "expiration";
    private static final String STORED_ATTEMPTS = "attempts";

    public enum Attempt {
        NO_CODE, TOO_MANY_ATTEMPTS, INVALID, VALIDATED
    }

    public static void put(SingleUseObjectProvider store, String key, String plainCode, int lifespanSeconds, long now) {
        store.put(key, lifespanSeconds, Map.of(
                STORED_CODE, sha256Hex(plainCode),
                STORED_EXPIRATION, Long.toString(now + lifespanSeconds),
                STORED_ATTEMPTS, "0"));
    }

    public static boolean exists(SingleUseObjectProvider store, String key, long now) {
        final var stored = store.get(key);
        return stored != null
                && stored.get(STORED_CODE) != null
                && Long.parseLong(stored.get(STORED_EXPIRATION)) > now;
    }

    public static Attempt tryValidate(SingleUseObjectProvider store, String key, String submittedCode, int maxAttempts, long now) {
        if (submittedCode == null || submittedCode.isEmpty()) {
            return Attempt.INVALID;
        }
        if (!exists(store, key, now)) {
            return Attempt.NO_CODE;
        }
        // remove() is the store's only atomic single-use operation: claiming the entry serializes concurrent submissions so the attempt counter cannot be raced past its cap
        final var claimed = store.remove(key);
        if (claimed == null) {
            return Attempt.NO_CODE;
        }
        final int attempts = Integer.parseInt(claimed.get(STORED_ATTEMPTS));
        if (attempts >= maxAttempts) {
            return Attempt.TOO_MANY_ATTEMPTS;
        }
        if (!codeMatches(claimed.get(STORED_CODE), submittedCode)) {
            final var remainingLifespan = Long.parseLong(claimed.get(STORED_EXPIRATION)) - now;
            if (remainingLifespan > 0) {
                final var updated = new HashMap<>(claimed);
                updated.put(STORED_ATTEMPTS, Integer.toString(attempts + 1));
                store.put(key, remainingLifespan, updated);
            }
            return Attempt.INVALID;
        }
        return Attempt.VALIDATED;
    }

    public static boolean claimSendSlot(SingleUseObjectProvider store, String cooldownKey, int cooldownSeconds, long now) {
        if (cooldownSeconds <= 0) {
            return true;
        }
        // putIfAbsent claims the send slot: only one of N concurrent senders gets through
        if (!store.putIfAbsent(cooldownKey, cooldownSeconds)) {
            return false;
        }
        store.put(cooldownKey, cooldownSeconds, Map.of(
                STORED_EXPIRATION, Long.toString(now + cooldownSeconds)));
        return true;
    }

    public static Long remainingCooldown(SingleUseObjectProvider store, String cooldownKey, long now) {
        final var cooldown = store.get(cooldownKey);
        // an empty map is the putIfAbsent claim marker: the winner has not written the readable entry yet
        final var expiration = cooldown == null ? null : cooldown.get(STORED_EXPIRATION);
        if (expiration == null) {
            return null;
        }
        final var remaining = Long.parseLong(expiration) - now;
        return remaining > 0 ? remaining : null;
    }

    public static boolean codeMatches(String storedCodeHash, String submittedCode) {
        return MessageDigest.isEqual(storedCodeHash.getBytes(StandardCharsets.UTF_8), sha256Hex(submittedCode).getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256Hex(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static String digitsOnly(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[^0-9]", "");
    }

    public static int boundedInt(String value, int defaultValue, int min, int max) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        final int parsed;
        try {
            parsed = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
        return Math.max(min, Math.min(max, parsed));
    }
}
