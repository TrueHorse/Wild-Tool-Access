package net.trueHooorse.wildToolAccess;

import net.fabricmc.api.ModInitializer;
import net.trueHooorse.wildToolAccess.config.WildToolAccessConfig;

public class WildToolAccess implements ModInitializer {

	@Override
	public void onInitialize() {
		SwapItemPacket.registerPacket();
		WildToolAccessConfig.loadCofigs();
	}
}
