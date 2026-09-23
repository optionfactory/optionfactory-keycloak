package net.optionfactory.keycloak.providers.filtering;

import org.junit.jupiter.api.Assertions;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

public class GroupFilterTest {

    private static final GroupFilter GROUPS = new GroupFilter("groups", "groups");

    @Test
    public void anyCompilesToJsonbExistsAnyWithPlaceholders() {
        final var cf = GROUPS.configure(new String[]{"ANY", "/admins", "/staff"});
        Assertions.assertEquals("jsonb_exists_any(groups, array[?,?])", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"/admins", "/staff"}, cf.parameters());
    }

    @Test
    public void allCompilesToJsonbExistsAll() {
        final var cf = GROUPS.configure(new String[]{"ALL", "/a", "/b", "/c"});
        Assertions.assertEquals("jsonb_exists_all(groups, array[?,?,?])", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"/a", "/b", "/c"}, cf.parameters());
    }

    @Test
    public void noneNegatesAny() {
        final var cf = GROUPS.configure(new String[]{"NONE", "/banned"});
        Assertions.assertEquals("not jsonb_exists_any(groups, array[?])", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"/banned"}, cf.parameters());
    }

    @Test
    public void groupPathsNeverReachTheGeneratedSql() {
        final var cf = GROUPS.configure(new String[]{"ANY", "/a') or true --"});
        Assertions.assertEquals("jsonb_exists_any(groups, array[?])", cf.expression());
        Assertions.assertArrayEquals(new Object[]{"/a') or true --"}, cf.parameters());
    }

    @Test
    public void atLeastOneValueIsRequired() {
        Assertions.assertThrows(BadRequestException.class, () -> GROUPS.configure(new String[]{"ANY"}));
    }

    @Test
    public void unknownOperatorsAreRejected() {
        Assertions.assertThrows(BadRequestException.class, () -> GROUPS.configure(new String[]{"EXACTLY", "/admins"}));
    }
    @Test
    public void aNullGroupValueIsRejected() {
        // it would bind as an untyped null inside array[...], which postgres cannot assign a type to
        Assertions.assertThrows(BadRequestException.class,
                () -> new GroupFilter("groups", "u.groups").configure(new String[]{"ANY", null}));
        Assertions.assertThrows(BadRequestException.class,
                () -> new GroupFilter("groups", "u.groups").configure(new String[]{"ANY", "/staff", null}));
    }
}
