package net.optionfactory.keycloak.api.provisioning;

import java.util.Base64;
import java.util.List;
import net.optionfactory.keycloak.providers.validation.Problem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PasswordRequestTest {

    private static final String SALT = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4});

    @Test
    public void clearTextNeedsNothingElse() {
        final var password = new PasswordRequest("s3cret", null, null, null, null);

        Assertions.assertTrue(password.clearText());
        Assertions.assertFalse(password.encoded());
        Assertions.assertEquals(List.of(), password.problems("password"));
    }

    @Test
    public void aCompleteHashIsAccepted() {
        final var password = new PasswordRequest(null, "pbkdf2-sha512", 210000, SALT, "aGFzaA==");

        Assertions.assertTrue(password.encoded());
        Assertions.assertEquals(List.of(), password.problems("password"));
        Assertions.assertArrayEquals(new byte[]{1, 2, 3, 4}, password.decodedSalt());
    }

    @Test
    public void thePasswordMustCarryOneFormOrTheOther() {
        final var neither = new PasswordRequest(null, null, null, null, null).problems("password");
        final var both = new PasswordRequest("s3cret", "pbkdf2-sha512", 210000, SALT, "aGFzaA==").problems("password");

        Assertions.assertEquals(1, neither.size());
        Assertions.assertEquals("must carry either a value or a hash", neither.get(0).reason());
        Assertions.assertEquals(1, both.size());
        Assertions.assertEquals("must carry either a value or a hash, not both", both.get(0).reason());
    }

    @Test
    public void anIncompleteHashReportsEveryMissingPart() {
        final var problems = new PasswordRequest(null, null, null, null, "aGFzaA==").problems("password");

        Assertions.assertEquals(2, problems.size());
        Assertions.assertEquals(
                List.of("password.algorithm", "password.hashIterations"),
                problems.stream().map(Problem::context).toList());
    }

    @Test
    public void aSelfContainedHashNeedsNoSalt() {
        // bcrypt and the like embed cost and salt in the value: keycloak's own export writes "salt": null
        final var bcrypt = new PasswordRequest(null, "legacy-bcrypt", 10, null,
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");

        Assertions.assertEquals(List.of(), bcrypt.problems("password"));
        Assertions.assertNull(bcrypt.decodedSalt());
    }

    @Test
    public void aBlankSaltIsTheSameAsNoneAtAll() {
        final var password = new PasswordRequest(null, "legacy-md5", 1, "  ", "aGFzaA==");

        Assertions.assertEquals(List.of(), password.problems("password"));
        Assertions.assertNull(password.decodedSalt());
    }

    @Test
    public void aSaltThatIsNotBase64IsReported() {
        final var problems = new PasswordRequest(null, "pbkdf2-sha512", 210000, "not base64!", "aGFzaA==").problems("password");

        Assertions.assertEquals(1, problems.size());
        Assertions.assertEquals("must be base64", problems.get(0).reason());
    }

    @Test
    public void aNonPositiveIterationCountIsReported() {
        final var problems = new PasswordRequest(null, "pbkdf2-sha512", 0, SALT, "aGFzaA==").problems("password");

        Assertions.assertEquals(1, problems.size());
        Assertions.assertEquals("password.hashIterations", problems.get(0).context());
    }
}
