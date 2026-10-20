package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PaintingTooltip implements ClientTooltipComponent {
    private static final int SIZE = 32;

    private final @Nullable TextureAtlasSprite sprite;
    private final FormattedCharSequence title;
    private final FormattedCharSequence dimensions;
    private final int width;
    private final int height;

    public PaintingTooltip(ItemStack itemStack) {
        var paintingHolder = ArmortipUtil.getPaintingVariant(itemStack);
        if (paintingHolder == null) {
            this.sprite = null;
            this.title = FormattedCharSequence.EMPTY;
            this.dimensions = FormattedCharSequence.EMPTY;
            this.width = 0;
            this.height = 0;
            return;
        }

        var painting = paintingHolder.value();
        this.title = paintingHolder.unwrapKey()
                .map(key -> Component.translatable(key.location().toLanguageKey("painting", "title")))
                .orElseGet(() -> Component.literal("???"))
                .getVisualOrderText();
        this.sprite = Minecraft.getInstance().getPaintingTextures().get(painting);

        int paintingWidth = Mth.positiveCeilDiv(painting.getWidth(), 16);
        int paintingHeight = Mth.positiveCeilDiv(painting.getHeight(), 16);
        float max = Math.max(paintingWidth, paintingHeight);
        this.width = (int) (SIZE * (paintingWidth / max));
        this.height = (int) (SIZE * (paintingHeight / max));
        this.dimensions = Component.translatable("painting.dimensions", paintingWidth, paintingHeight).getVisualOrderText();
    }

    @Override
    public int getHeight() {
        if (this.sprite == null) return 0;
        return height + ArmortipUtil.PADDING + 2 * ArmortipUtil.LINE_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        if (this.sprite == null) return 0;
        return Math.max(width, Math.max(font.width(title), font.width(dimensions)));
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (sprite == null) return;
        graphics.blit(x, y, 0, width, height, sprite);
        graphics.drawString(font, title, x, y + height + ArmortipUtil.PADDING, -1);
        graphics.drawString(font, dimensions, x, y + height + ArmortipUtil.PADDING + ArmortipUtil.LINE_HEIGHT, -1);
    }
}
