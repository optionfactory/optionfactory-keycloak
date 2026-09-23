package net.optionfactory.keycloak.providers.groups;

import jakarta.ws.rs.BadRequestException;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.models.GroupProvider;
import org.keycloak.models.GroupProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class GroupsTest {

    /// Splitting a path needs the realm's slash-escaping convention, which lives on the group provider
    /// factory: the validation below cannot run without a session any more. False is keycloak's default.
    private static KeycloakSession session(boolean escapeSlashes) {
        final var loader = GroupsTest.class.getClassLoader();
        final var groups = (GroupProviderFactory) Proxy.newProxyInstance(loader,
                new Class<?>[]{GroupProviderFactory.class}, (proxy, method, args)
                -> "escapeSlashesInGroupPath".equals(method.getName()) ? escapeSlashes : null);
        final var factory = (KeycloakSessionFactory) Proxy.newProxyInstance(loader,
                new Class<?>[]{KeycloakSessionFactory.class}, (proxy, method, args)
                -> "getProviderFactory".equals(method.getName()) && GroupProvider.class.equals(args[0]) ? groups : null);
        return (KeycloakSession) Proxy.newProxyInstance(loader,
                new Class<?>[]{KeycloakSession.class}, (proxy, method, args)
                -> "getKeycloakSessionFactory".equals(method.getName()) ? factory : null);
    }

    private static final KeycloakSession SESSION = session(false);

    @Test
    public void provideRejectsNullPath() {
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(SESSION, null, null));
    }

    @Test
    public void provideRejectsBlankPath() {
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(SESSION, null, " "));
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(SESSION, null, ""));
    }

    @Test
    public void provideRejectsRootPath() {
        // would otherwise create a group with an empty name
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(SESSION, null, "/"));
    }

    @Test
    public void provideRejectsBlankPathSegments() {
        // an interior double slash would create an empty-named intermediate group.
        // leading/trailing slashes (even doubled) are trimmed by splitPath, so only
        // interior blanks are rejected
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(SESSION, null, "a//b"));
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(SESSION, null, "/a//b/"));
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(SESSION, null, "a/b//c"));
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(SESSION, null, "//"));
    }
}
