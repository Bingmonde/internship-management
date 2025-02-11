package com.prose.service.dto;

import java.time.LocalDate;
import java.util.List;
public record JobPermissionRegisterDTO (
        List<String> disciplines,
        List<Long> studentIds,
        LocalDate expirationDate,
        boolean isApproved
){}

