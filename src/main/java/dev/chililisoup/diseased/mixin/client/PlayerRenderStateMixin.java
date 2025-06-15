package dev.chililisoup.diseased.mixin.client;

import dev.chililisoup.diseased.PlayerDiseases;
import dev.chililisoup.diseased.interfaceinjects.client.DiseasedPlayerRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public abstract class PlayerRenderStateMixin implements DiseasedPlayerRenderState {
    @Unique PlayerDiseases.PlayerDiseaseStatus status;

    @Override
    public PlayerDiseases.PlayerDiseaseStatus diseasedSMP$getStatus() {
        return status;
    }

    @Override
    public void diseasedSMP$setStatus(PlayerDiseases.PlayerDiseaseStatus status) {
        this.status = status;
    }
}
