package com.prose.gestiondestage.controleur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prose.entity.*;
import com.prose.presentation.Controller;
import com.prose.security.JwtTokenProvider;
import com.prose.service.*;
import com.prose.service.Exceptions.*;
import com.prose.service.dto.*;
import com.prose.security.exception.UserNotFoundException;
import com.prose.entity.users.auth.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(Controller.class)
@ContextConfiguration
@WebAppConfiguration
public class JobOfferControllerTests {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobOfferService jobOfferService;

    @MockBean
    private EmployeurService employeurService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private ProgramManagerService programManagerService;

    @MockBean
    private LoginService loginService;

    @MockBean
    private CurriculumVitaeService curriculumVitaeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ContractService contractService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private EvaluationEmployerService evaluationEmployerService;

    @MockBean
    private PDFService pdfService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void testCreateJobOffer_Success() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        JobOfferRegisterDTO jobOfferRegisterDTO = new JobOfferRegisterDTO(
                "Developer",
                "2023-01-01",
                "2023-12-31",
                "Remote",
                "Full-time",
                3,
                22.5,32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Develop software"
        );
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );

        JobOfferDTO jobOfferDTO = new JobOfferDTO(
                1L,
                jobOfferRegisterDTO.titre(),
                jobOfferRegisterDTO.dateDebut(),
                jobOfferRegisterDTO.dateFin(),
                jobOfferRegisterDTO.lieu(),
                jobOfferRegisterDTO.typeTravail(),
                jobOfferRegisterDTO.nombreStagiaire(),
                jobOfferRegisterDTO.tauxHoraire(),
                jobOfferRegisterDTO.weeklyHours(),
                jobOfferRegisterDTO.dailyScheduleFrom(),
                jobOfferRegisterDTO.dailyScheduleTo(),
                jobOfferRegisterDTO.description(),
                null,
                false,
                false,
                employeurDTO
        );

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        when(jobOfferService.createJobOffer(any(JobOfferRegisterDTO.class), any(), eq("test@example.com")))
                .thenReturn(jobOfferDTO);

        String json = objectMapper.writeValueAsString(jobOfferRegisterDTO);
        MockMultipartFile jobOffrePart = new MockMultipartFile("jobOffre", "", "application/json", json.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart("/employeur/jobOffers")
                        .file(jobOffrePart)
                        .file(filePart)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue()).isEqualTo(jobOfferDTO);
    }

    @Test
    void testCreateJobOffer_Unauthorized() throws Exception {
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        MockMultipartFile jobOffrePart = new MockMultipartFile("jobOffre", "", "application/json", "{}".getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart("/employeur/jobOffers")
                        .file(jobOffrePart)
                        .file(filePart)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void testCreateJobOffer_Forbidden() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "Stest@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.STUDENT);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MockMultipartFile jobOffrePart = new MockMultipartFile("jobOffre", "", "application/json", "{}".getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart("/employeur/jobOffers")
                        .file(jobOffrePart)
                        .file(filePart)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
        //}
    }

    @Test
    void testCreateJobOffer_IOException() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        JobOfferRegisterDTO jobOfferRegisterDTO = new JobOfferRegisterDTO(
                "Developer",
                "2023-01-01",
                "2023-12-31",
                "Remote",
                "Full-time",
                3,
                22.5,
                32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Develop software"
        );
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"  // 确保这是最后一个参数，并且类型正确
        );

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.EMPLOYEUR);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.createJobOffer(any(JobOfferRegisterDTO.class), any(), eq("test@example.com")))
                .thenThrow(new IOException());

        String json = objectMapper.writeValueAsString(jobOfferRegisterDTO);
        MockMultipartFile jobOffrePart = new MockMultipartFile("jobOffre", "", "application/json", json.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart("/employeur/jobOffers")
                        .file(jobOffrePart)
                        .file(filePart)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("ReadingPDFFails");
        //}
    }

    @Test
    void testGetJobOffersByEmployeur_Success() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.EMPLOYEUR);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        String email = "test@example.com";
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );

        when(employeurService.getEmployeurByEmail(email)).thenReturn(employeurDTO);

        List<JobOfferDTO> jobOffers = Arrays.asList(
                new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                        LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, false, false,null),
                new JobOfferDTO(2L, "Job2", "2023-02-01", "2023-07-01", "Location2", "Part-time", 1, 15.0, 32,
                        LocalTime.of(8, 0), LocalTime.of(16, 0),"Description2", null, false, false,null)
        );

        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);
        when(jobOfferService.getJobOffersByEmployeur(1L, 1L)).thenReturn(jobOffers);

        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue()).hasSize(2).containsExactlyElementsOf(jobOffers);
        //}
    }

    @Test
    void testGetJobOffersByEmployeur_Unauthorized() throws Exception {
        String token = "Bearer invalidToken";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void testGetJobOffersByEmployeur_Forbidden() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.STUDENT);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
        //}
    }

    @Test
    void testGetJobOffersByEmployeur_EmployeurNotFound() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(employeurService.getEmployeurByEmail("test@example.com")).thenThrow(new UserNotFoundException());
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);
        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("EmployeurNotFound");
        //}
    }

    @Test
    void testUpdateJobOffer_Success() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        Long jobOfferId = 1L;

        JobOfferRegisterDTO jobOfferRegisterDTO = new JobOfferRegisterDTO(
                "Updated Title",
                "2023-05-01",
                "2023-12-31",
                "Updated Location",
                "Part-time",
                2,
                25.0,32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Updated Description"
        );
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );

        JobOfferDTO updatedJobOfferDTO = new JobOfferDTO(
                jobOfferId,
                jobOfferRegisterDTO.titre(),
                jobOfferRegisterDTO.dateDebut(),
                jobOfferRegisterDTO.dateFin(),
                jobOfferRegisterDTO.lieu(),
                jobOfferRegisterDTO.typeTravail(),
                jobOfferRegisterDTO.nombreStagiaire(),
                jobOfferRegisterDTO.tauxHoraire(),
                jobOfferRegisterDTO.weeklyHours(),
                jobOfferRegisterDTO.dailyScheduleFrom(),
                jobOfferRegisterDTO.dailyScheduleTo(),
                jobOfferRegisterDTO.description(),
                null,
                false,
                false,
                employeurDTO
        );

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.EMPLOYEUR);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.updateJobOffer(eq(jobOfferId), any(JobOfferRegisterDTO.class), eq("test@example.com")))
                .thenReturn(updatedJobOfferDTO);

        String json = objectMapper.writeValueAsString(jobOfferRegisterDTO);

        MvcResult mvcResult = mockMvc.perform(put("/employeur/jobOffers/" + jobOfferId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue()).isEqualTo(updatedJobOfferDTO);
        //}
    }

    @Test
    void testUpdateJobOffer_Unauthorized() throws Exception {
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        Long jobOfferId = 1L;

        MvcResult mvcResult = mockMvc.perform(put("/employeur/jobOffers/" + jobOfferId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void testUpdateJobOffer_Forbidden() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.STUDENT);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        Long jobOfferId = 1L;

        MvcResult mvcResult = mockMvc.perform(put("/employeur/jobOffers/" + jobOfferId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
        //}
    }

    @Test
    void testUpdateJobOffer_JobOfferNotFound() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        Long jobOfferId = 1L;

        JobOfferRegisterDTO jobOfferRegisterDTO = new JobOfferRegisterDTO(
                "Updated Title",
                "2023-05-01",
                "2023-12-31",
                "Updated Location",
                "Part-time",
                2,
                25.0,32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Updated Description"
        );
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.EMPLOYEUR);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.updateJobOffer(eq(jobOfferId), any(JobOfferRegisterDTO.class), eq("test@example.com")))
                .thenThrow(new UserNotFoundException());

        String json = objectMapper.writeValueAsString(jobOfferRegisterDTO);

        MvcResult mvcResult = mockMvc.perform(put("/employeur/jobOffers/" + jobOfferId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JobOfferNotFound");
        //}
    }

    @Test
    void testUpdateJobOffer_MissingPermissions() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        Long jobOfferId = 1L;

        JobOfferRegisterDTO jobOfferRegisterDTO = new JobOfferRegisterDTO(
                "Updated Title",
                "2023-05-01",
                "2023-12-31",
                "Updated Location",
                "Part-time",
                2,
                25.0,32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Updated Description"
        );

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.EMPLOYEUR);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.updateJobOffer(eq(jobOfferId), any(JobOfferRegisterDTO.class), eq("test@example.com")))
                .thenThrow(new MissingPermissionsExceptions());

        String json = objectMapper.writeValueAsString(jobOfferRegisterDTO);

        MvcResult mvcResult = mockMvc.perform(put("/employeur/jobOffers/" + jobOfferId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("MissingPermissions");
        //}
    }

    // getJobOffersForConnectedStudent
    @Test
    void testGetJobOffersForConnectedStudent_Success() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");


        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        StudentDTO studentDTO = new StudentDTO(1L, "John", "Doe", jwtUsername, "Address", "Computer Science", Discipline.INFORMATIQUE.getTranslation());

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(studentService.getStudentByEmail(jwtUsername)).thenReturn(studentDTO);
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );
        List<JobOfferDTO> jobOffers = Arrays.asList(
                new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                        LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, false, false,employeurDTO),
                new JobOfferDTO(2L, "Job2", "2023-02-01", "2023-07-01", "Location2", "Part-time", 1, 15.0, 32,
                        LocalTime.of(8, 0), LocalTime.of(16, 0),"Description2", null, false, false,employeurDTO)
        );
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);
        when(jobOfferService.getJobOffersForStudent(eq(jwtUsername), anyLong(),anyString(),anyInt(),anyInt())).thenReturn(new PageImpl<>(jobOffers, PageRequest.of(5,5),5));


        MvcResult mvcResult = mockMvc.perform(get("/student/jobOffers?season=Automne&year=2023"
                        + "&page=5&size=5"
                        + "&q=hi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue()).hasSize(2).containsExactlyElementsOf(jobOffers);
    }

    @Test
    void testGetJobOffersForConnectedStudent_Unauthorized() throws Exception {
        String token = "Bearer invalidToken";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        MvcResult mvcResult = mockMvc.perform(get("/student/jobOffers?season=Automne&year=2023"
                        + "&page=5&size=5"
                        + "&q=hi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void testGetJobOffersForConnectedStudent_Forbidden() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        MvcResult mvcResult = mockMvc.perform(get("/student/jobOffers?season=Automne&year=2023"
                                                    + "&page=5&size=5"
                                                    + "&q=hi")
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void testGetJobOffersForConnectedStudent_StudentNotFound() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(studentService.getStudentByEmail(jwtUsername)).thenThrow(new UserNotFoundException());
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        MvcResult mvcResult = mockMvc.perform(get("/student/jobOffers?season=Automne&year=2023"
                        + "&page=5&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("StudentNotFound");
    }

    @Test
    void studentApplyToJobOfferTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        StudentDTO studentDTO = new StudentDTO(4L,"Nom","Prenom","a@a.com","adresse","1234567890",Discipline.getAllTranslations().getFirst());

        JobOfferApplicationRegisterDTO jobOfferApplicationRegisterDTO = new JobOfferApplicationRegisterDTO(2L,3L);
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );
        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1L,
                new JobOfferDTO(1L,"titre","string","string","lieu","remote",3,0d,32,
                        LocalTime.of(8, 0), LocalTime.of(16, 0),"desc",null,false,false,employeurDTO),
                new CurriculumVitaeDTO(1L,null,LocalDateTime.now(),"status",studentDTO),
                true, LocalDateTime.now(),ApprovalStatus.NOT_APPLICABLE);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.applyToJobOffer(jobOfferApplicationRegisterDTO.jobOffer(),jobOfferApplicationRegisterDTO.cv(), userDTO)).thenReturn(jobOfferApplicationDTO);

        String json = objectMapper.writeValueAsString(jobOfferApplicationRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/student/jobOffers/apply")
                        .header("Authorization", token)
                        .content(json)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();
        // Assert

        ResultValue<JobOfferApplicationDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue().jobOffer()).isEqualTo(jobOfferApplicationDTO.jobOffer());
        assertThat(resultValue.getValue().CV()).isEqualTo(jobOfferApplicationDTO.CV());
    }

    @Test
    void studentApplyToJobOfferUnauthorizedTest() throws Exception {
        String token = "Bearer invalidToken";
        JobOfferApplicationRegisterDTO jobOfferApplicationRegisterDTO = new JobOfferApplicationRegisterDTO(2L,3L);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        String json = objectMapper.writeValueAsString(jobOfferApplicationRegisterDTO);

        MvcResult mvcResult = mockMvc.perform(post("/student/jobOffers/apply")
                        .header("Authorization", token)
                        .content(json)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void studentApplyToJobOfferForbiddenTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        JobOfferApplicationRegisterDTO jobOfferApplicationRegisterDTO = new JobOfferApplicationRegisterDTO(2L,3L);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        String json = objectMapper.writeValueAsString(jobOfferApplicationRegisterDTO);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.TEACHER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MvcResult mvcResult = mockMvc.perform(post("/student/jobOffers/apply")
                        .header("Authorization", token)
                        .content(json)
                        .contentType("application/json"))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<List<JobOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void studentApplyToJobOfferCVNotFoundTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        JobOfferApplicationRegisterDTO jobOfferApplicationRegisterDTO = new JobOfferApplicationRegisterDTO(2L,3L);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.applyToJobOffer(jobOfferApplicationRegisterDTO.jobOffer(),jobOfferApplicationRegisterDTO.cv(), userDTO)).thenThrow(CVNotFoundException.class);

        String json = objectMapper.writeValueAsString(jobOfferApplicationRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/student/jobOffers/apply")
                        .header("Authorization", token)
                        .content(json)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andReturn();
        // Assert

        ResultValue<JobOfferApplicationDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("NotFound");
    }

    @Test
    void studentApplyToJobOfferNotFoundTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        JobOfferApplicationRegisterDTO jobOfferApplicationRegisterDTO = new JobOfferApplicationRegisterDTO(2L,3L);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.applyToJobOffer(jobOfferApplicationRegisterDTO.jobOffer(),jobOfferApplicationRegisterDTO.cv(), userDTO)).thenThrow(JobNotFoundException.class);

        String json = objectMapper.writeValueAsString(jobOfferApplicationRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/student/jobOffers/apply")
                        .header("Authorization", token)
                        .content(json)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andReturn();
        // Assert

        ResultValue<JobOfferApplicationDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("NotFound");
    }

    @Test
    void studentApplyToJobOfferStudentNotAllowedTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        JobOfferApplicationRegisterDTO jobOfferApplicationRegisterDTO = new JobOfferApplicationRegisterDTO(2L,3L);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.applyToJobOffer(jobOfferApplicationRegisterDTO.jobOffer(),jobOfferApplicationRegisterDTO.cv(), userDTO)).thenThrow(MissingPermissionsExceptions.class);

        String json = objectMapper.writeValueAsString(jobOfferApplicationRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/student/jobOffers/apply")
                        .header("Authorization", token)
                        .content(json)
                        .contentType("application/json"))
                .andExpect(status().isForbidden())
                .andReturn();
        // Assert

        ResultValue<JobOfferApplicationDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("MissingPermission");
    }

    @Test
    void studentApplyToJobOfferOtherApplicationActiveTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        JobOfferApplicationRegisterDTO jobOfferApplicationRegisterDTO = new JobOfferApplicationRegisterDTO(2L,3L);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.applyToJobOffer(jobOfferApplicationRegisterDTO.jobOffer(),jobOfferApplicationRegisterDTO.cv(), userDTO)).thenThrow(TooManyApplicationsExceptions.class);

        String json = objectMapper.writeValueAsString(jobOfferApplicationRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/student/jobOffers/apply")
                        .header("Authorization", token)
                        .content(json)
                        .contentType("application/json"))
                .andExpect(status().isForbidden())
                .andReturn();
        // Assert

        ResultValue<JobOfferApplicationDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("AlreadyApplied");
    }

    @Test
    void studentGetApplicationListEmptyTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        JobOfferApplicationRegisterDTO jobOfferApplicationRegisterDTO = new JobOfferApplicationRegisterDTO(2L,3L);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getJobOffersApplicationsFromStudent(eq(userDTO.id()), anyLong())).thenReturn(List.of());
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/student/applications?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        // Assert

        ResultValue<List<JobOfferApplicationDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue().isEmpty()).isTrue();
    }

    @Test
    void studentGetApplicationListFullTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        JobOfferApplicationRegisterDTO jobOfferApplicationRegisterDTO = new JobOfferApplicationRegisterDTO(2L,3L);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);

        StudentDTO studentDTO = new StudentDTO(4L,"Nom","Prenom","a@a.com","adresse","1234567890",Discipline.getAllTranslations().getFirst());
        CurriculumVitaeDTO curriculumVitaeDTO = new CurriculumVitaeDTO(6L,null,LocalDateTime.now(),ApprovalStatus.VALIDATED.toString(),studentDTO);
        JobOfferDTO jobOfferDTO = new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, false, false,null);

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getJobOffersApplicationsFromStudent(eq(userDTO.id()), anyLong())).thenReturn(List.of(new JobOfferApplicationDTO(1L,
                jobOfferDTO,curriculumVitaeDTO,true, LocalDateTime.now(),ApprovalStatus.NOT_APPLICABLE),new JobOfferApplicationDTO(2L,jobOfferDTO,curriculumVitaeDTO,false, LocalDateTime.now(),ApprovalStatus.NOT_APPLICABLE)));
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/student/applications?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        // Assert

        ResultValue<List<JobOfferApplicationDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue().size()).isEqualTo(2);
        assertThat(resultValue.getValue().getFirst().id()).isEqualTo(1L);
        assertThat(resultValue.getValue().getLast().id()).isEqualTo(2L);
    }

    @Test
    void studentGetApplicationListUnauthorizedTest() throws Exception {
        String token = "Bearer invalidToken";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");


        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);


        MvcResult mvcResult = mockMvc.perform(get("/student/applications?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<List<JobOfferApplicationDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void studentGetApplicationListForbiddenTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.TEACHER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MvcResult mvcResult = mockMvc.perform(get("/student/applications?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<List<JobOfferApplicationDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void cancelJobOfferApplicationStudentUnauthorizedTest() throws Exception {
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);


        MvcResult mvcResult = mockMvc.perform(post("/student/applications/cancel/1")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void cancelJobOfferApplicationStudentForbiddenTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.TEACHER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MvcResult mvcResult = mockMvc.perform(post("/student/applications/cancel/1")
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void cancelJobOfferApplicationStudentWrongPathTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MvcResult mvcResult = mockMvc.perform(post("/student/applications/cancel/1L")
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("NotFound");
    }

    @Test
    void cancelJobOfferApplicationStudentApplicationNotFoundTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        doThrow(JobNotFoundException.class).when(jobOfferService).cancelJobOfferApplicationStudent(anyLong(),anyLong());

        MvcResult mvcResult = mockMvc.perform(post("/student/applications/cancel/1")
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("NotFound");
    }

    @Test
    void cancelJobOfferApplicationWrongStudentTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        doThrow(MissingPermissionsExceptions.class).when(jobOfferService).cancelJobOfferApplicationStudent(anyLong(),anyLong());

        MvcResult mvcResult = mockMvc.perform(post("/student/applications/cancel/1")
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("MissingPermissions");
    }

    @Test
    void cancelJobOfferApplicationStudentTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);


        MvcResult mvcResult = mockMvc.perform(post("/student/applications/cancel/1")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue()).isEqualTo("ok");
    }

    @Test
    void getListJobApplicationByOfferId() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );
        JobOfferDTO jobOfferDTO = new JobOfferDTO(
                1L,
                "Job1",
                "2023-01-01",
                "2023-06-01",
                "Location1",
                "Full-time",
                2,
                20.0,
                32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Description1",
                null,
                true,
                false,
                employeurDTO
        );
        StudentDTO studentDTO = new StudentDTO(
                1L,
                "John", "Doe",
                "a@emil.com",
                "Address",
                "111-111-1111",
                Discipline.INFORMATIQUE.getTranslation());
        StudentDTO studentDTO2 = new StudentDTO(
                2L,
                "Mohn", "Foe",
                "a@emil.com",
                "Address",
                "111-111-1111",
                Discipline.INFORMATIQUE.getTranslation());
        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(
                1L,
                jobOfferDTO,
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),studentDTO),
                true,
                LocalDateTime.now(),
                ApprovalStatus.NOT_APPLICABLE
        );
        JobOfferApplicationDTO jobOfferApplicationDTO2 = new JobOfferApplicationDTO(
                2L,
                jobOfferDTO,
                new CurriculumVitaeDTO(2L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),studentDTO2),
                true,
                LocalDateTime.now(),
                ApprovalStatus.NOT_APPLICABLE
        );

        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                jobOfferApplicationDTO,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        List<JobOfferApplicationDTO> list = List.of(jobOfferApplicationDTO,jobOfferApplicationDTO2);
        List<JobInterviewDTO> interviewList = List.of(jobInterviewDTO);
        List<JobOfferApplicationFullDTO> returnList = List.of(new JobOfferApplicationFullDTO(jobOfferApplicationDTO,interviewList,null),new JobOfferApplicationFullDTO(jobOfferApplicationDTO2,interviewList,null));
        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.getJobOfferApplicationsFromJobOfferId(anyLong(), anyLong(),anyString(),any(),anyInt(),anyInt())).thenReturn(new PageImpl<>(list,PageRequest.of(5,5),5));
        when(jobOfferService.getInterviewFromJobOfferApplication(any())).thenReturn(interviewList);
        when(jobOfferService.getSessionFromDB(any(), any())).thenReturn(sessionDTO);

        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers/listeJobApplications/"+jobOfferDTO.id()+"?season=Automne&year=2023&page=5&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<JobOfferApplicationFullDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        //Assert
        assertThat(resultValue.getValue()).isEqualTo(returnList);
    }

    @Test
    void getListJobApplicationByOfferId_EmptyMap() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");
        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.getJobOfferApplicationsFromJobOfferId(anyLong(),anyLong(),anyString(),any(),anyInt(),anyInt())).thenThrow(JobNotFoundException.class);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers/listeJobApplications/"+anyLong()+"?season=Automne&year=2023&page=5&size=5&q=hi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<Map<String, JobOfferApplicationDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        //Assert
        assertThat(resultValue.getException()).isEqualTo("No valid applications found for job offer with ID " + 0);
    }

    @Test
    void offerInternshipToStudent() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        StudentDTO studentDTO = new StudentDTO(4L,"Nom","Prenom","a@a.com","adresse","1234567890",Discipline.getAllTranslations().getFirst());

        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );
        JobOfferDTO jobOfferDTO = new JobOfferDTO(
                1L,
                "Job1",
                "2023-01-01",
                "2023-06-01",
                "Location1",
                "Full-time",
                2,
                20.0,32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Description1",
                null,
                true,
                true,
                employeurDTO
        );
        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(
                1L,
                jobOfferDTO,
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),studentDTO),
                true,
                LocalDateTime.now(),
                ApprovalStatus.NOT_APPLICABLE
        );
        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now().plusDays(7),
                ApprovalStatus.WAITING,
                null,
                jobOfferApplicationDTO,
                null
        );

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.offerInternshipToStudent(1L, userDTO, 7L)).thenReturn(internshipOfferDTO);

        MvcResult mvcResult = mockMvc.perform(put("/employeur/offerInternshipToStudent?jobApplicationId="+1L+"&offerExpireInDays="+7L)
                        .header("Authorization", token)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        //Assert
        assertThat(resultValue.getValue()).isEqualTo(internshipOfferDTO);
    }

    @Test
    void offerInternshipToStudent_Unauthorized() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );

        JobOfferDTO jobOfferDTO = new JobOfferDTO(
                1L,
                "Job1",
                "2023-01-01",
                "2023-06-01",
                "Location1",
                "Full-time",
                2,
                20.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Description1",
                null,
                true,
                true,
                employeurDTO
        );
        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(
                1L,
                jobOfferDTO,
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                true,
                LocalDateTime.now(),
                ApprovalStatus.NOT_APPLICABLE
        );
        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now().plusDays(7),
                ApprovalStatus.WAITING,
                null,
                jobOfferApplicationDTO,
                null
        );

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.offerInternshipToStudent(1L, userDTO, 7L)).thenReturn(internshipOfferDTO);

        mockMvc.perform(put("/employeur/offerInternshipToStudent?jobApplicationId="+1L+"&offerExpireInDays="+7L)
                        .header("Authorization", token)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void offerInternshipToStudent_Forbidden() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );

        JobOfferDTO jobOfferDTO = new JobOfferDTO(
                1L,
                "Job1",
                "2023-01-01",
                "2023-06-01",
                "Location1",
                "Full-time",
                2,
                20.0,32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Description1",
                null,
                true,
                true,
                employeurDTO
        );
        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(
                1L,
                jobOfferDTO,
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                true,
                LocalDateTime.now(),
                ApprovalStatus.NOT_APPLICABLE
        );
        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now().plusDays(7),
                ApprovalStatus.WAITING,
                null,
                jobOfferApplicationDTO,
                null
        );

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.offerInternshipToStudent(1L, userDTO, 7L)).thenReturn(internshipOfferDTO);

        mockMvc.perform(put("/employeur/offerInternshipToStudent?jobApplicationId="+1L+"&offerExpireInDays="+7L)
                        .header("Authorization", token)
                        .contentType("application/json"))
                .andExpect(status().isForbidden());

    }

    @Test
    void offerInternshipToStudent_JobApplicationNotFound() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );

        JobOfferDTO jobOfferDTO = new JobOfferDTO(
                1L,
                "Job1",
                "2023-01-01",
                "2023-06-01",
                "Location1",
                "Full-time",
                2,
                20.0,32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Description1",
                null,
                true,
                true,
                employeurDTO
        );
        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(
                1L,
                jobOfferDTO,
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                true,
                LocalDateTime.now(),
                ApprovalStatus.NOT_APPLICABLE
        );
        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now().plusDays(7),
                ApprovalStatus.WAITING,
                null,
                jobOfferApplicationDTO,
                null
        );

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.offerInternshipToStudent(1L, userDTO, 7L)).thenThrow(JobNotFoundException.class);

        MvcResult mvcResult = mockMvc.perform(put("/employeur/offerInternshipToStudent?jobApplicationId="+1L+"&offerExpireInDays="+7L)
                        .header("Authorization", token)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        //Assert
        assertThat(resultValue.getException()).isEqualTo("JobOfferNotFound");
    }

    @Test
    void offerInternshipToStudent_StudentNotFound() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );
        JobOfferDTO jobOfferDTO = new JobOfferDTO(
                1L,
                "Job1",
                "2023-01-01",
                "2023-06-01",
                "Location1",
                "Full-time",
                2,
                20.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Description1",
                null,
                true,
                true,
                employeurDTO
        );
        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(
                1L,
                jobOfferDTO,
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                true,
                LocalDateTime.now(),
                ApprovalStatus.NOT_APPLICABLE
        );
        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now().plusDays(7),
                ApprovalStatus.WAITING,
                null,
                jobOfferApplicationDTO,
                null
        );

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.offerInternshipToStudent(1L, userDTO, 7L)).thenThrow(UserNotFoundException.class);

        MvcResult mvcResult = mockMvc.perform(put("/employeur/offerInternshipToStudent?jobApplicationId="+1L+"&offerExpireInDays="+7L)
                        .header("Authorization", token)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        //Assert
        assertThat(resultValue.getException()).isEqualTo("UserNotFoundException");
    }

    @Test
    void offerInternshipToStudent_WrongEmployer() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );
        JobOfferDTO jobOfferDTO = new JobOfferDTO(
                1L,
                "Job1",
                "2023-01-01",
                "2023-06-01",
                "Location1",
                "Full-time",
                2,
                20.0,32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Description1",
                null,
                true,
                true,
                employeurDTO
        );
        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(
                1L,
                jobOfferDTO,
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                true,
                LocalDateTime.now(),
                ApprovalStatus.NOT_APPLICABLE
        );
        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now().plusDays(7),
                ApprovalStatus.WAITING,
                null,
                jobOfferApplicationDTO,
                null
        );

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.offerInternshipToStudent(1L, userDTO, 7L)).thenThrow(MissingPermissionsExceptions.class);

        MvcResult mvcResult = mockMvc.perform(put("/employeur/offerInternshipToStudent?jobApplicationId="+1L+"&offerExpireInDays="+7L)
                        .header("Authorization", token)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        //Assert
        assertThat(resultValue.getException()).isEqualTo("MissingPermissions");
    }

    @Test
    void offerInternshipToStudent_NotENoughTimeAllocated() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        EmployeurDTO employeurDTO = new EmployeurDTO(
                1L,
                "Test Company",
                "Test Contact Person",
                "test@example.com",
                "password",
                "123 Test Street",
                "Test City",
                "A1B 2C3",
                "123@123.com"
        );
        JobOfferDTO jobOfferDTO = new JobOfferDTO(
                1L,
                "Job1",
                "2023-01-01",
                "2023-06-01",
                "Location1",
                "Full-time",
                2,
                20.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "Description1",
                null,
                true,
                true,
                employeurDTO
        );
        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(
                1L,
                jobOfferDTO,
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                true,
                LocalDateTime.now(),
                ApprovalStatus.NOT_APPLICABLE
        );
        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(
                1L,
                LocalDateTime.now().plusDays(7),
                ApprovalStatus.WAITING,
                null,
                jobOfferApplicationDTO,
                null
        );

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.offerInternshipToStudent(1L, userDTO, 7L)).thenThrow(NotEnoughTimeAllocatedException.class);

        MvcResult mvcResult = mockMvc.perform(put("/employeur/offerInternshipToStudent?jobApplicationId="+1L+"&offerExpireInDays="+7L)
                        .header("Authorization", token)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        //Assert
        assertThat(resultValue.getException()).isEqualTo("NotEnoughTimeAllocated");
    }

    @Test
    void getMyInternshipOffers_success() throws Exception {
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");


        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getMyInternshipOffers(anyLong(), anyLong(), anyString(),anyInt(),anyInt())).thenReturn(new PageImpl<>(List.of(new InternshipOfferDTO(1L, LocalDateTime.now(), ApprovalStatus.WAITING, null, null,null),
                new InternshipOfferDTO(2L, LocalDateTime.now(), ApprovalStatus.WAITING, null, null,null)),PageRequest.of(5,5),5));
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        // act
        MvcResult mvcResult = mockMvc.perform(get("/student/internshipOffers?season=Automne&year=2023"
                                                    + "&page=5&size=5&q=hi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();


        ResultValue<List<InternshipOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        // assert
        assertThat(resultValue.getValue().size()).isEqualTo(2);
        assertThat(resultValue.getValue().get(0).id()).isEqualTo(1L);
        assertThat(resultValue.getValue().get(1).id()).isEqualTo(2L);

    }

    @Test
    void getMyInternshipOffers_unauthorized() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getMyInternshipOffers(anyLong(), anyLong(), anyString(), anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of(new InternshipOfferDTO(1L, LocalDateTime.now(), ApprovalStatus.WAITING, null, null,null),
                new InternshipOfferDTO(2L, LocalDateTime.now(), ApprovalStatus.WAITING, null, null,null)), PageRequest.of(5,5),5));
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        // act
        MvcResult mvcResult = mockMvc.perform(get("/student/internshipOffers?season=Automne&year=2023"
                        + "&page=5&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();


        ResultValue<List<InternshipOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        // assert
        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }
    @Test
    void getMyInternshipOffers_internOfferNotFound() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getMyInternshipOffers(anyLong(), anyLong(),anyString(),anyInt(),anyInt())).thenThrow(InternshipNotFoundException.class);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        // act
        MvcResult mvcResult = mockMvc.perform(get("/student/internshipOffers?season=Automne&year=2023"
                        + "&page=5&size=5&q=hi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();


        ResultValue<List<InternshipOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        // assert
        assertThat(resultValue.getException()).isEqualTo("NoInternshipOffersFound");
    }


    @Test
    void confirmIntershipOffer_success() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);


        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(1L, LocalDateTime.of(2025,1, 1, 23,59,59),
                ApprovalStatus.ACCEPTED, LocalDateTime.of(2024,10,20, 13,0,0), null,null);

        when(jobOfferService.confirmInternshipOffer(anyLong(), anyString(), anyLong())).thenReturn(internshipOfferDTO);
