package net.optionfactory.keycloak.onlineaccess;

import jakarta.ws.rs.ForbiddenException;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;
import org.keycloak.crypto.SignatureProvider;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import org.keycloak.models.KeycloakContext;

/**
 * The alg guard runs before any session use beyond getProvider, so a proxy
 * session rejecting every provider lookup exercises the attacker-controlled
 * header path without a keycloak instance.
 */
public class OnlineAccessEndpointsAlgorithmGuardTest {

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> iface, BiFunction<Method, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, (p, m, a) -> handler.apply(m, a));
    }

    private static KeycloakSession sessionWithNoSignatureProviders() {
        final var context = proxy(KeycloakContext.class, (m, a) -> null);
        return proxy(KeycloakSession.class, (m, a) -> {
            return switch (m.getName()) {
                case "getContext" ->
                    context;
                default ->
                    null;
            };
        });
    }

    // an unsigned token: header alg = none
    private static final String UNSIGNED_TOKEN = "eyJhbGciOiJub25lIn0.e30.";

    @Test
    public void unknownOrNoneAlgorithmIsRejectedWith403Not500() {
        final var endpoints = new OnlineAccessEndpoints(sessionWithNoSignatureProviders(), 60);
        final var ex = Assertions.assertThrows(ForbiddenException.class, () -> endpoints.create(UNSIGNED_TOKEN, "https://app.example.com/cb"));
        Assertions.assertNotNull(ex.getResponse());
    }

    @Test
    public void garbageTokensAreRejectedWith403Not500() {
        final var endpoints = new OnlineAccessEndpoints(sessionWithNoSignatureProviders(), 60);
        // not even a jwt: verifier.create must fail before or inside verification
        Assertions.assertThrows(Exception.class, () -> endpoints.create("garbage", "https://app.example.com/cb"));
        Assertions.assertThrows(Exception.class, () -> endpoints.create(null, "https://app.example.com/cb"));
    }
}
