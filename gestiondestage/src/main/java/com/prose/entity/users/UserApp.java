package com.prose.entity.users;

import com.prose.entity.users.auth.Credentials;
import com.prose.entity.users.auth.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public abstract class UserApp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private Credentials credentials;

    public String getEmail() {
        return credentials.getUsername();
    }

    public String getPassword() {
        return credentials.getPassword();
    }

    public Role getRole() {
        return credentials.getRole();
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return credentials.getAuthorities();
    }

    private long NotificationIdCutoff;

    private UserState state;

    protected void setCredentials(String email, String password, Role role) {
        credentials = Credentials.builder().email(email).password(password).role(role).build();
    }

    @Column(name = "Adresse")
    private String adresse;

    @Column(name = "Telephone")
    private String telephone;
}
