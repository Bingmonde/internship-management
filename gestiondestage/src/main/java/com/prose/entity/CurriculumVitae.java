package com.prose.entity;

import com.prose.entity.users.Student;
import com.prose.entity.users.Teacher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
public class CurriculumVitae {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private PDFDocu pdfDocu;

    @ManyToOne
    @JoinColumn(name = "Etudiant_ID")
    private Student student;
    @ManyToOne
    @JoinColumn(name = "Teacher_ID")
    private Teacher teacher;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DateHeureAjout")
    private LocalDateTime dateHeureAjout;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    public long getStudentIdLong() {
        return student.getId();
    }

    public String getName() {
        return pdfDocu.getFileName();
    }

}