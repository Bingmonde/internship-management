package com.prose.entity.users;

import com.prose.entity.Discipline;
import com.prose.entity.users.auth.Credentials;
import com.prose.entity.users.auth.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@DiscriminatorValue("S")
@NoArgsConstructor
public class Student extends UserApp {

    public Student(Long id, String firstName, String lastName, String email, String mdp, String adresse, String telephone, Discipline discipline) {
        super(id, Credentials.builder().email(email).password(mdp).role(Role.STUDENT).build(),0,UserState.DEFAULT,adresse,telephone);
        this.nom = lastName;
        this.prenom = firstName;
        this.discipline = discipline;
    }

    public void setCredentials(String email, String password) {
        super.setCredentials(email,password,Role.STUDENT);
    }

    @Column(name = "Nom")
    private String nom;

    @Column(name = "Prenom")
    private String prenom;

    @Column(name = "LastConnection")
    private LocalDateTime lastConnection;

    @Column(name = "Discipline")
    @Enumerated(EnumType.STRING)
    private Discipline discipline;

    public String getFullName() {
        return prenom + " " + nom;
    }


}
