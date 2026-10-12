package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.Armortip;
import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class DurabilityTooltip implements ClientTooltipComponent {
    private final FormattedCharSequence icon;
    private final FormattedCharSequence durability;

    public DurabilityTooltip(ItemStack itemStack) {
        icon = Component.literal("\uE003 ").withStyle(ChatFormatting.GRAY).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(Armortip.identify("icons")))).getVisualOrderText();
        durability = Component.translatable("item.durability", itemStack.getMaxDamage() - itemStack.getDamageValue(), itemStack.getMaxDamage()).withStyle(ChatFormatting.GRAY).getVisualOrderText();
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return ArmortipUtil.LINE_HEIGHT + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return font.width(icon) + font.width(durability);
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, @NonNull Font font, int x, int y) {
        graphics.text(font, icon, x, y, -1);
        graphics.text(font, durability, x + font.width(icon), y, -1);
    }
}
