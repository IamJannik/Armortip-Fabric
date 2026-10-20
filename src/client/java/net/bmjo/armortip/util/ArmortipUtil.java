package net.bmjo.armortip.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import org.jetbrains.annotations.Nullable;

public class ArmortipUtil {
    public static int ticks;

    public static boolean isTipItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof Equipable || itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof Equipable || itemStack.getItem() instanceof SmithingTemplateItem;
    }

    public static boolean shouldExtend() {
        ItemStack focusedItem = getFocusedItem();
        return focusedItem != null && isTipItem(focusedItem);
    }

    @Nullable
    public static ItemStack getFocusedItem() {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<? extends AbstractContainerMenu> handledScreen && handledScreen instanceof ISlotScreen islotScreen)
            if (handledScreen.getMenu().getCarried().isEmpty() && islotScreen.getHoveredSlot() != null && islotScreen.getHoveredSlot().hasItem())
                return islotScreen.getHoveredSlot().getItem();
        return null;
    }

    public static void tick() {
        ticks++;
        ticks %= (int) (2 * Math.PI * 300.0F);
    }
}
