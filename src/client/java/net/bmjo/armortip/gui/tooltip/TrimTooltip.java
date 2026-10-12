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
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrimTooltip implements ClientTooltipComponent {
    private static final Map<Item, Holder<TrimPattern>> PATTERN_CACHE;
    private static List<Holder.Reference<TrimMaterial>> MATERIAL_CACHE;
    private static final Map<Holder<TrimMaterial>, Holder<Item>> ITEM_CACHE;
    private static final EquipmentSlot[] ARMOR_SLOTS;
    private static final Item[] DEFAULT_ARMOR;
    private static final int SIZE = 40;

    private final ItemStack itemStack;

    public TrimTooltip(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return SIZE;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return SIZE + 8;
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        var player = Minecraft.getInstance().player;
        if (player != null) renderTrim(x, y, graphics, player, player.level());
    }

    private void renderTrim(int x, int y, GuiGraphicsExtractor gui, Player player, Level level) {
        if (level == null) return;
        var pattern = getCachedTrimPattern(level, this.itemStack.getItem());
        var material = getCachedTrimMaterial(level);
        if (pattern == null || material == null) return;

        var originalArmor = new ItemStack[4];
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
        renderMaterial(material, x, y, gui, player.level());
        for (int i = 0; i < ARMOR_SLOTS.length; i++) player.setItemSlot(ARMOR_SLOTS[i], originalArmor[i]);
    }

    private static void renderMaterial(Holder<TrimMaterial> material, int x, int y, GuiGraphicsExtractor gui, Level world) {
        var item = getCachedMaterialItem(world, material);
        if (item == null) return;

        gui.pose().pushMatrix();
        int offset = 3 * SIZE / 4;
        gui.pose().translate(x + offset, y);
        gui.pose().scale(0.5F);
        gui.item(item.value().getDefaultInstance(), 0, 0);
        gui.pose().popMatrix();
    }

    static {
        PATTERN_CACHE = new HashMap<>();
        ITEM_CACHE = new HashMap<>();
        ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
        DEFAULT_ARMOR = new Item[]{Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};
    }

    private static @Nullable Holder<TrimPattern> getCachedTrimPattern(Level world, Item item) {
        return PATTERN_CACHE.computeIfAbsent(item, i -> {
            var itemId = BuiltInRegistries.ITEM.getKey(i);
            var trimId = itemId.toString().split("_", 2)[0];

            var registryAccess = world.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);
            return registry.listElements().filter(trim -> trim.value().assetId().toString().equals(trimId)).findFirst().orElse(null);
        });
    }

    private static Holder<TrimMaterial> getCachedTrimMaterial(Level world) {
        if (MATERIAL_CACHE == null) {
            var registryAccess = world.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);
            MATERIAL_CACHE = registry.listElements().toList();
        }
        return MATERIAL_CACHE.get((ArmortipUtil.ticks / 40) % MATERIAL_CACHE.size());
    }

    private static Holder<Item> getCachedMaterialItem(Level world, Holder<TrimMaterial> material) {
        return ITEM_CACHE.computeIfAbsent(material, m -> {
            var registryAccess = world.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.ITEM);
            return registry.listElements().filter(item -> {
                var materialProvider = item.value().getDefaultInstance().get(DataComponents.PROVIDES_TRIM_MATERIAL);
                if (materialProvider == null) return false;
                return materialProvider.value().equals(m.value());
            }).findFirst().orElse(null);
        });
    }
}
