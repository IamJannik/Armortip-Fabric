package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PaintingTooltip implements ClientTooltipComponent {
    private static final int SIZE = 32;

    private final @Nullable TextureAtlasSprite sprite;
    private final FormattedCharSequence title;
    private final FormattedCharSequence dimensions;
    private final int width;
    private final int height;

    public PaintingTooltip(ItemStack itemStack) {
        var paintingHolder = itemStack.get(DataComponents.PAINTING_VARIANT);
        if (paintingHolder == null) {
            this.sprite = null;
            this.title = FormattedCharSequence.EMPTY;
            this.dimensions = FormattedCharSequence.EMPTY;
            this.width = 0;
            this.height = 0;
            return;
        }

        var painting = paintingHolder.value();
        this.title = painting.title().orElseGet(() -> Component.literal("???")).getVisualOrderText();
        var paintingsAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PAINTINGS);
        this.sprite = paintingsAtlas.getSprite(painting.assetId());

        int paintingWidth = painting.width();
        int paintingHeight = painting.height();
        float max = Math.max(paintingWidth, paintingHeight);
        this.width = (int) (SIZE * (paintingWidth / max));
        this.height = (int) (SIZE * (paintingHeight / max));
        this.dimensions = Component.translatable("painting.dimensions", paintingWidth, paintingHeight).getVisualOrderText();
    }

    @Override
    public int getHeight(@NonNull Font font) {
        if (this.sprite == null) return 0;
        return height + ArmortipUtil.PADDING + 2 * ArmortipUtil.LINE_HEIGHT;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        if (this.sprite == null) return 0;
        return Math.max(width, Math.max(font.width(title), font.width(dimensions)));
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        if (sprite == null) return;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        graphics.text(font, title, x, y + height + ArmortipUtil.PADDING, -1);
        graphics.text(font, dimensions, x, y + height + ArmortipUtil.PADDING + ArmortipUtil.LINE_HEIGHT, -1);
    }
}
