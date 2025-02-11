package com.prose.service.dto;

import com.prose.entity.*;

public record EvaluationEmployerDTO(
        Long employerId,
        StageNumber stageNumber,
        String firstMonthHours,
        String secondMonthHours,
        String thirdMonthHours,

        StagePreference preferredStage,
        NumberOfInterns numberOfInterns,
        WillingnessToRehire willingToRehire,

        String schedule1Start,
        String schedule1End,
        String schedule2Start,
        String schedule2End,
        String schedule3Start,
        String schedule3End,

        String comments,
        String observations,
        String signatureDTO,
        String date,

        EvaluationOption tasksMetExpectations,
        EvaluationOption integrationSupport,
        EvaluationOption supervisionSufficient,
        EvaluationOption workEnvironment,
        EvaluationOption workClimate,
        EvaluationOption accessibleTransport,
        EvaluationOption salaryInteresting,
        EvaluationOption communicationWithSupervisor,
        EvaluationOption equipmentAdequate,
        EvaluationOption workloadAcceptable

) {
    public static EvaluationEmployerDTO toDTO(EvaluationEmployer evaluationEmployer) {
        if (evaluationEmployer == null) {
            return null;
        }
        return new EvaluationEmployerDTO(
                evaluationEmployer.getEmployeur().getId(),
                evaluationEmployer.getStageNumber(),
                evaluationEmployer.getFirstMonthHours(),
                evaluationEmployer.getSecondMonthHours(),
                evaluationEmployer.getThirdMonthHours(),

                evaluationEmployer.getPreferredStage(),
                evaluationEmployer.getNumberOfInterns(),
                evaluationEmployer.getWillingToRehire(),

                evaluationEmployer.getSchedule1Start(),
                evaluationEmployer.getSchedule1End(),
                evaluationEmployer.getSchedule2Start(),
                evaluationEmployer.getSchedule2End(),
                evaluationEmployer.getSchedule3Start(),
                evaluationEmployer.getSchedule3End(),

                evaluationEmployer.getComments(),
                evaluationEmployer.getObservations(),
                null,
                evaluationEmployer.getDate(),

                evaluationEmployer.getTasksMetExpectations(),
                evaluationEmployer.getIntegrationSupport(),
                evaluationEmployer.getSupervisionSufficient(),
                evaluationEmployer.getWorkEnvironment(),
                evaluationEmployer.getWorkClimate(),
                evaluationEmployer.getAccessibleTransport(),
                evaluationEmployer.getSalaryInteresting(),
                evaluationEmployer.getCommunicationWithSupervisor(),
                evaluationEmployer.getEquipmentAdequate(),
                evaluationEmployer.getWorkloadAcceptable()
        );
    }



}
