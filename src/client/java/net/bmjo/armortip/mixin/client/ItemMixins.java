package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.config.ArmortipConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemMixins {
    @Mixin(HangingEntityItem.class)
    public static class PaintingMixin {
        @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
        private void skip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag, CallbackInfo ci) {
            if (ArmortipConfig.isEnabled.get()) ci.cancel();
        }
    }

    @Mixin(SmithingTemplateItem.class)
    public static class TrimMixin {
        @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
        private void skip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag, CallbackInfo ci) {
            if (ArmortipConfig.isEnabled.get()) ci.cancel();
        }
    }
}