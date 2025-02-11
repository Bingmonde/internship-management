package com.prose.service.dto;

import com.prose.entity.ApprovalStatus;
import com.prose.entity.Discipline;
import com.prose.entity.users.Student;

import java.io.Serializable;

public record StudentDTO(Long id,
                         String nom,
                         String prenom,
                         String courriel,
                         String adresse,
                         String telephone,
                         DisciplineTranslationDTO discipline) implements Serializable {

    public static StudentDTO toDTO(Student etudiant) {
        return new StudentDTO(
                etudiant.getId(),
                etudiant.getNom(),
                etudiant.getPrenom(),
                etudiant.getCredentials().getEmail(),
                etudiant.getAdresse(),
                etudiant.getTelephone(),
                etudiant.getDiscipline().getTranslation());
    }

}
