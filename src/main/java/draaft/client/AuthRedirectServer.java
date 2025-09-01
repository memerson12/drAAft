package draaft.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class AuthRedirectServer {
	public String token = "";

	private final HttpServer server;

	public AuthRedirectServer() throws IOException {
		var bindAddr = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
		this.server = HttpServer.create(bindAddr, 0);
		this.server.createContext("/", new Handler());
		this.server.setExecutor(null);
		this.server.start();
	}

	public String uri() {
		var port = this.server.getAddress().getPort();

		return "http://localhost:%d/".formatted(port);
	}

	class Handler implements HttpHandler {
		final static String HTML_TEMPLATE = """
			<!DOCTYPE html>
			<html>
				<head>
					<title>Redirecting...</title>
					<script>
						const form = document.createElement('form');
						form.method = 'post';
						form.action = '%s';

						const tokenElement = document.createElement('input');
						tokenElement.setAttribute('type', 'hidden');
						tokenElement.setAttribute('name', 'token');
						tokenElement.setAttribute('value', '%s');
						form.appendChild(tokenElement);

						document.documentElement.appendChild(form);
						form.submit();
					</script>
				</head>
				<body>
					<h1>Redirecting...</h1>
				</body>
			</html>
			""";

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO: replace the hardcoded redirect target
			var response = HTML_TEMPLATE.formatted(
				"http://localhost:8000/authenticate-app",
				AuthRedirectServer.this.token
			).getBytes(StandardCharsets.UTF_8);

			exchange.getResponseHeaders().add("Content-Type", "text/html;charset=utf-8");
			exchange.sendResponseHeaders(200, response.length);

			var stream = exchange.getResponseBody();
			stream.write(response);
			stream.close();
		}
	}
}
