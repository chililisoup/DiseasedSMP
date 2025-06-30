package dev.chililisoup.diseased.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chililisoup.diseased.item.SlotBlocker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean preventSlotBlockerDrop(ItemStack itemStack, Operation<Boolean> original) {
        return itemStack.getItem() instanceof SlotBlocker || original.call(itemStack);
    }
}
