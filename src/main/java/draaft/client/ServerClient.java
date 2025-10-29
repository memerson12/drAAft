package draaft.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import draaft.client.models.Room;
import draaft.client.ws.DraaftWebSocketClient;
import draaft.client.ws.RoomEventDispatcher;
import draaft.client.ws.RoomEventListener;
import draaft.client.models.RoomDeserializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Session;
import net.minecraft.util.Util;
import org.apache.commons.codec.binary.Base32;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

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

    public static String connectingStatus = null;
    public static long connectingStatusTimestamp = 0; // 0 indicates to only show on hover

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

        MinecraftClient inst = MinecraftClient.getInstance();
        Session session = inst.getSession();
        // okay: then we add a "generate room" button ingame and it gives you a key
        // https://sessionserver.mojang.com/session/minecraft/hasJoined?username=DesktopFolder&serverId=draaft2025server

        var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // HTTP 2 is not supported by fastapi
                .build();

        var clientPassword = generateClientPassword();

        LOGGER.info("Contacting Minecraft auth server...");

        try {
            inst.getSessionService().joinServer(session.getProfile(), session.getAccessToken(), clientPassword);
            LOGGER.info("draaft successfully joined server with clientPassword");
        } catch (AuthenticationUnavailableException e) {
            LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.serversUnavailable", e);
            setConnectionText("Error: Servers unavailable!");
            return;
        } catch (InvalidCredentialsException e) {
            LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.invalidSession", e);
            setConnectionText("Error: Invalid session!");
            return;
        } catch (AuthenticationException authenticationException) {
            LOGGER.warn("disconnect.loginFailedInfo", authenticationException);
            setConnectionText(authenticationException.getMessage());
            return;
        }
        LOGGER.info("Contacting drAAft server...");
        String username = session.getUsername();

        String body = GSON.toJson(new LoginRequest(clientPassword, username), LoginRequest.class);

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
                setConnectionText("Error in drAAft server response!");
                return;
            }
        } catch (Throwable e) {
            LOGGER.error("Could not contact drAAft server: {}", e.getMessage());
            setConnectionText("Error contacting drAAft server!");
            return;
        }

        LOGGER.info("Successfully authenticated with the server, received {} long client token", clientToken.length());

        var authTokenServer = new AuthTokenServer(draaftServices, clientToken);

        INSTANCE = new ServerClient(authTokenServer, httpClient, clientToken, draaftServices);

        setConnectionText("Opening in browser...");
        INSTANCE.openWebLoginUri();
        INSTANCE.startWs();
    }

    private void openWebLoginUri() {
        var uri = this.authTokenServer.webLoginUri();

        // TODO(me-nx): display toasts, shift click tooltip

        if (Screen.hasShiftDown()) {
            MinecraftClient.getInstance().keyboard.setClipboard(uri);
        } else {
            Util.getOperatingSystem().open(uri);
        }
    }

    private static String generateClientPassword() {
        try {
            SecureRandom instanceStrong = SecureRandom.getInstanceStrong();

            byte[] randomBytes = new byte[15];
            instanceStrong.nextBytes(randomBytes);

            return new Base32().encodeAsString(randomBytes) + "draaaaft";
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Secure RNG not available", e);
        }
    }

    private static void setConnectionText(String text) {
        connectingStatus = text;
        connectingStatusTimestamp = Instant.now().getEpochSecond();
    }

    // GSON can't deserialize to method-local classes
    record LoginRequest(String serverID, String username) {
    }

    // Minecraft's version of GSON can't deserialize to records
    static class LoginResponse {
        public String token;
    }
}
