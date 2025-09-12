package draaft.client;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import draaft.draaft;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * An HTTP server serving tokens to the frontend
 */
public class AuthTokenServer {
    public String token = "";

    private final HttpServer server;
    private final Gson gson = new Gson();

    private static final AuthTokenServer INSTANCE;

    static {
		try {
            INSTANCE = new AuthTokenServer();
		} catch (IOException e) {
			throw new RuntimeException("failed to create an HTTP server", e);
		}
	}

    private AuthTokenServer() throws IOException {
        // Bind to any ephemeral free port, listen only for local connections
        var bindAddr = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);

        // 0 indicates the default backlog size
        this.server = HttpServer.create(bindAddr, 0);

        this.server.createContext("/", new Handler());

        this.server.setExecutor(null);
        this.server.start();
    }

    /**
     * @return the port the server is listening on.
     */
    public int port() {
        return this.server.getAddress().getPort();
    }

    public static AuthTokenServer get() {
        return INSTANCE;
    }

    record Response(String token) {}

    class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            var response = gson.toJson(new Response(token))
                .getBytes(StandardCharsets.UTF_8);

            var headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "application/json;charset=utf-8");
            headers.add("Access-Control-Allow-Origin", draaft.FRONTEND_ORIGIN);
            exchange.sendResponseHeaders(200, response.length);

            var stream = exchange.getResponseBody();
            stream.write(response);
            stream.close();
        }
    }
}
