package net.bmjo.armortip.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.bmjo.armortip.Armortip;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.OptionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArmortipConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(Armortip.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<Option> OPTIONS = new ArrayList<>();

    public static final OptionInstance<Boolean> isEnabled = register("enable");
    public static final OptionInstance<Boolean> showTool = register("tool");
    public static final OptionInstance<Boolean> showArmor = register("armor");
    public static final OptionInstance<Boolean> showTrim = register("trim");
    public static final OptionInstance<Boolean> showMob = register("mob");
    public static final OptionInstance<Boolean> showEffect = register("effect");
    public static final OptionInstance<Boolean> showPottery = register("pottery");
    public static final OptionInstance<Boolean> showPainting = register("painting");
    public static final OptionInstance<Boolean> showDisc = register("disc");
    public static final OptionInstance<Boolean> showBanner = register("banner");
    public static final OptionInstance<Boolean> showDurability = register("durability");

    public static void init() {
        load();
    }

    public static OptionInstance<?>[] getFeatureOptions() {
        return OPTIONS.stream()
                .map(Option::instance)
                .filter(option -> option != isEnabled)
                .toArray(OptionInstance[]::new);
    }

    private static void load() {
        var path = getConfigPath();
        if (!Files.exists(path)) {
            save();
            return;
        }

        try (var reader = Files.newBufferedReader(path)) {
            var json = JsonParser.parseReader(reader).getAsJsonObject();
            for (var option : OPTIONS) {
                var value = json.get(option.id());
                if (value == null) value = json.get(option.translationKey());
                if (value != null) option.instance().set(value.getAsBoolean());
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't read Armortip configuration file; falling back to defaults", e);
            save();
        }
    }

    public static void save() {
        JsonObject config = new JsonObject();
        for (var option : OPTIONS) {
            config.addProperty(option.id(), option.instance().get());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(getConfigPath())) {
            writer.write(GSON.toJson(config));
        } catch (Exception e) {
            LOGGER.error("Couldn't save Armortip configuration file", e);
        }
    }

    private static OptionInstance<Boolean> register(String id) {
        var instance = OptionInstance.createBoolean(translationKey(id), true);
        OPTIONS.add(new Option(id, instance));
        return instance;
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(Armortip.MOD_ID + ".json");
    }

    private static String translationKey(String id) {
        return "option." + Armortip.MOD_ID + "." + id;
    }

    private record Option(String id, OptionInstance<Boolean> instance) {
        String translationKey() {
            return ArmortipConfig.translationKey(this.id);
        }
    }
}
