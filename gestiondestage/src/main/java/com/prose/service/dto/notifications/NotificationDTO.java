package com.prose.service.dto.notifications;

import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.UserApp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationDTO {
    private long id;
    private String type;
    private Long sessionId;
    private Long referenceId;
    private boolean isRead;

    public NotificationDTO(long id, NotificationCode type, Long sessionId, Long referenceId, boolean isRead) {
        this.setId(id);
        this.setType(type.toString());
        this.setSessionId(sessionId);
        this.setReferenceId(referenceId);
        this.setRead(isRead);
    }

    public static NotificationDTO toDTO(Notification notification, UserApp user) {
        Long sessionId = null;
        if (notification.getSession() != null) {
            sessionId = notification.getSession().getId();
        }

        boolean seen = notification.getSeenBy().contains(user);

        return new NotificationDTO(notification.getId(),notification.getCode(),sessionId, notification.getReferenceId(), seen);
    }

}
