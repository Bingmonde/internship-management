package com.prose.entity.embedded;

import com.prose.entity.EvaluationOption;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class QualityOfWorkEvaluation {

    @Enumerated(EnumType.STRING)
    private EvaluationOption quality_a; // respecter les mandats;

    @Enumerated(EnumType.STRING)
    private EvaluationOption quality_b; // porter attention aux détails;

    @Enumerated(EnumType.STRING)
    private EvaluationOption quality_c; // vérifier son travail;

    @Enumerated(EnumType.STRING)
    private EvaluationOption quality_d; // se perfectionner;

    @Enumerated(EnumType.STRING)
    private EvaluationOption quality_e; // faire une bonne analyse des problèmes;
}
