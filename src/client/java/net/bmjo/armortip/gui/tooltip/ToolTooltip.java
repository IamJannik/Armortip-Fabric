package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.bmjo.armortip.util.AttributeComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ToolTooltip implements ClientTooltipComponent {
    private final List<AttributeComponent> widgets;

    public ToolTooltip(ItemStack itemStack) {
        this.widgets = AttributeComponent.fromItem(itemStack);
    }

    @Override
    public int getHeight() {
        return ArmortipUtil.LINE_HEIGHT * widgets.size() + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(Font font) {
        int width = 0;
        for (var widget : widgets) {
            width = Math.max(width, widget.getWidth(font));
        }
        return width;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int i = 0;
        for (var widget : widgets) {
            widget.render(font, x, y + i * ArmortipUtil.LINE_HEIGHT, graphics);
            i++;
        }
    }
}
