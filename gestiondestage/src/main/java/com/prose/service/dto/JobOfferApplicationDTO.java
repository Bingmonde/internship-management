package com.prose.service.dto;

import com.prose.entity.ApprovalStatus;
import com.prose.entity.JobOfferApplication;
import com.prose.entity.users.Employeur;

import java.time.LocalDateTime;

public record JobOfferApplicationDTO(Long id,
                                     JobOfferDTO jobOffer,
                                     CurriculumVitaeDTO CV,
                                     boolean active,
                                     LocalDateTime applicationDate,
                                     ApprovalStatus approvalStatus) {

    public Long getStudentId() {
        return CV().studentDTO().id();
    }

    public Long getJobOfferId() {
        return jobOffer.id();
    }

    public static JobOfferApplicationDTO toDTO(JobOfferApplication jobOfferApplication) {
        return new JobOfferApplicationDTO(jobOfferApplication.getId(),
                JobOfferDTO.toDTO(jobOfferApplication.getJobOffer()),
                CurriculumVitaeDTO.toDTO(jobOfferApplication.getCurriculumVitae()),
                jobOfferApplication.isActive(),
                jobOfferApplication.getApplicationDate(),
                (jobOfferApplication.getInternshipOffer() != null) ? jobOfferApplication.getInternshipOffer().getConfirmationStatus(): ApprovalStatus.NOT_APPLICABLE);
    }
}
