package net.optionfactory.keycloak.providers.filtering;

import org.junit.jupiter.api.Assertions;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

public class BooleanFilterTest {

    private static final BooleanFilter ENABLED = new BooleanFilter("enabled", "u.enabled");

    @Test
    public void equalityWithNullCompilesToIsNull() {
        Assertions.assertEquals("u.enabled is null", ENABLED.configure(new String[]{"EQ", null}).expression());
    }

    @Test
    public void inequalityWithNullCompilesToIsNotNull() {
        Assertions.assertEquals("u.enabled is not null", ENABLED.configure(new String[]{"NEQ", null}).expression());
    }

    @Test
    public void valuesAreBoundAsBooleans() {
        final var cf = ENABLED.configure(new String[]{"EQ", "true"});
        Assertions.assertEquals("u.enabled = ?", cf.expression());
        Assertions.assertArrayEquals(new Object[]{Boolean.TRUE}, cf.parameters());
    }

    @Test
    public void booleanValuesAreCaseInsensitive() {
        Assertions.assertArrayEquals(new Object[]{Boolean.FALSE}, ENABLED.configure(new String[]{"EQ", "FALSE"}).parameters());
    }

    @Test
    public void malformedValuesAreRejected() {
        Assertions.assertThrows(BadRequestException.class, () -> ENABLED.configure(new String[]{"EQ", "yes"}));
        Assertions.assertThrows(BadRequestException.class, () -> ENABLED.configure(new String[]{"EQ", "1"}));
    }

    @Test
    public void inequalityIsSupported() {
        Assertions.assertEquals("u.enabled <> ?", ENABLED.configure(new String[]{"NEQ", "false"}).expression());
    }

    @Test
    public void malformedRequestsAreRejected() {
        Assertions.assertThrows(BadRequestException.class, () -> ENABLED.configure(new String[]{"EQ"}));
        Assertions.assertThrows(BadRequestException.class, () -> ENABLED.configure(new String[]{"LIKE", "true"}));
    }
}
