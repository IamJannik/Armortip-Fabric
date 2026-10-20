package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.jetbrains.annotations.Nullable;

public class EffectTooltip implements ClientTooltipComponent {
    private static final int SIZE = 20;
    private final @Nullable TextureAtlasSprite sprite;

    public EffectTooltip(ItemStack itemStack) {
        var effects = PotionUtils.getMobEffects(itemStack);
        this.sprite = effects.isEmpty() ? null : Minecraft.getInstance().getMobEffectTextures().get(effects.get(0).getEffect());
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
