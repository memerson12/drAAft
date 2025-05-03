package draaft;

import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.InputStream;
import java.util.Properties;

public class draaft implements ModInitializer {
	public static final String MOD_ID = "draaft";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final String DRAAFT_VERSION = getDraaftVersion();

	@Override
	public void onInitialize() {
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