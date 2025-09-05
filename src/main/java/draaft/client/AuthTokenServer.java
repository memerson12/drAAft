package draaft.client;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class AuthTokenServer {
    public String token = "";

    private final HttpServer server;
    private final Gson gson = new Gson();

    public AuthTokenServer() throws IOException {
        var bindAddr = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
        this.server = HttpServer.create(bindAddr, 0);
        this.server.createContext("/", new Handler());
        this.server.setExecutor(null);
        this.server.start();
    }

    public int port() {
        return this.server.getAddress().getPort();
    }

    record Response(String token) {}

    class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            var response = gson.toJson(new Response(token))
                .getBytes(StandardCharsets.UTF_8);

            var headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "application/json;charset=utf-8");
            // TODO: replace the hardcoded draaft origin
            headers.add("Access-Control-Allow-Origin", "http://localhost:8080");
            exchange.sendResponseHeaders(200, response.length);

            var stream = exchange.getResponseBody();
            stream.write(response);
            stream.close();
        }
    }
}
