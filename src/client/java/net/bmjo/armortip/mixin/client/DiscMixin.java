package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.config.ArmortipConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecordItem.class)
public abstract class DiscMixin {
    @Shadow
    public abstract MutableComponent getDisplayName();

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void modifyDisc(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        if (!ArmortipConfig.isEnabled.get() || !ArmortipConfig.showDisc.get()) return;
        var desc = this.getDisplayName();
        var parts = desc.getString().split(" - ");
        if (parts.length != 2) return;

        tooltip.add(Component.literal(parts[1]).withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal(parts[0]).withStyle(ChatFormatting.GRAY));

        ci.cancel();
    }
}
