package net.bmjo.armortip;

import net.bmjo.armortip.config.ArmortipConfig;
import net.bmjo.armortip.gui.tooltip.EmptyTooltip;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.resources.ResourceLocation;

public class Armortip implements ClientModInitializer {
	public static final String MOD_ID = "armortip";

	public static ResourceLocation identify(String string) {
		return new ResourceLocation(MOD_ID, string);
	}

	@Override
	public void onInitializeClient() {
		ArmortipConfig.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> ArmortipUtil.tick());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ArmortipUtil.clearCaches());
		TooltipComponentCallback.EVENT.register((data) -> {
			if (data instanceof EmptyTooltip emptyTooltip) {
				return emptyTooltip;
			} else {
				return null;
			}
		});
	}
}
