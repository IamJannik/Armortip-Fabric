package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.bmjo.armortip.util.AttributeComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ToolTooltip implements ClientTooltipComponent {
    private final List<AttributeComponent> widgets = new ArrayList<>();

    public ToolTooltip(ItemStack itemStack) {
        var player = Minecraft.getInstance().player;

        var components = new ArrayList<Component>();
        itemStack.addAttributeTooltips(components::add, itemStack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT), player);

        for (var component : components) {
            var widget = AttributeComponent.from(component);
            if (widget == null) continue;
            widgets.add(widget);
        }
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return ArmortipUtil.LINE_HEIGHT * widgets.size() + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        int width = 0;
        for (var widget : widgets) {
            width = Math.max(width, widget.getWidth(font));
        }
        return width;
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        int i = 0;
        for (var widget : widgets) {
            widget.render(font, x, y + i * ArmortipUtil.LINE_HEIGHT, graphics);
            i++;
        }
    }
}
