package com.prose.entity.notification;

import com.prose.entity.Discipline;
import com.prose.entity.Session;
import com.prose.entity.users.UserApp;
import com.prose.entity.users.auth.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    protected NotificationCode code;

    protected Long referenceId;

    @ManyToOne
    protected Session session;

    @ManyToMany
    private List<UserApp> seenBy = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Role filter;

    @Enumerated(EnumType.STRING)
    private Discipline discipline;

    private Long userId; //TODO : Je veux avoir un lien avec l'utilisateur, mais je veux éviter d'avoir trop de requêtes =(

    public void viewNotification(UserApp userApp) {
        if (seenBy == null) {
            seenBy = new ArrayList<>();
        }
        if (!seenBy.contains(userApp)) {
            seenBy.add(userApp);
        }
    }

}