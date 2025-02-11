package com.prose.gestiondestage.service;

import com.prose.entity.Discipline;
import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.*;
import com.prose.entity.users.auth.Role;
import com.prose.repository.JobOfferRepository;
import com.prose.repository.StudentRepository;
import com.prose.repository.UserAppRepository;
import com.prose.repository.notification.NotificationRepository;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.NotificationNotFoundException;
import com.prose.service.NotificationService;
import com.prose.service.dto.UserDTO;
import com.prose.service.dto.notifications.NotificationRootDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    public NotificationRepository notificationRepository;

    public UserAppRepository userAppRepository;

    public NotificationService notificationService;
    public StudentRepository studentRepository;
    public JobOfferRepository jobOfferRepository;


    @BeforeEach
    public void beforeEach() {
        notificationRepository = mock(NotificationRepository.class);
        userAppRepository = mock(UserAppRepository.class);
        studentRepository = mock(StudentRepository.class);
        jobOfferRepository = mock(JobOfferRepository.class);

        notificationService = new NotificationService(userAppRepository,notificationRepository,studentRepository,jobOfferRepository);
    }

    @Test
    public void addNotificationTest() {
        // Arrange
        Notification notification = Notification.builder()
                .id(5L)
                .build();

        ArgumentCaptor<Notification> argumentCaptor = ArgumentCaptor.forClass(Notification.class);
        // Act
        notificationService.addNotification(notification);
        // Assert
        verify(notificationRepository).save(argumentCaptor.capture());

        Notification notification1 = argumentCaptor.getValue();
        assertThat(notification1.getId()).isEqualTo(notification.getId());
        assertThat(notification1.getSeenBy()).isNotNull();
    }

    @Test
    public void addNotificationFilledTest() {
        // Arrange
        Notification notification = Notification.builder()
                .id(5L)
                .seenBy(new ArrayList<>())
                .build();

        ArgumentCaptor<Notification> argumentCaptor = ArgumentCaptor.forClass(Notification.class);
        // Act
        notificationService.addNotification(notification);
        // Assert
        verify(notificationRepository).save(argumentCaptor.capture());

        Notification notification1 = argumentCaptor.getValue();
        assertThat(notification1).isEqualTo(notification);
    }

    @Test
    public void getUnreadNotificationsUserNotFoundTest() {
        // Arrange
        String email = "haha@xd.lol";

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> notificationService.getUnreadNotifications(email, 5L))
                .isInstanceOf(UserNotFoundException.class); // Assert

    }

    @Test
    public void getUnreadNotificationsUserEmptyTest() {
        // Arrange
        long notificationId = 3;
        Teacher teacher = new Teacher();
        teacher.setId(2L);
        teacher.setCredentials("email","mdp");
        teacher.setNotificationIdCutoff(notificationId);
        teacher.setState(UserState.DEFAULT);

        when(userAppRepository.findUserAppByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(notificationRepository.getUnreadNotificationsAfterId(anyLong(),any(),any(),any(),anyLong(),anyLong())).thenReturn(List.of());
        // Act
        NotificationRootDTO notificationDTO = notificationService.getUnreadNotifications(teacher.getEmail(),5L);
        // Assert
        assertThat(notificationDTO.getNotifications().isEmpty()).isTrue();
        assertThat(notificationDTO.getPage()).isEqualTo(teacher.getState().toString());
    }

    @Test
    public void getUnreadNotificationsUserTest() {
        // Arrange
        long notificationId = 3;
        Teacher teacher = new Teacher();
        teacher.setId(2L);
        teacher.setCredentials("email","mdp");
        teacher.setNotificationIdCutoff(notificationId);
        teacher.setState(UserState.DEFAULT);

        Notification userNotification = Notification.builder()
                .id(notificationId+1)
                .userId(teacher.getId())
                .code(NotificationCode.TEST_STACKABLE)
                .seenBy(new ArrayList<>())
                .build();

        Notification userNotification2 = Notification.builder()
                .id(notificationId+3)
                .userId(teacher.getId())
                .code(NotificationCode.TEST_STACKABLE)
                .seenBy(new ArrayList<>())
                .build();

        Notification userNotification3 = Notification.builder()
                .id(notificationId+2)
                .userId(teacher.getId())
                .code(NotificationCode.TEST_STACKABLE)
                .seenBy(new ArrayList<>())
                .build();

        when(userAppRepository.findUserAppByEmail(teacher.getEmail())).thenReturn(Optional.of(teacher));
        when(notificationRepository.getUnreadNotificationsAfterId(anyLong(),any(),any(),any(),anyLong(),anyLong())).thenReturn(List.of(userNotification,userNotification2,userNotification3));

        // Act
        NotificationRootDTO notificationDTO = notificationService.getUnreadNotifications(teacher.getEmail(),5L);
        // Assert
        assertThat(notificationDTO.getNotifications().size()).isEqualTo(3);
        assertThat(notificationDTO.getNotifications().get(0).getId()).isEqualTo(userNotification.getId());
        assertThat(notificationDTO.getNotifications().get(1).getId()).isEqualTo(userNotification2.getId());
        assertThat(notificationDTO.getNotifications().get(2).getId()).isEqualTo(userNotification3.getId());
        assertThat(notificationDTO.getPage()).isEqualTo(teacher.getState().toString());
    }




    @Test
    public void viewNotificationNotFoundTest() throws NotificationNotFoundException {
        // Arrange
        UserDTO userDTO = new UserDTO(3L,"email","password",Role.STUDENT);
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> notificationService.viewNotification(userDTO,5L))
                .isInstanceOf(NotificationNotFoundException.class); // Assert
    }

    @Test
    public void viewNotificationTest() throws NotificationNotFoundException {
        // Arrange
        UserApp userApp = new Student(3L,"firstName","lastName","emnail","mpd","add","1234567890", Discipline.INFORMATIQUE);

        UserDTO userDTO = UserDTO.toDTO(userApp);


        Notification userNotification = Notification.builder()
                .id(3L)
                .userId(userDTO.id())
                .code(NotificationCode.TEST_STACKABLE)
                .seenBy(new ArrayList<>())
                .build();

        when(userAppRepository.getReferenceById(userDTO.id())).thenReturn(userApp);
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(userNotification));

        ArgumentCaptor<Notification> argumentCaptor = ArgumentCaptor.forClass(Notification.class);
        // Act
        notificationService.viewNotification(userDTO,5L);
        // Assert
        verify(notificationRepository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getSeenBy()).contains(userApp);
    }

    @Test
    public void getLatestIdTest() {
        // Arrange
        Notification notification = new Notification();
        notification.setId(3L);
        when(notificationRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(notification));
        // Act
        long id = notificationService.getLatestId();
        // Assert
        assertThat(id).isEqualTo(notification.getId());
    }

    @Test
    public void getLatestIdEmptyTest() {
        // Arrange
        when(notificationRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        // Act
        long id = notificationService.getLatestId();
        // Assert
        assertThat(id).isEqualTo(-1);
    }
}
