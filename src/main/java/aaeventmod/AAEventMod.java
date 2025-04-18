package aaeventmod;

import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AAEventMod implements ModInitializer {
	public static final String MOD_ID = "aaeventmod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("AA Changes Initialized");
	}
}