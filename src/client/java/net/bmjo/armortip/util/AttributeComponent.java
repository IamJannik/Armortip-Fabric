package net.bmjo.armortip.util;

import net.bmjo.armortip.Armortip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;

import java.util.Map;
import java.util.function.Consumer;

public record AttributeComponent(FormattedCharSequence value, FormattedCharSequence icon) {
    private static final Map<String, String> ATTRIBUTE_ICONS;
    private static final Map<String, ChatFormatting> ATTRIBUTE_COLORS;

    public static AttributeComponent from(Component component) {
        AttributeComponent[] result = {null};
        walk(component, tc -> {
            if (result[0] != null) return;
            if (!tc.getKey().contains("attribute.modifier.")) return;

            var args = tc.getArgs();
            if (args.length < 2) return;

            var icon = Component.empty();
            if (args[1] instanceof Component c) {
                if (c.getContents() instanceof TranslatableContents inner) {
                    var key = inner.getKey();
                    if (ATTRIBUTE_COLORS.containsKey(key) && component instanceof MutableComponent mc) {
                        mc.withStyle(ATTRIBUTE_COLORS.get(key));
                    }
                    if (ATTRIBUTE_ICONS.containsKey(key)) {
                        icon = Component.literal(ATTRIBUTE_ICONS.getOrDefault(key, "")).withStyle(component.getStyle()).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(Armortip.identify("icons"))));
                    }
                }
            }

            result[0] = new AttributeComponent(component.getVisualOrderText(), icon.getVisualOrderText());
        });
        return result[0];
    }

    private static void walk(Component c, Consumer<TranslatableContents> consumer) {
        if (c.getContents() instanceof TranslatableContents tc) consumer.accept(tc);
        c.getSiblings().forEach(s -> walk(s, consumer));
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
