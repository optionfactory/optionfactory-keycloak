package net.optionfactory.keycloak.remoting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

/**
 * Test-only minimal http(s) server built on public jdk apis (no
 * jdk.httpserver dependency), with deterministic connection-level failure
 * injection: every response is delivered on a fresh connection that is
 * closed afterwards.
 */
final class TinyHttpServer implements AutoCloseable {

    enum Action {
        RESPOND, CLOSE_SILENT, RESET
    }

    record Request(String requestLine, List<String> headers, String body) {

        String method() {
            return requestLine.split(" ")[0];
        }

        String path() {
            return requestLine.split(" ")[1];
        }

        String header(String name) {
            final var prefix = name.toLowerCase() + ":";
            return headers.stream()
                    .filter(h -> h.toLowerCase().startsWith(prefix))
                    .findFirst()
                    .map(h -> h.substring(h.indexOf(':') + 1).trim())
                    .orElse(null);
        }
    }

    record Response(Action action, int status, Map<String, String> headers, String body) {

        static Response ok() {
            return new Response(Action.RESPOND, 200, Map.of(), "ok");
        }

        static Response of(int status, Map<String, String> headers, String body) {
            return new Response(Action.RESPOND, status, headers, body);
        }

        static Response notFound() {
            return new Response(Action.RESPOND, 404, Map.of(), "");
        }

        /**
         * Close the connection without answering (FIN): the client sees no
         * response.
         */
        static Response closeSilently() {
            return new Response(Action.CLOSE_SILENT, 0, Map.of(), "");
        }

        /**
         * Abort the connection with a tcp reset (SO_LINGER=0): the client
         * sees a broken connection.
         */
        static Response reset() {
            return new Response(Action.RESET, 0, Map.of(), "");
        }
    }

    interface Handler {

        Response handle(Request request) throws IOException;
    }

    private final ServerSocket serverSocket;
    private final ExecutorService connections;
    private final Handler handler;
    private final String scheme;
    private volatile boolean stopped = false;

    static TinyHttpServer plain(Handler handler) throws IOException {
        return new TinyHttpServer(new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1")), handler, "http");
    }

    static TinyHttpServer tls(Handler handler, KeyStore keystore, char[] keyPassword) throws Exception {
        final var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, keyPassword);
        final var ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
        // wildcard binding: wrong-hostname tests connect via the machine hostname
        final var serverSocket = (ServerSocket) ctx.getServerSocketFactory().createServerSocket(0, 50);
        return new TinyHttpServer(serverSocket, handler, "https");
    }

    TinyHttpServer(SSLServerSocket preconfigured, Handler handler, String scheme) {
        this((ServerSocket) preconfigured, handler, scheme);
    }

    private TinyHttpServer(ServerSocket serverSocket, Handler handler, String scheme) {
        this.serverSocket = serverSocket;
        this.handler = handler;
        this.scheme = scheme;
        this.connections = Executors.newCachedThreadPool(r -> {
            final var t = new Thread(r, "tiny-http-connection");
            t.setDaemon(true);
            return t;
        });
        final var acceptor = new Thread(this::acceptLoop, "tiny-http-acceptor");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    private void acceptLoop() {
        while (!stopped) {
            try {
                final var socket = serverSocket.accept();
                connections.execute(() -> serve(socket));
            } catch (IOException e) {
                if (stopped) {
                    return;
                }
            }
        }
    }

    private void serve(Socket socket) {
        try (socket) {
            socket.setSoTimeout(5_000);
            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            final var lines = new ArrayList<String>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                lines.add(line);
            }
            if (lines.isEmpty()) {
                return;
            }
            final var contentLength = lines.stream()
                    .filter(h -> h.toLowerCase().startsWith("content-length:"))
                    .findFirst()
                    .map(h -> Integer.parseInt(h.substring(h.indexOf(':') + 1).trim()))
                    .orElse(0);
            String body = "";
            if (contentLength > 0) {
                final var chars = new char[contentLength];
                int read = 0;
                while (read < contentLength) {
                    final var n = in.read(chars, read, contentLength - read);
                    if (n < 0) {
                        break;
                    }
                    read += n;
                }
                body = new String(chars, 0, read);
            }
            final var response = handler.handle(new Request(lines.get(0), lines, body));
            switch (response.action()) {
                case CLOSE_SILENT -> {
                    // plain close: the client sees a connection with no response
                }
                case RESET -> {
                    socket.setSoLinger(true, 0);
                }
                case RESPOND -> write(socket, response);
            }
        } catch (IOException e) {
            // connection-level failures are part of the test surface
        }
    }

    private static void write(Socket socket, Response response) throws IOException {
        final var bodyBytes = response.body().getBytes(StandardCharsets.UTF_8);
        final var headers = new LinkedHashMap<String, String>(response.headers());
        headers.putIfAbsent("Content-Length", String.valueOf(bodyBytes.length));
        final var head = new StringBuilder("HTTP/1.1 ").append(response.status()).append(reason(response.status())).append("\r\n");
        headers.forEach((k, v) -> head.append(k).append(": ").append(v).append("\r\n"));
        head.append("Connection: close\r\n\r\n");
        final var os = socket.getOutputStream();
        os.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
        os.write(bodyBytes);
        os.flush();
    }

    private static String reason(int status) {
        return switch (status) {
            case 200 ->
                " OK";
            case 302 ->
                " Found";
            case 401 ->
                " Unauthorized";
            default ->
                " Status";
        };
    }

    String url(String path) {
        return "%s://127.0.0.1:%d%s".formatted(scheme, serverSocket.getLocalPort(), path);
    }

    String urlForHost(String host, String path) {
        return "%s://%s:%d%s".formatted(scheme, host, serverSocket.getLocalPort(), path);
    }

    @Override
    public void close() throws IOException {
        stopped = true;
        serverSocket.close();
        connections.shutdownNow();
    }
}