//        String json = objectMapper.writeValueAsString(internshipOfferDTO);

        // act
        MvcResult mvcResult = mockMvc.perform(put("/student/internshipOffers/1/confirmation")
                        .header("Authorization", token)
                        .param("status", "accepted"))
//                        .contentType("application/json")
//                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        // assert
        assertThat(resultValue.getValue()).isEqualTo(internshipOfferDTO);
    }

    @Test
    void confirmIntershipOffer_unauthorized() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);


        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(1L, LocalDateTime.of(2025,1, 1, 23,59,59),
                ApprovalStatus.ACCEPTED, LocalDateTime.of(2024,10,20, 13,0,0), null,null);

        when(jobOfferService.confirmInternshipOffer(anyLong(), anyString(), anyLong())).thenReturn(internshipOfferDTO);
//        String json = objectMapper.writeValueAsString(internshipOfferDTO);

        // act
        MvcResult mvcResult = mockMvc.perform(put("/student/internshipOffers/1/confirmation")
                        .header("Authorization", token)
                        .param("status", "accepted"))
//                        .contentType("application/json")
//                        .content(json))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        // assert
        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void confirmIntershipOffer_internOfferNotFound() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);


        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(1L, LocalDateTime.of(2025,1, 1, 23,59,59),
                ApprovalStatus.ACCEPTED, LocalDateTime.of(2024,10,20, 13,0,0), null,null);

        when(jobOfferService.confirmInternshipOffer(anyLong(), anyString(), anyLong())).thenThrow(InternshipNotFoundException.class);

        // act
        MvcResult mvcResult = mockMvc.perform(put("/student/internshipOffers/1/confirmation")
                        .header("Authorization", token)
                        .param("status", "accepted"))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        // assert
        assertThat(resultValue.getException()).isEqualTo("NoInternshipOffersFound");
    }

    @Test
    void confirmIntershipOffer_missingPermission()throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);


        //        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(1L, LocalDateTime.of(2025,1, 1, 23,59,59),
