package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.Armortip;
import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class DurabilityTooltip implements ClientTooltipComponent {
    private final FormattedCharSequence icon;
    private final FormattedCharSequence durability;

    public DurabilityTooltip(ItemStack itemStack) {
        icon = Component.literal("\uE003 ").withStyle(ChatFormatting.GRAY).withStyle(Style.EMPTY.withFont(Armortip.identify("icons"))).getVisualOrderText();
        durability = Component.translatable("item.durability", itemStack.getMaxDamage() - itemStack.getDamageValue(), itemStack.getMaxDamage()).withStyle(ChatFormatting.GRAY).getVisualOrderText();
    }

    @Override
    public int getHeight() {
        return ArmortipUtil.LINE_HEIGHT + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(icon) + font.width(durability);
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        font.drawInBatch(icon, x, y, -1, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        font.drawInBatch(durability, x + font.width(icon), y, -1, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
    }
}
