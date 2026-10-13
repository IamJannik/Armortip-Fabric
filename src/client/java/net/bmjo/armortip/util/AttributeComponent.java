package net.bmjo.armortip.util;

import net.bmjo.armortip.Armortip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public record AttributeComponent(FormattedCharSequence value, FormattedCharSequence icon) {
    private static final Map<String, String> ATTRIBUTE_ICONS;
    private static final Map<String, ChatFormatting> ATTRIBUTE_COLORS;

    public static @Nullable AttributeComponent from(Component component) {
        var attributeKey = findAttributeKey(component);
        if (attributeKey == null) return null;

        var color = ATTRIBUTE_COLORS.get(attributeKey);
        var value = color != null ? component.copy().withStyle(color) : component;

        var iconChar = ATTRIBUTE_ICONS.get(attributeKey);
        var icon = iconChar != null
                ? Component.literal(ATTRIBUTE_ICONS.getOrDefault(attributeKey, "")).withStyle(value.getStyle()).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(Armortip.identify("icons"))))
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

    public void render(Font font, int x, int y, GuiGraphicsExtractor graphics) {
        graphics.text(font, icon, x, y, -1);
        graphics.text(font, value, x + font.width(icon), y, -1);
    }

    static {
        ATTRIBUTE_ICONS = Map.of(
                "attribute.name.attack_damage", "\uE001",
                "attribute.name.attack_speed", "\uE002");
        ATTRIBUTE_COLORS = Map.of(
                "attribute.name.attack_damage", ChatFormatting.RED,
                "attribute.name.attack_speed", ChatFormatting.YELLOW);
    }
}
