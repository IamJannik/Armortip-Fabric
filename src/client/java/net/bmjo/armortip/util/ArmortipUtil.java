package net.bmjo.armortip.util;

import net.bmjo.armortip.config.ArmortipConfig;
import net.bmjo.armortip.gui.tooltip.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

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
            TooltipEntry.of(ToolTooltip::new, ArmortipConfig.showTool, DataComponents.TOOL, DataComponents.WEAPON),
            TooltipEntry.of(ArmorTooltip::new, ArmortipConfig.showArmor, DataComponents.EQUIPPABLE),
            new TooltipEntry(TrimTooltip::new, ArmortipConfig.showTrim::get, stack -> stack.getItem() instanceof SmithingTemplateItem && stack.getItem() != Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
            TooltipEntry.of(EntityTooltip::new, ArmortipConfig.showMob, DataComponents.ENTITY_DATA),
            TooltipEntry.of(EffectTooltip::new, ArmortipConfig.showEffect, DataComponents.POTION_CONTENTS),
            TooltipEntry.of(PaintingTooltip::new, ArmortipConfig.showPainting, DataComponents.PAINTING_VARIANT),
            TooltipEntry.of(PotteryTooltip::new, ArmortipConfig.showPottery, DataComponents.PROVIDES_POTTERY_PATTERN),
            TooltipEntry.of(BannerTooltip::new, ArmortipConfig.showBanner, DataComponents.PROVIDES_BANNER_PATTERNS),
            new TooltipEntry(DurabilityTooltip::new, ArmortipConfig.showDurability::get, ItemStack::isDamaged)
    );

    public static int ticks;

    public static void tick() {
        ticks++;
    }

    public static void clearCaches() {
        ENTITY_CACHE.clear();
        TrimTooltip.clearCaches();
        BannerTooltip.clearCaches();
    }

    public static ItemStack getFocusedItem() {
        return Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen<?> screen && screen.hoveredSlot != null ? screen.hoveredSlot.getItem() : ItemStack.EMPTY;
    }

    public static void modifyTooltips(List<ClientTooltipComponent> lines) {
        if (!ArmortipConfig.isEnabled.get()) return;
        var itemStack = getFocusedItem();
        if (itemStack.isEmpty()) return;

        int i = 1;
        for (var entry : TOOLTIP_ENTRIES)
            if (entry.config().getAsBoolean() && entry.condition().test(itemStack))
                lines.add(i++, entry.factory().apply(itemStack));
    }

    public static boolean shouldSkipVanillaAttributes(ItemStack instance) {
        if (!ArmortipConfig.isEnabled.get()) return false;
        boolean showToolTip = (instance.has(DataComponents.TOOL) || instance.has(DataComponents.WEAPON)) && ArmortipConfig.showTool.get();
        boolean showArmorTip = instance.has(DataComponents.EQUIPPABLE) && ArmortipConfig.showArmor.get();
        return showToolTip || showArmorTip;
    }

    public static void renderEntity(@Nullable LivingEntity entity, int x, int y, int size, GuiGraphicsExtractor gui) {
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

        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() * 0.5F, 0.0F);
        if (entity instanceof AbstractHorse) {
            vector3f = new Vector3f(0.0F, entity.getBbHeight() * 0.75F, 0.0F);
        }

        var entityRenderState = getEntityRenderState(entity);
        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            livingEntityRenderState.bodyRot = 200.0F + yRot * 10.0F;
            livingEntityRenderState.yRot = yRot * 5.0F;
            livingEntityRenderState.xRot = -xRot * 10.0F;

            livingEntityRenderState.boundingBoxWidth /= livingEntityRenderState.scale;
            livingEntityRenderState.boundingBoxHeight /= livingEntityRenderState.scale;
            livingEntityRenderState.scale = 1;
        }
        gui.entity(entityRenderState, scale, vector3f, quaternionf, quaternionf2, x, y, x + size, y + size);
    }

    private static EntityRenderState getEntityRenderState(LivingEntity entity) {
        var entityRenderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        var entityRenderer = entityRenderManager.getRenderer(entity);
        var entityRenderState = entityRenderer.createRenderState(entity, 1.0F);
        entityRenderState.lightCoords = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        return entityRenderState;
    }

    public static @Nullable LivingEntity getCachedEntity(Level level, EntityType<?> type) {
        if (ENTITY_CACHE.containsKey(type)) return ENTITY_CACHE.get(type);

        var entity = type.create(level, EntitySpawnReason.LOAD);
        var livingEntity = entity instanceof LivingEntity living ? living : null;
        if (livingEntity != null) livingEntity.setId(-14547 - ENTITY_CACHE.size());
        ENTITY_CACHE.put(type, livingEntity);
        return livingEntity;
    }

    private record TooltipEntry(
            Function<ItemStack, ClientTooltipComponent> factory,
            BooleanSupplier config,
            Predicate<ItemStack> condition) {

        static TooltipEntry of(Function<ItemStack, ClientTooltipComponent> factory, OptionInstance<Boolean> config, DataComponentType<?>... types) {
            return new TooltipEntry(factory, config::get, stack -> {
                for (var type : types) if (stack.has(type)) return true;
                return false;
            });
        }
    }
}
