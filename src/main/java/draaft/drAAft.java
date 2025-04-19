package draaft;

import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class drAAft implements ModInitializer {
	public static final String MOD_ID = "draaft";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("AA Changes Initialized");
	}
}