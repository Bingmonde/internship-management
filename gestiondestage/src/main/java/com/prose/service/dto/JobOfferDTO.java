package com.prose.service.dto;

import com.prose.entity.JobOffer;
import lombok.Builder;

import java.time.LocalTime;

public record JobOfferDTO(
        Long id,
        String titre,
        String dateDebut,
        String dateFin,
        String lieu,
        String typeTravail,
        int nombreStagiaire,
        double tauxHoraire,
        double weeklyHours,
        LocalTime dayScheduleFrom,
        LocalTime dayScheduleTo,

        String description,
        PDFDocuUploadDTO pdfDocu,
        boolean isApproved,
        boolean isActivated,
        EmployeurDTO employeurDTO
) {
    @Builder
    public JobOfferDTO {
    }

    public static JobOfferDTO toDTO(JobOffer jobOffer) {
        return JobOfferDTO.builder()
                .id(jobOffer.getId())
                .titre(jobOffer.getTitre())
                .dateDebut(jobOffer.getDateDebut())
                .dateFin(jobOffer.getDateFin())
                .lieu(jobOffer.getLieu())
                .typeTravail(jobOffer.getTypeTravail())
                .nombreStagiaire(jobOffer.getNombreStagiaire())
                .tauxHoraire(jobOffer.getTauxHoraire())
                .weeklyHours(jobOffer.getWeeklyHours())
                .dayScheduleFrom(jobOffer.getDayScheduleFrom())
                .dayScheduleTo(jobOffer.getDayScheduleTo())
                .description(jobOffer.getDescription())
                .pdfDocu(PDFDocuUploadDTO.toDTO(jobOffer.getPdfDocu()))
                .isApproved(jobOffer.isApproved())
                .isActivated(jobOffer.isActivated())
                .employeurDTO(EmployeurDTO.toDTO(jobOffer.getEmployeur()))
                .build();
    }
}