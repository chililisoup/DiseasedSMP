package dev.chililisoup.diseased.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chililisoup.diseased.PlayerDiseases;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @WrapOperation(method = "travelInAir", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;getFriction()F"
    ))
    private float adjustBlockFriction(Block instance, Operation<Float> original) {
        float base = original.call(instance);
        if (base >= 0.98F) return base;

        if ((LivingEntity) (Object) this instanceof Player player && PlayerDiseases.SLIPPERY.foundOn(player))
            return 0.98F;

        return base;
    }

    @Inject(method = "isInvertedHealAndHarm", at = @At("RETURN"), cancellable = true)
    private void invertHeal(CallbackInfoReturnable<Boolean> cir) {
        if ((LivingEntity) (Object) this instanceof Player player && PlayerDiseases.UNDEAD.foundOn(player))
            cir.setReturnValue(true);
    }

    @WrapOperation(method = "baseTick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"
    ))
    private boolean invertFluid(LivingEntity entity, TagKey<Fluid> tagKey, Operation<Boolean> original) {
        boolean base = original.call(entity, tagKey);

        if (entity instanceof Player player) {
            if (PlayerDiseases.GILLED.foundOn(player))
                return !base;
        }

        return base;
    }
}
