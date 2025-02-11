package com.prose.service.dto;

import com.prose.entity.Contract;

import java.time.LocalDateTime;

public record ContractSignatureDTO(LocalDateTime employer, LocalDateTime student, LocalDateTime manager) {
    public static ContractSignatureDTO toDTO(Contract contract) {
        if (contract == null) {
            return null;
        }
        return new ContractSignatureDTO(contract.getEmployerSign(),contract.getStudentSign(),contract.getManagerSign());
    }
}
