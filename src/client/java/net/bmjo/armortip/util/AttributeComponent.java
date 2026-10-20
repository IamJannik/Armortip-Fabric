package net.bmjo.armortip.util;

import net.bmjo.armortip.Armortip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record AttributeComponent(FormattedCharSequence value, FormattedCharSequence icon) {
    private static final Map<String, String> ATTRIBUTE_ICONS;
    private static final Map<String, ChatFormatting> ATTRIBUTE_COLORS;

    public static List<AttributeComponent> fromItem(ItemStack itemStack) {
        var widgets = new ArrayList<AttributeComponent>();
        for (var component : ArmortipUtil.getAttributeTooltips(itemStack)) {
            var widget = from(component);
            if (widget == null) continue;
            widgets.add(widget);
        }
        return widgets;
    }

    public static @Nullable AttributeComponent from(Component component) {
        var attributeKey = findAttributeKey(component);
        if (attributeKey == null) return null;

        var color = ATTRIBUTE_COLORS.get(attributeKey);
        var value = color != null ? component.copy().withStyle(color) : component;

        var iconChar = ATTRIBUTE_ICONS.get(attributeKey);
        var icon = iconChar != null
                ? Component.literal(iconChar).withStyle(value.getStyle()).withStyle(Style.EMPTY.withFont(Armortip.identify("icons")))
                : Component.empty();

        return new AttributeComponent(value.getVisualOrderText(), icon.getVisualOrderText());
    }

    private static @Nullable String findAttributeKey(Component component) {
        if (component.getContents() instanceof TranslatableContents contents && contents.getKey().contains("attribute.modifier.")) {
            var args = contents.getArgs();
            if (args.length >= 2
                    && args[1] instanceof Component name
                    && name.getContents() instanceof TranslatableContents nameContents) {
                return nameContents.getKey();
            }
        }

        for (var sibling : component.getSiblings()) {
            var key = findAttributeKey(sibling);
            if (key != null) return key;
        }
        return null;
    }

    public int getWidth(Font font) {
        return font.width(value) + font.width(icon);
    }

    public void render(Font font, int x, int y, GuiGraphics graphics) {
        graphics.drawString(font, icon, x, y, -1);
        graphics.drawString(font, value, x + font.width(icon), y, -1);
    }

    static {
        ATTRIBUTE_ICONS = Map.of(
                "attribute.name.generic.attack_damage", "\uE001",
                "attribute.name.generic.attack_speed", "\uE002");
        ATTRIBUTE_COLORS = Map.of(
                "attribute.name.generic.attack_damage", ChatFormatting.RED,
                "attribute.name.generic.attack_speed", ChatFormatting.YELLOW);
    }
}
