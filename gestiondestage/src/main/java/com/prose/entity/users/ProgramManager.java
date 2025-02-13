package com.prose.entity.users;

import com.prose.entity.users.UserState;
import com.prose.entity.users.auth.Credentials;
import com.prose.entity.users.auth.Role;
import com.prose.entity.users.UserApp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ProgramManager extends UserApp {

    public ProgramManager(Long id, String firstName, String lastName, String email, String mdp, String adresse, String telephone) {
        super(id, Credentials.builder().email(email).password(mdp).role(Role.PROGRAM_MANAGER).build(),0, UserState.DEFAULT,adresse,telephone);
        this.nom = lastName;
        this.prenom = firstName;
    }

    public void setCredentials(String email, String password) {
        super.setCredentials(email,password,Role.PROGRAM_MANAGER);
    }

    @Column(name = "Nom")
    private String nom;

    @Column(name = "Prenom")
    private String prenom;
    @Column(name = "Courriel")
    private String courriel;

}
