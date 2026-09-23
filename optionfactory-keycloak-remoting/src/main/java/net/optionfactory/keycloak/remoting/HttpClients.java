package net.optionfactory.keycloak.remoting;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import javax.net.ssl.HostnameVerifier;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.logging.Logger;
import java.time.Duration;
import org.apache.http.client.CookieStore;
import org.apache.http.ssl.TrustStrategy;

public class HttpClients {

    private static final Logger LOGGER = Logger.getLogger(HttpClients.class);

    public static class KeyMaterial {

        public final KeyStore keystore;
        public final Optional<String> keyPassword;

        public KeyMaterial(KeyStore keystore, Optional<String> keyPassword) {
            this.keystore = keystore;
            this.keyPassword = keyPassword;
        }

        public static KeyMaterial fromKeystoreFile(String path, String type, Optional<String> keystorePassword, Optional<String> keyPassword) {
            try {
                final var keystore = KeyStore.getInstance(type);
                try (var is = new FileInputStream(path)) {
                    keystore.load(is, keystorePassword.map(pwd -> pwd.toCharArray()).orElse(null));
                }
                return new KeyMaterial(keystore, keyPassword);
            } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public static KeyMaterial fromJksFile(String path, Optional<String> keystorePassword, Optional<String> keyPassword) {
            return fromKeystoreFile(path, "JKS", keystorePassword, keyPassword);
        }

        public static KeyMaterial fromPkcs12File(String path, Optional<String> keystorePassword, Optional<String> keyPassword) {
            return fromKeystoreFile(path, "PKCS12", keystorePassword, keyPassword);
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {

        private final String name;
        private KeyMaterial keyMaterial;
        private KeyStore truststore = null;
        private TrustStrategy trustStrategy = null;
        private HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
        private Duration connectTimeout = Duration.ofSeconds(3);
        private Duration socketTimeout = Duration.ofSeconds(30);
        private Duration connectionRequestTimeout = Duration.ofMinutes(2);
        private boolean followRedirects = false;
        private CookieStore cookieStore;

        private Builder(String name) {
            this.name = name;
        }

        /**
         * Mutual TLS: present the client certificate from the given key
         * material.
         */
        public Builder keyMaterial(KeyMaterial keyMaterial) {
            this.keyMaterial = keyMaterial;
            return this;
        }

        /**
         * Validate the certificate chain against the default jvm trust
         * material (default).
         */
        public Builder trustSystem() {
            this.truststore = null;
            this.trustStrategy = null;
            return this;
        }

        /**
         * Validate the certificate chain against the given truststore.
         */
        public Builder trustCertificatesIn(KeyStore truststore) {
            if (truststore == null) {
                throw new IllegalArgumentException("truststore is required");
            }
            this.truststore = truststore;
            this.trustStrategy = null;
            return this;
        }

        /**
         * Disable certificate chain validation. Use only against endpoints
         * where transport authenticity is irrelevant or enforced by other
         * means.
         */
        public Builder trustAny() {
            this.truststore = null;
            this.trustStrategy = (chain, authType) -> true;
            return this;
        }

        /**
         * Require the server certificate to match the requested hostname,
         * rfc 2818/6125 (default).
         */
        public Builder verifyHostnames() {
            this.hostnameVerifier = new DefaultHostnameVerifier();
            return this;
        }

        /**
         * Verify hostnames using the given verifier instead of the default
         * rfc 2818/6125 policy.
         */
        public Builder verifyHostnamesWith(HostnameVerifier hostnameVerifier) {
            if (hostnameVerifier == null) {
                throw new IllegalArgumentException("hostnameVerifier is required");
            }
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * Do not verify hostnames.
         */
        public Builder verifyNoHostname() {
            this.hostnameVerifier = new NoopHostnameVerifier();
            return this;
        }

        public Builder timeouts(Duration connectTimeout, Duration socketTimeout) {
            return timeouts(connectTimeout, socketTimeout, this.connectionRequestTimeout);
        }

        /**
         * The third timeout bounds the wait for a connection from the pool, which
         * the other two never reach: a thread queued for a slot has no socket yet.
         * Left unset it is -1, which the pool reads as "wait forever", so a route
         * whose connections are all leaked never fails, it stops answering.
         */
        public Builder timeouts(Duration connectTimeout, Duration socketTimeout, Duration connectionRequestTimeout) {
            if (connectTimeout == null || connectTimeout.isNegative() || connectTimeout.isZero()) {
                throw new IllegalArgumentException("connectTimeout must be positive");
            }
            if (socketTimeout == null || socketTimeout.isNegative() || socketTimeout.isZero()) {
                throw new IllegalArgumentException("socketTimeout must be positive");
            }
            if (connectionRequestTimeout == null || connectionRequestTimeout.isNegative() || connectionRequestTimeout.isZero()) {
                throw new IllegalArgumentException("connectionRequestTimeout must be positive");
            }
            this.connectTimeout = connectTimeout;
            this.socketTimeout = socketTimeout;
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        /**
         * Follow 3xx redirects (GET/HEAD, up to the library limit) instead of
         * returning them to the caller. Disabled by default: the destination
         * of a redirect is server-controlled, and following it replays the
         * request (headers, query) to that destination.
         */
        public Builder followRedirects() {
            this.followRedirects = true;
            return this;
        }

        /**
         * Maintain cookies across requests using the given store. Disabled by
         * default: a shared client would otherwise carry session state (and
         * any authenticated session cookies) across unrelated requests.
         */
        public Builder cookieStore(CookieStore cookieStore) {
            if (cookieStore == null) {
                throw new IllegalArgumentException("cookieStore is required");
            }
            this.cookieStore = cookieStore;
            return this;
        }

        public CloseableHttpClient build() {
            final var sslcb = new SSLContextBuilder();
            try {
                // null strategy = standard jsse validation against the store
                // (null store = default jvm trust material)
                sslcb.loadTrustMaterial(truststore, trustStrategy);
                if (keyMaterial != null) {
                    sslcb.loadKeyMaterial(keyMaterial.keystore, keyMaterial.keyPassword.map(pwd -> pwd.toCharArray()).orElse(null));
                }
                final var sslc = sslcb.build();
                final var socketFactory = new SSLConnectionSocketFactory(sslc, hostnameVerifier);
                final var counter = new AtomicLong(0);
                final var cb = HttpClientBuilder.create()
                        .setSSLSocketFactory(socketFactory)
                        // redirects, cookies and auth caching are off by default:
                        // the client does exactly what each request says. retries
                        // keep the library default (idempotent requests only).
                        .disableAuthCaching()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setConnectTimeout((int) connectTimeout.toMillis())
                                .setSocketTimeout((int) socketTimeout.toMillis())
                                .setConnectionRequestTimeout((int) connectionRequestTimeout.toMillis())
                                .build())
                        .setDefaultSocketConfig(SocketConfig.custom().setSoKeepAlive(true).build())
                        .addInterceptorLast((HttpRequest hr, HttpContext hc) -> {
                            if (hc.getAttribute("log") == null) {
                                return;
                            }
                            final var rid = counter.incrementAndGet();
                            hc.setAttribute("rid", rid);
                            final var uri = hr.getRequestLine().getUri();
                            final var method = hr.getRequestLine().getMethod();
                            LOGGER.infof("[c:%s][rid:%s] %s request on %s", name, rid, method, uri);
                        })
                        .addInterceptorFirst((HttpResponse hr, HttpContext hc) -> {
                            if (hc.getAttribute("log") == null) {
                                return;
                            }
                            final var rid = (long) hc.getAttribute("rid");
                            final var status = hr.getStatusLine().getStatusCode();
                            LOGGER.infof("[c:%s][rid:%s] response status: %s", name, rid, status);
                        });
                if (!followRedirects) {
                    cb.disableRedirectHandling();
                }
                if (cookieStore == null) {
                    cb.disableCookieManagement();
                } else {
                    cb.setDefaultCookieStore(cookieStore);
                }
                return cb.build();
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
                throw new IllegalStateException(ex);
            } catch (UnrecoverableKeyException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }

}
