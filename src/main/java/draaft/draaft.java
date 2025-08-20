package draaft;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.InputStream;
import java.util.Properties;

public class draaft implements ModInitializer {
	public static final String MOD_ID = "draaft";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final String DRAAFT_VERSION = getDraaftVersion();

	public void evilConnectionTesting(/* :) */) {
		MinecraftClient inst = MinecraftClient.getInstance();
		Session session = inst.getSession();
		try {
			inst.getSessionService().joinServer(session.getProfile(), session.getAccessToken(), "draaft2025server");
			LOGGER.info("draaft successfully joined server draaft2025server");
		} catch (AuthenticationUnavailableException var3) {
			LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.serversUnavailable");
		} catch (InvalidCredentialsException var4) {
			LOGGER.warn("disconnect.loginFailedInfo: disconnect.loginFailedInfo.invalidSession");
		} catch (AuthenticationException authenticationException) {
			LOGGER.warn("disconnect.loginFailedInfo {}", authenticationException.getMessage());
		}
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Draaft version: {}", DRAAFT_VERSION);
		evilConnectionTesting();
	}
	public static String getDraaftVersion() {
		try (InputStream input = draaft.class.getClassLoader().getResourceAsStream("config.properties")) {
			Properties prop = new Properties();
			prop.load(input);
            String version = prop.getProperty("DRAAFT_VERSION");
			if(version == null) {
				LOGGER.warn("Could not get draaft version");
				return "Unknown";
			}
			return version;
		} catch (Exception e) {
			LOGGER.error("Failed to load config.properties", e);
			return "Unknown";
		}
	}
}