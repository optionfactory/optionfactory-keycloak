package net.optionfactory.keycloak.remoting;

import java.time.Duration;
import org.apache.http.client.config.RequestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpClientsTimeoutsTest {

    /// The built client keeps its defaults in a private field; reading it back is the only way to see
    /// what a caller's configuration actually became.
    private static RequestConfig configOf(Object client) throws Exception {
        final var field = client.getClass().getDeclaredField("defaultConfig");
        field.setAccessible(true);
        return (RequestConfig) field.get(client);
    }

    @Test
    public void theWaitForAPooledConnectionIsBounded() throws Exception {
        // -1 is what RequestConfig carries when it is not set, and the pool reads it as "wait forever"
        final var config = configOf(HttpClients.builder("test").trustSystem().verifyHostnames().build());

        Assertions.assertEquals(120_000, config.getConnectionRequestTimeout());
    }

    @Test
    public void allThreeTimeoutsAreConfigurable() throws Exception {
        final var client = HttpClients.builder("test")
                .trustSystem()
                .verifyHostnames()
                .timeouts(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(3))
                .build();

        final var config = configOf(client);

        Assertions.assertEquals(1_000, config.getConnectTimeout());
        Assertions.assertEquals(2_000, config.getSocketTimeout());
        Assertions.assertEquals(3_000, config.getConnectionRequestTimeout());
    }

    @Test
    public void theTwoArgumentFormLeavesTheThirdAtItsDefault() throws Exception {
        final var client = HttpClients.builder("test")
                .trustSystem()
                .verifyHostnames()
                .timeouts(Duration.ofSeconds(1), Duration.ofSeconds(2))
                .build();

        Assertions.assertEquals(120_000, configOf(client).getConnectionRequestTimeout());
    }

    @Test
    public void aNonPositiveConnectionRequestTimeoutIsRejected() {
        final var builder = HttpClients.builder("test").trustSystem().verifyHostnames();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.timeouts(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.timeouts(Duration.ofSeconds(1), Duration.ofSeconds(2), null));
    }
}
