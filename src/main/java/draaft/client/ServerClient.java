package draaft.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import draaft.draaft;
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

import static draaft.draaft.*;

public class ServerClient {
    private final String clientPassword;

    public static final Logger LOGGER = draaft.LOGGER;
    HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    public String connectingStatus = null;
    public long connectingStatusTimestamp = 0; // 0 indicates to only show on hover

    public static ServerClient INSTANCE = null;

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

    public static ServerClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerClient();
        }
        return INSTANCE;
    }

    private void setConnectionText(String text) {
        this.connectingStatus = text;
        this.connectingStatusTimestamp = Instant.now().getEpochSecond();
    }

    // thanks menx :)
    public String draaftLogin(/* :) */) {
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
            return null;
        } catch (InvalidCredentialsException var4) {
            LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.invalidSession");
            this.setConnectionText("Error: Invalid session!");
            return null;
        } catch (AuthenticationException authenticationException) {
            LOGGER.warn("disconnect.loginFailedInfo {}", authenticationException.getMessage());
            this.setConnectionText(authenticationException.getMessage());
            return null;
        }
        this.setConnectionText("Contacting drAAft server...");
        String username = session.getUsername();

        JsonObject body = new JsonObject();
        body.addProperty("serverID", clientPassword);
        body.addProperty("username", username);

        String clientToken;
        try {
            var req = HttpRequest.newBuilder(API_BASE_URI.resolve("authenticate"))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .setHeader("Content-Type", "application/json")
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            JsonObject result  = new Gson().fromJson(resp.body(), JsonObject.class);
            clientToken = result.get("token").getAsString();
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Could not contact drAAft server: {}", e.getMessage());
            this.setConnectionText("Error contacting drAAft server!");
            return null;
        }

//        AuthTokenServer.get().token = clientToken;
        return clientToken;
//
//        Util.getOperatingSystem().open(
//            FRONTEND_BASE_URI + "?auth_port=%d".formatted(AuthTokenServer.get().port())
//        );

//        this.setConnectionText("Opening in browser...");
    }
}
