package net.bmjo.armortip.config;

import com.google.gson.*;
import net.bmjo.armortip.Armortip;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.OptionInstance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ArmortipConfig {
    public static OptionInstance<Boolean> isEnabled = OptionInstance.createBoolean(getTranslation("enable"), Boolean.TRUE);
    public static OptionInstance<Boolean> showArmor = OptionInstance.createBoolean(getTranslation("armor"), Boolean.TRUE);
    public static OptionInstance<Boolean> showMob = OptionInstance.createBoolean(getTranslation("mob"), Boolean.TRUE);
    public static OptionInstance<Boolean> showEffect = OptionInstance.createBoolean(getTranslation("effect"), Boolean.TRUE);
    public static OptionInstance<Boolean> showPottery = OptionInstance.createBoolean(getTranslation("pottery"), Boolean.TRUE);
    public static OptionInstance<Boolean> showPainting = OptionInstance.createBoolean(getTranslation("painting"), Boolean.TRUE);
    public static OptionInstance<Boolean> showBanner = OptionInstance.createBoolean(getTranslation("banner"), Boolean.FALSE);

    public static void init() {
        load();
    }

    public static List<OptionInstance<Boolean>> getOptions() {
        return List.of(showArmor, showMob, showEffect, showPottery, showPainting, showBanner);
    }

    private static void load() {
        var path = getConfigPath();

        try {
            if (!Files.exists(path)) {
                save();
            }

            if (Files.exists(path)) {
                var br = Files.newBufferedReader(path);
                var json = JsonParser.parseReader(br).getAsJsonObject();

                var enabled = json.get(isEnabled.toString());
                if (enabled != null) isEnabled.set(enabled.getAsBoolean());

                for (var option : getOptions()) {
                    var config = json.get(option.toString());
                    if (config != null) option.set(config.getAsBoolean());
                }
            }
        } catch (IOException e) {
            System.err.println("Couldn't load Armortip configuration file; reverting to defaults");
        }
    }

    public static void save() {
        var path = getConfigPath();
        JsonObject config = new JsonObject();

        config.addProperty(isEnabled.toString(), isEnabled.get());
        for (var option : getOptions()) {
            config.addProperty(option.toString(), option.get());
        }

        var jsonString = Armortip.GSON.toJson(config);
        try (BufferedWriter fileWriter = Files.newBufferedWriter(path)) {
            fileWriter.write(jsonString);
        } catch (IOException e) {
            System.err.println("Couldn't save Armortip configuration file");
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(Armortip.MOD_ID + ".json");
    }

    private static String getTranslation(String id) {
        return "option." + Armortip.MOD_ID + "." + id;
    }
}
