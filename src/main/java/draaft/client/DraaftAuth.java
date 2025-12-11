package draaft.client;

import draaft.client.gui.DraaftToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static draaft.draaft.MOD_ID;

public class DraaftAuth {
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static @Nullable String authenticate(DraaftServices draaftServices, HttpClient http, String serverID) {
        String username = MinecraftClient.getInstance().getSession().getUsername();

        String body = ServerClient.GSON.toJson(new LoginRequest(serverID, username), LoginRequest.class);

        String clientToken;
        try {
            if (!verifyServerVersion(draaftServices, http)) {
                return null;
            }

            final var remote = draaftServices.apiBase().resolve("authenticate");
            LOGGER.info("Connecting to {}", remote);

            var loginReq = HttpRequest.newBuilder(remote)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .setHeader("Content-Type", "application/json")
                .build();

            HttpResponse<String> loginResp = http.send(loginReq, HttpResponse.BodyHandlers.ofString());

            clientToken = ServerClient.GSON.fromJson(loginResp.body(), LoginResponse.class).token;

            if (clientToken == null) {
                LOGGER.error("Could not get token from drAAft server result!");

                DraaftToast.showError(
                    new TranslatableText("draaft.login.failed.title"),
                    new TranslatableText("draaft.login.failed.unexpectedResponse")
                );

                return null;
            }
        } catch (Throwable e) {
            LOGGER.error("Could not contact drAAft server: {}", e.getMessage());

            DraaftToast.showError(
                new TranslatableText("draaft.login.failed.title"),
                new TranslatableText("draaft.login.failed.errorContactingDraaft")
            );

            return null;
        }

        return clientToken;
    }

    public static @Nullable String authenticateDevMode(DraaftServices draaftServices, HttpClient http, String username) {
        try {
            assert draaftServices.supportsDevAuth();

            if (!verifyServerVersion(draaftServices, http)) {
                return null;
            }

            var req = HttpRequest.newBuilder(draaftServices.apiBase().resolve("/dev/becomeuser?username=" + username))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            var resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            return ServerClient.GSON.fromJson(resp.body(), LoginResponse.class).token;
        } catch (InterruptedException | IOException e) {
            LOGGER.error("Could not contact drAAft server (dev mode)", e);

            DraaftToast.showError(
                new TranslatableText("draaft.login.failed.title"),
                new TranslatableText("draaft.login.failed.errorContactingDraaft")
            );

            return null;
        }
    }

    private static boolean verifyServerVersion(DraaftServices draaftServices, HttpClient http) throws IOException, InterruptedException {
        var versionReq = HttpRequest.newBuilder(draaftServices.apiBase().resolve("/version"))
            .GET()
            .build();

        var versionResp = http.send(versionReq, HttpResponse.BodyHandlers.ofString());
        var version = ServerClient.GSON.fromJson(versionResp.body(), int.class);

        if (version != DraaftServices.API_VERSION) {
            LOGGER.error("drAAft server version not supported (expected {}, got {}).", DraaftServices.API_VERSION, version);

            DraaftToast.showError(
                new TranslatableText("draaft.login.failed.title"),
                new TranslatableText("draaft.login.failed.unsupportedVersion")
            );

            return false;
        } else {
            return true;
        }
    }

    // GSON can't deserialize to method-local classes
    private record LoginRequest(String serverID, String username) {
    }

    // Minecraft's version of GSON can't deserialize to records
    @SuppressWarnings("unused") // assigned by GSON
    private static class LoginResponse {
        public String token;
    }
}
