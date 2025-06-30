package dev.chililisoup.diseased.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chililisoup.diseased.PlayerDiseases;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
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

    @Unique
    protected boolean teleport(Player player) {
        if (player.level().isClientSide() || !player.isAlive())
            return false;

        double d = player.getX() + (player.getRandom().nextDouble() - 0.5) * 64.0;
        double e = player.getY() + (player.getRandom().nextInt(64) - 32);
        double f = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 64.0;
        return teleport(player, d, e, f);
    }

    @Unique
    private boolean teleport(Player player, double d, double e, double f) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(d, e, f);

        while (mutableBlockPos.getY() > player.level().getMinY() && !player.level().getBlockState(mutableBlockPos).blocksMotion()) {
            mutableBlockPos.move(Direction.DOWN);
        }

        BlockState blockState = player.level().getBlockState(mutableBlockPos);
        if (!blockState.blocksMotion() || blockState.getFluidState().is(FluidTags.WATER))
            return false;

        if (player.randomTeleport(d, e, f, true)) {
            player.level().gameEvent(GameEvent.TELEPORT, player.position(), GameEvent.Context.of(player));
            if (!player.isSilent()) {
                player.level().playSound(null, player.xo, player.yo, player.zo, SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);
                player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }

            return true;
        }

        return false;
    }

    @WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean diseaseHurt(Player player, ServerLevel level, DamageSource damageSource, float f, Operation<Boolean> original) {
        boolean base = original.call(player, level, damageSource, f);

        if (PlayerDiseases.ENDER.foundOn(player)) {
            if (damageSource.getDirectEntity() instanceof AbstractThrownPotion potion) {
                if (potion.getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
                    for (int i = 0; i < 64; i++) if (teleport(player)) return true;
                }
            } else if (damageSource.is(DamageTypeTags.IS_DROWNING) && player.getRandom().nextInt(40) == 0) {
                for (int i = 0; i < 64; i++) if (teleport(player)) return base;
            }
        }

        return base;
    }
}
