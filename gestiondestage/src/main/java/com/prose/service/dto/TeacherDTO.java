package com.prose.service.dto;


import com.prose.entity.Discipline;
import com.prose.entity.users.Teacher;

public record TeacherDTO(Long id,
                         String nom,
                         String prenom,
                         String courriel,
                         String adresse,
                         String telephone,
                         DisciplineTranslationDTO discipline)

{

    public static TeacherDTO toDTO(Teacher teacher) {
        return new TeacherDTO(
                teacher.getId(),
                teacher.getNom(),
                teacher.getPrenom(),
                teacher.getCredentials().getEmail(),
                teacher.getAdresse(),
                teacher.getTelephone(),
                teacher.getDiscipline().getTranslation());
    }
}
