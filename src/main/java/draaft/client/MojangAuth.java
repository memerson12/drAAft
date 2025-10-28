package draaft.client;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import draaft.client.gui.DraaftToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import org.apache.commons.codec.binary.Base32;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static draaft.draaft.MOD_ID;

public class MojangAuth {
    private static final TranslatableText ERROR_TITLE = new TranslatableText("draaft.login.failed.title");
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static @Nullable String joinDraaftServer() {
        var client = MinecraftClient.getInstance();

        var session = client.getSession();

        String clientPassword = generateClientPassword();

        try {
            client.getSessionService().joinServer(session.getProfile(), session.getAccessToken(), clientPassword);

            LOGGER.info("draaft successfully joined server with clientPassword");
        } catch (AuthenticationUnavailableException e) {
            LOGGER.error("disconnect.loginFailedInfo: disconnect.loginFailedInfo.serversUnavailable", e);
            DraaftToast.showError(ERROR_TITLE, new TranslatableText("draaft.login.failed.mojangUnavailable"));
            return null;
        } catch (InvalidCredentialsException e) {
            LOGGER.error("disconnect.loginFailedInfo: disconnect.loginFailedInfo.invalidSession", e);
            DraaftToast.showError(ERROR_TITLE, new TranslatableText("draaft.login.failed.invalidSession"));
            return null;
        } catch (AuthenticationException authenticationException) {
            LOGGER.error("disconnect.loginFailedInfo", authenticationException);
            DraaftToast.showError(ERROR_TITLE, new TranslatableText("draaft.login.failed.mojangFailed"));
            return null;
        }

        return clientPassword;
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
}
