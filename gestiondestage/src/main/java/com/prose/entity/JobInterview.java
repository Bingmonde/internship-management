package com.prose.entity;

import com.prose.entity.users.Employeur;
import com.prose.entity.users.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Setter
@Getter
public class JobInterview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "Intervie_Date")
    private LocalDateTime interviewDate;

    @Column(name = "Interview_Type")
    @Enumerated(EnumType.STRING)
    private InterviewType interviewType;

    @Column(name = "Interview_Location_Or_Link")
    private String interviewLocationOrLink;

    @ManyToOne
    @JoinColumn(name = "JobOfferApplication_ID")
    private JobOfferApplication jobOfferApplication;

    @Column(name = "Confirmed")
    private boolean isConfirmedByStudent;

    @Column(name = "Confirmation_Date")
    private LocalDateTime confirmationDate;

    @Column(name = "Creation_Date")
    private LocalDateTime creationDate;

    @Column(name = "Cancelled_Date")
    private LocalDateTime cancelledDate;

    public Long getStudentId() {
        return jobOfferApplication.getStudentId();
    }

    public JobOffer getJobOffer() {
        return jobOfferApplication.getJobOffer();
    }

    public Student getStudent() {
        if(jobOfferApplication == null || jobOfferApplication.getCurriculumVitae() == null) {
            return null;
        }
        return jobOfferApplication.getCurriculumVitae().getStudent();
    }

    public Employeur getEmployeur() {
        return getJobOffer().getEmployeur();
    }
}

