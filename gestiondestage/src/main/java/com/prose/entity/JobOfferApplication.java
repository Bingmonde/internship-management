package com.prose.entity;
import com.prose.entity.users.Employeur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class JobOfferApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="JobOffer_ID")
    private Long id;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "application_date")
    private LocalDateTime applicationDate;

    @ManyToOne
    private JobOffer jobOffer;

    @ManyToOne
    private CurriculumVitae curriculumVitae;

    @OneToMany(mappedBy = "jobOfferApplication")
    private List<JobInterview> jobInterviews = new ArrayList<>();;

    @OneToOne
    private InternshipOffer internshipOffer;

    public Long getStudentId() {
        return curriculumVitae.getStudentIdLong();
    }

    public Employeur getEmployeur() {
        return jobOffer.getEmployeur();
    }
}