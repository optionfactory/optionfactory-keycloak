package net.optionfactory.keycloak.api.provisioning;

import jakarta.ws.rs.BadRequestException;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;

/**
 * The validation paths reach the session only to mark the transaction for rollback, so a session
 * that answers getTransactionManager() and records that call is enough to exercise them.
 */
public class ProvisioningEndpointsTest {

    private final AtomicBoolean rolledBack = new AtomicBoolean();
    private final ProvisioningEndpoints endpoints = new ProvisioningEndpoints(recordingSession(rolledBack));

    private static KeycloakSession recordingSession(AtomicBoolean rolledBack) {
        final var loader = ProvisioningEndpointsTest.class.getClassLoader();
        final var tx = (KeycloakTransactionManager) Proxy.newProxyInstance(loader,
                new Class<?>[]{KeycloakTransactionManager.class}, (proxy, method, args) -> {
                    if ("setRollbackOnly".equals(method.getName())) {
                        rolledBack.set(true);
                    }
                    return boolean.class.equals(method.getReturnType()) ? false : null;
                });
        return (KeycloakSession) Proxy.newProxyInstance(loader,
                new Class<?>[]{KeycloakSession.class}, (proxy, method, args)
                -> "getTransactionManager".equals(method.getName()) ? tx : null);
    }

    private static UserProvisioningRequest request(String username) {
        return new UserProvisioningRequest("id-1", username, "user@example.com", "Alice", "Al", Map.of(), List.of(), List.of(), null, true, true);
    }

    @Test
    public void provideRejectsMissingUsernameWith400() {
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.provide(request(null)));
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.provide(request(" ")));
    }

    @Test
    public void provideRejectsBlankIdWithEmailAndNames() {
        final var ex = Assertions.assertThrows(BadRequestException.class, () -> endpoints.provide(
                new UserProvisioningRequest("", "alice", null, null, null, null, null, null, null, true, true)));
        final var problems = (List<?>) ex.getResponse().getEntity();
        Assertions.assertEquals(7, problems.size(), "id, username, email, firstName, lastName, attributes, groups must all be reported");
    }

    @Test
    public void patchRejectsMissingId() {
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.patch(new UserPatchRequest(null, null, null, null, null, null, null, null, null, null, null, null, null, null)));
    }
    @Test
    public void aRejectedRequestMarksTheTransactionForRollback() {
        // resteasy returns a BadRequestException carrying an entity as it is, so nothing else would
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.provide(request(null)));

        Assertions.assertTrue(rolledBack.get());
    }

    @Test
    public void provideRejectsAMissingEnabledFlag() {
        final var ex = Assertions.assertThrows(BadRequestException.class, () -> endpoints.provide(
                new UserProvisioningRequest("id-1", "alice", "a@b.com", "Alice", "Al", Map.of(), List.of(), List.of(), null, null, true)));
        final var problems = (List<?>) ex.getResponse().getEntity();

        Assertions.assertEquals(1, problems.size(), "a missing enabled flag must be reported, not defaulted to false");
    }

    @Test
    public void rootAttributesAreNotPatchable() {
        // keycloak reports them among the attributes, and removing one nulls the field itself
        final var reported = Map.of(
                "username", List.of("alice"),
                "firstName", List.of("Alice"),
                "lastName", List.of("Al"),
                "email", List.of("a@b.com"),
                "costCenter", List.of("42"));

        final var patchable = ProvisioningEndpoints.patchableAttributes(reported);

        Assertions.assertEquals(Map.of("costCenter", List.of("42")), patchable);
    }
}
