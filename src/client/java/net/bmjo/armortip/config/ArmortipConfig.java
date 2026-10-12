package net.bmjo.armortip.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.bmjo.armortip.Armortip;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.OptionInstance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ArmortipConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final OptionInstance<Boolean> isEnabled = OptionInstance.createBoolean(getTranslation("enable"), Boolean.TRUE);
    public static final OptionInstance<Boolean> showTool = OptionInstance.createBoolean(getTranslation("tool"), Boolean.TRUE);
    public static final OptionInstance<Boolean> showArmor = OptionInstance.createBoolean(getTranslation("armor"), Boolean.TRUE);
    public static final OptionInstance<Boolean> showTrim = OptionInstance.createBoolean(getTranslation("trim"), Boolean.TRUE);
    public static final OptionInstance<Boolean> showMob = OptionInstance.createBoolean(getTranslation("mob"), Boolean.TRUE);
    public static final OptionInstance<Boolean> showEffect = OptionInstance.createBoolean(getTranslation("effect"), Boolean.TRUE);
    public static final OptionInstance<Boolean> showPottery = OptionInstance.createBoolean(getTranslation("pottery"), Boolean.TRUE);
    public static final OptionInstance<Boolean> showPainting = OptionInstance.createBoolean(getTranslation("painting"), Boolean.TRUE);
    public static final OptionInstance<Boolean> showBanner = OptionInstance.createBoolean(getTranslation("banner"), Boolean.TRUE);

    public static void init() {
        load();
    }

    public static List<OptionInstance<Boolean>> getOptions() {
        return List.of(isEnabled, showTool, showArmor, showTrim, showMob, showEffect, showPottery, showPainting, showBanner);
    }

    public static String getOptionKey(OptionInstance<Boolean> option) {
        var map = Map.of(
                isEnabled, getTranslation("enable"),
                showTool, getTranslation("tool"),
                showArmor, getTranslation("armor"),
                showTrim, getTranslation("trim"),
                showMob, getTranslation("mob"),
                showEffect, getTranslation("effect"),
                showPottery, getTranslation("pottery"),
                showPainting, getTranslation("painting"),
                showBanner, getTranslation("banner")
        );
        return map.getOrDefault(option, "none");
    }

    private static void load() {
        var path = getConfigPath();

        try {
            if (!Files.exists(path)) {
                save();
            } else {
                var br = Files.newBufferedReader(path);
                var json = JsonParser.parseReader(br).getAsJsonObject();
                for (var option : getOptions()) {
                    var config = json.get(getOptionKey(option));
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

        for (var option : getOptions()) {
            config.addProperty(getOptionKey(option), option.get());
        }

        var jsonString = GSON.toJson(config);
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
