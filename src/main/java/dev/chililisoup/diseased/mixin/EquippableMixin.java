package dev.chililisoup.diseased.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.chililisoup.diseased.item.SlotBlocker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Equippable.class)
public abstract class EquippableMixin {
    @ModifyExpressionValue(
            method = "swapWithEquipmentSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;has(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/component/DataComponentType;)Z")
    )
    private boolean preventSwap(boolean original, @Local(ordinal = 1) ItemStack itemStack) {
        return itemStack.getItem() instanceof SlotBlocker || original;
    }

    @ModifyExpressionValue(
            method = "swapWithEquipmentSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isCreative()Z")
    )
    private boolean preventCreativeSwap(boolean original, @Local(ordinal = 1) ItemStack itemStack) {
        return !(itemStack.getItem() instanceof SlotBlocker) && original;
    }
}
