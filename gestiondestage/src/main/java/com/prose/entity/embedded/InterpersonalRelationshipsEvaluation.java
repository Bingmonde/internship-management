package com.prose.entity.embedded;

import com.prose.entity.EvaluationOption;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class InterpersonalRelationshipsEvaluation {

    @Enumerated(EnumType.STRING)
    private EvaluationOption interPersonal_a; // établir contact avec les autres;

    @Enumerated(EnumType.STRING)
    private EvaluationOption interPersonal_b; // contribuer activement au travail d'équipe;

    @Enumerated(EnumType.STRING)
    private EvaluationOption interPersonal_c; // s'adapte facliement à l'entreprise;

    @Enumerated(EnumType.STRING)
    private EvaluationOption interPersonal_d; // accepte les critiques constructives;

    @Enumerated(EnumType.STRING)
    private EvaluationOption interPersonal_e; // respectueux envers les autres;

    @Enumerated(EnumType.STRING)
    private EvaluationOption interPersonal_f; // écoute attentive des autres;


}
