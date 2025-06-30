package dev.chililisoup.diseased.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chililisoup.diseased.item.SlotBlocker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
    @WrapOperation(
            method = "slotClicked",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;set(Lnet/minecraft/world/item/ItemStack;)V")
    )
    private void preventClearBlockers(Slot slot, ItemStack itemStack, Operation<Void> original) {
        if (!(slot.getItem().getItem() instanceof SlotBlocker))
            original.call(slot, itemStack);
    }

    @WrapOperation(
            method = "slotClicked",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleCreativeModeItemAdd(Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 0)
    )
    private void preventServerClearBlockers(MultiPlayerGameMode gameMode, ItemStack itemStack, int i, Operation<Void> original) {
        Minecraft minecraft = ((CreativeModeInventoryScreen) (Object) this).minecraft;
        if (
                minecraft == null ||
                minecraft.player == null ||
                !(minecraft.player.inventoryMenu.getSlot(i).getItem().getItem() instanceof SlotBlocker)
        ) {
            original.call(gameMode, itemStack, i);
        }
    }
}
