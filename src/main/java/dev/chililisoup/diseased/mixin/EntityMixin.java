package dev.chililisoup.diseased.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chililisoup.diseased.PlayerDiseases;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @WrapOperation(method = "move", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;updateEntityMovementAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V"
    ))
    private void bounce(Block instance, BlockGetter blockGetter, Entity entity, Operation<Void> original) {
        if (entity instanceof Player player) {
            if (PlayerDiseases.BOUNCY.foundOn(player)) {
                Vec3 vec3 = entity.getDeltaMovement();
                if (vec3.y < 0.0) {
                    entity.setDeltaMovement(vec3.x, -vec3.y, vec3.z);
                    return;
                }
            }
        }

        original.call(instance, blockGetter, entity);
    }
}
