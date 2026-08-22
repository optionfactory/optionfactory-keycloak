package net.optionfactory.keycloak.apple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.util.JsonSerialization;

public class AppleUserTest {

    @Test
    public void fullUserJsonIsParsed() throws Exception {
        final var user = JsonSerialization.readValue("""
            {"name": {"firstName": "John", "lastName": "Appleseed"}, "email": "john@apple.com"}
            """, AppleUser.class);
        Assertions.assertEquals("John", user.name.firstName);
        Assertions.assertEquals("Appleseed", user.name.lastName);
        Assertions.assertEquals("john@apple.com", user.email);
    }

    @Test
    public void nameIsNullOnSubsequentLogins() throws Exception {
        // apple only sends the name on the first authorization
        final var user = JsonSerialization.readValue("{\"email\": \"john@apple.com\"}", AppleUser.class);
        Assertions.assertNull(user.name);
        Assertions.assertEquals("john@apple.com", user.email);
    }

    @Test
    public void unknownFieldsAreIgnored() {
        Assertions.assertDoesNotThrow(() -> JsonSerialization.readValue("""
            {"name": {"firstName": "J", "lastName": "A"}, "email": "j@a.com", "futureField": {"x": 1}}
            """, AppleUser.class));
    }
}