//                ApprovalStatus.ACCEPTED, LocalDateTime.of(2024,10,20, 13,0,0), null);

        when(jobOfferService.confirmInternshipOffer(anyLong(), anyString(), anyLong())).thenThrow(MissingPermissionsExceptions.class);
//        String json = objectMapper.writeValueAsString(internshipOfferDTO);

        // act
        MvcResult mvcResult = mockMvc.perform(put("/student/internshipOffers/1/confirmation")
                        .header("Authorization", token)
                        .param("status", "accepted"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        // assert
        assertThat(resultValue.getException()).isEqualTo("InternshipOfferNotYours");
    }

    @Test
    void confirmIntershipOffer_dateExpired() throws Exception {
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.confirmInternshipOffer(anyLong(), anyString(), anyLong())).thenThrow(DateExpiredException.class);

        // act
        MvcResult mvcResult = mockMvc.perform(put("/student/internshipOffers/1/confirmation")
                        .header("Authorization", token)
                        .param("status", "accepted"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        // assert
        assertThat(resultValue.getException()).isEqualTo("DateExpired");
    }

    @Test
    void confirmIntershipOffer_alreadyConfirmed() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);


        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(1L, LocalDateTime.of(2025,1, 1, 23,59,59),
                ApprovalStatus.ACCEPTED, LocalDateTime.of(2024,10,20, 13,0,0), null,null);

        when(jobOfferService.confirmInternshipOffer(anyLong(), anyString(), anyLong())).thenThrow(InternshipOfferAlreadyConfirmedException.class);
        //String json = objectMapper.writeValueAsString(internshipOfferDTO);

        // act
        MvcResult mvcResult = mockMvc.perform(put("/student/internshipOffers/1/confirmation")
                        .header("Authorization", token)
                        .param("status", "accepted"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        // assert
        assertThat(resultValue.getException()).isEqualTo("InternshipOfferAlreadyConfirmed");
    }

    @Test
    void confirmIntershipOffer_invalidApprovalStatus() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);


        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(1L, LocalDateTime.of(2025,1, 1, 23,59,59),
                ApprovalStatus.ACCEPTED, LocalDateTime.of(2024,10,20, 13,0,0), null,null);

        when(jobOfferService.confirmInternshipOffer(anyLong(), anyString(), anyLong())).thenThrow(IllegalArgumentException.class);
        //String json = objectMapper.writeValueAsString(internshipOfferDTO);

        // act
        MvcResult mvcResult = mockMvc.perform(put("/student/internshipOffers/1/confirmation")
                        .header("Authorization", token)
                        .param("status", "accepted"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        // assert
        assertThat(resultValue.getException()).isEqualTo("InvalidApprovalStatus");
    }



    @Test
    void confirmIntershipOffer_databaseError() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);


        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(1L, LocalDateTime.of(2025,1, 1, 23,59,59),
                ApprovalStatus.ACCEPTED, LocalDateTime.of(2024,10,20, 13,0,0), null,null);

        when(jobOfferService.confirmInternshipOffer(anyLong(), anyString(), anyLong())).thenThrow(JobNotFoundException.class);
        //String json = objectMapper.writeValueAsString(internshipOfferDTO);

        // act
        MvcResult mvcResult = mockMvc.perform(put("/student/internshipOffers/1/confirmation")
                        .header("Authorization", token)
                        .param("status", "accepted"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        // assert
        assertThat(resultValue.getException()).isEqualTo("DatabaseError");
    }

    @Test
    void getJobOffersStats_ShouldReturnJobOffersStats_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String season = "Automne";
        String year = "2024";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);  // Valid EMPLOYEUR role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        SessionDTO sessionDTO = new SessionDTO(1L, season, year, "", "");
        when(jobOfferService.getSessionFromDB(season, year)).thenReturn(sessionDTO);

        // Mock job offer stats data
        Map<Long, JobOfferStatsDTO> jobOffersStatsDTO = new HashMap<>();
        jobOffersStatsDTO.put(1L, new JobOfferStatsDTO(1, 3, 2, 1)); // Mocked stats data

        when(jobOfferService.getJobOffersStats(userDTO.id(), sessionDTO.id())).thenReturn(jobOffersStatsDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers/stats?season=" + season + "&year=" + year)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<Map<Long, JobOfferStatsDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {}
        );
        assertThat(resultValue.getValue()).isNotNull();
        assertThat(resultValue.getValue().size()).isEqualTo(1);
        assertThat(resultValue.getValue().get(1L).totalNbApplications()).isEqualTo(1);
        assertThat(resultValue.getValue().get(1L).nbInternsNeeded()).isEqualTo(3);
        assertThat(resultValue.getValue().get(1L).nbInternshipOffersSent()).isEqualTo(2);
        assertThat(resultValue.getValue().get(1L).nbInternshipOffersAccepted()).isEqualTo(1);
    }

    @Test
    void getJobOffersStats_ShouldReturnForbidden_WhenUserRoleIsNotEmployeur() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String season = "Automne";
        String year = "2024";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);  // Non-EMPLOYEUR role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers/stats?season=" + season + "&year=" + year)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        // Assert
        ResultValue<Map<Long, JobOfferStatsDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {}
        );
        assertThat(resultValue.getException()).isEqualTo("User is not authorized to access this resource");
    }

    @Test
    void getJobOffersStats_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String season = "Automne";
        String year = "2024";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(jwtUsername)).thenThrow(new UserNotFoundException("User not found"));

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers/stats?season=" + season + "&year=" + year)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        ResultValue<Map<Long, JobOfferStatsDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {}
        );
        assertThat(resultValue.getException()).isEqualTo("UserNotFound");
    }

    @Test
    void getJobOffersStats_ShouldReturnUnauthorized_WhenNoTokenIsProvided() throws Exception {
        // Arrange
        String season = "Automne";
        String year = "2024";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null); // No token provided

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobOffers/stats?season=" + season + "&year=" + year)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Assert
        ResultValue<Map<Long, JobOfferStatsDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {}
        );
        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void getJobOfferFromId_ShouldReturnJobOffer_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);  // Valid STUDENT role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        JobOfferDTO jobOfferDTO = new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Description1", null, true, true, null);
        when(jobOfferService.getJobOfferDTOById(1L)).thenReturn(jobOfferDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/jobOffers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<JobOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        assertThat(resultValue.getValue()).isNotNull();
        assertThat(resultValue.getValue().id()).isEqualTo(1L);
    }

    @Test
    void getInternshipOffer_ShouldReturnInternshipOffer_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);  // Valid STUDENT role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(1L, LocalDateTime.now().plusDays(7), ApprovalStatus.WAITING, null, null, null);
        when(jobOfferService.getInternshipOffer(1L)).thenReturn(internshipOfferDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/internshipOffers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<InternshipOfferDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        assertThat(resultValue.getValue()).isNotNull();
        assertThat(resultValue.getValue().id()).isEqualTo(1L);
    }

    @Test
    void getListAcademicSessions_ShouldReturnAcademicSessions_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);  // Valid STUDENT role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        List<SessionDTO> sessionDTOList = new ArrayList<>();
        sessionDTOList.add(new SessionDTO(1L, "Automne", "2023", "", ""));
        sessionDTOList.add(new SessionDTO(2L, "Hiver", "2024", "", ""));
        when(jobOfferService.getAcademicSessions()).thenReturn(sessionDTOList);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/academicSessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert

        ResultValue<List<SessionDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        assertThat(resultValue.getValue()).isNotNull();
        assertThat(resultValue.getValue().size()).isEqualTo(2);
        assertThat(resultValue.getValue().get(0).id()).isEqualTo(1L);
        assertThat(resultValue.getValue().get(1).id()).isEqualTo(2L);
    }

    @Test
    void getCurrentAcademicSession_ShouldReturnCurrentAcademicSession_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);  // Valid STUDENT role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");
        when(programManagerService.getCurrentAcademicSessionDTO()).thenReturn(sessionDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/currentAcademicSession")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<SessionDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        assertThat(resultValue.getValue()).isNotNull();
        assertThat(resultValue.getValue().id()).isEqualTo(1L);
    }









}
