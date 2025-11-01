package draaft.client;

import com.google.gson.Gson;
import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.codec.binary.Base32;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;

public class MenxServerAuth {
    static final Gson GSON = new Gson();
    private final String header;
    public static final Duration API_TIMEOUT = Duration.ofSeconds(10);

    public MenxServerAuth(String token) {
        this.header = "Bearer " + token;
    }

    public String header() {
        return this.header;
    }

    public static MenxServerAuth authenticate(HttpClient http, URI baseUri)
            throws IOException, InterruptedException, NoSuchAlgorithmException, AuthenticationException
    {
        final String API_VERSION = "1";

        var authReq = HttpRequest.newBuilder(baseUri.resolve("/auth"))
                .GET()
                .timeout(API_TIMEOUT)
                .build();

        var authResponse = http.send(authReq, HttpResponse.BodyHandlers.ofString());

        var serverVersion = GSON.fromJson(authResponse.body(), AuthVersionOnly.class).version;
        if (!Objects.equals(serverVersion, API_VERSION)) {
            throw new RuntimeException("**** fault? serverVersionIncompatible");
        }

        var authInfo = GSON.fromJson(authResponse.body(), AuthInfo.class);

        String clientPrefix = "";

        var session = MinecraftClient.getInstance().getSession();

        var rng = SecureRandom.getInstanceStrong();
        byte[] clientPrefixBytes = new byte[15];
        rng.nextBytes(clientPrefixBytes);

        clientPrefix = new Base32().encodeAsString(clientPrefixBytes);

        var serverId = clientPrefix + authInfo.server_suffix;

        var sessionService = MinecraftClient.getInstance().getSessionService();

        sessionService.joinServer(session.getProfile(), session.getAccessToken(), serverId);

        var login = new LoginData(session.getUsername(), clientPrefix);

        var loginReq = HttpRequest.newBuilder(baseUri.resolve("/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(login, LoginData.class)))
                .setHeader("Content-Type", "application/json")
                .timeout(API_TIMEOUT)
                .build();

        var loginResponse = http.send(loginReq, HttpResponse.BodyHandlers.ofString());

        return switch (loginResponse.statusCode()) {
            case 200 -> new MenxServerAuth(
                    GSON.fromJson(loginResponse.body(), LoginResponse.class).token
            );
            case 403 -> throw new RuntimeException("worldimporter.notWhitelisted");
            default -> throw new IllegalStateException("Unexpected value: " + loginResponse.statusCode());
        };
    }

    @SuppressWarnings("unused") // GSON
    private static final class AuthVersionOnly {
        public String version;
    }

    @SuppressWarnings("unused") // GSON
    private static final class AuthInfo {
        private boolean enabled;
        private String server_suffix;
    }

    private record LoginData(String username, String client_prefix) {}

    @SuppressWarnings("unused") // GSON
    private static final class LoginResponse {
        private String token;
    }
}