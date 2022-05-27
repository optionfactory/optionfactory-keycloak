package net.optionfactory.keycloak.login.stats;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Must be configured on {@code Event -> Config -> Event Listeners}
 * 
 */
public class LoginStatsEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger logger = Logger.getLogger(LoginStatsEventListenerProviderFactory.class);
    private String attribute;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new LoginStatsEventListenerProvider(session, attribute);
    }

    @Override
    public void init(Config.Scope config) {
        this.attribute = config.get("attribute", "loginStats");
        logger.infof("%s initialized: attribute=%s", this.getId(), this.attribute);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        //kc.spi-eventsListener-opfa-login-stats
        return "opfa-login-stats";
    }

}
