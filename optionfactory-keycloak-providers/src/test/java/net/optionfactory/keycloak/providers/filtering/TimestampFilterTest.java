package net.optionfactory.keycloak.providers.filtering;

import org.junit.jupiter.api.Assertions;
import jakarta.ws.rs.BadRequestException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class TimestampFilterTest {

    private static final TimestampFilter CREATED_AT = new TimestampFilter("createdAt", "u.created_timestamp");

    @Test
    public void equalityWithNullCompilesToIsNull() {
        Assertions.assertEquals("u.created_timestamp is null", CREATED_AT.configure(new String[]{"EQ", null}).expression());
    }

    @Test
    public void inequalityWithNullCompilesToIsNotNull() {
        Assertions.assertEquals("u.created_timestamp is not null", CREATED_AT.configure(new String[]{"NEQ", null}).expression());
    }

    @Test
    public void orderingOperatorsRejectNullValues() {
        Assertions.assertThrows(BadRequestException.class, () -> CREATED_AT.configure(new String[]{"LT", null}));
        Assertions.assertThrows(BadRequestException.class, () -> CREATED_AT.configure(new String[]{"GTE", null}));
    }

    @Test
    public void instantsAreBoundAsEpochMillis() {
        final var expected = Instant.parse("2024-05-06T07:08:09Z").toEpochMilli();
        final var cf = CREATED_AT.configure(new String[]{"GTE", "2024-05-06T07:08:09Z"});
        Assertions.assertEquals("u.created_timestamp >= ?", cf.expression());
        Assertions.assertArrayEquals(new Object[]{expected}, cf.parameters());
    }

    @Test
    public void malformedInstantsAreRejected() {
        Assertions.assertThrows(BadRequestException.class, () -> CREATED_AT.configure(new String[]{"EQ", "yesterday"}));
    }

    @Test
    public void malformedRequestsAreRejected() {
        Assertions.assertThrows(BadRequestException.class, () -> CREATED_AT.configure(new String[]{"EQ"}));
        Assertions.assertThrows(BadRequestException.class, () -> CREATED_AT.configure(new String[]{"LIKE", "2024-05-06T07:08:09Z"}));
    }
}
