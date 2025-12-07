package draaft.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import draaft.client.gui.DraaftToast;
import draaft.client.models.Room;
import draaft.client.models.RoomDeserializer;
import draaft.client.ws.DraaftWebSocketClient;
import draaft.client.ws.EventBus;
import draaft.client.ws.EventListener;
import draaft.client.ws.events.DraftPickEvents;
import draaft.client.ws.events.RoomMemberEvents;
import draaft.client.ws.events.RoomStateEvents;
import draaft.client.ws.outgoing.GameUpdate;
import draaft.draaft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

public class ServerClient {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Room.class, new RoomDeserializer())
        .create();

    private final AuthTokenServer authTokenServer;
    private final HttpClient httpClient;
    private final String token;
    private final DraaftServices draaftServices;
    private final EventBus eventBus;
    private DraaftWebSocketClient wsClient;

    private static final Logger logger = draaft.LOGGER;

    private static @Nullable ServerClient INSTANCE = null;

    public static ServerClient getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ServerClient not initialized. Call ServerClient.login() first.");
        }
        return INSTANCE;
    }

    public static boolean hasInstance() {
        return INSTANCE != null;
    }

    public static @Nullable ServerClient getInstanceOrNull() {
        return INSTANCE;
    }

    private ServerClient(AuthTokenServer authTokenServer, HttpClient httpClient, String token,
                         DraaftServices draaftServices) {
        this.authTokenServer = authTokenServer;
        this.httpClient = httpClient;
        this.token = token;
        this.draaftServices = draaftServices;
        this.eventBus = new EventBus();

        this.startWs();
    }

    public <T> HttpResponse<T> httpSend(HttpRequest request, HttpResponse.BodyHandler<T> bodyPublisher)
        throws IOException, InterruptedException {
        ServerClient.logger.debug("HTTP request to {}", request.uri());
        return this.httpClient.send(request, bodyPublisher);
    }

    public HttpRequest.Builder authenticatedHttpRequestBuilder(URI uri) {
        return HttpRequest.newBuilder(uri)
            .header("token", this.token)
            .setHeader("Content-Type", "application/json");
    }

    public void addAdvancement(String advancement) {
        this.wsClient.sendMessage(new GameUpdate.Advance(advancement));
    }

    public Room getRoom() {
        try {
            final URI remote = this.draaftServices.apiBase().resolve("room");
            HttpRequest req = authenticatedHttpRequestBuilder(remote)
                .GET()
                .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            // User is not in a room
            if (resp.statusCode() == 404) {
                logger.debug("User is not in a room");
                return null;
            }

            Room room = GSON.fromJson(resp.body(), Room.class);

            if (room == null) {
                logger.error("Failed to get room from server");
                return null;
            }
            return room;
        } catch (Throwable e) {
            logger.error("Failed getting room: {}", e.getMessage());
            return null;
        }
    }

    public String getUsernameFromUUID(UUID uuid) {
        try {
            final URI remote = URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/" + uuid.toString());
            HttpRequest req = HttpRequest.newBuilder(remote)
                .setHeader("Content-Type", "application/json")
                .GET()
                .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonObject = new JsonParser().parse(resp.body()).getAsJsonObject();
            return jsonObject.get("name").getAsString();

        } catch (Throwable e) {
            logger.error("Failed getting username from UUID: {}", e.getMessage());
            return null;
        }
    }

    public void startWs() {
        if (this.wsClient != null)
            return;
        this.wsClient = new DraaftWebSocketClient(this.httpClient, this.draaftServices, this.token, this.eventBus);
        this.wsClient.start();
    }

    public void stopWs() {
        if (this.wsClient != null) {
            this.wsClient.stop();
            this.wsClient = null;
        }
    }

    public void addRoomMemberEventListener(EventListener<RoomMemberEvents> listener) {
        this.eventBus.register(RoomMemberEvents.class, listener);
    }

    public void removeRoomMemberEventListener(EventListener<RoomMemberEvents> listener) {
        this.eventBus.unregister(RoomMemberEvents.class, listener);
    }

    public void addPickEventListener(EventListener<DraftPickEvents> listener) {
        this.eventBus.register(DraftPickEvents.class, listener);
    }

    public void removePickEventListener(EventListener<DraftPickEvents> listener) {
        this.eventBus.unregister(DraftPickEvents.class, listener);
    }

    public void addRoomStateEventListener(EventListener<RoomStateEvents> listener) {
        this.eventBus.register(RoomStateEvents.class, listener);
    }

    public void removeRoomStateEventListener(EventListener<RoomStateEvents> listener) {
        this.eventBus.unregister(RoomStateEvents.class, listener);
    }

    // thanks menx :)
    public static void login(DraaftServices draaftServices) {
        if (INSTANCE != null) {
            // If already logged in just open the browser link
            INSTANCE.openWebLoginUri();

            return;
        }

        // okay: then we add a "generate room" button ingame and it gives you a key
        // https://sessionserver.mojang.com/session/minecraft/hasJoined?username=DesktopFolder&serverId=draaft2025server

        var httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1) // HTTP 2 is not supported by fastapi
            .build();

        logger.info("Contacting Minecraft auth server...");
        var serverID = MojangAuth.joinDraaftServer();
        if (serverID == null) {
            return;
        }

        logger.info("Contacting drAAft server...");
        String clientToken = DraaftAuth.authenticate(draaftServices, httpClient, serverID);
        if (clientToken == null) {
            return;
        }

        logger.info("Successfully authenticated with the server, received {} long client token", clientToken.length());

        var authTokenServer = new AuthTokenServer(draaftServices, clientToken);

        INSTANCE = new ServerClient(authTokenServer, httpClient, clientToken, draaftServices);

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
}
