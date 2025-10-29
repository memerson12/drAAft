package draaft;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import draaft.client.ServerClient;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

public class draaft implements ModInitializer {
	public static final String MOD_ID = "draaft";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final String DRAAFT_VERSION = getDraaftVersion();

	public static final String FRONTEND_ORIGIN = "http://localhost:8080";
	public static final URI FRONTEND_BASE_URI = URI.create(FRONTEND_ORIGIN + "/draaft/");
	public static final URI API_BASE_URI = URI.create("http://localhost:8000/");

	@Override
	public void onInitialize() {
        Configurator.setLevel(MOD_ID, Level.DEBUG);
		LOGGER.info("Draaft version: {}", DRAAFT_VERSION);
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