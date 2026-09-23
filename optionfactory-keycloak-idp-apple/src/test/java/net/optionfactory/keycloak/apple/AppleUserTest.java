package net.optionfactory.keycloak.apple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderModel;
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
    private static BrokeredIdentityContext contextFromIdToken(String email) {
        final var idp = new IdentityProviderModel();
        idp.setEnabled(true);
        final var context = new BrokeredIdentityContext("apple-subject", idp);
        context.setEmail(email);
        return context;
    }

    @Test
    public void theNameIsAppliedToTheContext() throws Exception {
        final var context = contextFromIdToken("john@privaterelay.appleid.com");

        JsonSerialization.readValue("""
            {"name": {"firstName": "John", "lastName": "Appleseed"}, "email": "john@apple.com"}
            """, AppleUser.class).applyTo(context);

        Assertions.assertEquals("John", context.getFirstName());
        Assertions.assertEquals("Appleseed", context.getLastName());
    }

    @Test
    public void theEmailNeverOverridesTheIdToken() throws Exception {
        // the payload is posted by the browser and is not signed: an address taken from it would
        // inherit the verified status the id_token earned for a different address
        final var context = contextFromIdToken("attacker@privaterelay.appleid.com");

        JsonSerialization.readValue("""
            {"name": {"firstName": "V", "lastName": "C"}, "email": "victim@corp.example"}
            """, AppleUser.class).applyTo(context);

        Assertions.assertEquals("attacker@privaterelay.appleid.com", context.getEmail());
    }

    @Test
    public void anEmailOnlyPayloadLeavesTheContextAlone() throws Exception {
        final var context = contextFromIdToken("john@apple.com");
        context.setFirstName("John");

        JsonSerialization.readValue("{\"email\": \"victim@corp.example\"}", AppleUser.class).applyTo(context);

        Assertions.assertEquals("john@apple.com", context.getEmail());
        Assertions.assertEquals("John", context.getFirstName());
    }

    @Test
    public void anAbsentNameFieldDoesNotWipeWhatIsThere() throws Exception {
        final var context = contextFromIdToken("john@apple.com");
        context.setFirstName("John");
        context.setLastName("Appleseed");

        JsonSerialization.readValue("{\"name\": {\"firstName\": \"Johnny\"}}", AppleUser.class).applyTo(context);

        Assertions.assertEquals("Johnny", context.getFirstName());
        Assertions.assertEquals("Appleseed", context.getLastName());
    }
}
