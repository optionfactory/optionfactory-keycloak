package net.optionfactory.keycloak.onlineaccess;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RoleModel;

public class OnlineAccessRedirectsTest {

    private static RoleModel role(List<String> redirectUris) {
        return (RoleModel) Proxy.newProxyInstance(OnlineAccessRedirectsTest.class.getClassLoader(),
                new Class<?>[]{RoleModel.class}, (proxy, method, args)
                -> "getAttributeStream".equals(method.getName()) && "redirect_uri".equals(args[0])
                        ? redirectUris.stream() : null);
    }

    private static ClientModel client(String clientId, Set<String> redirectUris, RoleModel onlineAccess) {
        return (ClientModel) Proxy.newProxyInstance(OnlineAccessRedirectsTest.class.getClassLoader(),
                new Class<?>[]{ClientModel.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getClientId" ->
                clientId;
            case "getRedirectUris" ->
                redirectUris;
            case "getRole" ->
                onlineAccess;
            default ->
                boolean.class.equals(method.getReturnType()) ? false : null;
        });
    }

    @Test
    public void theIssuersOwnRedirectsAndTheRolesAreBothAllowed() {
        final var caller = client("backend", Set.of(), role(List.of("https://app.example/landing")));
        final var issuer = client("spa", Set.of("https://spa.example/callback"), null);

        Assertions.assertEquals(
                Set.of("https://spa.example/callback", "https://app.example/landing"),
                OnlineAccessEndpoints.validRedirects(caller, issuer));
    }

    @Test
    public void theCallingClientsIdIsNotARedirectUri() {
        final var caller = client("backend", Set.of(), null);
        final var issuer = client("spa", Set.of("https://spa.example/callback"), null);

        Assertions.assertEquals(Set.of("https://spa.example/callback"),
                OnlineAccessEndpoints.validRedirects(caller, issuer));
    }

    @Test
    public void withoutTheRoleOnlyTheIssuersRedirectsRemain() {
        final var caller = client("backend", Set.of(), null);
        final var issuer = client("spa", Set.of("https://spa.example/callback", "https://spa.example/other"), null);

        Assertions.assertEquals(Set.of("https://spa.example/callback", "https://spa.example/other"),
                OnlineAccessEndpoints.validRedirects(caller, issuer));
    }
}
