// src/main/java/com/prose/entity/embedded/ProductivityEvaluation.java

package com.prose.entity.embedded;

import com.prose.entity.EvaluationOption;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ProductivityEvaluation {

    @Enumerated(EnumType.STRING)
    private EvaluationOption production_a; // planifier et organiser son travail de façon efficace

    @Enumerated(EnumType.STRING)
    private EvaluationOption production_b; // comprendre rapidement les directives relatives à son travail

    @Enumerated(EnumType.STRING)
    private EvaluationOption production_c; // maintenir un rythme de travail soutenu

    @Enumerated(EnumType.STRING)
    private EvaluationOption production_d; // établir ses priorités

    @Enumerated(EnumType.STRING)
    private EvaluationOption production_e; // respecter ses échéanciers
}
