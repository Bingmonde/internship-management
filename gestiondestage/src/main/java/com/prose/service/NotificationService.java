package com.prose.service;

import com.prose.entity.Discipline;
import com.prose.entity.notification.*;
import com.prose.entity.users.Student;
import com.prose.entity.users.UserApp;
import com.prose.entity.users.UserState;
import com.prose.entity.users.auth.Role;
import com.prose.repository.JobOfferRepository;
import com.prose.repository.StudentRepository;
import com.prose.repository.UserAppRepository;
import com.prose.repository.notification.NotificationRepository;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.NotificationNotFoundException;
import com.prose.service.dto.UserDTO;
import com.prose.service.dto.notifications.NotificationDTO;
import com.prose.service.dto.notifications.NotificationRootDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final UserAppRepository userAppRepository;
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final JobOfferRepository jobOfferRepository;

    public NotificationService(UserAppRepository userAppRepository, NotificationRepository notificationRepository, StudentRepository studentRepository, JobOfferRepository jobOfferRepository) {
        this.userAppRepository = userAppRepository;
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.jobOfferRepository = jobOfferRepository;
    }

    private void markAsRead(UserApp userApp, Notification notification) {
        notification.viewNotification(userApp);
        notificationRepository.save(notification);
    }

    @Transactional
    public void tryFindViewNotification(UserDTO userDTO, NotificationCode notificationCode, Long refId) {
        UserApp userApp = userAppRepository.getReferenceById(userDTO.id());
        List<Notification> notificationList = notificationRepository.findUnseenByNotificationCodeAndRefId(notificationCode,refId, userApp);
        for (Notification notification : notificationList) {
            markAsRead(userApp,notification);
        }
    }

    public void viewNotification(UserDTO userDTO, Long id) throws NotificationNotFoundException {
        UserApp userApp = userAppRepository.getReferenceById(userDTO.id()); // =/
        Notification notification = notificationRepository.findById(id).orElseThrow(NotificationNotFoundException::new);
        markAsRead(userApp,notification);
    }

    public NotificationRootDTO getUnreadNotifications(String email, Long sessionId) {
        NotificationRootDTO notificationRootDTO = new NotificationRootDTO();
        UserApp userApp = userAppRepository.findUserAppByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        Discipline discipline = null;
        Optional<Student> student = studentRepository.findById(userApp.getId());
        if (student.isPresent()) {
            discipline = student.get().getDiscipline();
        }

        List<Notification> notificationList = notificationRepository.getUnreadNotificationsAfterId(userApp.getNotificationIdCutoff(), userApp,discipline,userApp.getRole(), userApp.getId(), sessionId);

        String stateStr = (userApp.getState() != null) ? userApp.getState().toString() : UserState.DEFAULT.toString();
        notificationRootDTO.setPage(setSpecialState(userApp,sessionId,stateStr));

        notificationRootDTO.setNotifications(notificationList.stream().map(notification -> NotificationDTO.toDTO(notification,userApp)).toList());

        return notificationRootDTO;
    }

    private String setSpecialState(UserApp userApp, Long sessionId, String currentState) {
        if (userApp.getRole().equals(Role.EMPLOYEUR)) {
            if (jobOfferRepository.notApprovedInSession(sessionId)) {
                return UserState.MISSING_JOBOFFER.toString();
            }
        }
        return currentState;
    }

    public void addNotification(Notification notification) {
        if (notification.getSeenBy() == null) {
            notification.setSeenBy(new ArrayList<>());
        }
        notificationRepository.save(notification);
    }


    public long getLatestId() {
        Optional<Notification> optional = notificationRepository.findTopByOrderByIdDesc();
        if (optional.isEmpty()) {
            return -1;
        }
        return optional.get().getId();
    }
}
