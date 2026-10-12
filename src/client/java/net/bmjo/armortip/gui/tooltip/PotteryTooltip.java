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
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class PotteryTooltip implements ClientTooltipComponent {
    private static final int SIZE = 24;
    private final TextureAtlasSprite sprite;

    public PotteryTooltip(ItemStack itemStack) {
        var pottery = itemStack.get(DataComponents.PROVIDES_POTTERY_PATTERN);
        if (pottery == null) {
            this.sprite = null;
            return;
        }
        var potAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.DECORATED_POT);
        this.sprite = potAtlas.getSprite(pottery.value().assetId().withPrefix("entity/decorated_pot/"));
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return SIZE + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return SIZE;
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        if (this.sprite == null) return;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, x, y, SIZE, SIZE);
    }
}
