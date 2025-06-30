package dev.chililisoup.diseased.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chililisoup.diseased.item.SlotBlocker;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityEquipment.class)
public abstract class EntityEquipmentMixin {
    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity preventSlotBlockerDrop(LivingEntity instance, ItemStack itemStack, boolean bl, boolean bl2, Operation<ItemEntity> original) {
        return (itemStack.getItem() instanceof SlotBlocker) ? null : original.call(instance, itemStack, bl, bl2);
    }

    @Redirect(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityEquipment;clear()V"))
    private void preventSlotBlockerClear(EntityEquipment instance) {
        instance.items.replaceAll((equipmentSlot, itemStack) ->
                (itemStack.getItem() instanceof SlotBlocker) ? itemStack : ItemStack.EMPTY
        );
    }
}
