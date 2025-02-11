package com.prose.service.dto;

import com.prose.entity.ProgramManager;

public record ProgramManagerDTO(
        Long id,
        String nom,
        String prenom,
        String courriel,
        String adresse,
        String telephone) {

    public static ProgramManagerDTO toDTO(ProgramManager programManager) {
        return new ProgramManagerDTO(
                programManager.getId(),
                programManager.getNom(),
                programManager.getPrenom(),
                programManager.getCredentials().getEmail(),
                programManager.getAdresse(),
                programManager.getTelephone());
    }
}
