package dev.chililisoup.diseased.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.chililisoup.diseased.PlayerDiseases;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Unique
    protected boolean isSunBurnTick() {
        Player player = (Player) (Object) this;

        if (!player.level().isBrightOutside() || player.level().isClientSide)
            return false;

        if (player.isInWaterOrRain() || player.isInPowderSnow || player.wasInPowderSnow)
            return false;

        BlockPos blockPos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (!player.level().canSeeSky(blockPos))
            return false;

        float light = player.getLightLevelDependentMagicValue();
        return light > 0.5F && player.getRandom().nextFloat() * 30.0F < (light - 0.4F) * 2.0F;
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void diseaseStep(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (PlayerDiseases.UNDEAD.foundOn(player) && this.isSunBurnTick())
            player.igniteForSeconds(8.0F);
    }

    @WrapMethod(method = "getDefaultDimensions")
    private EntityDimensions adjustDefaultHitbox(Pose pose, Operation<EntityDimensions> original) {
        EntityDimensions base = original.call(pose);

        Player player = (Player) (Object) this;
        if (PlayerDiseases.TALL.foundOn(player))
            return base.scale(1, 1.125F);

        return base;
    }
}
