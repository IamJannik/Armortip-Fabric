package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class EntityTooltip implements ClientTooltipComponent {
    private static final int SIZE = 30;
    private final ItemStack itemStack;

    public EntityTooltip(ItemStack itemStack) {
        this.itemStack = itemStack;
    }


    @Override
    public int getHeight(@NonNull Font font) {
        return SIZE + ArmortipUtil.PADDING;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return SIZE;
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        var player = Minecraft.getInstance().player;
        if (player != null) renderEgg(player, x, y, graphics);
    }

    private void renderEgg(Player player, int x, int y, GuiGraphicsExtractor gui) {
        var type = itemStack.get(DataComponents.ENTITY_DATA);
        if (type == null) return;
        var entity = ArmortipUtil.getCachedEntity(player.level(), type.type());
        ArmortipUtil.renderEntity(entity, x, y, SIZE, gui);
    }
}
