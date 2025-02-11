package com.prose.service.dto;

public record ProgramManagerRegisterDTO(
        String nom,
        String prenom,
        String courriel,
        String adresse,
        String telephone,
        String mdp) {
}
