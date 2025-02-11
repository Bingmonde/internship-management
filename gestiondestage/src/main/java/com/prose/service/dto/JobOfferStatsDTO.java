package com.prose.service.dto;

public record JobOfferStatsDTO(
        long totalNbApplications,
    long nbInternsNeeded,
    long nbInternshipOffersSent,
    long nbInternshipOffersAccepted
) {
}
