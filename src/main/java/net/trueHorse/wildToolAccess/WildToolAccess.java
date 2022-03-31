package net.trueHorse.wildToolAccess;

import net.fabricmc.api.ModInitializer;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

public class WildToolAccess implements ModInitializer {

	@Override
	public void onInitialize() {
		SwapItemPacket.registerPacket();
		WildToolAccessConfig.loadCofigs();
	}
}
