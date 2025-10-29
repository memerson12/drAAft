package draaft.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import draaft.client.gui.DraaftToast;
import draaft.client.models.Room;
import draaft.client.ws.DraaftWebSocketClient;
import draaft.client.ws.RoomEventDispatcher;
import draaft.client.ws.RoomEventListener;
import draaft.client.models.RoomDeserializer;
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
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Room.class, new RoomDeserializer())
            .create();

    private final AuthTokenServer authTokenServer;
    private final HttpClient httpClient;
    private final String token;
    private final DraaftServices draaftServices;
    private final RoomEventDispatcher wsDispatcher;
    private DraaftWebSocketClient wsClient;
    private volatile Room cachedRoom;

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static @Nullable ServerClient INSTANCE = null;

    public static @Nullable ServerClient getInstance() {
        return INSTANCE;
    }

    private ServerClient(AuthTokenServer authTokenServer, HttpClient httpClient, String token,
            DraaftServices draaftServices) {
        this.authTokenServer = authTokenServer;
        this.httpClient = httpClient;
        this.token = token;
        this.draaftServices = draaftServices;
        this.wsDispatcher = new RoomEventDispatcher();
    }

    public <T> HttpResponse<T> httpSend(HttpRequest request, HttpResponse.BodyHandler<T> bodyPublisher)
            throws IOException, InterruptedException {
        ServerClient.LOGGER.debug("HTTP request to {}", request.uri());
        return this.httpClient.send(request, bodyPublisher);
    }

    public HttpRequest.Builder authenticatedHttpRequestBuilder(URI uri) {
        return HttpRequest.newBuilder(uri)
                .header("token", this.token)
                .setHeader("Content-Type", "application/json");
    }

    public Room getRoom() {
        try {
            final URI remote = this.draaftServices.apiBase().resolve("room");
            HttpRequest req = authenticatedHttpRequestBuilder(remote)
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            Room room = GSON.fromJson(resp.body(), Room.class);

            if (room == null) {
                LOGGER.error("Failed to get room from server");
                setConnectionText("Error in drAAft server response!");
                return null;
            }
            this.cachedRoom = room;
            return room;
        } catch (Throwable e) {
            LOGGER.error("Failed getting room: {}", e.getMessage());
            setConnectionText("Error contacting drAAft server!");
            return null;
        }
    }

    public @Nullable Room getCachedRoom() {
        return this.cachedRoom;
    }

    private void refreshRoomFromServer() {
        try {
            Room room = getRoom();
            if (room != null) {
                LOGGER.info("Updated room cache: {} members", room.members().size());
            }
        } catch (Throwable ignored) {
        }
    }

    public void startWs() {
        if (this.wsClient != null)
            return;
        this.wsClient = new DraaftWebSocketClient(this.httpClient, this.draaftServices, this.token, this.wsDispatcher);
        this.wsClient.start();
        // Keep a simple cache updated on membership changes
        this.addRoomEventListener(event -> {
            String type = event.type();
            if ("room_member_join".equals(type) || "room_member_leave".equals(type)) {
                refreshRoomFromServer();
            }
        });
    }

    public void stopWs() {
        if (this.wsClient != null) {
            this.wsClient.stop();
            this.wsClient = null;
        }
    }

    public void addRoomEventListener(RoomEventListener listener) {
        this.wsDispatcher.addListener(listener);
    }

    public void removeRoomEventListener(RoomEventListener listener) {
        this.wsDispatcher.removeListener(listener);
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

        LOGGER.info("Contacting Minecraft auth server...");
        var serverID = MojangAuth.joinDraaftServer();
        if (serverID == null) {
            return;
        }

        LOGGER.info("Contacting drAAft server...");
        String clientToken = DraaftAuth.authenticate(draaftServices, httpClient, serverID);
        if (clientToken == null) {
            return;
        }

        LOGGER.info("Successfully authenticated with the server, received {} long client token", clientToken.length());

        var authTokenServer = new AuthTokenServer(draaftServices, clientToken);

        INSTANCE = new ServerClient(authTokenServer, httpClient, clientToken, draaftServices);

        INSTANCE.openWebLoginUri();
        INSTANCE.startWs();
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
