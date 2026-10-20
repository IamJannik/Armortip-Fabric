package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.jetbrains.annotations.Nullable;

public class PotteryTooltip implements ClientTooltipComponent {
    private static final int SIZE = 24;
    private final @Nullable TextureAtlasSprite sprite;

    public PotteryTooltip(ItemStack itemStack) {
        var pattern = DecoratedPotPatterns.getResourceKey(itemStack.getItem());
        var material = pattern == null ? null : Sheets.getDecoratedPotMaterial(pattern);
        this.sprite = material == null ? null : material.sprite();
    }

    @Override
    public int getHeight() {
        return sprite == null ? 0 : SIZE + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(Font font) {
        return sprite == null ? 0 : SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (this.sprite == null) return;
        graphics.blit(x, y, 0, SIZE, SIZE, this.sprite);
    }
}
