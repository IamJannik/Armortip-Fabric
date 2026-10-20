package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.config.ArmortipConfig;
import net.bmjo.armortip.gui.tooltip.EmptyTooltip;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Optional;

@Mixin(GuiGraphics.class)
public abstract class GuiMixin {
    @ModifyVariable(method = "renderTooltipInternal", at = @At("HEAD"), argsOnly = true)
    private List<ClientTooltipComponent> armortip$addItemtips(List<ClientTooltipComponent> lines) {
        return ArmortipUtil.modifyTooltips(lines);
    }

    @ModifyVariable(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V", at = @At("HEAD"), argsOnly = true)
    private Optional<TooltipComponent> armortip$addaItemtips(Optional<TooltipComponent> optional) {
        if (ArmortipConfig.isEnabled.get() && FabricLoader.getInstance().isModLoaded("legendarytooltips"))
            return Optional.of(new EmptyTooltip());
        return optional;
    }
}
