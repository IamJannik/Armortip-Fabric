package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class EffectTooltip implements ClientTooltipComponent {
    private static final int SIZE = 20;
    private final Identifier sprite;

    public EffectTooltip(ItemStack itemStack) {
        var potion = itemStack.get(DataComponents.POTION_CONTENTS);
        if (potion == null) {
            this.sprite = null;
            return;
        }
        var optEffect = potion.potion().map(p -> p.value().getEffects());
        if (optEffect.isEmpty() || optEffect.get().isEmpty()) {
            this.sprite = null;
            return;
        }
        this.sprite = Hud.getMobEffectSprite(optEffect.get().getFirst().getEffect());
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return sprite == null ? 0 : SIZE + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return sprite == null ? 0 : SIZE;
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        if (this.sprite == null) return;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, x, y, SIZE, SIZE);
    }
}
