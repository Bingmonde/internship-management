package com.prose.service.dto;

import com.prose.entity.*;

import java.time.LocalDateTime;

public record InternshipOfferDTO(
        Long id,
        LocalDateTime expirationDate,
        ApprovalStatus studentsApprovalStatus,
        LocalDateTime confirmationDate,
        JobOfferApplicationDTO jobOfferApplicationDTO,
        ContractSignatureDTO contractSignatureDTO

){
    public static InternshipOfferDTO toDTO(InternshipOffer internshipOffer) {
        return new InternshipOfferDTO(
                internshipOffer.getId(),
                internshipOffer.getExpirationDate(),
                internshipOffer.getConfirmationStatus(),
                internshipOffer.getConfirmationDate(),
                JobOfferApplicationDTO.toDTO(internshipOffer.getJobOfferApplication()),
                ContractSignatureDTO.toDTO(internshipOffer.getContract())
        );
    }
}
