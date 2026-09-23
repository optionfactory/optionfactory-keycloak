package net.optionfactory.keycloak.api.provisioning;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import net.optionfactory.keycloak.providers.validation.Problem;

/// A password to set on a user, in one of two forms: `value` alone carries clear text, while
/// `algorithm`, `hashIterations`, `salt` and `hash` carry one already hashed somewhere else.
///
/// Clear text is hashed with the realm's own algorithm and cost, but is deliberately not checked
/// against the realm's password policy: provisioning has to be able to carry over a password the
/// policy would refuse to accept today. An already hashed password is stored as it arrives, so its
/// algorithm and cost are whatever the source system used; keycloak re-hashes it with the realm's
/// policy the first time the user logs in with it.
public record PasswordRequest(
        @Nullable String value,
        @Nullable String algorithm,
        @Nullable Integer hashIterations,
        @Nullable String salt,
        @Nullable String hash) {

    public boolean encoded() {
        return algorithm != null || hashIterations != null || salt != null || hash != null;
    }

    public boolean clearText() {
        return value != null && !value.isBlank();
    }

    /// The salt as stored, or null when it is absent or not base64.
    @Nullable
    public byte[] decodedSalt() {
        try {
            return Base64.getDecoder().decode(salt == null ? "" : salt);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public List<Problem> problems(String field) {
        final var problems = new ArrayList<Problem>();
        if (clearText() == encoded()) {
            problems.add(new Problem("FIELD_ERROR", field, clearText()
                    ? "must carry either a value or a hash, not both"
                    : "must carry either a value or a hash"));
            return problems;
        }
        if (clearText()) {
            return problems;
        }
        if (algorithm == null || algorithm.isBlank()) {
            problems.add(new Problem("FIELD_ERROR", field + ".algorithm", "must not be blank"));
        }
        if (hashIterations == null || hashIterations <= 0) {
            problems.add(new Problem("FIELD_ERROR", field + ".hashIterations", "must be positive"));
        }
        if (hash == null || hash.isBlank()) {
            problems.add(new Problem("FIELD_ERROR", field + ".hash", "must not be blank"));
        }
        if (salt == null || salt.isBlank()) {
            problems.add(new Problem("FIELD_ERROR", field + ".salt", "must not be blank"));
        } else if (decodedSalt() == null) {
            problems.add(new Problem("FIELD_ERROR", field + ".salt", "must be base64"));
        }
        return problems;
    }
}
