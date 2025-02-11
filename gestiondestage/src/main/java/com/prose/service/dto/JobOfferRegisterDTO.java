package com.prose.service.dto;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public record JobOfferRegisterDTO(
        String titre,
        String dateDebut,
        String dateFin,
        String lieu,
        String typeTravail,
        int nombreStagiaire,
        double tauxHoraire,
        double weeklyHours,
        LocalTime dailyScheduleFrom,
        LocalTime dailyScheduleTo,
        String description
) {
}
