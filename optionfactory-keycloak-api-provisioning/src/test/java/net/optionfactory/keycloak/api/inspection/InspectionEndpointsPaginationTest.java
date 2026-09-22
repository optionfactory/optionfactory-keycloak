package net.optionfactory.keycloak.api.inspection;

import jakarta.ws.rs.BadRequestException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The pagination guards run before the session is dereferenced, so a null
 * session is enough to exercise them.
 */
public class InspectionEndpointsPaginationTest {

    private final InspectionEndpoints endpoints = new InspectionEndpoints(null);

    @Test
    public void usersRejectsNegativeOffset() {
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.users(null, Map.of(), List.of(), -1, 0));
    }

    @Test
    public void usersRejectsNegativeLimit() {
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.users(null, Map.of(), List.of(), 0, -1));
    }

    @Test
    public void usersAcceptsZeroOffsetAndLimit() {
        // zero is the documented default meaning "no constraint": must reach the
        // session lookup rather than fail validation
        Assertions.assertThrows(NullPointerException.class, () -> endpoints.users(null, Map.of(), List.of(), 0, 0));
    }

    @Test
    public void groupMembershipByPathRejectsNegativePagination() {
        final var endpoints = new InspectionEndpoints(null);
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.groupMemberhip(null, "/staff", -1, 0));
        Assertions.assertThrows(BadRequestException.class, () -> endpoints.groupMemberhip(null, "/staff", 0, -1));
    }

    /// The window is what tells a slice where to stop: it fetches one row more than asked for,
    /// and that extra row must be reported as "there is more" rather than handed out.

    @Test
    public void aPartiallyFilledPageIsTheLastOne() {
        final var window = InspectionEndpoints.Window.of(3, 5);

        Assertions.assertEquals(3, window.size());
        Assertions.assertTrue(window.last());
    }

    @Test
    public void aPageFilledExactlyIsTheLastOne() {
        // nothing was fetched beyond the limit, so there is no further page to offer
        final var window = InspectionEndpoints.Window.of(5, 5);

        Assertions.assertEquals(5, window.size());
        Assertions.assertTrue(window.last());
    }

    @Test
    public void theRowFetchedBeyondTheLimitAnnouncesTheNextPageWithoutBeingReturned() {
        final var window = InspectionEndpoints.Window.of(6, 5);

        Assertions.assertEquals(5, window.size());
        Assertions.assertFalse(window.last());
    }

    @Test
    public void anUnboundedRequestReturnsEverythingAndIsTheLastOne() {
        final var window = InspectionEndpoints.Window.of(42, 0);

        Assertions.assertEquals(42, window.size());
        Assertions.assertTrue(window.last());
    }

    @Test
    public void anEmptyResultIsTheLastOne() {
        final var window = InspectionEndpoints.Window.of(0, 5);

        Assertions.assertEquals(0, window.size());
        Assertions.assertTrue(window.last());
    }
}
