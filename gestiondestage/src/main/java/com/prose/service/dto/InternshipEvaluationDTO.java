package com.prose.service.dto;

import com.prose.entity.InternshipEvaluation;

public record InternshipEvaluationDTO(
        long id,
        TeacherDTO teacher,
        InternshipOfferDTO internshipOffer,
        EvaluationEmployerDTO evaluationEmployer,
        EvaluationInternDTO evaluationIntern

) {

    public static InternshipEvaluationDTO toDTO(InternshipEvaluation internshipEvaluation) {
        return new InternshipEvaluationDTO(
                internshipEvaluation.getId(),
                TeacherDTO.toDTO(internshipEvaluation.getTeacher()),
                InternshipOfferDTO.toDTO(internshipEvaluation.getInternshipOffer()),
                EvaluationEmployerDTO.toDTO(internshipEvaluation.getEvaluationEmployer()),
                EvaluationInternDTO.toDTO(internshipEvaluation.getEvaluationIntern())
        );
    }
}
