package com.prose.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saison")
    private String season;

    @Column(name = "annee_session")
    private String year;

    @Column(name = "date_debut")
    private LocalDateTime startDate;

    @Column(name = "date_fin")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "session")
    private List<JobOffer> jobOffers;

    public void addJobOffer(JobOffer jobOffer) {
        jobOffers.add(jobOffer);
    }

    public void getNextSeason(){
        switch (this.getSeason()) {
            case "Automne" : {
                this.setSeason("Hiver");
                setYear(String.valueOf(Integer.parseInt(this.getYear())+1));
                this.setStartDate(LocalDateTime.of(Integer.parseInt(this.getYear()), 1, 1, 0, 0));
                this.setEndDate(LocalDateTime.of(Integer.parseInt(this.getYear()), 5, 31, 23, 59));
                break;
            }
            case "Hiver" : {
                this.setSeason("Été");
                setYear(String.valueOf(Integer.parseInt(this.getYear())));
                this.setStartDate(LocalDateTime.of(Integer.parseInt(this.getYear()), 6, 1, 0, 0));
                this.setEndDate(LocalDateTime.of(Integer.parseInt(this.getYear()), 8, 31, 23, 59));
                break;
            }
            case "Été" : {
                this.setSeason("Automne");
                setYear(String.valueOf(Integer.parseInt(this.getYear())));
                this.setStartDate(LocalDateTime.of(Integer.parseInt(this.getYear()), 9, 1, 0, 0));
                this.setEndDate(LocalDateTime.of(Integer.parseInt(this.getYear()), 12, 30, 23, 59));
                break;
            }
            default:{
                break;
            }

        }
    }

    public boolean isPriorTo(Session currentSession){
        switch (this.getSeason()){
            case "Hiver" : {
                if (currentSession.getSeason().equals("Été") && Integer.parseInt(currentSession.getYear()) == Integer.parseInt(this.getYear())){
                    return true;
                }
                break;
            }
            case "Été" : {
                if (currentSession.getSeason().equals("Automne") && Integer.parseInt(currentSession.getYear()) == Integer.parseInt(this.getYear())){
                    return true;
                }
                break;
            }
            case "Automne" : {
                if (currentSession.getSeason().equals("Hiver") && Integer.parseInt(currentSession.getYear()) == Integer.parseInt(this.getYear()) + 1){
                    return true;
                }
                break;
            }
        }
        return false;
    }
}
