package draaft.client;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * An HTTP server serving tokens to the frontend
 */
public class AuthTokenServer {
    private final HttpServer server;
    private final DraaftServices draaftServices;
    private final String token;

    public AuthTokenServer(DraaftServices draaftServices, String token) {
        this.draaftServices = draaftServices;
        this.token = token;

        // Bind to any ephemeral free port, listen only for local connections
        var bindAddr = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);

        try {
            // 0 indicates the default backlog size
            this.server = HttpServer.create(bindAddr, 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create an HTTP server for auth token passing", e);
        }

        this.server.createContext("/", new Handler());

        this.server.setExecutor(null);
        this.server.start();
    }

    public String webLoginUri() {
        return "%s?auth_port=%d".formatted(this.draaftServices.webBase().toString(), this.port());
    }

    /**
     * @return the port the server is listening on.
     */
    private int port() {
        return this.server.getAddress().getPort();
    }

    record Response(String token) {}

    class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            var response = ServerClient.GSON.toJson(new Response(token))
                .getBytes(StandardCharsets.UTF_8);

            var headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "application/json;charset=utf-8");
            headers.add("Access-Control-Allow-Origin", AuthTokenServer.this.draaftServices.webOrigin());
            exchange.sendResponseHeaders(200, response.length);

            var stream = exchange.getResponseBody();
            stream.write(response);
            stream.close();
        }
    }
}
