package dev.chililisoup.diseased.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.diseased.Diseased;
import dev.chililisoup.diseased.PlayerDiseases;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow protected M model;

    /*
    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V",
            ordinal = 0,
            shift = At.Shift.AFTER
    ))
    private void adjustVisualScale(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (livingEntityRenderState instanceof PlayerRenderState playerRenderState) {
            PlayerDiseases.PlayerDiseaseStatus status = playerRenderState.diseasedSMP$getStatus();

            if (PlayerDiseases.TALL.foundOn(status)) {
                float inverse = 1.0F / 1.125F;
                poseStack.scale(inverse, inverse, inverse);
            }
        }
    }
    */

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)V",
            shift = At.Shift.AFTER
    ))
    private void setupDiseasedAnim(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (livingEntityRenderState instanceof PlayerRenderState playerRenderState && this.model instanceof HumanoidModel<?> humanoidModel) {
            PlayerDiseases.PlayerDiseaseStatus status = playerRenderState.diseasedSMP$getStatus();

            if (PlayerDiseases.CRICK.foundOn(status))
                humanoidModel.head.zRot -= Diseased.CRICK_AMOUNT;

            if (PlayerDiseases.TALL.foundOn(status))
                humanoidModel.head.yScale *= 1.5F;
        }
    }
}
