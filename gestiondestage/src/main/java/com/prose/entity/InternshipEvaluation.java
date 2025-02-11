package com.prose.entity;

import com.prose.entity.users.Employeur;
import com.prose.entity.users.Teacher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class InternshipEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Teacher teacher;

    @OneToOne(cascade = CascadeType.ALL)
    private InternshipOffer internshipOffer;

    @OneToOne
    private EvaluationEmployer evaluationEmployer;

    @OneToOne
    private EvaluationIntern evaluationIntern;

    public Session getSession(){
        return internshipOffer.getSession();
    }


}
