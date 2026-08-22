package net.optionfactory.keycloak.remoting;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * End-to-end behavior of the built client against a local raw-socket http(s)
 * server: the trust x hostname matrix, redirect/cookie defaults and retry
 * semantics.
 */
public class HttpClientsTest {

    @TempDir
    static Path work;

    private static TinyHttpServer tlsServer;
    private static KeyStore truststore;

    @BeforeAll
    public static void bootTlsServer() throws Exception {
        final var serverJks = work.resolve("server.jks");
        final var trustJks = work.resolve("trust.jks");
        generateMaterial(serverJks, trustJks);

        final var serverKeys = KeyStore.getInstance("JKS");
        try (var is = new FileInputStream(serverJks.toFile())) {
            serverKeys.load(is, "changeit".toCharArray());
        }
        truststore = KeyStore.getInstance("JKS");
        try (var is = new FileInputStream(trustJks.toFile())) {
            truststore.load(is, "changeit".toCharArray());
        }

        tlsServer = TinyHttpServer.tls(request -> TinyHttpServer.Response.ok(), serverKeys, "changeit".toCharArray());
    }

    @AfterAll
    public static void stopTlsServer() throws Exception {
        if (tlsServer != null) {
            tlsServer.close();
        }
    }

    private static void generateMaterial(Path serverJks, Path trustJks) throws Exception {
        runKeytool("-genkeypair", "-alias", "test", "-keyalg", "RSA", "-keysize", "2048", "-validity", "1",
                "-dname", "CN=localhost", "-ext", "SAN=DNS:localhost,IP:127.0.0.1",
                "-keystore", serverJks.toString(), "-storepass", "changeit", "-keypass", "changeit", "-storetype", "JKS");
        final var cer = work.resolve("server.cer");
        runKeytool("-exportcert", "-alias", "test", "-keystore", serverJks.toString(), "-storepass", "changeit", "-file", cer.toString());
        runKeytool("-importcert", "-alias", "test", "-file", cer.toString(), "-keystore", trustJks.toString(), "-storepass", "changeit", "-noprompt", "-storetype", "JKS");
    }

    private static void runKeytool(String... args) throws Exception {
        final var javaCmd = Path.of(ProcessHandle.current().info().command().orElseThrow());
        final var tool = javaCmd.getParent().resolve("keytool");
        final var command = new ArrayList<String>();
        command.add(Files.exists(tool) ? tool.toString() : "keytool");
        command.addAll(Arrays.asList(args));
        final var pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Assertions.assertTrue(pb.start().waitFor(30, TimeUnit.SECONDS), "keytool must terminate");
    }

    @Test
    public void defaultConfigurationRejectsUntrustedSelfSignedChain() {
        // defaults: trustSystem + verifyHostnames
        final var client = HttpClients.builder("default").build();
        Assertions.assertThrows(Exception.class, () -> client.execute(new HttpGet(tlsServer.url("/"))),
                "self-signed certificate not in the jvm truststore must be rejected by default");
    }

