package net.optionfactory.keycloak.sms.client;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaceboSmsClient implements SmsClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String send(String phoneNumber, String message) {
        logger.info("Mock SMS sent to n. {}: {}", phoneNumber, message);
        return UUID.randomUUID().toString();
    }

    @Override
    public void close() {
        
    }
}
