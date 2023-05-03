package net.optionfactory.keycloak.authenticators.sms;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class ConfigurableSmsClientFactory implements SmsClientFactory {

    private final Logger logger = Logger.getLogger(ConfigurableSmsClientFactory.class);
    private final AtomicReference<CloseableHttpClient> httpClientRef = new AtomicReference<>();
    private final AtomicReference<SmsClient> clientRef = new AtomicReference<>();

    @Override
    public void init(Config.Scope config) {
        final var type = config.get("type", "placebo");
        SmsClient.ensure(Set.of("placebo", "sns").contains(type), "type can only be placebo or sns: got %s", type);
        if("placebo".equals(type)){
            logger.infof("configured a PlaceboSmsClient");
            clientRef.set(new PlaceboSmsClient());
            return;
        }

        final var clientId = config.get("clientId");
        SmsClient.ensure(clientId != null, "clientId must be configured");
        final var clientSecret = config.get("clientSecret");
        SmsClient.ensure(clientSecret != null, "clientSecret must be configured");
        final var region = config.get("region", "eu-west-1");
        final var senderIdOrNull = config.get("senderId");
        logger.infof("SnsSmsClient configured: clientId:%s, clientSecret:%s, region:%s, senderId:%s",
                clientId,
                "***hidden***",
                region,
                senderIdOrNull);
        try {
            final var sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (chain, authType) -> true).build(); 
            final var hostnameVerifier = new NoopHostnameVerifier();

            final var socketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);

            final var httpClient = HttpClientBuilder.create()
                    .setSSLSocketFactory(socketFactory)
                    .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(3_000).build())
                    .setDefaultSocketConfig(SocketConfig.custom().setSoKeepAlive(true).build())
                    .build();

            httpClientRef.set(httpClient);
            clientRef.set(new SnsSmsClient(httpClient, clientId, clientSecret, region, senderIdOrNull));
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
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
        final var maybeHttpClient = httpClientRef.get();
        if (maybeHttpClient == null) {
            return;
        }
        try {
            maybeHttpClient.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String getId() {
        return "opfa-sms-client-configurable";
    }

}
