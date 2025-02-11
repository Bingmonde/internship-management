package com.prose.entity;

import com.prose.entity.users.Employeur;
import com.prose.entity.users.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class JobPermission {
    @Id
    @GeneratedValue
    @Column(name = "JobPermission_ID")
    private Long id;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "JobOffer_ID")
    private JobOffer jobOffer;

    @Column(name = "JobOffer_Disciplines")
    private String disciplines;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "JobPermission_Student",
            joinColumns = @JoinColumn(name = "JobPermission_ID"),
            inverseJoinColumns = @JoinColumn(name = "Etudiant_ID")
    )
    private List<Student> students;
    private LocalDate expirationDate;

    public Employeur getEmployeur() {
        return jobOffer.getEmployeur();
    }


}
