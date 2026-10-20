package net.bmjo.armortip.util;

import net.bmjo.armortip.config.ArmortipConfig;
import net.bmjo.armortip.gui.tooltip.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArmortipUtil {
    public static final int PADDING = 2;
    public static final int LINE_HEIGHT = 10;
    public static final int ITEM_SIZE = 8;
    public static final int CYCLE_TICKS = 40;

    private static final Map<EntityType<?>, @Nullable LivingEntity> ENTITY_CACHE = new HashMap<>();

    private static final List<TooltipEntry> TOOLTIP_ENTRIES = List.of(
            TooltipEntry.of(ToolTooltip::new, ArmortipConfig.showTool, ArmortipUtil::isTool),
            TooltipEntry.of(ArmorTooltip::new, ArmortipConfig.showArmor, ArmortipUtil::isArmor),
            TooltipEntry.of(TrimTooltip::new, ArmortipConfig.showTrim, stack -> stack.getItem() instanceof SmithingTemplateItem && !stack.is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)),
            TooltipEntry.of(EntityTooltip::new, ArmortipConfig.showMob, stack -> stack.getItem() instanceof SpawnEggItem),
            TooltipEntry.of(EffectTooltip::new, ArmortipConfig.showEffect, stack -> stack.getItem() instanceof PotionItem || stack.getItem() instanceof TippedArrowItem),
            TooltipEntry.of(PaintingTooltip::new, ArmortipConfig.showPainting, stack -> getPaintingVariant(stack) != null),
            TooltipEntry.of(PotteryTooltip::new, ArmortipConfig.showPottery, stack -> stack.is(ItemTags.DECORATED_POT_SHERDS)),
            TooltipEntry.of(BannerTooltip::new, ArmortipConfig.showBanner, stack -> stack.getItem() instanceof BannerPatternItem),
            TooltipEntry.of(DurabilityTooltip::new, ArmortipConfig.showDurability, ItemStack::isDamaged)
    );

    public static int ticks;
    private static boolean collectingAttributes;

    public static void tick() {
        ticks++;
    }

    public static void clearCaches() {
        ENTITY_CACHE.clear();
        TrimTooltip.clearCaches();
    }

    public static ItemStack getFocusedItem() {
        return Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen && screen.hoveredSlot != null ? screen.hoveredSlot.getItem() : ItemStack.EMPTY;
    }

    public static List<ClientTooltipComponent> modifyTooltips(List<ClientTooltipComponent> lines) {
        if (!ArmortipConfig.isEnabled.get() || lines.isEmpty()) return lines;
        var itemStack = getFocusedItem();
        if (itemStack.isEmpty()) return lines;

        var modified = new ArrayList<>(lines);
        int i = 1;
        if (FabricLoader.getInstance().isModLoaded("legendarytooltips") && lines.size() > 2) i = 3;
        for (var entry : TOOLTIP_ENTRIES)
            if (entry.config().getAsBoolean() && entry.condition().test(itemStack))
                modified.add(i++, entry.factory().apply(itemStack));
        return modified;
    }

    public static boolean isTool(ItemStack itemStack) {
        return !itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty();
    }

    public static boolean isArmor(ItemStack itemStack) {
        return Equipable.get(itemStack) != null || itemStack.getItem() instanceof HorseArmorItem;
    }

    public static boolean shouldSkipVanillaAttributes(ItemStack instance) {
        if (collectingAttributes || !ArmortipConfig.isEnabled.get()) return false;
        boolean showToolTip = isTool(instance) && ArmortipConfig.showTool.get();
        boolean showArmorTip = isArmor(instance) && ArmortipConfig.showArmor.get();
        return showToolTip || showArmorTip;
    }

    public static List<Component> getAttributeTooltips(ItemStack itemStack) {
        collectingAttributes = true;
        try {
            return itemStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.NORMAL);
        } finally {
            collectingAttributes = false;
        }
    }

    public static @Nullable Holder<PaintingVariant> getPaintingVariant(ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (!itemStack.is(Items.PAINTING) || tag == null || !tag.contains("EntityTag", Tag.TAG_COMPOUND)) return null;
        var variant = Painting.loadVariant(tag.getCompound("EntityTag"));
        return variant.orElse(null);
    }

    public static ItemStack setPlayerItem(Player player, EquipmentSlot slot, ItemStack itemStack) {
        // set the inventory directly, setItemSlot would trigger equip events
        var inventory = player.getInventory();
        return switch (slot.getType()) {
            case HAND ->
                    slot == EquipmentSlot.MAINHAND ? inventory.items.set(inventory.selected, itemStack) : inventory.offhand.set(0, itemStack);
            case ARMOR -> inventory.armor.set(slot.getIndex(), itemStack);
        };
    }

    public static void renderEntity(@Nullable LivingEntity entity, int x, int y, int size, GuiGraphics gui) {
        if (entity == null)
            return;

        float yRot = (float) Math.atan(80 * Math.cos(ticks / 64.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * ticks / 64.0F) / 40.0F);

        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        var scale = size * 0.8F / Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (!(entity instanceof Player)) {
            scale *= 0.8F;
        }

        float center = entity instanceof AbstractHorse ? 0.75F : 0.5F;

        float bodyRot = entity.yBodyRot;
        float bodyRotO = entity.yBodyRotO;
        float headRot = entity.yHeadRot;
        float headRotO = entity.yHeadRotO;
        float entityYRot = entity.getYRot();
        float entityYRotO = entity.yRotO;
        float entityXRot = entity.getXRot();
        float entityXRotO = entity.xRotO;

        entity.yBodyRot = entity.yBodyRotO = 200.0F + yRot * 10.0F;
        entity.setYRot(entity.yBodyRot);
        entity.yRotO = entity.yBodyRot;
        entity.yHeadRot = entity.yHeadRotO = entity.yBodyRot + yRot * 5.0F;
        entity.setXRot(-xRot * 10.0F);
        entity.xRotO = entity.getXRot();

        int feetY = (int) (y + size / 2.0F + entity.getBbHeight() * scale * center);
        InventoryScreen.renderEntityInInventory(gui, x + size / 2, feetY, (int) scale, quaternionf, quaternionf2, entity);

        entity.yBodyRot = bodyRot;
        entity.yBodyRotO = bodyRotO;
        entity.yHeadRot = headRot;
        entity.yHeadRotO = headRotO;
        entity.setYRot(entityYRot);
        entity.yRotO = entityYRotO;
        entity.setXRot(entityXRot);
        entity.xRotO = entityXRotO;
    }

    public static @Nullable LivingEntity getCachedEntity(Level level, EntityType<?> type) {
        if (ENTITY_CACHE.containsKey(type)) return ENTITY_CACHE.get(type);

        var entity = type.create(level);
        var livingEntity = entity instanceof LivingEntity living ? living : null;
        if (livingEntity != null) livingEntity.setId(-14547 - ENTITY_CACHE.size());
        ENTITY_CACHE.put(type, livingEntity);
        return livingEntity;
    }

    private record TooltipEntry(
            Function<ItemStack, ClientTooltipComponent> factory,
            BooleanSupplier config,
            Predicate<ItemStack> condition) {

        static TooltipEntry of(Function<ItemStack, ClientTooltipComponent> factory, OptionInstance<Boolean> config, Predicate<ItemStack> condition) {
            return new TooltipEntry(factory, config::get, condition);
        }
    }
}
