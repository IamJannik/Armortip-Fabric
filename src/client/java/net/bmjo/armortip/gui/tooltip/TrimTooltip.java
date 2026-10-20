package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrimTooltip implements ClientTooltipComponent {
    private static final int SIZE = 40;

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static final Item[] DEFAULT_ARMOR = {Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};

    private static final Map<Item, @Nullable Holder<TrimPattern>> PATTERN_CACHE = new HashMap<>();
    private static @Nullable List<Holder.Reference<TrimMaterial>> materialCache;

    private final ItemStack itemStack;

    public TrimTooltip(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static void clearCaches() {
        PATTERN_CACHE.clear();
        materialCache = null;
    }

    @Override
    public int getHeight() {
        return SIZE + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(Font font) {
        return SIZE + ArmortipUtil.ITEM_SIZE / 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        var player = Minecraft.getInstance().player;
        if (player != null) renderTrim(x, y, graphics, player, player.level());
    }

    private void renderTrim(int x, int y, GuiGraphics gui, Player player, @Nullable Level level) {
        if (level == null) return;
        var pattern = getCachedTrimPattern(level, this.itemStack);
        var material = getCachedTrimMaterial(level);
        if (pattern == null || material == null) return;

        var trim = new ArmorTrim(material, pattern);
        var originalArmor = new ItemStack[ARMOR_SLOTS.length];
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            var original = player.getItemBySlot(ARMOR_SLOTS[i]);
            originalArmor[i] = original;

            ItemStack armor;
            if (original.isEmpty()) armor = DEFAULT_ARMOR[i].getDefaultInstance();
            else if (original.is(ItemTags.TRIMMABLE_ARMOR)) armor = original.copy();
            else continue;

            ArmorTrim.setTrim(level.registryAccess(), armor, trim);
            ArmortipUtil.setPlayerItem(player, ARMOR_SLOTS[i], armor);
        }
        ArmortipUtil.renderEntity(player, x - SIZE / 6, y, SIZE, gui);
        renderMaterial(material, x, y, gui);
        for (int i = 0; i < ARMOR_SLOTS.length; i++)
            ArmortipUtil.setPlayerItem(player, ARMOR_SLOTS[i], originalArmor[i]);
    }

    private static void renderMaterial(Holder<TrimMaterial> material, int x, int y, GuiGraphics gui) {
        var item = material.value().ingredient().value();
        if (item == Items.AIR) return;

        gui.pose().pushPose();
        gui.pose().translate(x + 3 * SIZE / 4 - ArmortipUtil.ITEM_SIZE / 2, y, 0);
        gui.pose().scale(0.5F, 0.5F, 0.5F);
        gui.renderItem(item.getDefaultInstance(), 0, 0);
        gui.pose().popPose();
    }

    private static @Nullable Holder<TrimPattern> getCachedTrimPattern(Level level, ItemStack template) {
        var item = template.getItem();
        if (PATTERN_CACHE.containsKey(item)) return PATTERN_CACHE.get(item);

        Holder<TrimPattern> pattern = TrimPatterns.getFromTemplate(level.registryAccess(), template).orElse(null);
        PATTERN_CACHE.put(item, pattern);
        return pattern;
    }

    private static @Nullable Holder<TrimMaterial> getCachedTrimMaterial(Level level) {
        if (materialCache == null) {
            materialCache = level.registryAccess().registryOrThrow(Registries.TRIM_MATERIAL).holders().toList();
        }
        if (materialCache.isEmpty()) return null;
        return materialCache.get((ArmortipUtil.ticks / ArmortipUtil.CYCLE_TICKS) % materialCache.size());
    }
}
