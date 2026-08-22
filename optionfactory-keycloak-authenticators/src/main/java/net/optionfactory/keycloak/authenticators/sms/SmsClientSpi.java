package net.optionfactory.keycloak.authenticators.sms;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;
import net.optionfactory.keycloak.providers.Conf;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class SmsClientSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "opfa-sms-client";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return SmsClient.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return Factory.class;
    }

    public static class Factory implements ProviderFactory<SmsClient> {

        private final Logger logger = Logger.getLogger(Factory.class);
        private final AtomicReference<CloseableHttpClient> httpClientRef = new AtomicReference<>();
        private final AtomicReference<SmsClient> clientRef = new AtomicReference<>();

        @Override
        public void init(Config.Scope scope) {
            final var config = new Conf(getId(), scope);

            final var type = config.anyOfWithDefault("type", "placebo", "placebo", "sns");
            if ("placebo".equals(type)) {
                logger.infof("configured a PlaceboSmsClient");
                clientRef.set(new PlaceboSmsClient());
                return;
            }

            final var clientId = config.string("clientId");
            final var clientSecret = config.string("clientSecret");
            final var region = config.string("region", "eu-west-1");
            final var senderIdOrNull = config.string("senderId", null);
            logger.infof("SnsSmsClient configured: clientId:%s, clientSecret:%s, region:%s, senderId:%s",
                    clientId,
                    "***hidden***",
                    region,
                    senderIdOrNull);
            try {
                // sns.<region>.amazonaws.com serves a publicly-trusted certificate:
                // default jvm trust material and hostname verification are used.
                final var sslContext = new SSLContextBuilder().build();
                final var hostnameVerifier = new DefaultHostnameVerifier();

                final var socketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);

                final var httpClient = HttpClientBuilder.create()
                        .setSSLSocketFactory(socketFactory)
                        .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(3_000).build())
                        .setDefaultSocketConfig(SocketConfig.custom().setSoKeepAlive(true).build())
                        .build();

                httpClientRef.set(httpClient);
                clientRef.set(new SnsSmsClient(httpClient, clientId, clientSecret, region, senderIdOrNull));
            } catch (KeyManagementException | NoSuchAlgorithmException ex) {
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
            return "opfa-sms-client";
        }

    }

}
