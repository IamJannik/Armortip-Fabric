package net.bmjo.armortip.client.gui;

import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector2ic;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ArmortipRenderer {
    private static List<Holder.Reference<TrimMaterial>> MATERIAL_CACHE;
    private static final Item[] DEFAULT_ARMOR = {Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};
    protected static final int SIZE = 24;
    public static final int WIDTH = SIZE;
    public static final int HEIGHT = SIZE * 2;
    public static final int MARGIN = 6;

    public static void renderArmortip(GuiGraphics graphics, ItemStack itemStack, int mouseX, int mouseY, Player player, ClientTooltipPositioner tooltipPositioner, boolean drawBG) {
        if (!(ArmortipUtil.isTipItem(itemStack)))
            return;

        Vector2ic vector2ic = tooltipPositioner.positionTooltip(graphics.guiWidth(), graphics.guiHeight(), mouseX, mouseY, WIDTH, HEIGHT);

        int startX = vector2ic.x();
        int startY = vector2ic.y();
        graphics.pose().pushPose();

        if (drawBG)
            TooltipRenderUtil.renderTooltipBackground(graphics, startX, startY, WIDTH, HEIGHT, 400);
        graphics.pose().translate(0.0F, 0.0F, 400.0F);

        if (itemStack.getItem() instanceof SmithingTemplateItem) {
            renderArmorTrim(player, graphics, itemStack, startX, startY);
        } else {
            renderPlayer(graphics, itemStack, startX, startY, player);
        }

        graphics.pose().popPose();
    }

    private static void renderPlayer(GuiGraphics graphics, ItemStack itemStack, int x, int y, Player player) {
        EquipmentSlot slot;
        if (itemStack.getItem() instanceof Equipable equipable)
            slot = equipable.getEquipmentSlot();
        else if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof Equipable equipable) {
            slot = equipable.getEquipmentSlot();
            if (!slot.isArmor())
                slot = EquipmentSlot.OFFHAND;
        } else
            return;

        NonNullList<ItemStack> inventory = slot.isArmor() ? player.getInventory().armor : player.getInventory().offhand;
        ItemStack originalArmor = player.getItemBySlot(slot);
        int slotId = slot == EquipmentSlot.OFFHAND ? 0 : slot.getIndex();
        inventory.set(slotId, itemStack);
        renderEntity(graphics, x, y, player);
        inventory.set(slotId, originalArmor);
    }

    private static void renderArmorTrim(Player player, GuiGraphics graphics, ItemStack itemStack, int x, int y) {
        NonNullList<ItemStack> armor = player.getInventory().armor;

        ItemStack[] originalArmor = new ItemStack[4];
        for (int i = 0; i < armor.size(); i++) originalArmor[i] = armor.get(i).copy();

        var wrapperLookup = player.level().registryAccess();
        var optMaterial = getCachedTrimMaterial(player.level());
        var optPattern = TrimPatterns.getFromTemplate(wrapperLookup, itemStack);

        if (optMaterial == null || optPattern.isEmpty()) return;
        var armorTrim = new ArmorTrim(optMaterial, optPattern.get());
        for (int i = 0; i < armor.size(); i++) {
            ItemStack armorStack = armor.get(i);
            if (armorStack.isEmpty()) {
                var defaultStack = DEFAULT_ARMOR[i].getDefaultInstance();
                ArmorTrim.setTrim(wrapperLookup, defaultStack, armorTrim);
                armor.set(i, defaultStack);
            } else {
                ArmorTrim.setTrim(wrapperLookup, armorStack, armorTrim);
            }
        }
        renderEntity(graphics, x, y, player);
        renderMaterial(optMaterial, x, y, graphics);
        for (int i = 0; i < armor.size(); i++) {
            armor.set(i, originalArmor[i]);
        }
    }

    private static void renderEntity(GuiGraphics graphics, int x, int y, Player player) {
        float bodyYaw = player.getVisualRotationYInDegrees();
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        float headYaw = player.getYHeadRot();

        float yRot = (float) Math.atan(80 * Math.cos(ArmortipUtil.ticks / 64.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * ArmortipUtil.ticks / 64.0F) / 40.0F);

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        player.setYBodyRot(200.0F + yRot * 10.0F);
        player.setYRot(180.0F + yRot * 20.0F);
        player.setXRot(-xRot * 10.0F);
        player.setYHeadRot(player.getYRot());

        InventoryScreen.renderEntityInInventory(graphics, x - MARGIN + WIDTH / 2, y + HEIGHT, ArmortipRenderer.SIZE, quaternionf, quaternionf2, player);

        player.setYBodyRot(bodyYaw);
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(headYaw);
    }

    private static void renderMaterial(Holder<TrimMaterial> material, int x, int y, GuiGraphics graphics) {
        var item = material.value().ingredient().value();
        if (item == Items.AIR) return;

        graphics.pose().pushPose();
        graphics.pose().translate(x + ArmortipRenderer.WIDTH - MARGIN, y, 0);
        graphics.pose().scale(0.5F, 0.5F, 0.5F);
        graphics.renderItem(item.getDefaultInstance(), 0, 0);
        graphics.pose().popPose();
    }

    private static Holder<TrimMaterial> getCachedTrimMaterial(Level level) {
        if (MATERIAL_CACHE == null) {
            var registryAccess = level.registryAccess();
            var registry = registryAccess.registryOrThrow(Registries.TRIM_MATERIAL);
            MATERIAL_CACHE = registry.holders().toList();
        }
        return MATERIAL_CACHE.get((ArmortipUtil.ticks / 40) % MATERIAL_CACHE.size());
    }
}
