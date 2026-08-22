package net.optionfactory.keycloak.onlineaccess;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.common.VerificationException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RoleModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

public class OnlineAccessPredicatesTest {

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> iface, BiFunction<Method, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, (p, m, a) -> handler.apply(m, a));
    }

    private static ClientModel client(String clientId, Map<String, List<String>> roleAttributes) {
        final var role = roleAttributes == null ? null : proxy(RoleModel.class, (m, a) -> {
            return "getAttributeStream".equals(m.getName()) ? roleAttributes.get((String) a[0]).stream() : null;
        });
        return proxy(ClientModel.class, (m, a) -> {
            return switch (m.getName()) {
                case "getClientId" ->
                    clientId;
                case "getRole" ->
                    role;
                default ->
                    null;
            };
        });
    }

    private static JsonWebToken token(String issuedFor) {
        final var t = new JsonWebToken();
        t.issuedFor(issuedFor);
        return t;
    }

    @Test
    public void issuedForCheckAcceptsTheRequestingClientItself() throws Exception {
        Assertions.assertTrue(new OnlineAccessEndpoints.IssuedForContainsAuthorizedClient(client("webapp", null)).test(token("webapp")));
    }

    @Test
    public void issuedForCheckAcceptsClientsListedInRoleAttributes() throws Exception {
        final var authorized = client("webapp", Map.of("clients", List.of("mobile-app", "tv-app")));
        Assertions.assertTrue(new OnlineAccessEndpoints.IssuedForContainsAuthorizedClient(authorized).test(token("mobile-app")));
        Assertions.assertTrue(new OnlineAccessEndpoints.IssuedForContainsAuthorizedClient(authorized).test(token("tv-app")));
    }

    @Test
    public void issuedForCheckRejectsUnlistedClients() {
        final var authorized = client("webapp", Map.of("clients", List.of("mobile-app")));
        Assertions.assertThrows(VerificationException.class, () -> new OnlineAccessEndpoints.IssuedForContainsAuthorizedClient(authorized).test(token("attacker")));
        Assertions.assertThrows(VerificationException.class, () -> new OnlineAccessEndpoints.IssuedForContainsAuthorizedClient(authorized).test(token(null)));
    }

    @Test
    public void issuedForCheckToleratesMissingRole() {
        // role absent: only the client id itself is authorized
        final var authorized = client("webapp", null);
        Assertions.assertThrows(VerificationException.class, () -> new OnlineAccessEndpoints.IssuedForContainsAuthorizedClient(authorized).test(token("other")));
    }

    private static AccessToken tokenWithScope(String scope) {
        final var t = new AccessToken();
        t.setScope(scope);
        return t;
    }

    @Test
    public void offlineAccessScopeCheckAcceptsScopeAmongMany() throws Exception {
        Assertions.assertTrue(new OnlineAccessEndpoints.HasOfflineAccessScope().test(tokenWithScope("openid profile offline_access email")));
    }

    @Test
    public void offlineAccessScopeCheckRejectsMissingScope() {
        Assertions.assertThrows(VerificationException.class, () -> new OnlineAccessEndpoints.HasOfflineAccessScope().test(tokenWithScope("openid profile")));
    }

    @Test
    public void offlineAccessScopeCheckRejectsNullAndBlankScopes() {
        Assertions.assertThrows(VerificationException.class, () -> new OnlineAccessEndpoints.HasOfflineAccessScope().test(tokenWithScope(null)));
        Assertions.assertThrows(VerificationException.class, () -> new OnlineAccessEndpoints.HasOfflineAccessScope().test(tokenWithScope("  ")));
    }
}
