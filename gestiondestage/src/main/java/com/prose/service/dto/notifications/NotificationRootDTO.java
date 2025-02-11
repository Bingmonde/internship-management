package com.prose.service.dto.notifications;

import com.prose.entity.users.UserState;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class NotificationRootDTO {

    private List<NotificationDTO> notifications;
    private String page;

    public NotificationRootDTO() {
        notifications = new ArrayList<>();
        page = UserState.DEFAULT.toString();
    }

}
