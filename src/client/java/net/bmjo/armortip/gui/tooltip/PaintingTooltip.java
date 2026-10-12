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

public class PaintingTooltip implements ClientTooltipComponent {
    private static final int SIZE = 32;
    private final FormattedCharSequence title;
    private final TextureAtlasSprite sprite;
    private final int width;
    private final int height;
    private final FormattedCharSequence dimensions;

    public PaintingTooltip(ItemStack itemStack) {
        var painting = itemStack.get(DataComponents.PAINTING_VARIANT);
        if (painting == null) {
            this.title = null;
            this.sprite = null;
            this.width = 0;
            this.height = 0;
            this.dimensions = null;
            return;
        }

        this.title = painting.value().title().isPresent() ? painting.value().title().get().getVisualOrderText() : Component.literal("???").getVisualOrderText();

        var paintingsAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PAINTINGS);
        this.sprite = paintingsAtlas.getSprite(painting.value().assetId());

        int pWidth = painting.value().width();
        int pHeight = painting.value().height();
        float max = Math.max(pWidth, pHeight);
        this.width = (int) (SIZE * (pWidth / max));
        this.height = (int) (SIZE * (pHeight / max));
        this.dimensions = Component.translatable("painting.dimensions", pWidth, pHeight).getVisualOrderText();

    }

    @Override
    public int getHeight(@NonNull Font font) {
        return height + ArmortipUtil.PADDING + (title != null ? ArmortipUtil.LINE_HEIGHT : 0) + (dimensions != null ? 10 : 0);
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return Math.max(width, Math.max(title != null ? font.width(title) : 0, dimensions != null ? font.width(dimensions) : 0));
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        if (this.sprite == null) return;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        graphics.text(font, title, x, y + height + ArmortipUtil.PADDING, -1);
        graphics.text(font, dimensions, x, y + height + ArmortipUtil.PADDING + ArmortipUtil.LINE_HEIGHT, -1);
    }
}
