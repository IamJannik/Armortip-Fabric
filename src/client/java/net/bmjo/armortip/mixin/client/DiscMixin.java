package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.config.ArmortipConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(JukeboxPlayable.class)
public class DiscMixin {
    @Shadow
    @Final
    private Holder<JukeboxSong> song;

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private void modifyDisc(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components, CallbackInfo ci) {
        if (!ArmortipConfig.isEnabled.get() || !ArmortipConfig.showDisc.get()) return;
        var desc = song.value().description();
        var parts = desc.getString().split(" - ");
        if (parts.length != 2) return;

        consumer.accept(Component.literal(parts[1]).withStyle(ChatFormatting.WHITE));
        consumer.accept(Component.literal(parts[0]).withStyle(ChatFormatting.GRAY));

        ci.cancel();
    }
}
