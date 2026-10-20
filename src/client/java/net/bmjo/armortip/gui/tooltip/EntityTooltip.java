package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class EntityTooltip implements ClientTooltipComponent {
    private static final int SIZE = 30;
    private final ItemStack itemStack;

    public EntityTooltip(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public int getHeight() {
        return SIZE + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(Font font) {
        return SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        var player = Minecraft.getInstance().player;
        if (player != null) renderEgg(player, x, y, graphics);
    }

    private void renderEgg(Player player, int x, int y, GuiGraphics gui) {
        if (!(itemStack.getItem() instanceof SpawnEggItem spawnEgg)) return;
        var type = spawnEgg.getType(itemStack.getTag());
        var entity = ArmortipUtil.getCachedEntity(player.level(), type);
        ArmortipUtil.renderEntity(entity, x, y, SIZE, gui);
    }
}
