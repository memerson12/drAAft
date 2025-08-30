package draaft.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.Session;
import org.apache.commons.codec.binary.Base32;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static draaft.draaft.MOD_ID;

public class ServerClient {
    private String clientPassword = null;
    private String clientToken = null;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    public static ServerClient INSTANCE = null;

    public static ServerClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerClient();
        }
        return INSTANCE;
    }

    public ServerClient() {
        SecureRandom instanceStrong = null;
        try {
            instanceStrong = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] randomBytes = new byte[15];
        instanceStrong.nextBytes(randomBytes);
        clientPassword = new Base32().encodeAsString(randomBytes) + "draaaaft";
    }

    // thanks menx :)
    public void evilConnectionTesting(/* :) */) {
        MinecraftClient inst = MinecraftClient.getInstance();
        Session session = inst.getSession();
        // okay: then we add a "generate room" button ingame and it gives you a key
        // https://sessionserver.mojang.com/session/minecraft/hasJoined?username=DesktopFolder&serverId=draaft2025server

        try {
            inst.getSessionService().joinServer(session.getProfile(), session.getAccessToken(), clientPassword);
            LOGGER.info("draaft successfully joined server with clientPassword");
        } catch (AuthenticationUnavailableException var3) {
            LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.serversUnavailable");
        } catch (InvalidCredentialsException var4) {
            LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.invalidSession");
        } catch (AuthenticationException authenticationException) {
            LOGGER.warn("disconnect.loginFailedInfo {}", authenticationException.getMessage());
        }
        String username = session.getUsername();

        JsonObject body = new JsonObject();
        body.addProperty("serverID", clientPassword);
        body.addProperty("username", username);

        try {
            var req = HttpRequest.newBuilder(new URI("http://localhost:8000/authenticate"))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .setHeader("Content-Type", "application/json")
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            JsonObject result  = new Gson().fromJson(resp.body(), JsonObject.class);
            clientToken = result.get("token").getAsString();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

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

        Clipboard clipboard = new Clipboard();
        clipboard.setClipboard(MinecraftClient.getInstance().getWindow().getHandle(), clientToken);
    }
}
