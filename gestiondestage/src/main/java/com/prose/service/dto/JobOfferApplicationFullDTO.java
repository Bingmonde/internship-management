package com.prose.service.dto;

import java.util.List;

public record JobOfferApplicationFullDTO(
        JobOfferApplicationDTO jobOfferApplication,
        List<JobInterviewDTO> interviewOffer,
        InternshipOfferDTO internshipOffer) {
}