    @Test
    public void trustCustomAcceptsWhenCertificateIsInTheTruststore() throws Exception {
        final var client = HttpClients.builder("custom").trustCertificatesIn(truststore).build();
        final var response = client.execute(new HttpGet(tlsServer.url("/")));
        Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void trustCustomRejectsUntrustedChain() throws Exception {
        final var empty = KeyStore.getInstance("JKS");
        empty.load(null, null);
        final var client = HttpClients.builder("custom-empty").trustCertificatesIn(empty).build();
        Assertions.assertThrows(Exception.class, () -> client.execute(new HttpGet(tlsServer.url("/"))));
    }

    @Test
    public void trustCertificatesInRequiresATruststore() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HttpClients.builder("no-ts").trustCertificatesIn(null));
    }

    @Test
    public void trustSelectionIsMutuallyExclusiveLastWins() throws Exception {
        final var client = HttpClients.builder("last-wins")
                .trustCertificatesIn(truststore)
                .trustSystem()
                .build();
        Assertions.assertThrows(Exception.class, () -> client.execute(new HttpGet(tlsServer.url("/"))),
                "trustSystem() must override the previously selected custom truststore");
    }

    @Test
    public void hostnameVerificationRejectsWrongHostnameEvenWhenChainIsTrusted() throws Exception {
        // the certificate only covers localhost/127.0.0.1: the machine's own hostname
        // resolves to the same host but is not in the SAN, so verification must fail
        final var wrongHost = InetAddress.getLocalHost().getHostName();
        final var client = HttpClients.builder("hostname").trustCertificatesIn(truststore).build();
        Assertions.assertThrows(Exception.class, () -> client.execute(new HttpGet(tlsServer.urlForHost(wrongHost, "/"))),
                "trusted chain but wrong hostname must be rejected");
    }

    @Test
    public void skipHostnameVerificationAcceptsWrongHostnameWithTrustedChain() throws Exception {
        final var wrongHost = InetAddress.getLocalHost().getHostName();
        final var client = HttpClients.builder("hostname-any").trustCertificatesIn(truststore).verifyNoHostname().build();
        final var response = client.execute(new HttpGet(tlsServer.urlForHost(wrongHost, "/")));
        Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void trustAnyAcceptsUntrustedSelfSignedChain() throws Exception {
        final var client = HttpClients.builder("any").trustAny().verifyNoHostname().build();
        final var response = client.execute(new HttpGet(tlsServer.url("/")));
        Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void trustAnyStillVerifiesHostnamesWhenConfigured() throws Exception {
        // chain validation off, hostname verification on: wrong hostname must still fail
        final var wrongHost = InetAddress.getLocalHost().getHostName();
        final var client = HttpClients.builder("any-verify").trustAny().build();
        Assertions.assertThrows(Exception.class, () -> client.execute(new HttpGet(tlsServer.urlForHost(wrongHost, "/"))));
    }

    @Test
    public void customHostnameVerifierIsHonored() throws Exception {
        final var expectedHost = InetAddress.getLocalHost().getHostName();
        final HostnameVerifier onlyMachineName = (host, session) -> expectedHost.equals(host);
        final var client = HttpClients.builder("custom-verifier").trustCertificatesIn(truststore).verifyHostnamesWith(onlyMachineName).build();
        final var accepted = client.execute(new HttpGet(tlsServer.urlForHost(expectedHost, "/")));
        Assertions.assertEquals(200, accepted.getStatusLine().getStatusCode());
        Assertions.assertThrows(Exception.class, () -> client.execute(new HttpGet(tlsServer.url("/"))),
                "the custom verifier must actually gate the hostname");
    }

    @Test
    public void nullHostnameVerifierIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> HttpClients.builder("null-verifier").verifyHostnamesWith(null));
    }

    @Test
    public void keyMaterialIsLoadedFromPkcs12AndUsedForMutualTls() throws Exception {
        // generate client and server keypairs, then a shared p12 truststore
        // holding both certificates: the server requires client auth
        final var clientP12 = work.resolve("client.p12");
        runKeytool("-genkeypair", "-alias", "client", "-keyalg", "RSA", "-keysize", "2048", "-validity", "1",
                "-dname", "CN=client", "-keystore", clientP12.toString(), "-storepass", "changeit",
                "-keypass", "changeit", "-storetype", "PKCS12");
        final var serverJks = work.resolve("mtls-server.jks");
        runKeytool("-genkeypair", "-alias", "server", "-keyalg", "RSA", "-keysize", "2048", "-validity", "1",
                "-dname", "CN=localhost", "-ext", "SAN=DNS:localhost,IP:127.0.0.1",
                "-keystore", serverJks.toString(), "-storepass", "changeit", "-keypass", "changeit", "-storetype", "JKS");
        final var clientCrt = work.resolve("client.crt");
        runKeytool("-exportcert", "-alias", "client", "-keystore", clientP12.toString(), "-storepass", "changeit", "-file", clientCrt.toString());
        final var serverCrt = work.resolve("mtls-server.crt");
        runKeytool("-exportcert", "-alias", "server", "-keystore", serverJks.toString(), "-storepass", "changeit", "-file", serverCrt.toString());
        final var caP12 = work.resolve("trust.p12");
        runKeytool("-importcert", "-alias", "client", "-file", clientCrt.toString(), "-keystore", caP12.toString(), "-storepass", "changeit", "-noprompt", "-storetype", "PKCS12");
        runKeytool("-importcert", "-alias", "server", "-file", serverCrt.toString(), "-keystore", caP12.toString(), "-storepass", "changeit", "-noprompt", "-storetype", "PKCS12");

        final var serverKeys = KeyStore.getInstance("JKS");
        try (var is = new FileInputStream(serverJks.toFile())) {
            serverKeys.load(is, "changeit".toCharArray());
        }
        final var sharedTrust = KeyStore.getInstance("PKCS12");
        try (var is = new FileInputStream(caP12.toFile())) {
            sharedTrust.load(is, "changeit".toCharArray());
        }

        final var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(serverKeys, "changeit".toCharArray());
        final var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(sharedTrust);
        final var ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        final var serverSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        serverSocket.setNeedClientAuth(true);
        final var mtlsServer = new TinyHttpServer(serverSocket, request -> TinyHttpServer.Response.ok(), "https");

        try (mtlsServer) {
            final var km = HttpClients.KeyMaterial.fromPkcs12File(clientP12.toString(), Optional.of("changeit"), Optional.of("changeit"));
            final var client = HttpClients.builder("mtls")
                    .keyMaterial(km)
                    .trustCertificatesIn(sharedTrust)
                    .build();
            final var response = client.execute(new HttpGet(mtlsServer.url("/")));
            Assertions.assertEquals(200, response.getStatusLine().getStatusCode(), "client certificate must be presented and accepted");
        }
    }

    @Test
    public void mutualTlsFailsWithoutClientCertificateWhenRequired() throws Exception {
        final var serverJks = work.resolve("mtls-server2.jks");
        runKeytool("-genkeypair", "-alias", "server", "-keyalg", "RSA", "-keysize", "2048", "-validity", "1",
                "-dname", "CN=localhost", "-ext", "SAN=DNS:localhost,IP:127.0.0.1",
                "-keystore", serverJks.toString(), "-storepass", "changeit", "-keypass", "changeit", "-storetype", "JKS");
        final var trustJks = work.resolve("mtls-trust2.jks");
        runKeytool("-exportcert", "-alias", "server", "-keystore", serverJks.toString(), "-storepass", "changeit", "-file", work.resolve("mtls-server2.crt").toString());
        runKeytool("-importcert", "-alias", "server", "-file", work.resolve("mtls-server2.crt").toString(), "-keystore", trustJks.toString(), "-storepass", "changeit", "-noprompt", "-storetype", "JKS");

        final var serverKeys = KeyStore.getInstance("JKS");
        try (var is = new FileInputStream(serverJks.toFile())) {
            serverKeys.load(is, "changeit".toCharArray());
        }
        final var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(serverKeys, "changeit".toCharArray());
        final var ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
        final var serverSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        serverSocket.setNeedClientAuth(true);
        final var mtlsServer = new TinyHttpServer(serverSocket, request -> TinyHttpServer.Response.ok(), "https");

        final var clientTrust = KeyStore.getInstance("JKS");
        try (var is = new FileInputStream(trustJks.toFile())) {
            clientTrust.load(is, "changeit".toCharArray());
        }
        try (mtlsServer) {
            // trust the server chain but present no client certificate
            final var client = HttpClients.builder("mtls-nocert")
                    .trustCertificatesIn(clientTrust)
                    .build();
            Assertions.assertThrows(Exception.class, () -> client.execute(new HttpGet(mtlsServer.url("/"))),
                    "a server requiring client certificates must refuse a client without one");
        }
    }

    @Test
    public void redirectsAreNotFollowedByDefault() throws Exception {
        final var base = new AtomicReference<String>();
        final var endHits = new AtomicInteger(0);
        try (var server = TinyHttpServer.plain(request -> {
            return switch (request.path()) {
                case "/start" ->
                    TinyHttpServer.Response.of(302, Map.of("Location", base.get() + "/end"), "");
                case "/end" -> {
                    endHits.incrementAndGet();
                    yield TinyHttpServer.Response.ok();
                }
                default ->
                    TinyHttpServer.Response.notFound();
            };
        })) {
            base.set(server.url(""));
            final var client = HttpClients.builder("redirect-default").build();
            final var response = client.execute(new HttpGet(server.url("/start")));
            Assertions.assertEquals(302, response.getStatusLine().getStatusCode(), "the 302 must be returned as-is");
            Assertions.assertEquals(0, endHits.get(), "the redirect target must not be requested");
        }
    }

    @Test
    public void redirectsAreFollowedWhenOptedIn() throws Exception {
        final var base = new AtomicReference<String>();
        try (var server = TinyHttpServer.plain(request -> {
            return switch (request.path()) {
                case "/start" ->
                    TinyHttpServer.Response.of(302, Map.of("Location", base.get() + "/end"), "");
                case "/end" ->
                    TinyHttpServer.Response.ok();
                default ->
                    TinyHttpServer.Response.notFound();
            };
        })) {
            base.set(server.url(""));
            final var client = HttpClients.builder("redirect-optin").followRedirects().build();
            final var response = client.execute(new HttpGet(server.url("/start")));
            Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void cookiesAreNotRetainedByDefault() throws Exception {
        final var requestsWithCookie = new AtomicInteger(0);
        try (var server = TinyHttpServer.plain(request -> {
            if (request.header("Cookie") != null) {
                requestsWithCookie.incrementAndGet();
            }
            return TinyHttpServer.Response.of(200, Map.of("Set-Cookie", "session=abc; Path=/"), "ok");
        })) {
            final var client = HttpClients.builder("cookie-default").build();
            client.execute(new HttpGet(server.url("/"))).close();
            client.execute(new HttpGet(server.url("/"))).close();
            Assertions.assertEquals(0, requestsWithCookie.get(), "no cookie may be replayed on the second request");
        }
    }

    @Test
    public void cookiesAreRetainedWhenAStoreIsConfigured() throws Exception {
        final var requestsWithCookie = new AtomicInteger(0);
        try (var server = TinyHttpServer.plain(request -> {
            if (request.header("Cookie") != null) {
                requestsWithCookie.incrementAndGet();
                return TinyHttpServer.Response.ok();
            }
            return TinyHttpServer.Response.of(200, Map.of("Set-Cookie", "session=abc; Path=/"), "ok");
        })) {
            final var client = HttpClients.builder("cookie-optin")
                    .cookieStore(new BasicCookieStore())
                    .build();
            client.execute(new HttpGet(server.url("/"))).close();
            client.execute(new HttpGet(server.url("/"))).close();
            Assertions.assertEquals(1, requestsWithCookie.get(), "the cookie must be replayed on the second request");
        }
    }

    @Test
    public void transportFailuresAreRetriedByDefaultForIdempotentRequests() throws Exception {
        final var attempts = new AtomicInteger(0);
        try (var server = TinyHttpServer.plain(request -> {
            if (attempts.incrementAndGet() < 3) {
                return TinyHttpServer.Response.closeSilently();
            }
            return TinyHttpServer.Response.ok();
        })) {
            final var client = HttpClients.builder("flaky-retry").timeouts(Duration.ofSeconds(1), Duration.ofSeconds(2)).build();
            final var response = client.execute(new HttpGet(server.url("/")));
            Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
            Assertions.assertEquals(3, attempts.get(), "library default: idempotent requests are retried on transport errors");
        }
    }

    @Test
    public void postsAreNeverRetried() throws Exception {
        final var attempts = new AtomicInteger(0);
        try (var server = TinyHttpServer.plain(request -> {
            attempts.incrementAndGet();
            return TinyHttpServer.Response.closeSilently();
        })) {
            final var client = HttpClients.builder("flaky-post").timeouts(Duration.ofSeconds(1), Duration.ofSeconds(2)).build();
            final var post = new HttpPost(server.url("/"));
            post.setEntity(new StringEntity("payload"));
            Assertions.assertThrows(Exception.class, () -> client.execute(post));
            Assertions.assertEquals(1, attempts.get(), "entity-enclosing requests must never be retried: the far side may have committed them");
        }
    }

    @Test
    public void credentialsAreNotSentPreemptivelyAfterAChallenge() throws Exception {
        final var authorizationHeaders = Collections.synchronizedList(new ArrayList<String>());
        try (var server = TinyHttpServer.plain(request -> {
            final var auth = request.header("Authorization");
            if (auth != null) {
                authorizationHeaders.add(auth);
            }
            return TinyHttpServer.Response.of(401, Map.of("WWW-Authenticate", "Basic realm=\"test\""), "");
        })) {
            final var client = HttpClients.builder("auth").build();
            client.execute(new HttpGet(server.url("/"))).close();
            client.execute(new HttpGet(server.url("/"))).close();
            Assertions.assertTrue(authorizationHeaders.isEmpty(), "no credentials may ever be sent without the caller asking");
        }
    }
}
