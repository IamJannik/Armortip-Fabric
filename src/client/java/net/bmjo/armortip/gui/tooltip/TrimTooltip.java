package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrimTooltip implements ClientTooltipComponent {
    private static final int SIZE = 40;

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static final Item[] DEFAULT_ARMOR = {Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};

    private static final Map<Item, @Nullable Holder<TrimPattern>> PATTERN_CACHE = new HashMap<>();
    private static final Map<Holder<TrimMaterial>, @Nullable Holder<Item>> ITEM_CACHE = new HashMap<>();
    private static @Nullable List<Holder.Reference<TrimMaterial>> materialCache;

    private final ItemStack itemStack;

    public TrimTooltip(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static void clearCaches() {
        PATTERN_CACHE.clear();
        ITEM_CACHE.clear();
        materialCache = null;
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return SIZE + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return SIZE + ArmortipUtil.ITEM_SIZE / 2;
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        var player = Minecraft.getInstance().player;
        if (player != null) renderTrim(x, y, graphics, player, player.level());
    }

    private void renderTrim(int x, int y, GuiGraphicsExtractor gui, Player player, @Nullable Level level) {
        if (level == null) return;
        var pattern = getCachedTrimPattern(level, this.itemStack.getItem());
        var material = getCachedTrimMaterial(level);
        if (pattern == null || material == null) return;

        var originalArmor = new ItemStack[ARMOR_SLOTS.length];
        for (int i = 0; i < ARMOR_SLOTS.length; i++) originalArmor[i] = player.getItemBySlot(ARMOR_SLOTS[i]).copy();
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            var itemStack = player.getItemBySlot(ARMOR_SLOTS[i]);
            if (itemStack.isEmpty()) {
                var armor = DEFAULT_ARMOR[i].getDefaultInstance();
                armor.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
                player.setItemSlot(ARMOR_SLOTS[i], armor);
            } else if (level.recipeAccess().propertySet(RecipePropertySet.SMITHING_BASE).test(itemStack)) {
                itemStack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
            }
        }
        ArmortipUtil.renderEntity(player, x - SIZE / 6, y, SIZE, gui);
        renderMaterial(material, x, y, gui, level);
        for (int i = 0; i < ARMOR_SLOTS.length; i++) player.setItemSlot(ARMOR_SLOTS[i], originalArmor[i]);
    }

    private static void renderMaterial(Holder<TrimMaterial> material, int x, int y, GuiGraphicsExtractor gui, Level level) {
        var item = getCachedMaterialItem(level, material);
        if (item == null) return;

        gui.pose().pushMatrix();
        gui.pose().translate(x + 3 * SIZE / 4 - ArmortipUtil.ITEM_SIZE / 2, y);
        gui.pose().scale(0.5F);
        gui.item(item.value().getDefaultInstance(), 0, 0);
        gui.pose().popMatrix();
    }

    private static @Nullable Holder<TrimPattern> getCachedTrimPattern(Level level, Item item) {
        if (PATTERN_CACHE.containsKey(item)) return PATTERN_CACHE.get(item);

        var trimId = BuiltInRegistries.ITEM.getKey(item).toString().split("_", 2)[0];
        var registry = level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
        Holder<TrimPattern> pattern = registry.listElements()
                .filter(trim -> trim.value().assetId().toString().equals(trimId))
                .findFirst()
                .orElse(null);
        PATTERN_CACHE.put(item, pattern);
        return pattern;
    }

    private static @Nullable Holder<TrimMaterial> getCachedTrimMaterial(Level level) {
        if (materialCache == null) {
            materialCache = level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).listElements().toList();
        }
        if (materialCache.isEmpty()) return null;
        return materialCache.get((ArmortipUtil.ticks / ArmortipUtil.CYCLE_TICKS) % materialCache.size());
    }

    private static @Nullable Holder<Item> getCachedMaterialItem(Level level, Holder<TrimMaterial> material) {
        if (ITEM_CACHE.containsKey(material)) return ITEM_CACHE.get(material);

        var registry = level.registryAccess().lookupOrThrow(Registries.ITEM);
        var item = registry.listElements().filter(candidate -> {
            var materialProvider = candidate.value().getDefaultInstance().get(DataComponents.PROVIDES_TRIM_MATERIAL);
            return materialProvider != null && materialProvider.value().equals(material.value());
        }).findFirst().orElse(null);
        ITEM_CACHE.put(material, item);
        return item;
    }
}
