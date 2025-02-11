package com.prose.entity;

import com.prose.entity.*;
import com.prose.entity.embedded.InterpersonalRelationshipsEvaluation;
import com.prose.entity.embedded.PersonalSkillsEvaluation;
import com.prose.entity.embedded.ProductivityEvaluation;
import com.prose.entity.embedded.QualityOfWorkEvaluation;
import com.prose.entity.users.Employeur;
import com.prose.entity.users.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class EvaluationIntern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Relationships
    @ManyToOne
    private Student student;

    @ManyToOne
    private Employeur employeur;

    // Identification Fields
    private String program;
    private String companyName;
    private String supervisorName;
    private String telephone;

    // Evaluation Sections
    @Embedded
    private ProductivityEvaluation productivityEvaluation;

    @Embedded
    private QualityOfWorkEvaluation qualityOfWorkEvaluation;

    @Embedded
    private InterpersonalRelationshipsEvaluation interpersonalRelationshipsEvaluation;

    @Embedded
    private PersonalSkillsEvaluation personalSkillsEvaluation;

    // Comments
    @Lob
    private String productivityComments;

    @Lob
    private String qualityOfWorkComments;

    @Lob
    private String interpersonalRelationshipsComments;

    @Lob
    private String personalSkillsComments;

    // Overall Appreciation
    @Enumerated(EnumType.STRING)
    private OverallAppreciation overallAppreciation;

    @Lob
    private String overallComments;

    private boolean evaluationDiscussedWithIntern;

    private double supervisionHoursPerWeek;

    @Enumerated(EnumType.STRING)
    private WillingnessToRehire willingnessToRehire;

    @Lob
    private String technicalTrainingComments;

    // Signature Fields
    private String name;
    private String function;
    private byte[] employerSignature;

    private LocalDate date;
    private String returnFormToName;
    private String returnFormToEmail;
}
