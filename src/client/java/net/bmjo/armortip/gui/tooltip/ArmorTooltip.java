package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.bmjo.armortip.util.AttributeComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArmorTooltip implements ClientTooltipComponent {
    private final List<AttributeComponent> widgets;
    private final ItemStack itemStack;
    private final @Nullable Equipable equipable;
    private final boolean horseArmor;

    public ArmorTooltip(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.equipable = Equipable.get(itemStack);
        this.horseArmor = itemStack.getItem() instanceof HorseArmorItem;
        this.widgets = AttributeComponent.fromItem(itemStack);
    }

    @Override
    public int getHeight() {
        return Math.max(getSize(), ArmortipUtil.LINE_HEIGHT * widgets.size()) + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(Font font) {
        var width = getSize();
        for (var widget : widgets) {
            width = Math.max(width, getSize() + widget.getWidth(font));
        }
        return width;
    }

    private int getSize() {
        if (equipable != null) return 40;
        if (horseArmor) return 30;
        return 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (equipable == null && !horseArmor) return;
        var player = Minecraft.getInstance().player;
        if (player != null) renderArmor(x, y, graphics, player);

        int j = 0;
        int x0 = equipable != null ? x + 3 * getSize() / 4 : x + getSize();
        for (var widget : widgets) {
            widget.render(font, x0, y + (j * ArmortipUtil.LINE_HEIGHT), graphics);
            j++;
        }
    }

    private void renderArmor(int x, int y, GuiGraphics graphics, Player player) {
        if (equipable != null) this.renderPlayer(equipable.getEquipmentSlot(), x, y, graphics, player);
        else this.renderHorse(x, y, graphics, player);
    }

    private void renderPlayer(EquipmentSlot slot, int x, int y, GuiGraphics graphics, Player player) {
        var originalStack = ArmortipUtil.setPlayerItem(player, slot, this.itemStack);
        ArmortipUtil.renderEntity(player, x - getSize() / 6, y, getSize(), graphics);
        ArmortipUtil.setPlayerItem(player, slot, originalStack);
    }

    private void renderHorse(int x, int y, GuiGraphics graphics, Player player) {
        var horse = ArmortipUtil.getCachedEntity(player.level(), EntityType.HORSE);
        if (horse == null) return;
        ItemStack originalStack = horse.getItemBySlot(EquipmentSlot.CHEST);
        horse.setItemSlot(EquipmentSlot.CHEST, this.itemStack);
        ArmortipUtil.renderEntity(horse, x, y, getSize(), graphics);
        horse.setItemSlot(EquipmentSlot.CHEST, originalStack);
    }
}
