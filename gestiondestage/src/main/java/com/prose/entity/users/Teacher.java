package com.prose.entity.users;

import com.prose.entity.Discipline;
import com.prose.entity.users.auth.Credentials;
import com.prose.entity.users.auth.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class Teacher extends UserApp {
    public Teacher(Long id, String firstName, String lastName, String email, String mdp, String adresse, String telephone) {
        super(id, Credentials.builder().email(email).password(mdp).role(Role.TEACHER).build(),0,UserState.DEFAULT,adresse,telephone);
        this.nom = lastName;
        this.prenom = firstName;
    }

    public void setCredentials(String email, String password) {
        super.setCredentials(email,password,Role.TEACHER);
    }

    @Column(name = "Nom")
    private String nom;

    @Column(name = "Prenom")
    private String prenom;

    @Column(name = "Discipline")
    @Enumerated(EnumType.STRING)
    private Discipline discipline;


}
