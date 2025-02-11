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
public class PersonalSkillsEvaluation {

    @Enumerated(EnumType.STRING)
    private EvaluationOption personalStills_a; // démontre de l'intérêt et motivation pour son travail

    @Enumerated(EnumType.STRING)
    private EvaluationOption personalStills_b; // exprime clairement ses idées

    @Enumerated(EnumType.STRING)
    private EvaluationOption personalStills_c; // prend des initiatives

    @Enumerated(EnumType.STRING)
    private EvaluationOption personalStills_d; // travaille de façon sécuritaire

    @Enumerated(EnumType.STRING)
    private EvaluationOption personalStills_e; // sens de responsabilité

    @Enumerated(EnumType.STRING)
    private EvaluationOption personalStills_f; // ponctualité et assiduité
}
