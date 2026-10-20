package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.config.ArmortipConfig;
import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    private static boolean shouldShowInTooltip(int hideFlags, ItemStack.TooltipPart part) {
        throw new AssertionError();
    }

    @Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shouldShowInTooltip(ILnet/minecraft/world/item/ItemStack$TooltipPart;)Z"))
    private boolean skipAttributes(int hideFlags, ItemStack.TooltipPart part) {
        if (part == ItemStack.TooltipPart.MODIFIERS) return false;
        if (ArmortipUtil.shouldSkipVanillaAttributes((ItemStack) (Object) this)) return false;
        return shouldShowInTooltip(hideFlags, part);
    }

    @Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z"))
    private boolean showVanillaDurability(ItemStack instance) {
        if (ArmortipConfig.isEnabled.get() && ArmortipConfig.showDurability.get()) return false;
        return instance.isDamaged();
    }
}
