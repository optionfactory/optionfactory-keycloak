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
        final long now = System.currentTimeMillis();
        final var realm = session.realms().getRealm(event.getRealmId());
        final var user = session.users().getUserById(realm, event.getUserId());
        final var current = Models.attributeFirst(user, attribute)
                .filter(v -> !v.isBlank())
                .map(v -> v.split(":"))
                .orElse(new String[]{"0", Long.toString(now), ""});

        final long count = Long.parseLong(current[0]) + 1;
        final long first = Long.parseLong(current[1]);
        user.setAttribute(attribute, List.of(String.format("%s:%s:%s", count, first, now)));
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
    }

    @Override
    public void close() {

    }

}
