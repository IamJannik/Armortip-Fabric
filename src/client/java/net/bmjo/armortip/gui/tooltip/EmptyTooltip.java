package net.bmjo.armortip.gui.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class EmptyTooltip extends ClientTextTooltip implements TooltipComponent {
    public EmptyTooltip() {
        super(FormattedCharSequence.EMPTY);
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }
}
