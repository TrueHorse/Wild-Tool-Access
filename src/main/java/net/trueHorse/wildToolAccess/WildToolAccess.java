package net.trueHorse.wildToolAccess;

import net.fabricmc.api.ModInitializer;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WildToolAccess implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("wildtoolaccess");

	@Override
	public void onInitialize() {
		SwapItemPacket.registerPacket();
		WildToolAccessConfig.loadCofigs();
	}
}
