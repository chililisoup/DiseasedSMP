package dev.chililisoup.diseased.interfaceinjects.client;

import dev.chililisoup.diseased.PlayerDiseases;

public interface DiseasedPlayerRenderState {
    default PlayerDiseases.PlayerDiseaseStatus diseasedSMP$getStatus() {
        return new PlayerDiseases.PlayerDiseaseStatus();
    }

    default void diseasedSMP$setStatus(PlayerDiseases.PlayerDiseaseStatus status) {}
}
