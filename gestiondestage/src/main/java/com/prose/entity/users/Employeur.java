package com.prose.entity.users;

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
public class Employeur extends UserApp {

    @Column(name = "NomCompagnie")
    private String nomCompagnie;

    @Column(name = "contact_person")
    private String contactPerson;

//    @Column(name = "address")
//    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "fax")
    private String fax;  // 传真

    public Employeur(Long id, String nomCompagnie, String contactPerson, String email, String mdp, String adresse, String city, String postalCode, String telephone, String fax) {
        super(id, Credentials.builder().email(email).password(mdp).role(Role.EMPLOYEUR).build(), 0, UserState.DEFAULT, adresse, telephone);
        this.nomCompagnie = nomCompagnie;
        this.contactPerson = contactPerson;
        this.city = city;
        this.postalCode = postalCode;
        this.fax = fax;
    }

    public void setCredentials(String email, String password) {
        super.setCredentials(email, password, Role.EMPLOYEUR);
    }
}

