package draaft.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.util.Util;
import org.apache.commons.codec.binary.Base32;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final String clientPassword;

    private static final boolean LOCAL_TESTING = false;
    private static final String WEBSITE_BASE_URI = (ServerClient.LOCAL_TESTING ? "http://localhost:8080" : "https://disrespec.tech");
    private static final String API_BASE_TESTING = (ServerClient.LOCAL_TESTING ? "http://localhost:8000" : "https://api.disrespec.tech");


    // TODO(me-nx): remove hardcoded URIs
    public static final URI API_BASE_URI = URI.create(String.format("%s/", API_BASE_TESTING));
    public static final URI FRONTEND_BASE_URI = URI.create(String.format("%s/draaft/", WEBSITE_BASE_URI));
    public static final String FRONTEND_ORIGIN = WEBSITE_BASE_URI; // do not add a trailing slash

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    public String connectingStatus = null;
    public long connectingStatusTimestamp = 0; // 0 indicates to only show on hover

    public static ServerClient INSTANCE = null;

    public static ServerClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerClient();
        }
        return INSTANCE;
    }

    public ServerClient() {
        SecureRandom instanceStrong;
        try {
            instanceStrong = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] randomBytes = new byte[15];
        instanceStrong.nextBytes(randomBytes);
        clientPassword = new Base32().encodeAsString(randomBytes) + "draaaaft";
    }

    private void setConnectionText(String text) {
        this.connectingStatus = text;
        this.connectingStatusTimestamp = Instant.now().getEpochSecond();
    }

    // thanks menx :)
    public void draaftLogin(/* :) */) {
        MinecraftClient inst = MinecraftClient.getInstance();
        Session session = inst.getSession();
        // okay: then we add a "generate room" button ingame and it gives you a key
        // https://sessionserver.mojang.com/session/minecraft/hasJoined?username=DesktopFolder&serverId=draaft2025server

        this.setConnectionText("Contacting Minecraft auth server...");
        try {
            inst.getSessionService().joinServer(session.getProfile(), session.getAccessToken(), clientPassword);
            LOGGER.info("draaft successfully joined server with clientPassword");
        } catch (AuthenticationUnavailableException var3) {
            LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.serversUnavailable");
            this.setConnectionText("Error: Servers unavailable!");
            return;
        } catch (InvalidCredentialsException var4) {
            LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.invalidSession");
            this.setConnectionText("Error: Invalid session!");
            return;
        } catch (AuthenticationException authenticationException) {
            LOGGER.warn("disconnect.loginFailedInfo {}", authenticationException.getMessage());
            this.setConnectionText(authenticationException.getMessage());
            return;
        }
        this.setConnectionText("Contacting drAAft server...");
        String username = session.getUsername();

        JsonObject body = new JsonObject();
        body.addProperty("serverID", clientPassword);
        body.addProperty("username", username);

        String clientToken;
        try {
            final var remote = API_BASE_URI.resolve("authenticate");
            LOGGER.info("Connecting to {}", remote);
            var req = HttpRequest.newBuilder(remote)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .setHeader("Content-Type", "application/json")
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            JsonObject result  = new Gson().fromJson(resp.body(), JsonObject.class);
            var decodeRes = result.get("token");
            if (decodeRes == null) {
                LOGGER.warn("Could not get token from drAAft server result!");
                this.setConnectionText("Error in drAAft server response!");
                return;
            }
            clientToken = decodeRes.getAsString();
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Could not contact drAAft server: {}", e.getMessage());
            this.setConnectionText("Error contacting drAAft server!");
            return;
        }
        LOGGER.info("Successfully authenticated with the server, received {} long client token", clientToken.length());

        /*
        try {
            var req = HttpRequest.newBuilder(new URI("http://localhost:8000/room/create"))
                    .GET()
                    .setHeader("Content-Type", "application/json")
                    .setHeader("token", clientToken)
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("----- YOUR DRAAFT ROOM CODE! READY FOR JOINING :) (in 2027)");
            System.out.println(resp.body());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        */

        AuthTokenServer.get().token = clientToken;

        Util.getOperatingSystem().open(
            FRONTEND_BASE_URI.toString() + "?auth_port=%d".formatted(AuthTokenServer.get().port())
        );

        this.setConnectionText("Opening in browser...");
    }
}
