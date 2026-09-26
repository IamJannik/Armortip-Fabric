package net.bmjo.armortip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bmjo.armortip.config.ArmortipConfig;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Armortip implements ClientModInitializer {
	public static final String MOD_ID = "armortip";
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public void onInitializeClient() {
		ArmortipConfig.init();
        ClientTickEvents.END_CLIENT_TICK.register(ArmortipUtil::tick);
	}
}