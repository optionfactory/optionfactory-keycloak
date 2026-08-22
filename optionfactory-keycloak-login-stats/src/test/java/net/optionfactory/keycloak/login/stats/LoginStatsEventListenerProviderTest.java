package net.optionfactory.keycloak.login.stats;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

public class LoginStatsEventListenerProviderTest {

    private static final String ATTR = "loginStats";

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> iface, java.util.function.BiFunction<java.lang.reflect.Method, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, (p, m, a) -> handler.apply(m, a));
    }

    private static Event loginEvent() {
        final var e = new Event();
        e.setType(EventType.LOGIN);
        e.setRealmId("realm-1");
        e.setUserId("user-1");
        return e;
    }

    private static final class FakeUser {

        final Map<String, List<String>> attributes = new HashMap<>();
        private final UserModel model = proxy(UserModel.class, (m, a) -> {
            return switch (m.getName()) {
                case "getAttributeStream" ->
                    attributes.getOrDefault((String) a[0], List.of()).stream();
                case "setAttribute" -> {
                    attributes.put((String) a[0], (List<String>) a[1]);
                    yield null;
                }
                default ->
                    null;
            };
        });

        UserModel model() {
            return model;
        }
    }

    private static KeycloakSession session(RealmModel realm, UserModel user) {
        final var realms = proxy(RealmProvider.class, (m, a) -> realm);
        final var users = proxy(UserProvider.class, (m, a) -> user);
        return proxy(KeycloakSession.class, (m, a) -> {
            return "realms".equals(m.getName()) ? realms : users;
        });
    }

    @Test
    public void firstLoginInitializesCountAndKeepsFirstTimestamp() {
        final var user = new FakeUser();
        new LoginStatsEventListenerProvider(session(proxy(RealmModel.class, (m, a) -> null), user.model()), ATTR).onEvent(loginEvent());
        final var value = user.attributes.get(ATTR).get(0);
        final var parts = value.split(":", 3);
        Assertions.assertEquals(3, parts.length);
        Assertions.assertEquals("1", parts[0], "first login must record count=1");
        Assertions.assertEquals(parts[1], parts[2], "on first login first==last");
    }

    @Test
    public void subsequentLoginsIncrementCountAndPreserveFirst() {
        final var user = new FakeUser();
        user.attributes.put(ATTR, List.of("5:1000:2000"));
        new LoginStatsEventListenerProvider(session(proxy(RealmModel.class, (m, a) -> null), user.model()), ATTR).onEvent(loginEvent());
        final var parts = user.attributes.get(ATTR).get(0).split(":", 3);
        Assertions.assertEquals("6", parts[0]);
        Assertions.assertEquals("1000", parts[1], "first login timestamp must be sticky");
        Assertions.assertNotEquals("2000", parts[2]);
    }

    @Test
    public void malformedStoredAttributeRestartsFromDefaults() {
        for (final var malformed : new String[]{"", "garbage", "5", "a:b:c", "5:x:2000", "5:1000:2000:extra:colons:ok"}) {
            final var user = new FakeUser();
            user.attributes.put(ATTR, List.of(malformed));
            new LoginStatsEventListenerProvider(session(proxy(RealmModel.class, (m, a) -> null), user.model()), ATTR).onEvent(loginEvent());
            Assertions.assertDoesNotThrow(() -> Long.parseLong(user.attributes.get(ATTR).get(0).split(":", 3)[0]),
                    () -> "value must recover for malformed input: '" + malformed + "'");
        }
    }

    @Test
    public void missingUserOrRealmIsSkipped() {
        final var withNullRealm = session(null, new FakeUser().model());
        Assertions.assertDoesNotThrow(() -> new LoginStatsEventListenerProvider(withNullRealm, ATTR).onEvent(loginEvent()));
        final var withNullUser = session(proxy(RealmModel.class, (m, a) -> null), null);
        Assertions.assertDoesNotThrow(() -> new LoginStatsEventListenerProvider(withNullUser, ATTR).onEvent(loginEvent()));
    }

    @Test
    public void nonLoginEventsAreIgnored() {
        final var user = new FakeUser();
        final var e = loginEvent();
        e.setType(EventType.LOGOUT);
        new LoginStatsEventListenerProvider(session(proxy(RealmModel.class, (m, a) -> null), user.model()), ATTR).onEvent(e);
        Assertions.assertNull(user.attributes.get(ATTR));
    }
}
