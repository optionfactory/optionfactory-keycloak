package net.optionfactory.keycloak.api.provisioning;

import jakarta.ws.rs.BadRequestException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The validation paths run before the session is dereferenced, so a null
 * session is enough to exercise them.
 */
public class ProvisioningEndpointsTest {

    private final ProvisioningEndpoints endpoints = new ProvisioningEndpoints(null);

    private static UserProvisioningRequest request(String username) {
        return new UserProvisioningRequest("id-1", username, "user@example.com", "Alice", "Al", Map.of(), List.of(), List.of(), true, true);
    }

    @Test
    public void provideRejectsMissingUsernameWith400() {
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.provide(request(null)));
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.provide(request(" ")));
    }

    @Test
    public void provideRejectsBlankIdWithEmailAndNames() {
        final var ex = Assertions.assertThrows(BadRequestException.class, () -> endpoints.provide(
                new UserProvisioningRequest("", "alice", null, null, null, null, null, null, true, true)));
        final var problems = (List<?>) ex.getResponse().getEntity();
        Assertions.assertEquals(7, problems.size(), "id, username, email, firstName, lastName, attributes, groups must all be reported");
    }

    @Test
    public void patchRejectsMissingId() {
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.patch(new UserPatchRequest(null, null, null, null, null, null, null, null, null, null, null, null, null)));
    }
}
