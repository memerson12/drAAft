package draaft.client;

import com.google.gson.Gson;
import draaft.client.gui.DraaftToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static draaft.draaft.MOD_ID;

public class ServerClient {
    public static final Gson GSON = new Gson();

    private final AuthTokenServer authTokenServer;
    private final HttpClient httpClient;
    private final String token;

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static @Nullable ServerClient INSTANCE = null;

    public static @Nullable ServerClient getInstance() {
        return INSTANCE;
    }

    private ServerClient(AuthTokenServer authTokenServer, HttpClient httpClient, String token) {
        this.authTokenServer = authTokenServer;
        this.httpClient = httpClient;
        this.token = token;
    }

    public <T> HttpResponse<T> httpSend(HttpRequest request, HttpResponse.BodyHandler<T> bodyPublisher)
        throws IOException, InterruptedException
    {
        ServerClient.LOGGER.debug("HTTP request to {}", request.uri());

        return this.httpClient.send(request, bodyPublisher);
    }

    public HttpRequest.Builder httpRequest(URI uri) {
        return HttpRequest.newBuilder(uri)
            .header("token", this.token);
    }

    // thanks menx :)
    public static void login(DraaftServices draaftServices) {
        if (INSTANCE != null) {
            // If already logged in just open the browser link
            INSTANCE.openWebLoginUri();

            return;
        }

        MinecraftClient inst = MinecraftClient.getInstance();
        // okay: then we add a "generate room" button ingame and it gives you a key
        // https://sessionserver.mojang.com/session/minecraft/hasJoined?username=DesktopFolder&serverId=draaft2025server

        var httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1) // HTTP 2 is not supported by fastapi
            .build();

        LOGGER.info("Contacting Minecraft auth server...");
        var serverID = MojangAuth.joinDraaftServer();
        if (serverID == null) {
            return;
        }

        LOGGER.info("Contacting drAAft server...");
        String username = inst.getSession().getUsername();

        String body = GSON.toJson(new LoginRequest(serverID, username), LoginRequest.class);

        String clientToken;
        try {
            final var remote = draaftServices.apiBase().resolve("authenticate");
            LOGGER.info("Connecting to {}", remote);

            var req = HttpRequest.newBuilder(remote)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .setHeader("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            clientToken = GSON.fromJson(resp.body(), LoginResponse.class).token;

            if (clientToken == null) {
                LOGGER.error("Could not get token from drAAft server result!");
                DraaftToast.showError(
                    new TranslatableText("draaft.login.failed.title"),
                    new TranslatableText("draaft.login.failed.unexpectedResponse")
                );
                return;
            }
        } catch (Throwable e) {
            LOGGER.error("Could not contact drAAft server: {}", e.getMessage());
            DraaftToast.showError(
                new TranslatableText("draaft.login.failed.title"),
                new TranslatableText("draaft.login.failed.errorContactingDraaft")
            );
            return;
        }

        LOGGER.info("Successfully authenticated with the server, received {} long client token", clientToken.length());

        var authTokenServer = new AuthTokenServer(draaftServices, clientToken);

        INSTANCE = new ServerClient(authTokenServer, httpClient, clientToken);

        INSTANCE.openWebLoginUri();
    }

    private void openWebLoginUri() {
        var uri = this.authTokenServer.webLoginUri();

        // TODO(me-nx): shift click tooltip?

        if (Screen.hasShiftDown()) {
            MinecraftClient.getInstance().keyboard.setClipboard(uri);

            DraaftToast.show(
                new TranslatableText("draaft.login.linkCopied"),
                new TranslatableText("draaft.login.linkCopied.desc")
            );
        } else {
            Util.getOperatingSystem().open(uri);

            DraaftToast.show(
                new TranslatableText("draaft.login.success"),
                new TranslatableText("draaft.login.success.desc")
            );
        }
    }

    // GSON can't deserialize to method-local classes
    record LoginRequest(String serverID, String username) {}

    // Minecraft's version of GSON can't deserialize to records
    @SuppressWarnings("unused") // assigned by GSON
	static class LoginResponse {
        public String token;
    }
}
