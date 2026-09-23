package net.optionfactory.keycloak.api.provisioning;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import net.optionfactory.keycloak.providers.validation.Problem;

/// A password to set on a user, in one of two forms: `value` alone carries clear text, while `algorithm`,
/// `hashIterations` and `hash` carry one already hashed elsewhere, with `salt` only where the algorithm keeps
/// it apart from the hash - bcrypt and the like embed their own and send none, as keycloak's own export does.
///
/// Clear text is hashed with the realm's algorithm and cost but never measured against its password policy:
/// provisioning has to carry over passwords the policy would refuse today.
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

    /// The salt as stored, or null when there is none. A hash that carries its own - bcrypt and the like
    /// embed cost and salt in the value itself - is stored with a null salt, which is how keycloak writes
    /// it in an export and what `PasswordSecretData` expects.
    @Nullable
    public byte[] decodedSalt() {
        if (salt == null || salt.isBlank()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(salt);
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
        // optional: a self contained hash has no separate salt to carry
        if (salt != null && !salt.isBlank() && decodedSalt() == null) {
            problems.add(new Problem("FIELD_ERROR", field + ".salt", "must be base64"));
        }
        return problems;
    }
}
