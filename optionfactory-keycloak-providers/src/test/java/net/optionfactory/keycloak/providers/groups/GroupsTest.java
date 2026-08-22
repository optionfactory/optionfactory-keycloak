package net.optionfactory.keycloak.providers.groups;

import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GroupsTest {

    @Test
    public void provideRejectsNullPath() {
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(null, null, null));
    }

    @Test
    public void provideRejectsBlankPath() {
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(null, null, " "));
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(null, null, ""));
    }

    @Test
    public void provideRejectsRootPath() {
        // would otherwise create a group with an empty name
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(null, null, "/"));
    }

    @Test
    public void provideRejectsBlankPathSegments() {
        // an interior double slash would create an empty-named intermediate group.
        // leading/trailing slashes (even doubled) are trimmed by splitPath, so only
        // interior blanks are rejected
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(null, null, "a//b"));
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(null, null, "/a//b/"));
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(null, null, "a/b//c"));
        Assertions.assertThrows(BadRequestException.class, () -> Groups.provide(null, null, "//"));
    }
}
