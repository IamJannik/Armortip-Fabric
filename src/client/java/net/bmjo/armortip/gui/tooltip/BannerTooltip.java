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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BannerTooltip implements ClientTooltipComponent {
    private static final Map<DyeColor, Holder<Item>> ITEM_CACHE = new HashMap<>();
    private final HolderSet<BannerPattern> pattern;
    private final BannerFlagModel bannerFlagModel;

    public BannerTooltip(ItemStack itemStack) {
        this.pattern = itemStack.get(DataComponents.PROVIDES_BANNER_PATTERNS);

        var modelPart = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG);
        this.bannerFlagModel = new BannerFlagModel(modelPart);
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return 40 + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return 20 + ArmortipUtil.PADDING + 8;
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        if (this.pattern == null || this.bannerFlagModel == null) return;

        var color = getDyeColor();
        var layer = new BannerPatternLayers.Layer(pattern.get(0), color);
        var bannerPatternLayers = new BannerPatternLayers(List.of(layer));

        graphics.bannerPattern(bannerFlagModel, DyeColor.WHITE, bannerPatternLayers, x, y, x + 20, y + 40);
        renderDye(color, x, y, graphics, Minecraft.getInstance().level);
    }

    private static void renderDye(DyeColor color, int x, int y, GuiGraphicsExtractor gui, Level world) {
        var item = getCachedDyeItem(world, color);
        if (item == null) return;

        gui.pose().pushMatrix();
        gui.pose().translate(x + 20 + ArmortipUtil.PADDING, y);
        gui.pose().scale(0.5F);
        gui.item(item.value().getDefaultInstance(), 0, 0);
        gui.pose().popMatrix();
    }

    private static DyeColor getDyeColor() {
        return DyeColor.values()[(ArmortipUtil.ticks / 40) % DyeColor.values().length];
    }

    private static Holder<Item> getCachedDyeItem(Level world, DyeColor color) {
        return ITEM_CACHE.computeIfAbsent(color, c -> {
            var registryAccess = world.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.ITEM);
            return registry.listElements().filter(item -> {
                var dyeProvider = item.value().getDefaultInstance().get(DataComponents.DYE);
                if (dyeProvider == null) return false;
                return dyeProvider == c;
            }).findFirst().orElse(null);
        });
    }
}
