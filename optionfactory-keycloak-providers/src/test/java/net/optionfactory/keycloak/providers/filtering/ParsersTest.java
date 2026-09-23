package net.optionfactory.keycloak.providers.filtering;

import net.optionfactory.keycloak.providers.validation.Problem;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import jakarta.ws.rs.BadRequestException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import net.optionfactory.keycloak.providers.filtering.ConfiguredSorter.Direction;
import org.junit.jupiter.api.Test;

public class ParsersTest {

    private enum Color {
        RED, GREEN
    }

    @Test
    public void enumerationYieldsValueWhenNameIsKnown() {
        Assertions.assertEquals(Color.RED, Parsers.enumeration(Color.class, "RED", "color"));
        Assertions.assertEquals(Color.GREEN, Parsers.enumeration(Color.class, "GREEN", "color"));
    }

    @Test
    public void enumerationIsCaseSensitive() {
        final var ex = Assertions.assertThrows(BadRequestException.class, () -> Parsers.enumeration(Color.class, "red", "color"));
        Assertions.assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    public void enumerationRejectsUnknownNames() {
        Assertions.assertThrows(BadRequestException.class, () -> Parsers.enumeration(Color.class, "BLUE", "color"));
    }

    @Test
    public void instantParsesIsoInstantToEpochMillis() {
        final var expected = Instant.parse("2024-01-01T00:00:00Z").toEpochMilli();
        Assertions.assertEquals(expected, Parsers.instant("2024-01-01T00:00:00Z", "createdAt"));
    }

    @Test
    public void instantToleratesNull() {
        Assertions.assertEquals(null, Parsers.instant(null, "createdAt"));
    }

    @Test
    public void instantRejectsMalformedValues() {
        Assertions.assertThrows(BadRequestException.class, () -> Parsers.instant("2024-01-01", "createdAt"));
        Assertions.assertThrows(BadRequestException.class, () -> Parsers.instant("", "createdAt"));
    }

    @Test
    public void sortersYieldEmptyListForNullOrNoRequest() {
        Assertions.assertEquals(List.of(), Parsers.sorters(Map.of("username", "u.username"), null));
        Assertions.assertEquals(List.of(), Parsers.sorters(Map.of("username", "u.username"), List.of()));
    }

    @Test
    public void sortersDefaultToAscending() {
        final var sorters = Parsers.sorters(Map.of("username", "u.username"), List.of("username"));
        Assertions.assertEquals(List.of(new ConfiguredSorter("u.username", Direction.ASC)), sorters);
    }

    @Test
    public void sortersHonorDescendingDirection() {
        final var sorters = Parsers.sorters(Map.of("username", "u.username"), List.of("username,DESC"));
        Assertions.assertEquals(List.of(new ConfiguredSorter("u.username", Direction.DESC)), sorters);
    }

    @Test
    public void sortersDirectionIsCaseInsensitive() {
        final var sorters = Parsers.sorters(Map.of("username", "u.username"), List.of("username,desc"));
        Assertions.assertEquals(List.of(new ConfiguredSorter("u.username", Direction.DESC)), sorters);
    }

    @Test
    public void sortersIgnoreUnknownNames() {
        Assertions.assertEquals(List.of(), Parsers.sorters(Map.of("username", "u.username"), List.of("evil; drop table users", "email")));
    }

    @Test
    public void sortersMapToConfiguredAliases() {
        final var sorters = Parsers.sorters(Map.of("createdAt", "u.created_timestamp"), List.of(" createdAt , DESC "));
        Assertions.assertEquals(List.of(new ConfiguredSorter("u.created_timestamp", Direction.DESC)), sorters);
    }

    @Test
    public void sortersKeepRequestOrder() {
        final var sorters = Parsers.sorters(Map.of("username", "u.username", "email", "u.email"), List.of("email,DESC", "username"));
        Assertions.assertEquals(List.of(new ConfiguredSorter("u.email", Direction.DESC), new ConfiguredSorter("u.username", Direction.ASC)), sorters);
    }

    @Test
    public void ensurePassesThroughWhenTestSucceeds() {
        Parsers.ensure(true, "path", "no failure");
    }

    @Test
    public void ensureThrowsBadRequestWithProblemDetailsWhenTestFails() {
        final var ex = Assertions.assertThrows(BadRequestException.class, () -> Parsers.ensure(false, "path", "failed %s", "because"));
        Assertions.assertNotNull(ex.getResponse());
        Assertions.assertEquals(400, ex.getResponse().getStatus());
        Assertions.assertTrue(ex.getResponse().getEntity() instanceof List<?> problems && problems.size() == 1);
    }
    @Test
    public void aSortExpressionOfNothingButSeparatorsIsIgnored() {
        Assertions.assertEquals(List.of(), Parsers.sorters(Map.of("username", "u.username"), List.of(",")));
        Assertions.assertEquals(List.of(), Parsers.sorters(Map.of("username", "u.username"), List.of(",,")));
    }

    @Test
    public void aNullSortExpressionIsIgnored() {
        final var requested = new ArrayList<String>();
        requested.add(null);
        requested.add("username,DESC");

        final var sorters = Parsers.sorters(Map.of("username", "u.username"), requested);

        Assertions.assertEquals(1, sorters.size());
    }

    @Test
    public void anInstantBeyondEpochMillisecondsIsRejected() {
        final var ex = Assertions.assertThrows(BadRequestException.class,
                () -> Parsers.instant("+1000000000-12-31T23:59:59Z", "createdAt"));
        final var problems = (List<?>) ex.getResponse().getEntity();

        Assertions.assertEquals(1, problems.size());
        Assertions.assertTrue(((Problem) problems.get(0)).reason().contains("out of range"));
    }

    @Test
    public void anInstantBeforeEpochMillisecondsIsRejected() {
        Assertions.assertThrows(BadRequestException.class,
                () -> Parsers.instant("-1000000000-01-01T00:00:00Z", "createdAt"));
    }
}
