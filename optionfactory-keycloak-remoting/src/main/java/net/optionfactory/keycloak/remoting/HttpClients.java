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
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.logging.Logger;

public class HttpClients {

    private static final Logger LOGGER = Logger.getLogger(HttpClients.class);

    public static class KeyMaterial {

        public KeyStore keystore;
        public Optional<String> keyPassword;

        public static KeyMaterial fromJksFile(String path, Optional<String> keystorePassword, Optional<String> keyPassword) {
            try {
                final var keystore = KeyStore.getInstance("JKS");
                try (var is = new FileInputStream(path)) {
                    keystore.load(is, keystorePassword.map(pwd -> pwd.toCharArray()).orElse(null));
                }
                final KeyMaterial km = new KeyMaterial();
                km.keystore = keystore;
                km.keyPassword = keyPassword;
                return km;
            } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    public enum HostnameOptions {
        VERIFY, TRUST;
    }
    public static class Timeouts {
        public int connect;
        public int socket;

        public static Timeouts defaults() {
            final var t = new Timeouts();
            t.connect = 3_000;
            t.socket = 30_000;
            return t;
        }
        
        
    }

    public static CloseableHttpClient create(String name, Optional<KeyMaterial> keyMaterial, HostnameOptions hostnameOptions, Timeouts timeouts) {
        final var sslcb = new SSLContextBuilder();
        try {
            sslcb.loadTrustMaterial(null, (chain, authType) -> true);
            keyMaterial.ifPresent(km -> {
                try {
                    sslcb.loadKeyMaterial(km.keystore, km.keyPassword.map(pwd -> pwd.toCharArray()).orElse(null));
                } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException ex) {
                    throw new IllegalStateException(ex);
                }
            });
            final var sslc = sslcb.build();
            final var hostnameVerifier = hostnameOptions == HostnameOptions.VERIFY ? null : new NoopHostnameVerifier();
            final var socketFactory = new SSLConnectionSocketFactory(sslc, hostnameVerifier);
            final var counter = new AtomicLong(0);
            return HttpClientBuilder.create()
                    .setSSLSocketFactory(socketFactory)
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(timeouts.connect)
                            .setSocketTimeout(timeouts.socket)
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
                    }).build();

        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
