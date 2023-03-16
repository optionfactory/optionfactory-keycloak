package net.optionfactory.keycloak.sms.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.logging.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class SnsSmsClient implements SmsClient {

    private final Logger logger = Logger.getLogger(SnsSmsClient.class);
    private final Map<String, MessageAttributeValue> attrs = new ConcurrentHashMap<>();
    private final SnsClient client;

    public SnsSmsClient(String accessKey, String secretKey, String region, String senderId) {
        this.attrs.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder().stringValue("Transactional").dataType("String").build());
        if (senderId != null) {
            this.attrs.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder().stringValue(senderId).dataType("String").build());
        }
        this.client = SnsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
    }

    @Override
    public String send(String phoneNumber, String message) {
        logger.info(String.format("sending sms to: %s, message: %s", phoneNumber, message));
        final var result = client.publish(PublishRequest.builder()
                .message(message)
                .phoneNumber(phoneNumber)
                .messageAttributes(attrs)
                .build());
        return result.messageId();
    }

    @Override
    public void close() {
        client.close();
    }

}
