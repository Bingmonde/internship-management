package com.prose.repository.notification;

import com.prose.entity.Discipline;
import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.UserApp;
import com.prose.entity.users.auth.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        select n from Notification n
        where n.id > :cutoff
        and (n.discipline is null OR n.discipline = :d)
        and (n.filter is null OR n.filter = :r)
        and (n.userId is null OR n.userId = :uid)
        and (n.session is null OR n.session.id = :s)
        and (:u not member of n.seenBy)
""")
    List<Notification> getUnreadNotificationsAfterId(@Param("cutoff") long cutoffId, @Param("u") UserApp userApp, @Param("d") Discipline discipline, @Param("r") Role role, @Param("uid") Long userId, @Param("s") Long sessionId);

    @Query("""
        select n from Notification n
        where n.code = :code
        and n.referenceId = :refid
        and (:u not member of n.seenBy)
""")
    List<Notification> findUnseenByNotificationCodeAndRefId(@Param("code")NotificationCode notificationCode, @Param("refid") Long refid, @Param("u") UserApp userApp);



    Optional<Notification> findTopByOrderByIdDesc();
}
