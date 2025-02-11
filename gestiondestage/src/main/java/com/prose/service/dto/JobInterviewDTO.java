package com.prose.service.dto;

import com.prose.entity.JobInterview;

import java.time.LocalDateTime;

public record JobInterviewDTO (
        Long id,
        LocalDateTime interviewDate,
        String interviewType,
        String interviewLocationOrLink,
        JobOfferApplicationDTO jobOfferApplication,
        boolean isConfirmedByStudent,
        LocalDateTime confirmationDate,
        LocalDateTime creationDate,
        LocalDateTime cancelledDate

) {
    public static JobInterviewDTO toDTO(JobInterview jobInterview) {
        return new JobInterviewDTO(jobInterview.getId(),
                jobInterview.getInterviewDate(),
                jobInterview.getInterviewType().toString(),
                jobInterview.getInterviewLocationOrLink(),
                JobOfferApplicationDTO.toDTO(jobInterview.getJobOfferApplication()),
                jobInterview.isConfirmedByStudent(),
                jobInterview.getConfirmationDate(),
                jobInterview.getCreationDate(),
                jobInterview.getCancelledDate());
    }

}
