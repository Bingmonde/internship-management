
package com.prose.service.dto;

import com.prose.entity.EvaluationIntern;
import com.prose.entity.embedded.*;
import com.prose.entity.OverallAppreciation;
import com.prose.entity.WillingnessToRehire;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EvaluationInternDTO {
    private long id;

    // Identification Fields
    private Long studentId;
    private Long employeurId;
    private String studentName;
    private String program;
    private String companyName;
    private String supervisorName;
    private String telephone;

    // Evaluation Sections
    private ProductivityEvaluation productivityEvaluation;
    private QualityOfWorkEvaluation qualityOfWorkEvaluation;
    private InterpersonalRelationshipsEvaluation interpersonalRelationshipsEvaluation;
    private PersonalSkillsEvaluation personalSkillsEvaluation;

    // Comments
    private String productivityComments;
    private String qualityOfWorkComments;
    private String interpersonalRelationshipsComments;
    private String personalSkillsComments;

    // Overall Appreciation
    private OverallAppreciation overallAppreciation;
    private String overallComments;

    private boolean evaluationDiscussedWithIntern;
    private double supervisionHoursPerWeek;
    private WillingnessToRehire willingnessToRehire;
    private String technicalTrainingComments;

    // Signature Fields
    private String name;
    private String function;
    private String employerSignature;
    private LocalDate date;
    private String returnFormToName;
    private String returnFormToEmail;

    public static EvaluationInternDTO toDTO(EvaluationIntern evaluationIntern) {
        if (evaluationIntern == null) {
            return null;
        }
        EvaluationInternDTO dto = new EvaluationInternDTO();
        dto.setId(evaluationIntern.getId());
        if(evaluationIntern.getStudent() != null){
            dto.setStudentId(evaluationIntern.getStudent().getId());
            dto.setStudentName(evaluationIntern.getStudent().getNom() + " " + evaluationIntern.getStudent().getPrenom());
        }
        dto.setEmployeurId(evaluationIntern.getEmployeur().getId());
        dto.setProgram(evaluationIntern.getProgram());
        dto.setCompanyName(evaluationIntern.getCompanyName());
        dto.setSupervisorName(evaluationIntern.getSupervisorName());
        dto.setTelephone(evaluationIntern.getTelephone());
        dto.setProductivityEvaluation(evaluationIntern.getProductivityEvaluation());
        dto.setQualityOfWorkEvaluation(evaluationIntern.getQualityOfWorkEvaluation());
        dto.setInterpersonalRelationshipsEvaluation(evaluationIntern.getInterpersonalRelationshipsEvaluation());
        dto.setPersonalSkillsEvaluation(evaluationIntern.getPersonalSkillsEvaluation());
        dto.setProductivityComments(evaluationIntern.getProductivityComments());
        dto.setQualityOfWorkComments(evaluationIntern.getQualityOfWorkComments());
        dto.setInterpersonalRelationshipsComments(evaluationIntern.getInterpersonalRelationshipsComments());
        dto.setPersonalSkillsComments(evaluationIntern.getPersonalSkillsComments());
        dto.setOverallAppreciation(evaluationIntern.getOverallAppreciation());
        dto.setOverallComments(evaluationIntern.getOverallComments());
        dto.setEvaluationDiscussedWithIntern(evaluationIntern.isEvaluationDiscussedWithIntern());
        dto.setSupervisionHoursPerWeek(evaluationIntern.getSupervisionHoursPerWeek());
        dto.setWillingnessToRehire(evaluationIntern.getWillingnessToRehire());
        dto.setTechnicalTrainingComments(evaluationIntern.getTechnicalTrainingComments());
        dto.setName(evaluationIntern.getName());
        dto.setFunction(evaluationIntern.getFunction());
        dto.setDate(evaluationIntern.getDate());
        dto.setReturnFormToName(evaluationIntern.getReturnFormToName());
        dto.setReturnFormToEmail(evaluationIntern.getReturnFormToEmail());
        return dto;

    }

}

















