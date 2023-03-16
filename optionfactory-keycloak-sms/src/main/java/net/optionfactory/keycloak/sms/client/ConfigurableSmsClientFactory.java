package net.optionfactory.keycloak.sms.client;

import java.util.concurrent.atomic.AtomicReference;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class ConfigurableSmsClientFactory implements SmsClientFactory {

    private final Logger logger = Logger.getLogger(ConfigurableSmsClientFactory.class);
    private final AtomicReference<SmsClient> clientRef = new AtomicReference<>();

    @Override
    public void init(Config.Scope config) {
        final var clientId = config.get("clientId");

        if (clientId == null) {
            logger.infof("clientId is not configured, using a PlaceboSmsClient");
            clientRef.set(new PlaceboSmsClient());
            return;
        }
        final var clientSecret = config.get("clientSecret");
        if (clientSecret == null) {
            logger.infof("clientSecret is not configured, using a PlaceboSmsClient");
            clientRef.set(new PlaceboSmsClient());
            return;
        }
        final var region = config.get("region", "eu-west-1");
        final var senderId = config.get("senderId");
        logger.infof("ConfigurableSmsClientFactory configured: clientId:%s, clientSecret:%s, region:%s, senderId:%s",
                clientId,
                "***hidden***",
                region,
                senderId);
        clientRef.set(new SnsSmsClient(clientId, clientSecret, region, senderId));
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public SmsClient create(KeycloakSession session) {
        return clientRef.get();
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "opfa-sms-client-configurable";
    }

}
