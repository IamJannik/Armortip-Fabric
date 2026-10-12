package net.bmjo.armortip;

import net.bmjo.armortip.config.ArmortipConfig;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class Armortip implements ClientModInitializer {
	public static final String MOD_ID = "armortip";

	public static @NonNull Identifier identify(String string) {
		return Identifier.fromNamespaceAndPath(MOD_ID, string);
	}

	@Override
	public void onInitializeClient() {
		ArmortipConfig.init();
        ClientTickEvents.END_CLIENT_TICK.register(ArmortipUtil::tick);
	}
}