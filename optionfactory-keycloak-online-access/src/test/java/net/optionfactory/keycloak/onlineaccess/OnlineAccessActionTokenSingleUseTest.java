package net.optionfactory.keycloak.onlineaccess;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.models.RevokedTokenProvider;
import org.keycloak.models.SingleUseObjectProvider;

/**
 * Verifies single-use enforcement of the online-access action token without a
 * running keycloak: the guard must write through the RevokedTokenProvider spi
 * (what the framework check reads since keycloak 26.6) and must reject
 * concurrent/replayed consumption.
 */
public class OnlineAccessActionTokenSingleUseTest {

    /**
     * Mimics the put-if-absent contract of both real implementations
     * (InfinispanRevokedTokenProvider delegates to putIfAbsent,
     * JpaRevokedTokenProvider uses insertRevokeTokenIfAbsent and checks rows==1).
     */
    private static final class FakeRevokedTokenProvider implements RevokedTokenProvider {

        final Map<String, Long> revoked = new HashMap<>();

        @Override
        public boolean put(String id, long lifespanSeconds) {
            return revoked.putIfAbsent(id, lifespanSeconds) == null;
        }

        @Override
        public boolean contains(String id) {
            return revoked.containsKey(id);
        }

        @Override
        public void close() {
        }
    }

    private static OnlineAccessActionToken token() {
        // expiration comfortably in the future relative to Time.currentTimeSeconds()
        return new OnlineAccessActionToken("user-1", Integer.MAX_VALUE, "webapp", "https://app.example.com/cb");
    }

    @Test
    public void firstUseIsAcceptedAndMarksTheTokenAsRevoked() {
        final var revoked = new FakeRevokedTokenProvider();
        final var token = token();
        Assertions.assertTrue(OnlineAccessActionTokenHandler.consumeTokenIfUnused(revoked, token));
        Assertions.assertTrue(revoked.contains(token.serializeKey()), "the marker key must be visible to the provider the framework checks");
    }

    @Test
    public void replayIsRejected() {
        final var revoked = new FakeRevokedTokenProvider();
        final var token = token();
        Assertions.assertTrue(OnlineAccessActionTokenHandler.consumeTokenIfUnused(revoked, token));
        Assertions.assertFalse(OnlineAccessActionTokenHandler.consumeTokenIfUnused(revoked, token), "second use of the same token must be refused");
    }

    @Test
    public void concurrentReplayIsRejectedBecausePutIsAtomic() {
        // two threads, first insertion wins; the framework check reads the same provider
        final var revoked = new FakeRevokedTokenProvider();
        final var token = token();
        final var results = new java.util.concurrent.ConcurrentLinkedQueue<Boolean>();
        java.util.stream.IntStream.range(0, 2).parallel().forEach(i -> {
            results.add(OnlineAccessActionTokenHandler.consumeTokenIfUnused(revoked, token));
        });
        Assertions.assertEquals(1, results.stream().filter(b -> b).count(), "exactly one concurrent consumer may win");
        Assertions.assertEquals(1, results.stream().filter(b -> !b).count());
    }

    @Test
    public void markerIsWrittenUnderTheKeyTheFrameworkCheckReads() {
        // replicates LoginActionsServiceChecks.checkTokenWasNotUsedYet on 26.7.2:
        // it asks the RevokedTokenProvider (no key suffixing by the caller)
        final var revoked = new FakeRevokedTokenProvider();
        final var token = token();
        OnlineAccessActionTokenHandler.consumeTokenIfUnused(revoked, token);
        Assertions.assertTrue(revoked.contains(token.serializeKey()),
                "the framework's checkTokenWasNotUsedYet must observe the marker");
    }

    @Test
    public void distinctTokensDoNotInterfere() {
        final var revoked = new FakeRevokedTokenProvider();
        Assertions.assertTrue(OnlineAccessActionTokenHandler.consumeTokenIfUnused(revoked, token()));
        final var other = new OnlineAccessActionToken("user-2", Integer.MAX_VALUE, "webapp", "https://app.example.com/cb");
        Assertions.assertTrue(OnlineAccessActionTokenHandler.consumeTokenIfUnused(revoked, other));
    }

    @Test
    public void serializeKeyIsStableForAGivenTokenInstance() {
        final var token = token();
        Assertions.assertEquals(token.serializeKey(), token.serializeKey(), "the key must be deterministic so check and marker agree");
        // each issued token carries a fresh nonce: distinct issuances must not collide
        Assertions.assertNotEquals(token().serializeKey(), token().serializeKey());
    }

}
