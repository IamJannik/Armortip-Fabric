package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.config.ArmortipConfig;
import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HangingEntityItem.class)
public abstract class PaintingMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void skip(ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        if (ArmortipConfig.isEnabled.get() && ArmortipConfig.showPainting.get() && ArmortipUtil.getPaintingVariant(itemStack) != null)
            ci.cancel();
    }
}
