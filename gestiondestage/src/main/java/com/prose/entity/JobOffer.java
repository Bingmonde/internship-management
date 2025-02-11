package com.prose.entity;

import com.prose.entity.users.Employeur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JobOffer_ID")
    private Long id;

    @Column(name = "Titre")
    private String titre;

    @Column(name = "DateDebut")
    private String dateDebut;

    @Column(name = "DateFin")
    private String dateFin;

    @Column(name = "Lieu")
    private String lieu;

    @Column(name = "TypeTravail")
    private String typeTravail;

    @Column(name = "NombreStagiaire")
    private int nombreStagiaire;

    @Column(name = "TauxHoraire")
    private double tauxHoraire;

    @Column(name = "Description")
    private String description;

    @Column(name = "WeeklyHours")
    private double weeklyHours;

    @Column(name = "DayScheduleFrom")
    private LocalTime dayScheduleFrom;

    @Column(name = "DayScheduleTo")
    private LocalTime dayScheduleTo;

    @OneToOne
    private PDFDocu pdfDocu;

    @ManyToOne
    @JoinColumn(name = "Employeur_ID")
    private Employeur employeur;

    @Column(name = "IsApproved")
    private boolean isApproved;

    @Column(name = "IsActivated")
    private boolean isActivated;

    @OneToMany(orphanRemoval = true)
    private List<JobOfferApplication> jobOfferApplications = new ArrayList<>();

    @ManyToOne
    private Session session;
}
