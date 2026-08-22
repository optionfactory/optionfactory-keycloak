package net.optionfactory.keycloak.authenticators.sms;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;

public class SnsSmsClient implements SmsClient {

    private final Logger logger = Logger.getLogger(SnsSmsClient.class);
    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String senderIdOrNull;
    private final CloseableHttpClient client;

    public SnsSmsClient(CloseableHttpClient client, String accessKey, String secretKey, String region, String senderIdOrNull) {
        this.client = client;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region.toLowerCase(Locale.ROOT);
        this.senderIdOrNull = senderIdOrNull;
    }

    @Override
    public String send(String phoneNumber, String message) {
        logger.infof("sending sms to: %s, message: %s", phoneNumber, message);

        final var ps = new ArrayList<NameValuePair>();
        ps.add(new BasicNameValuePair("Action", "Publish"));
        ps.add(new BasicNameValuePair("Version", "2010-03-31"));
        ps.add(new BasicNameValuePair("PhoneNumber", phoneNumber));
        ps.add(new BasicNameValuePair("Message", message));
        ps.add(new BasicNameValuePair("MessageAttributes.entry.1.Name", "AWS.SNS.SMS.SMSType"));
        ps.add(new BasicNameValuePair("MessageAttributes.entry.1.Value.DataType", "String"));
        ps.add(new BasicNameValuePair("MessageAttributes.entry.1.Value.StringValue", "Transactional"));
        if (senderIdOrNull != null) {
            ps.add(new BasicNameValuePair("MessageAttributes.entry.2.Name", "AWS.SNS.SMS.SenderID"));
            ps.add(new BasicNameValuePair("MessageAttributes.entry.2.Value.DataType", "String"));
            ps.add(new BasicNameValuePair("MessageAttributes.entry.2.Value.StringValue", senderIdOrNull));
        }
        final var entity = new UrlEncodedFormEntity(ps, StandardCharsets.UTF_8);

        final var uuid = UUID.randomUUID().toString();
        final var headers = new ArrayList<Header>();
        headers.add(new BasicHeader("amz-sdk-invocation-id", uuid));
        headers.add(new BasicHeader("amz-sdk-request", "attempt=1; max=4"));
        headers.add(new BasicHeader("User-Agent", "opfa"));
        headers.addAll(authorizationHeaders(entity));

        final var request = new HttpPost("https://sns.%s.amazonaws.com".formatted(region));
        request.setEntity(entity);
        request.setHeaders(headers.toArray(l -> new BasicHeader[l]));
        try {
            try (final var response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    return uuid;
                }
                try (var is = response.getEntity().getContent()) {
                    throw new IllegalStateException(String.format("Error sending message: %s", new String(is.readAllBytes(), StandardCharsets.UTF_8)));
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private List<BasicHeader> authorizationHeaders(UrlEncodedFormEntity entity) {
        final var now = Instant.now().atZone(ZoneId.of("UTC"));

        final var isoInstant = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(now); //"20230327T094736Z"
        final var isoBasicDate = DateTimeFormatter.ofPattern("uuuuMMdd").format(now);  //20230327

        final var dateKey = hmacSha256("AWS4%s".formatted(secretKey).getBytes(StandardCharsets.UTF_8), isoBasicDate.getBytes(StandardCharsets.UTF_8));
        final var dateRegionKey = hmacSha256(dateKey, region.getBytes(StandardCharsets.UTF_8));
        final var dateRegionServiceKey = hmacSha256(dateRegionKey, "sns".getBytes(StandardCharsets.UTF_8));
        final var signingKey = hmacSha256(dateRegionServiceKey, "aws4_request".getBytes(StandardCharsets.UTF_8));
        final var signedHeaders = "host;x-amz-date";
        final var canonicalReq = Stream.of(
                "POST",//HTTPMethod
                "/",//CanonicalUri
                "",//CanonicalQueryString
                "host:sns.%s.amazonaws.com".formatted(region),//CanonicalHeaders
                "x-amz-date:%s".formatted(isoInstant),//CanonicalHeaders
                "", //CanonicalHeaders
                signedHeaders,//SignedHeaders
                hex(sha256(formToBytes(entity)))//HashedPayload
        ).collect(Collectors.joining("\n"));
        final var stringToSign = Stream.of(
                "AWS4-HMAC-SHA256",
                isoInstant,
                "%s/%s/sns/aws4_request".formatted(isoBasicDate, region),
                hex(sha256(canonicalReq.getBytes(StandardCharsets.UTF_8)))
        ).collect(Collectors.joining("\n"));
        final var signature = hex(hmacSha256(signingKey, stringToSign.getBytes(StandardCharsets.UTF_8)));
        final var authHeader = "AWS4-HMAC-SHA256 Credential=%s/%s/%s/sns/aws4_request, SignedHeaders=%s, Signature=%s"
                .formatted(
                        accessKey,
                        isoBasicDate,
                        region,
                        signedHeaders,
                        signature
                );
        return List.of(
                new BasicHeader("X-Amz-Date", isoInstant),
                new BasicHeader("Authorization", authHeader)
        );
    }

    @Override
    public void close() {

    }

    private static String hex(byte[] data) {
        return HexFormat.of().formatHex(data);
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) {
        try {
            final var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static byte[] formToBytes(UrlEncodedFormEntity entity) {
        try (final var is = entity.getContent()) {
            return is.readAllBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
