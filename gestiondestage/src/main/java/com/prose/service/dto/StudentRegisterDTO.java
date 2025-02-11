package com.prose.service.dto;

public record StudentRegisterDTO(String nom,
                                 String prenom,
                                 String courriel,
                                 String adresse,
                                 String telephone,
                                 String mdp,
                                 String discipline) {
}
