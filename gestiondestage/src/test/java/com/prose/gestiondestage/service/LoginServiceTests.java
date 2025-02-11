package com.prose.gestiondestage.service;

import com.prose.entity.Discipline;
import com.prose.entity.users.Student;
import com.prose.entity.users.UserApp;
import com.prose.repository.*;
import com.prose.security.AuthProvider;
import com.prose.security.JwtTokenProvider;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.*;
import com.prose.service.Exceptions.InvalidPasswordException;
import com.prose.service.dto.UserDTO;
import com.prose.service.dto.auth.LoginDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoginServiceTests {

    LoginService loginService;
    AuthProvider authenticationManager;
    JwtTokenProvider jwtTokenProvider;
    UserAppRepository userAppRepository;
    StudentService studentService;
    EmployeurService employeurService;
    TeacherService teacherService;
    ProgramManagerService programManagerService;
    PasswordEncoder passwordEncoder;

    EvaluationEmployerService evaluationEmployerService;

    @BeforeEach
    void beforeEach() {

        authenticationManager = mock(AuthProvider.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userAppRepository = mock(UserAppRepository.class);
        studentService = mock(StudentService.class);
        employeurService = mock(EmployeurService.class);
        teacherService = mock(TeacherService.class);
        programManagerService = mock(ProgramManagerService.class);
        passwordEncoder = mock(PasswordEncoder.class);

        loginService = new LoginService(authenticationManager,jwtTokenProvider, userAppRepository,studentService,employeurService,teacherService,programManagerService,passwordEncoder);
    }

    @Test
    void authenticateUserTest() {
        // Arrange
        ArgumentCaptor<UsernamePasswordAuthenticationToken> usernamePasswordAuthenticationTokenArgumentCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        String email = "emailer";
        String password = "mpd";
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(password);

        Student student = new Student();
        student.setCredentials(email,password);

        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(email,password,student.getAuthorities()));


        // Act
        loginService.authenticateUser(loginDTO);
        // Assert
        verify(authenticationManager).authenticate(usernamePasswordAuthenticationTokenArgumentCaptor.capture());
        verify(jwtTokenProvider).generateToken(any());
        assertThat(usernamePasswordAuthenticationTokenArgumentCaptor.getValue().getCredentials()).isEqualTo(password);
        assertThat(usernamePasswordAuthenticationTokenArgumentCaptor.getValue().getPrincipal()).isEqualTo(email);
    }

    @Test
    void getUserByEmailTest() {
        // Arrange
        UserApp userApp = new Student(1L,"first","last","email","mpd","addresse","1234567890", Discipline.EC_EDUCATION);

        when(userAppRepository.findUserAppByEmail("email")).thenReturn(Optional.of(userApp));
        // Act
        UserDTO userDTO = loginService.getUserByEmail("email");
        // Assert
        assertThat(userDTO.id()).isEqualTo(userApp.getId());
    }

    @Test
    void getUserByEmailTestNotFound() {
        // Arrange

        when(userAppRepository.findUserAppByEmail("email")).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> loginService.getUserByEmail("email"))
                .isInstanceOf(UserNotFoundException.class); // Assert

    }

    @Test
    void getUserByEmailValidatePasswordTest() throws InvalidPasswordException {
        // Arrange
        UserApp userApp = new Student(1L,"first","last","email","mpd","addresse","1234567890", Discipline.EC_EDUCATION);

        when(userAppRepository.findUserAppByEmail("email")).thenReturn(Optional.of(userApp));
        when(passwordEncoder.matches("mpd","mpd")).thenReturn(true);
        // Act
        UserDTO userDTO = loginService.getUserByEmailValidatePassword("email","mpd");
        // Assert
        assertThat(userDTO.id()).isEqualTo(userApp.getId());
    }

    @Test
    void getUserByEmailValidatePasswordTest_NoMatch() throws InvalidPasswordException {
        // Arrange
        UserApp userApp = new Student(1L,"first","last","email","mpd","addresse","1234567890", Discipline.EC_EDUCATION);

        when(userAppRepository.findUserAppByEmail("email")).thenReturn(Optional.of(userApp));
        when(passwordEncoder.matches("mpd","mpd")).thenReturn(true);

        try {
            loginService.getUserByEmailValidatePassword("email","mpd2");
        } catch (InvalidPasswordException e) {
            assertThat(e.getMessage()).isEqualTo("Password is incorrect");
        }
    }
}
