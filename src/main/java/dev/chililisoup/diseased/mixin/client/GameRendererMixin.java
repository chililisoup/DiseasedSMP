package dev.chililisoup.diseased.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.chililisoup.diseased.Diseased;
import dev.chililisoup.diseased.PlayerDiseases;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderLevel", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
            shift = At.Shift.AFTER
    ))
    private void setupDiseasedCamera(DeltaTracker deltaTracker, CallbackInfo ci, @Local PoseStack poseStack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (PlayerDiseases.CRICK.foundOn(player)) {
            poseStack.mulPose(Axis.ZP.rotation(Diseased.CRICK_AMOUNT));
        }
    }
}
