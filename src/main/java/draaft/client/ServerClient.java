package draaft.client;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import org.apache.commons.codec.binary.Base32;
import org.apache.http.client.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static draaft.draaft.MOD_ID;

public class ServerClient {
    private String clientPassword = null;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ServerClient() {
        SecureRandom instanceStrong = null;
        try {
            instanceStrong = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] randomBytes = new byte[20];
        instanceStrong.nextBytes(randomBytes);
        clientPassword = new Base32().encodeAsString(randomBytes);
    }

    // thanks menx :)
    public void evilConnectionTesting(/* :) */) {
        MinecraftClient inst = MinecraftClient.getInstance();
        Session session = inst.getSession();
        // okay: then we add a "generate room" button ingame and it gives you a key
        // https://sessionserver.mojang.com/session/minecraft/hasJoined?username=DesktopFolder&serverId=draaft2025server

        try {
            inst.getSessionService().joinServer(session.getProfile(), session.getAccessToken(), clientPassword);
            LOGGER.info("draaft successfully joined server draaft2025server");
        } catch (AuthenticationUnavailableException var3) {
            LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.serversUnavailable");
        } catch (InvalidCredentialsException var4) {
            LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.invalidSession");
        } catch (AuthenticationException authenticationException) {
            LOGGER.warn("disconnect.loginFailedInfo {}", authenticationException.getMessage());
        }
    }
}
