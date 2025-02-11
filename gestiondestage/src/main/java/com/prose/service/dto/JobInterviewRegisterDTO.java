package com.prose.service.dto;

import java.time.LocalDateTime;

public record JobInterviewRegisterDTO (
        String interviewDate,
        String interviewType,
        String interviewLocationOrLink,
        Long jobOfferApplicationId
) {
}
