package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.bmjo.armortip.util.AttributeComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.Equippable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArmorTooltip implements ClientTooltipComponent {
    private final List<AttributeComponent> widgets = new ArrayList<>();
    private final ItemStack itemStack;
    private final @Nullable Equippable equippable;

    public ArmorTooltip(ItemStack itemStack) {
        this.itemStack = itemStack;
        var player = Minecraft.getInstance().player;

        this.equippable = this.itemStack.get(DataComponents.EQUIPPABLE);

        var components = new ArrayList<Component>();
        itemStack.addAttributeTooltips(components::add, itemStack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT), player);

        for (var component : components) {
            var widget = AttributeComponent.from(component);
            if (widget == null) continue;
            widgets.add(widget);
        }
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return Math.max(getSize(), ArmortipUtil.LINE_HEIGHT * widgets.size()) + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        var width = getSize();
        for (var widget : widgets) {
            width = Math.max(width, getSize() + widget.getWidth(font));
        }
        return width;
    }

    private int getSize() {
        if (equippable == null) return 0;
        return switch (equippable.slot().getType()) {
            case HAND, HUMANOID_ARMOR -> 40;
            case ANIMAL_ARMOR, SADDLE -> 30;
        };
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        if (equippable == null) return;
        var player = Minecraft.getInstance().player;
        if (player != null) renderArmor(x, y, graphics, player);

        int j = 0;
        int x0 = switch (equippable.slot().getType()) {
            case HAND, HUMANOID_ARMOR -> x + 3 * getSize() / 4;
            case ANIMAL_ARMOR, SADDLE -> x + getSize();
        };
        for (var widget : widgets) {
            widget.render(font, x0, y + (j * ArmortipUtil.LINE_HEIGHT), graphics);
            j++;
        }
    }

    private void renderArmor(int x, int y, GuiGraphicsExtractor graphics, Player player) {
        if (equippable == null) return;
        switch (equippable.slot().getType()) {
            case HAND, HUMANOID_ARMOR -> this.renderPlayer(equippable.slot(), x, y, graphics, player);
            case ANIMAL_ARMOR, SADDLE -> this.renderAnimal(equippable.slot(), equippable, x, y, graphics, player);
        }
    }

    private void renderPlayer(EquipmentSlot slot, int x, int y, GuiGraphicsExtractor graphics, Player player) {
        var originalStack = player.getItemBySlot(slot);
        player.setItemSlot(slot, this.itemStack);
        ArmortipUtil.renderEntity(player, x - getSize() / 6, y, getSize(), graphics);
        player.setItemSlot(slot, originalStack);
    }

    private void renderAnimal(EquipmentSlot slot, Equippable equippableComponent, int x, int y, GuiGraphicsExtractor graphics, Player player) {
        var entities = equippableComponent.allowedEntities();
        if (entities.isEmpty() || entities.get().size() <= 0) return;
        var animalType = entities.get().get(0).value();
        var animal = ArmortipUtil.getCachedEntity(player.level(), animalType);
        if (animal == null) return;
        ItemStack originalStack = animal.getItemBySlot(slot);
        animal.setItemSlot(slot, this.itemStack);
        ArmortipUtil.renderEntity(animal, x, y, getSize(), graphics);
        animal.setItemSlot(slot, originalStack);
    }
}
