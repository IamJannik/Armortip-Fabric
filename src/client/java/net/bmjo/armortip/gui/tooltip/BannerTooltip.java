package net.bmjo.armortip.gui.tooltip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.datafixers.util.Pair;
import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BannerTooltip implements ClientTooltipComponent {
    private static final int BANNER_WIDTH = 20;
    private static final int BANNER_HEIGHT = 40;

    private static final DyeColor[] DYE_COLORS = DyeColor.values();

    private static @Nullable ModelPart bannerFlag;

    private final @Nullable Holder<BannerPattern> pattern;

    public BannerTooltip(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BannerPatternItem patternItem) {
            this.pattern = BuiltInRegistries.BANNER_PATTERN.getTag(patternItem.getBannerPattern())
                    .filter(set -> set.size() > 0)
                    .map(set -> set.get(0))
                    .orElse(null);
        } else {
            this.pattern = null;
        }
    }

    @Override
    public int getHeight() {
        return pattern == null ? 0 : BANNER_HEIGHT + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(Font font) {
        return pattern == null ? 0 : BANNER_WIDTH + ArmortipUtil.PADDING + ArmortipUtil.ITEM_SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (this.pattern == null) return;

        var color = getDyeColor();
        Holder<BannerPattern> base = BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(BannerPatterns.BASE);
        List<Pair<Holder<BannerPattern>, DyeColor>> layers = List.of(Pair.of(base, DyeColor.WHITE), Pair.of(this.pattern, color));

        // same transformation as the loom screen, moved so the flag's top left corner is at x, y
        var flag = getBannerFlag();
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x - 2, y + 44, 0.0F);
        pose.scale(24.0F, -24.0F, 1.0F);
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.scale(0.6666667F, -0.6666667F, -0.6666667F);
        flag.xRot = 0.0F;
        flag.y = -32.0F;
        Lighting.setupForFlatItems();
        BannerRenderer.renderPatterns(pose, graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, flag, ModelBakery.BANNER_BASE, true, layers);
        graphics.flush();
        Lighting.setupFor3DItems();
        pose.popPose();

        renderDye(color, x, y, graphics);
    }

    private static void renderDye(DyeColor color, int x, int y, GuiGraphics gui) {
        gui.pose().pushPose();
        gui.pose().translate(x + BANNER_WIDTH + ArmortipUtil.PADDING, y, 0);
        gui.pose().scale(0.5F, 0.5F, 0.5F);
        gui.renderItem(DyeItem.byColor(color).getDefaultInstance(), 0, 0);
        gui.pose().popPose();
    }

    private static ModelPart getBannerFlag() {
        if (bannerFlag == null) {
            bannerFlag = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BANNER).getChild("flag");
        }
        return bannerFlag;
    }

    private static DyeColor getDyeColor() {
        return DYE_COLORS[(ArmortipUtil.ticks / ArmortipUtil.CYCLE_TICKS) % DYE_COLORS.length];
    }
}
