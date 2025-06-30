package dev.chililisoup.diseased.mixin;

import dev.chililisoup.diseased.item.SlotBlocker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "mayPickup", at = @At("RETURN"), cancellable = true)
    private void preventPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (((Slot) (Object) this).getItem().getItem() instanceof SlotBlocker)
            cir.setReturnValue(false);
    }
}
