package com.prose.service.dto;

public record TeacherRegisterDTO(
      String nom,
      String prenom,
      String courriel,
      String adresse,
      String telephone,
      String mdp,
      String discipline
) {
}
