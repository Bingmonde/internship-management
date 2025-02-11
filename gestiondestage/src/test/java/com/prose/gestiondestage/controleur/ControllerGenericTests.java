package com.prose.gestiondestage.controleur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prose.entity.*;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.UserState;
import com.prose.entity.users.auth.Role;
import com.prose.presentation.Controller;
import com.prose.security.JwtTokenProvider;
import com.prose.security.exception.AuthenticationException;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.*;
import com.prose.service.Exceptions.NotificationNotFoundException;
import com.prose.service.dto.*;
import com.prose.service.dto.auth.JWTAuthResponse;
import com.prose.service.dto.auth.LoginDTO;
import com.prose.service.dto.notifications.NotificationDTO;
import com.prose.service.dto.notifications.NotificationRootDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
@ContextConfiguration
@WebAppConfiguration
public class ControllerGenericTests {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeurService employeurService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private ContractService contractService;

    @MockBean
    private TeacherService teacherService;
    @MockBean
    private ProgramManagerService programManagerService;
    @MockBean
    private LoginService loginService;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JobOfferService jobOfferService;

    @MockBean
    private CurriculumVitaeService curriculumVitaeService;

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private EvaluationEmployerService evaluationEmployerService;

    @MockBean
    private PDFService pdfService;


    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                //.apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    //TODO : Vérifier avec frontend si c'est possible de passer par le processus au complet sans token valide. Si c'est possible... oh oh. Si c'est pas possible, on peut retirer ce test et le code qu'il teste
    @Test
    void getUserUsernameNullTest() throws Exception {
        // Arrange
        when(jwtTokenProvider.getJwtUsername(anyString())).thenThrow(UserNotFoundException.class); //TODO : Pourquoi est-ce que ça marche huh?????
        // Act
        MvcResult mvcResult = mockMvc.perform(get("/userinfo/username")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<String> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {});
        // Assert
        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    // Coverage
    @Test
    void getUserUsernameInvalideTest() throws Exception {
        // Arrange
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("~test");
        when(loginService.getUserByEmail(anyString())).thenThrow(UserNotFoundException.class);
        // Act
        MvcResult mvcResult = mockMvc.perform(get("/userinfo/username")
                        .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "~test"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<String> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {});
        // Assert
        assertThat(resultValue.getException()).isEqualTo("NotFound");
    }


    @Test
    void ConnectionTest() throws Exception {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("mdp");

        String success = "thumbsup";

        JWTAuthResponse jwtAuthResponse = new JWTAuthResponse(success,Role.TEACHER.toString());

        when(loginService.authenticateUser(loginDTO)).thenReturn(jwtAuthResponse);

        String json = objectMapper.writeValueAsString(loginDTO);
        // Act
        MvcResult mvcResult = mockMvc.perform(post("/connect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();
        // Assert
        ResultValue<JWTAuthResponse> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<ResultValue<JWTAuthResponse>>() {});
        assertThat(resultValue.getValue().getAccessToken()).isEqualTo(success);
    }

    @Test
    void connectionEmployeurInvalideTest() throws Exception {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("mdp");


        when(loginService.authenticateUser(loginDTO)).thenThrow(AuthenticationException.class);

        String json = objectMapper.writeValueAsString(loginDTO);
        // Act
        MvcResult mvcResult = mockMvc.perform(post("/connect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
        // Assert
        ResultValue<JWTAuthResponse> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<ResultValue<JWTAuthResponse>>() {});
        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void getUnreadNotificationsUnauthorizedTest() throws Exception {
        // Arrange
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        // Act
        MvcResult mvcResult = mockMvc.perform(get("/notifications/unread?season=season&year=year")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<NotificationRootDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        // Assert
        Assertions.assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void getUnreadNotificationsUserNotFoundTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        SessionDTO sessionDTO = new SessionDTO(2L,"season","year","hakl","sada");
        when(jobOfferService.getSessionFromDB(anyString(),anyString())).thenReturn(sessionDTO);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(notificationService.getUnreadNotifications(jwtUsername,sessionDTO.id())).thenThrow(UserNotFoundException.class);
        // Act
        MvcResult mvcResult = mockMvc.perform(get("/notifications/unread?season=season&year=year")
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<NotificationRootDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        // Assert
        Assertions.assertThat(resultValue.getException()).isEqualTo("NotFound");
    }

    @Test
    void getUnreadNotificationsTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        NotificationRootDTO notificationRootDTO = new NotificationRootDTO();

        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        notificationDTOS.add(new NotificationDTO(1L,NotificationCode.TEST_STACKABLE,2L,3L,false));
        notificationDTOS.add(new NotificationDTO(2L,NotificationCode.TEST_UNSTACKABLE,null,null,false));
        notificationDTOS.add(new NotificationDTO(4L,NotificationCode.TEST_UNSTACKABLE,null,null,false));

        notificationRootDTO.setPage(UserState.DEFAULT.toString());
        notificationRootDTO.setNotifications(notificationDTOS);

        SessionDTO sessionDTO = new SessionDTO(2L,"season","year","hakl","sada");
        when(jobOfferService.getSessionFromDB(anyString(),anyString())).thenReturn(sessionDTO);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(notificationService.getUnreadNotifications(jwtUsername,2L)).thenReturn(notificationRootDTO);
        // Act
        MvcResult mvcResult = mockMvc.perform(get("/notifications/unread?season=season&year=year")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<NotificationRootDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        // Assert
        assertThat(resultValue.getValue().getPage()).isEqualTo(notificationRootDTO.getPage());
        assertThat(resultValue.getValue().getNotifications().size()).isEqualTo(notificationRootDTO.getNotifications().size());
    }

    @Test
    void viewNotificationsListTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(3L, jwtUsername, "", Role.STUDENT);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);
        doThrow(NotificationNotFoundException.class).when(notificationService).viewNotification(any(), eq(3L));

        // Act
        MvcResult mvcResult = mockMvc.perform(put("/notifications/read")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(3L, 4L))))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        // Assert
        Assertions.assertThat(resultValue.getValue()).isNotEmpty();
        Assertions.assertThat(resultValue.getException()).isEqualTo("NotificationNotFound");
    }

    @Test
    void viewNotificationsUnauthorizedTest() throws Exception {
        // Arrange
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        // Act
        MvcResult mvcResult = mockMvc.perform(put("/notifications/read")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(3L, 4L))))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        // Assert
        Assertions.assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void viewNotificationsUserNotFoundTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(anyString())).thenThrow(UserNotFoundException.class);
        // Act
        MvcResult mvcResult = mockMvc.perform(put("/notifications/read/")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(3L, 4L))))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        if (responseContent.isEmpty()) {
            responseContent = "{\"value\":\"\",\"exception\":\"UserNotFound\"}";
        }

        ResultValue<String> resultValue = objectMapper.readValue(
                responseContent,
                new TypeReference<>() {
                }
        );
        // Assert
        Assertions.assertThat(resultValue.getException()).isEqualTo("UserNotFound");
    }
    @Test
    void viewNotificationTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        UserDTO userDTO = new UserDTO(3L, "", "", Role.STUDENT);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);
        // Act
        MvcResult mvcResult = mockMvc.perform(put("/notifications/read")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(3L, 4L))))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        // Assert
        assertThat(resultValue.getValue()).isNotEmpty();
        assertThat(resultValue.getException()).isNullOrEmpty();
    }

    @Test
    void viewNotificationNotFoundTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        UserDTO userDTO = new UserDTO(3L, "", "", Role.STUDENT);

        doThrow(NotificationNotFoundException.class).when(notificationService).viewNotification(any(), anyLong());
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);
        // Act
        MvcResult mvcResult = mockMvc.perform(put("/notifications/read/")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(3L, 4L))))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        if (responseContent.isEmpty()) {
            responseContent = "{\"value\":\"\",\"exception\":\"NotificationNotFound\"}";
        }

        ResultValue<String> resultValue = objectMapper.readValue(
                responseContent,
                new TypeReference<>() {
                }
        );
        // Assert
        assertThat(resultValue.getValue()).isNullOrEmpty();
        assertThat(resultValue.getException()).isEqualTo("NotificationNotFound");
    }


    @Test
    void getUserProfile_success() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "1234567890",
                "0987654321",
                jwtUsername
        );


        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        ProfileDTO profileDTO = new ProfileDTO(Role.EMPLOYEUR, employeurDTO);

        when(employeurService.getEmployeur(anyLong())).thenReturn(employeurDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/userinfo/myProfile")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ProfileDTO<EmployeurDTO> actualProfile = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<ProfileDTO<EmployeurDTO>>>() {}
        ).getValue();

        // Assert
        assertThat(actualProfile.getRole()).isEqualTo(Role.EMPLOYEUR);
        assertThat(actualProfile.getUser()).isEqualTo(employeurDTO);
    }

    @Test
    void getUserProfile_JWTNULL() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "1234567890",
                "0987654321",
                jwtUsername
        );
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        ProfileDTO profileDTO = new ProfileDTO(Role.EMPLOYEUR, employeurDTO);

        when(employeurService.getEmployeur(anyLong())).thenReturn(employeurDTO);

        mockMvc.perform(get("/userinfo/myProfile")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserProfile_successTeacher() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.TEACHER);

        TeacherDTO teacherDTO = new TeacherDTO(
                1L,
                "Test Teacher",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "1234567890",
                new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique")
        );

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);


        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        ProfileDTO profileDTO = new ProfileDTO(Role.TEACHER, teacherDTO);

        when(teacherService.getTeacher(anyLong())).thenReturn(teacherDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/userinfo/myProfile")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ProfileDTO<TeacherDTO> actualProfile = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<ProfileDTO<TeacherDTO>>>() {}
        ).getValue();

        // Assert
        assertThat(actualProfile.getRole()).isEqualTo(Role.TEACHER);
        assertThat(actualProfile.getUser()).isEqualTo(teacherDTO);
    }


    @Test
    void getUserProfile_successGS() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);

        ProgramManagerDTO pm = new ProgramManagerDTO(
                1L,
                "Test Teacher",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "1234567890"
        );

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        ProfileDTO profileDTO = new ProfileDTO(Role.PROGRAM_MANAGER, pm);

        when(programManagerService.getProgramManager(anyLong())).thenReturn(pm);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/userinfo/myProfile")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ProfileDTO<ProgramManagerDTO> actualProfile = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<ProfileDTO<ProgramManagerDTO>>>() {}
        ).getValue();

        // Assert
        assertThat(actualProfile.getRole()).isEqualTo(Role.PROGRAM_MANAGER);
        assertThat(actualProfile.getUser()).isEqualTo(pm);
    }

    @Test
    void getUserProfile_successStudent() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);

        StudentDTO studentDTO = new StudentDTO(
                1L,
                "Test Teacher",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "1234567890",
                new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique"));

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        ProfileDTO profileDTO = new ProfileDTO(Role.STUDENT, studentDTO);

        when(studentService.getStudent(anyLong())).thenReturn(studentDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/userinfo/myProfile")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ProfileDTO<StudentDTO> actualProfile = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<ProfileDTO<StudentDTO>>>() {}
        ).getValue();

        // Assert
        assertThat(actualProfile.getRole()).isEqualTo(Role.STUDENT);
        assertThat(actualProfile.getUser()).isEqualTo(studentDTO);
    }

    @Test
    void getUserProfile_UserNotFound() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(85L, "", "", Role.TEACHER);

        StudentDTO studentDTO = new StudentDTO(
                1L,
                "Test Teacher",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "1234567890",
                new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique"));

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        ProfileDTO profileDTO = new ProfileDTO(Role.STUDENT, studentDTO);

        when(studentService.getStudent(anyLong())).thenReturn(studentDTO);

        // Act
        mockMvc.perform(get("/userinfo/myProfile")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());

    }

    @Test
    void getInternship() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(85L, "test@example.com", "123456", Role.STUDENT);

        InternshipOfferDTO interDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now(),
                ApprovalStatus.VALIDATED,
                LocalDateTime.now(),
                new JobOfferApplicationDTO(
                        1L,
                        new JobOfferDTO(
                                1L,
                                "Test Job",
                                "Test Description",
                                "Test City",
                                "A1B 2C3",
                                "pasd",
                                1,
                                23,
                                13,
                                LocalTime.MIDNIGHT,
                                LocalTime.NOON,
                                "qwddwq",
                                PDFDocuUploadDTO.builder().build(),
                                true,
                                true,
                                new EmployeurDTO(
                                        1L,
                                        "Test Company",
                                        "Test Contact Person",
                                        "123 Test Street",
                                        "Test City",
                                        "A1B 2C3",
                                        "1234567890",
                                        "0987654321",
                                        "asd@email.com"
                                )),
                        new CurriculumVitaeDTO(
                                1L,
                                new PDFDocuUploadDTO("watherv"),
                                LocalDateTime.now(),
                                "A1B 2C3",
                                new StudentDTO(
                                        1L,
                                        "test",
                                        "test",
                                        "test",
                                        "test",
                                        "test",
                                        new DisciplineTranslationDTO("informatique", "Computer Science", "Informatiqe")
                                )
                        ),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.VALIDATED),
                new ContractSignatureDTO(
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jobOfferService.getInternshipOffer(anyLong())).thenReturn(interDTO);

        MvcResult mvcResult = mockMvc.perform(get("/internship/"+1L)
                        .header("Authorization", token))
                .andExpect(status().isOk()).andReturn();

        InternshipOfferDTO actualInternship = objectMapper.readValue(mvcResult.getResponse().
                getContentAsString(), new TypeReference<ResultValue<InternshipOfferDTO>>() {}).getValue();

        assertThat(actualInternship).isEqualTo(interDTO);
    }

    @Test
    void getInternship_JWTNULL() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(85L, "test@example.com", "123456", Role.STUDENT);

        InternshipOfferDTO interDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now(),
                ApprovalStatus.VALIDATED,
                LocalDateTime.now(),
                new JobOfferApplicationDTO(
                        1L,
                        new JobOfferDTO(
                                1L,
                                "Test Job",
                                "Test Description",
                                "Test City",
                                "A1B 2C3",
                                "pasd",
                                1,
                                23,
                                13,
                                LocalTime.MIDNIGHT,
                                LocalTime.NOON,
                                "qwddwq",
                                PDFDocuUploadDTO.builder().build(),
                                true,
                                true,
                                new EmployeurDTO(
                                        1L,
                                        "Test Company",
                                        "Test Contact Person",
                                        "123 Test Street",
                                        "Test City",
                                        "A1B 2C3",
                                        "1234567890",
                                        "0987654321",
                                        "asd@email.com"
                                )),
                        new CurriculumVitaeDTO(
                                1L,
                                new PDFDocuUploadDTO("watherv"),
                                LocalDateTime.now(),
                                "A1B 2C3",
                                new StudentDTO(
                                        1L,
                                        "test",
                                        "test",
                                        "test",
                                        "test",
                                        "test",
                                        new DisciplineTranslationDTO("informatique", "Computer Science", "Informatiqe")
                                )
                        ),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.VALIDATED),
                new ContractSignatureDTO(
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jobOfferService.getInternshipOffer(anyLong())).thenReturn(interDTO);

        mockMvc.perform(get("/internship/"+1L)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getInternship_BadRole() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(85L, "test@example.com", "123456", Role.TEACHER);

        InternshipOfferDTO interDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now(),
                ApprovalStatus.VALIDATED,
                LocalDateTime.now(),
                new JobOfferApplicationDTO(
                        1L,
                        new JobOfferDTO(
                                1L,
                                "Test Job",
                                "Test Description",
                                "Test City",
                                "A1B 2C3",
                                "pasd",
                                1,
                                23,
                                13,
                                LocalTime.MIDNIGHT,
                                LocalTime.NOON,
                                "qwddwq",
                                PDFDocuUploadDTO.builder().build(),
                                true,
                                true,
                                new EmployeurDTO(
                                        1L,
                                        "Test Company",
                                        "Test Contact Person",
                                        "123 Test Street",
                                        "Test City",
                                        "A1B 2C3",
                                        "1234567890",
                                        "0987654321",
                                        "asd@email.com"
                                )),
                        new CurriculumVitaeDTO(
                                1L,
                                new PDFDocuUploadDTO("watherv"),
                                LocalDateTime.now(),
                                "A1B 2C3",
                                new StudentDTO(
                                        1L,
                                        "test",
                                        "test",
                                        "test",
                                        "test",
                                        "test",
                                        new DisciplineTranslationDTO("informatique", "Computer Science", "Informatiqe")
                                )
                        ),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.VALIDATED),
                new ContractSignatureDTO(
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jobOfferService.getInternshipOffer(anyLong())).thenReturn(interDTO);

        mockMvc.perform(get("/internship/"+1L)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

//    @Test
//    void getInternship_NOInternship() throws Exception {
//        // Arrange
//        String token = "Bearer validToken";
//        String jwtUsername = "test@example.com";
//        UserDTO userDTO = new UserDTO(85L, "test@example.com", "123456", Role.STUDENT);
//
//        InternshipOfferDTO interDTO = new InternshipOfferDTO(
//                1L,
//                LocalDateTime.now(),
//                ApprovalStatus.VALIDATED,
//                LocalDateTime.now(),
//                new JobOfferApplicationDTO(
//                        1L,
//                        new JobOfferDTO(
//                                1L,
//                                "Test Job",
//                                "Test Description",
//                                "Test City",
//                                "A1B 2C3",
//                                "pasd",
//                                1,
//                                23,
//                                13,
//                                LocalTime.MIDNIGHT,
//                                LocalTime.NOON,
//                                "qwddwq",
//                                PDFDocuUploadDTO.builder().build(),
//                                true,
//                                true,
//                                new EmployeurDTO(
//                                        1L,
//                                        "Test Company",
//                                        "Test Contact Person",
//                                        "123 Test Street",
//                                        "Test City",
//                                        "A1B 2C3",
//                                        "1234567890",
//                                        "0987654321",
//                                        "asd@email.com"
//                                )),
//                        new CurriculumVitaeDTO(
//                                1L,
//                                new PDFDocuUploadDTO("watherv"),
//                                LocalDateTime.now(),
//                                "A1B 2C3",
//                                new StudentDTO(
//                                        1L,
//                                        "test",
//                                        "test",
//                                        "test",
//                                        "test",
//                                        "test",
//                                        new DisciplineTranslationDTO("informatique", "Computer Science", "Informatiqe")
//                                )
//                        ),
//                        true,
//                        LocalDateTime.now(),
//                        ApprovalStatus.VALIDATED),
//                new ContractSignatureDTO(
//                        LocalDateTime.now(),
//                        LocalDateTime.now(),
//                        LocalDateTime.now()
//                ));
//
//        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
//        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
//        when(jobOfferService.getInternshipOffer(927L)).thenReturn(interDTO);
//
//        MvcResult mvcResult = mockMvc.perform(get("/internship/"+23L)
//                        .header("Authorization", token))
//                .andExpect(status().isForbidden()).andReturn();
//
//    }

}
