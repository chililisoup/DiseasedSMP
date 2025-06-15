package dev.chililisoup.diseased.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chililisoup.diseased.PlayerDiseases;
import net.minecraft.client.gui.Gui;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @WrapOperation(method = "renderAirBubbles", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"
    ))
    private boolean invertFluid(Player player, TagKey<Fluid> tagKey, Operation<Boolean> original) {
        boolean base = original.call(player, tagKey);

        if (PlayerDiseases.GILLED.foundOn(player))
            return !base;

        return base;
    }
}
