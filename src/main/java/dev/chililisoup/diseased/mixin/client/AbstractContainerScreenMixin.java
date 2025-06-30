package dev.chililisoup.diseased.mixin.client;

import dev.chililisoup.diseased.item.SlotBlocker;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void setNotHovering(Slot slot, double d, double e, CallbackInfoReturnable<Boolean> cir) {
        if (slot.getItem().getItem() instanceof SlotBlocker)
            cir.setReturnValue(false);
    }
}
