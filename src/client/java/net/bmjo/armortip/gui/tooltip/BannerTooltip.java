package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BannerTooltip implements ClientTooltipComponent {
    private static final int BANNER_WIDTH = 20;
    private static final int BANNER_HEIGHT = 40;

    private static final DyeColor[] DYE_COLORS = DyeColor.values();
    private static final Map<DyeColor, @Nullable Holder<Item>> ITEM_CACHE = new HashMap<>();

    private static @Nullable BannerFlagModel bannerFlagModel;

    private final @Nullable HolderSet<BannerPattern> pattern;

    public BannerTooltip(ItemStack itemStack) {
        this.pattern = itemStack.get(DataComponents.PROVIDES_BANNER_PATTERNS);
    }

    public static void clearCaches() {
        ITEM_CACHE.clear();
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return BANNER_HEIGHT + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return BANNER_WIDTH + ArmortipUtil.PADDING + ArmortipUtil.ITEM_SIZE;
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        if (this.pattern == null || this.pattern.size() == 0) return;

        var color = getDyeColor();
        var layer = new BannerPatternLayers.Layer(this.pattern.get(0), color);
        var bannerPatternLayers = new BannerPatternLayers(List.of(layer));

        graphics.bannerPattern(getBannerFlagModel(), DyeColor.WHITE, bannerPatternLayers, x, y, x + BANNER_WIDTH, y + BANNER_HEIGHT);

        var level = Minecraft.getInstance().level;
        if (level != null) renderDye(color, x, y, graphics, level);
    }

    private static void renderDye(DyeColor color, int x, int y, GuiGraphicsExtractor gui, Level level) {
        var item = getCachedDyeItem(level, color);
        if (item == null) return;

        gui.pose().pushMatrix();
        gui.pose().translate(x + BANNER_WIDTH + ArmortipUtil.PADDING, y);
        gui.pose().scale(0.5F);
        gui.item(item.value().getDefaultInstance(), 0, 0);
        gui.pose().popMatrix();
    }

    private static BannerFlagModel getBannerFlagModel() {
        var models = Minecraft.getInstance().getEntityModels();
        if (bannerFlagModel == null) {
            bannerFlagModel = new BannerFlagModel(models.bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
        }
        return bannerFlagModel;
    }

    private static DyeColor getDyeColor() {
        return DYE_COLORS[(ArmortipUtil.ticks / ArmortipUtil.CYCLE_TICKS) % DYE_COLORS.length];
    }

    private static @Nullable Holder<Item> getCachedDyeItem(Level level, DyeColor color) {
        if (ITEM_CACHE.containsKey(color)) return ITEM_CACHE.get(color);

        var registry = level.registryAccess().lookupOrThrow(Registries.ITEM);
        var item = registry.listElements()
                .filter(candidate -> candidate.value().getDefaultInstance().get(DataComponents.DYE) == color)
                .findFirst()
                .orElse(null);
        ITEM_CACHE.put(color, item);
        return item;
    }
}
