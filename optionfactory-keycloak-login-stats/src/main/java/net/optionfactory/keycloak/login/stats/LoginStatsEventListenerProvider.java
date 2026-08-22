package net.optionfactory.keycloak.login.stats;

import java.util.List;
import net.optionfactory.keycloak.providers.model.Models;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public class LoginStatsEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;
    private final String attribute;

    public LoginStatsEventListenerProvider(KeycloakSession session, String attribute) {
        this.session = session;
        this.attribute = attribute;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() != EventType.LOGIN) {
            return;
        }
        final var realm = session.realms().getRealm(event.getRealmId());
        if (realm == null) {
            return;
        }
        final var user = session.users().getUserById(realm, event.getUserId());
        if (user == null) {
            return;
        }
        final long now = System.currentTimeMillis();
        final var parsed = Models.attributeFirst(user, attribute)
                .filter(v -> !v.isBlank())
                .map(v -> v.split(":", 3))
                .orElse(null);
        long count = 0;
        long first = now;
        if (parsed != null && parsed.length == 3 && parsed[0].matches("\\d+") && parsed[1].matches("\\d+")) {
            try {
                count = Long.parseLong(parsed[0]);
                first = Long.parseLong(parsed[1]);
            } catch (NumberFormatException e) {
                count = 0;
                first = now;
            }
        }
        user.setAttribute(attribute, List.of(String.format("%s:%s:%s", count + 1, first, now)));
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
    }

    @Override
    public void close() {

    }

}
