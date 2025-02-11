// src/main/java/com/prose/entity/EvaluationEmployer.java

package com.prose.entity;

import com.prose.entity.users.Employeur;
import com.prose.entity.users.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class EvaluationEmployer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Employeur employeur;

    @ManyToOne
    private Student student;

    @ManyToOne
    private JobOffer jobOffer;
    @Enumerated(EnumType.STRING)
    private StageNumber stageNumber;
    private String firstMonthHours;
    private String secondMonthHours;
    private String thirdMonthHours;

    @Enumerated(EnumType.STRING)
    private StagePreference preferredStage;

    @Enumerated(EnumType.STRING)
    private NumberOfInterns numberOfInterns;

    @Enumerated(EnumType.STRING)
    private WillingnessToRehire willingToRehire;

    private String schedule1Start;
    private String schedule1End;
    private String schedule2Start;
    private String schedule2End;
    private String schedule3Start;
    private String schedule3End;

    private String comments;
    private String observations;
    private byte[] teacherSignImage;
    // todo LocalDateTime
    private String date;

    // Other evaluation fields
    @Enumerated(EnumType.STRING)
    private EvaluationOption tasksMetExpectations;

    @Enumerated(EnumType.STRING)
    private EvaluationOption integrationSupport;

    @Enumerated(EnumType.STRING)
    private EvaluationOption supervisionSufficient;

    @Enumerated(EnumType.STRING)
    private EvaluationOption workEnvironment;

    @Enumerated(EnumType.STRING)
    private EvaluationOption workClimate;

    @Enumerated(EnumType.STRING)
    private EvaluationOption accessibleTransport;

    @Enumerated(EnumType.STRING)
    private EvaluationOption salaryInteresting;

    @Enumerated(EnumType.STRING)
    private EvaluationOption communicationWithSupervisor;

    @Enumerated(EnumType.STRING)
    private EvaluationOption equipmentAdequate;

    @Enumerated(EnumType.STRING)
    private EvaluationOption workloadAcceptable;


}
