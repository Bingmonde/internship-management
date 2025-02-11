package com.prose.entity;

import com.prose.entity.users.Student;
import com.prose.service.Exceptions.JobApplicationNotFoundException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class InternshipOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_before")
    private LocalDateTime expirationDate;

    @Column(name = "approval_status")
    @Enumerated(EnumType.STRING)
    private ApprovalStatus confirmationStatus;

    @Column(name = "student_answer_date")
    private LocalDateTime confirmationDate;

    @OneToOne
    private JobOfferApplication jobOfferApplication;

    @OneToOne
    @Cascade(CascadeType.ALL)
    private Contract contract;

    @OneToOne
    private InternshipEvaluation internshipEvaluation;

    public Student getStudent() {
        if(jobOfferApplication.getCurriculumVitae() == null) return null;
        return jobOfferApplication.getCurriculumVitae().getStudent();
    }

    public Long getEmployeurId() throws JobApplicationNotFoundException {
        if(jobOfferApplication.getJobOffer() == null) throw new JobApplicationNotFoundException();
        if (jobOfferApplication.getJobOffer().getEmployeur() == null) throw new JobApplicationNotFoundException();
        return jobOfferApplication.getJobOffer().getEmployeur().getId();
    }

    public Long getStudentId() {
        return jobOfferApplication.getStudentId();
    }

    public JobOffer getJobOffer() {
        return jobOfferApplication.getJobOffer();
    }

    public Session getSession(){
        return jobOfferApplication.getJobOffer().getSession();
    }

    public boolean contractIsSigned(){
        if(this.getContract() == null){
            return false;
        }
        else {
            return getContract().getEmployerSign() != null && getContract().getStudentSign() != null && getContract().getManagerSign() != null;
        }
    }
}