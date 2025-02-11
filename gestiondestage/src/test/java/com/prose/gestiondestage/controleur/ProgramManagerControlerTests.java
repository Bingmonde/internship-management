package com.prose.gestiondestage.controleur;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prose.entity.*;
import com.prose.entity.users.auth.Role;
import com.prose.presentation.Controller;
import com.prose.security.JwtTokenProvider;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.*;
import com.prose.service.Exceptions.*;
import com.prose.service.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
@ContextConfiguration
@WebAppConfiguration
public class ProgramManagerControlerTests {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeurService employeurService;

    @MockBean
    private ContractService contractService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private ProgramManagerService programManagerService;

    @MockBean
    private LoginService loginService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JobOfferService jobOfferService;

    @MockBean
    private CurriculumVitaeService curriculumVitaeService;

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
                //.apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testInscriptionProgramManagers() throws Exception {
        // Arrange
        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO(
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone",
                "mdp"
        );
        ProgramManagerDTO programManagerDTO = new ProgramManagerDTO(
                1L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone"
        );

        when(programManagerService.createProgramManager(programManagerRegisterDTO)).thenReturn(programManagerDTO);
        String json = objectMapper.writeValueAsString(programManagerRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/programManagers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ResultValue<ProgramManagerDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<ProgramManagerDTO>>() {});

        // Assert
        assertThat(result.getValue()).isEqualTo(programManagerDTO);
    }

    @Test
    void testInscriptionProgramManagersAlreadyExists() throws Exception {
        // Arrange
        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO("nom", "prénom",
                "email", "3030 rue popo", "1111111",
                "mdp");
        when(programManagerService.createProgramManager(programManagerRegisterDTO)).thenThrow(AlreadyExistsException.class);
        String json = objectMapper.writeValueAsString(programManagerRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/programManagers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        ResultValue<ProgramManagerDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<ProgramManagerDTO>>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("AlreadyExists");
    }

    @Test
    void testInscriptionProgramManagersInvalidUserFormat() throws Exception {
        // Arrange
        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO("nom", "prénom",
                "email", "3030 rue popo", "1111111",
                "mdp");
        when(programManagerService.createProgramManager(programManagerRegisterDTO)).thenThrow(InvalidUserFormatException.class);
        String json = objectMapper.writeValueAsString(programManagerRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/programManagers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<ProgramManagerDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<ProgramManagerDTO>>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("InvalidUserFormat");
    }

    @Test
    void getUserProgramManagerTest() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO = new ProgramManagerDTO(
                1L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone"
        );
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(programManagerService.getProgramManager(anyLong())).thenReturn(programManagerDTO);

        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        // Act
        MvcResult mvcResult = mockMvc.perform(get("/userinfo/username")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test"))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<String> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {});
        // Assert
        assertThat(resultValue.getValue()).isEqualTo(programManagerDTO.prenom() + " " + programManagerDTO.nom());
    }


    @Test
    void createJobPermissionTest_success() throws Exception {
        // Arrange
        StudentDTO student1 = new StudentDTO(
                1l,
                "nom",
                "prenom",
                "courriel@test.com",
                "adresse",
                "telephone",
                Discipline.INFORMATIQUE.getTranslation()
        );
        StudentDTO student2 = new StudentDTO(
                2l,
                "nom",
                "prenom",
                "courriel1@test.com",
                "adresse",
                "telephone",
                Discipline.ACCOUNTING.getTranslation()
        );
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn(jwtUsername);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        JobPermissionRegisterDTO jobPermissionRegisterDTO = new JobPermissionRegisterDTO(
                new ArrayList<String>(List.of("informatique", "accounting")),
                new ArrayList<Long>(List.of(1L, 2L)),
                LocalDate.of(2025, 2, 1),
                true
        );
        JobPermissionDTO jobPermissionDTO = new JobPermissionDTO(
                1L,
                new ArrayList<>(List.of(Discipline.INFORMATIQUE, Discipline.ACCOUNTING)),
                new ArrayList<>(List.of(student1, student2)),
                LocalDate.of(2025, 2, 1),
                true
        );

        when(programManagerService.createJobPermission(anyLong(), any(JobPermissionRegisterDTO.class),any())).thenReturn(jobPermissionDTO);
        String json = objectMapper.writeValueAsString(jobPermissionRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/validateInternship/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        ResultValue<JobPermissionDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<JobPermissionDTO>>() {});

        // Assert
        assertThat(result.getValue()).isEqualTo(jobPermissionDTO);
    }

    @Test
    void createJobPermissionTest_unauthorized() throws Exception {

        // Arrange
        String token = "Bearer validToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        JobPermissionRegisterDTO jobPermissionRegisterDTO = new JobPermissionRegisterDTO(
                new ArrayList<String>(List.of("informatique", "accounting")),
                new ArrayList<Long>(List.of(1L, 2L)),
                LocalDate.of(2025, 2, 1),
                true
        );

        String json = objectMapper.writeValueAsString(jobPermissionRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/validateInternship/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<JobPermissionDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<JobPermissionDTO>>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("JWT");

    }

    @Test
    void createJobPermissionTest_jobOffreNotFound() throws Exception{
        // Arrange
        StudentDTO student1 = new StudentDTO(
                1L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone",
                Discipline.INFORMATIQUE.getTranslation()
        );
        StudentDTO student2 = new StudentDTO(
                2L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone",
                Discipline.ACCOUNTING.getTranslation()
        );
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        JobPermissionRegisterDTO jobPermissionRegisterDTO = new JobPermissionRegisterDTO(
                new ArrayList<String>(List.of("informatique", "accounting")),
                new ArrayList<Long>(List.of(1L, 2L)),
                LocalDate.of(2025, 2, 1),
                true
        );
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(programManagerService.createJobPermission(anyLong(), any(JobPermissionRegisterDTO.class),any())).thenThrow(new JobNotFoundException());
        String json = objectMapper.writeValueAsString(jobPermissionRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/validateInternship/1")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<JobPermissionDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<JobPermissionDTO>>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("JobOfferNotFound");
    }

    @Test
    void createJobPermissionTest_disciplineNotFound() throws Exception{
        // Arrange
        StudentDTO student1 = new StudentDTO(
                1L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone",
                Discipline.INFORMATIQUE.getTranslation()
        );
        StudentDTO student2 = new StudentDTO(
                2L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone",
                Discipline.ACCOUNTING.getTranslation()
        );
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        JobPermissionRegisterDTO jobPermissionRegisterDTO = new JobPermissionRegisterDTO(
                new ArrayList<String>(List.of("informatique", "accounting")),
                new ArrayList<Long>(List.of(1L, 2L)),
                LocalDate.of(2025, 2, 1),
                true
        );
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(programManagerService.createJobPermission(anyLong(), any(JobPermissionRegisterDTO.class),any())).thenThrow(new DisciplineNotFoundException());
        String json = objectMapper.writeValueAsString(jobPermissionRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/validateInternship/1")
                        .header("Authorization",  "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<JobPermissionDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<JobPermissionDTO>>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("DisciplineNotFound");

    }

    @Test
    void createJobPermissionTest_UserNotFoundException() throws Exception{
        // Arrange
        StudentDTO student1 = new StudentDTO(
                1L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone",
                Discipline.INFORMATIQUE.getTranslation()
        );
        StudentDTO student2 = new StudentDTO(
                2L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone",
                Discipline.ACCOUNTING.getTranslation()
        );
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        JobPermissionRegisterDTO jobPermissionRegisterDTO = new JobPermissionRegisterDTO(
                new ArrayList<String>(List.of("informatique", "accounting")),
                new ArrayList<Long>(List.of(1L, 2L)),
                LocalDate.of(2025, 2, 1),
                true
        );
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(programManagerService.createJobPermission(anyLong(), any(JobPermissionRegisterDTO.class),any())).thenThrow(new UserNotFoundException());
        String json = objectMapper.writeValueAsString(jobPermissionRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/validateInternship/1")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<JobPermissionDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<JobPermissionDTO>>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("StudentListContainsInvalidUser");

    }

    @Test
    void createJobPermissionTest_dtoIsNull() throws Exception{
        // Arrange
        JobPermissionRegisterDTO jobPermissionRegisterDTO = null;

        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(programManagerService.createJobPermission(anyLong(), any(JobPermissionRegisterDTO.class),any())).thenThrow(new NullPointerException());
        String json = objectMapper.writeValueAsString(jobPermissionRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/validateInternship/1")
                        .header("Authorization","test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        // Assert
        assertThat(responseContent).isEmpty();
    }

    @Test
    void getJobOffersWaitingForApproval_success() throws Exception {
        // Arrange
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
        JobOfferDTO jobOfferDTO1 = new JobOfferDTO(
                1L,
                "titre",
                "2025-03-01",
                "2025-05-01",
                "lieu",
                "remote",
                1,
                20,
                32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "petit description",
                null,
                false,
                false,
                employeurDTO
        );
        EmployeurDTO employeurDTO02 = new EmployeurDTO(
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
        JobOfferDTO jobOfferDTO2 = new JobOfferDTO(
                2L,
                "titre",
                "2025-02-01",
                "2025-06-01",
                "lieu",
                "hybrid",
                2,
                19,
                32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                "petit description",
                null,
                false,
                false,
                employeurDTO02
        );

        List<JobOfferDTO> jobOfferDTOList = new ArrayList<>(List.of(jobOfferDTO1, jobOfferDTO2));

        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(programManagerService.getJobOffersWaitingForApproval(anyLong())).thenReturn(jobOfferDTOList);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/jobOffers/waitingForApproval?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<List<JobOfferDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<List<JobOfferDTO>>>() {});
        assertThat(result.getValue().size()).isEqualTo(2);
    }

    @Test
    void getJobOffersWaitingForApproval_noJobWaitingForApproval() throws Exception {
        // Arrange
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);
        when(programManagerService.getJobOffersWaitingForApproval(anyLong())).thenThrow(new JobNotFoundException());

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/jobOffers/waitingForApproval?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        ResultValue<List<JobOfferDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<List<JobOfferDTO>>>() {});

        assertThat(result.getException()).isEqualTo("NoJobOffersWaitingForApproval");



    }

    @Test
    void validateCVTest_success() throws Exception {
        // Arrange
        StudentDTO studentDTO = new StudentDTO(
                1L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone",
                Discipline.INFORMATIQUE.getTranslation()
        );
        CurriculumVitaeDTO cvDTO = new CurriculumVitaeDTO(
                1L,
                new PDFDocuUploadDTO( "test.pdf"),
                LocalDateTime.now(),
                "approved",
                null
        );

        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(curriculumVitaeService.validateCV(anyLong(), anyString(),any())).thenReturn(cvDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(put("/intershipmanager/validateCV/1")
                        .param("approvalResult", "approved")
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<CurriculumVitaeDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<CurriculumVitaeDTO>>() {});
        // Assert
        assertThat(result.getValue().status()).isEqualTo(cvDTO.status());
    }

    @Test
    void validateCVTest_unauthorized() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "Stest@example.com";
        // Arrange
        StudentDTO studentDTO =
                new StudentDTO(
                        2L,
                        "nom",
                        "prenom",
                        "courriel",
                        "adresse",
                        "telephone",
                        Discipline.ACCOUNTING.getTranslation()
                );
        UserDTO userDTO = new UserDTO(studentDTO.id(),studentDTO.courriel(),"---",Role.STUDENT);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        // Act
        MvcResult mvcResult = mockMvc.perform(put("/intershipmanager/validateCV/1")
                        .param("approvalResult", "approved")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<CurriculumVitaeDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<CurriculumVitaeDTO>>() {});
        // Assert
        assertThat(result.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void validateCVTest_cvNotFound() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(curriculumVitaeService.validateCV(anyLong(), anyString(),any())).thenThrow(CVNotFoundException.class);

        // Act
        MvcResult mvcResult = mockMvc.perform(put("/intershipmanager/validateCV/1")
                            .param("approvalResult", "approved")
                            .header("Authorization", "test")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<CurriculumVitaeDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<CurriculumVitaeDTO>>() {});
            // Assert
            assertThat(result.getException()).isEqualTo("CVNotFound");

    }

    @Test
    void getCVsByDisciplineTest_success() throws Exception {
        // Arrange
        CurriculumVitaeDTO cvDTO = new CurriculumVitaeDTO(
                1L,
                new PDFDocuUploadDTO( "test.pdf"),
                LocalDateTime.now(),
                "waiting",
                null
        );
        StudentDTO studentDTO = new StudentDTO(
                1L,
                "nom",
                "prenom",
                "courriel",
                "adresse",
                "telephone",
                Discipline.INFORMATIQUE.getTranslation()
        );
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        //when(studentService.getStudentsByDiscipline(anyString())).thenReturn(java.util.List.of(studentDTO));
        when(curriculumVitaeService.getCVsByDiscipline(anyString())).thenReturn(java.util.List.of(cvDTO));

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/intershipmanager/validateCV/discipline/informatique")
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<List<CurriculumVitaeDTO>>>() {});

        // Assert
        assertThat(result.getValue().get(0).status()).isEqualTo(cvDTO.status());

    }


    @Test
    void getCVsByDisciplineTest_unauthorized() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "Stest@example.com";
        StudentDTO studentDTO =
                new StudentDTO(
                        2L,
                        "nom",
                        "prenom",
                        "courriel",
                        "adresse",
                        "telephone",
                        Discipline.ACCOUNTING.getTranslation()
                );
        UserDTO userDTO = new UserDTO(studentDTO.id(),studentDTO.courriel(),"---",Role.STUDENT);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/intershipmanager/validateCV/discipline/informatique")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<List<CurriculumVitaeDTO>>>() {});
        // Assert
        assertThat(result.getException()).isEqualTo("Unauthorized");

    }

    @Test
    void getCVsByDisciplineTest_disciplineNotFound() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(curriculumVitaeService.getCVsByDiscipline(anyString())).thenThrow(DisciplineNotFoundException.class);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/intershipmanager/validateCV/discipline/discipline_not_exist")
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        ResultValue<List<CurriculumVitaeDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<List<CurriculumVitaeDTO>>>() {});
        // Assert
        assertThat(result.getException()).isEqualTo("DisciplineNotFound");

    }

    @Test
    void getCVsByDisciplineTest_cvNotFound() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(curriculumVitaeService.getCVsByDiscipline(anyString())).thenThrow(CVNotFoundException.class);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/intershipmanager/validateCV/discipline/informatique")
                            .header("Authorization", "test")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<List<CurriculumVitaeDTO>>>() {});
        // Assert
        assertThat(result.getException()).isEqualTo("CVNotFound");
    }


    @Test
    void getCVsWaitingForApprovalTest_success() throws Exception {
        // Arrange
        CurriculumVitaeDTO cvDTO = new CurriculumVitaeDTO(
                1L,
                new PDFDocuUploadDTO( "test.pdf"),
                LocalDateTime.now(),
                "waiting",
                null
        );
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(curriculumVitaeService.getCVsWaitingForApproval()).thenReturn(java.util.List.of(cvDTO));

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/intershipmanager/validateCV/waitingforapproval")
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});
        // Assert
        assertThat(result.getValue().get(0).status()).isEqualTo(cvDTO.status());
    }

    @Test
    void getCVsWaitingForApprovalTest_unauthorized() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "Stest@example.com";
        StudentDTO studentDTO =
                new StudentDTO(
                        2L,
                        "nom",
                        "prenom",
                        "courriel",
                        "adresse",
                        "telephone",
                        Discipline.ACCOUNTING.getTranslation()
                );
        UserDTO userDTO = new UserDTO(studentDTO.id(),studentDTO.courriel(),"---",Role.STUDENT);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/intershipmanager/validateCV/waitingforapproval")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("Unauthorized");

    }


    @Test
    void getCVsWaitingForApprovalTest_cvNotFound() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(curriculumVitaeService.getCVsWaitingForApproval()).thenThrow(CVNotFoundException.class);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/intershipmanager/validateCV/waitingforapproval")
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});
        // Assert
        assertThat(result.getException()).isEqualTo("CVNotFound");
    }

    @Test
    void beginContractProcessTest() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");

        // Act
        mockMvc.perform(post("/internshipManager/contracts/begin/3")
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        verify(programManagerService).beginContract(eq(3L),any());
    }

    @Test
    void beginContractProcessTest_unauthorized() throws Exception {
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/contracts/begin/3L")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void beginContractProcessTest_forbidden() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.EMPLOYEUR);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/contracts/begin/3L")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void beginContractProcessNotANumberTest() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/contracts/begin/abcdef")
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getException()).isEqualTo("NotFound");
    }

    @Test
    void beginContractProcessNotFoundTest() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        doThrow(InternshipNotFoundException.class).when(programManagerService).beginContract(eq(3L),any());
        // Act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/contracts/begin/3L")
                        .header("Authorization", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        ResultValue<String> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getException()).isEqualTo("NotFound");
    }


    @Test
    void geAlltProfs_success() throws Exception {
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        TeacherDTO teacherDTO = new TeacherDTO(1L, "nom", "prenom", "courriel", "adresse", "telephone", Discipline.INFORMATIQUE.getTranslation());
        when(teacherService.getAllProfs()).thenReturn(List.of(teacherDTO));

        // act
        MvcResult mvcResult = mockMvc.perform(get("/internshipManager/profs/all")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<TeacherDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<List<TeacherDTO>>>() {});

        // assert
        assertThat(result.getValue().size()).isEqualTo(1);
        assertThat(result.getValue().get(0).id()).isEqualTo(teacherDTO.id());
    }

    @Test
    void geAlltProfs_NoProfsFound() throws Exception {
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(teacherService.getAllProfs()).thenThrow(UserNotFoundException.class);

        // act
        MvcResult mvcResult = mockMvc.perform(get("/internshipManager/profs/all")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<List<TeacherDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<List<TeacherDTO>>>() {});

        // assert
        assertThat(result.getException()).isEqualTo("NoProfsFound");
    }

    @Test
    void getInternsWaitingForAssignmentToProf_success() throws Exception {
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        InternshipOfferDTO internshipOfferDTO = new InternshipOfferDTO(1L, LocalDateTime.of(2025,1, 1, 23,59,59),
                ApprovalStatus.ACCEPTED, LocalDateTime.of(2024,10,20, 13,0,0), null,null);

        when(programManagerService.getInternsWaitingForAssignmentToProf()).thenReturn(List.of(internshipOfferDTO));

        // act
        MvcResult mvcResult = mockMvc.perform(get("/internshipManager/interns/waitingForAssignmentToProf")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<InternshipOfferDTO>> result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        // assert
        assertThat(result.getValue().size()).isEqualTo(1);
        assertThat(result.getValue().get(0).id()).isEqualTo(internshipOfferDTO.id());

    }

    @Test
    void getInternsWaitingForAssignmentToProf_noInternWaitingForAssignment() throws Exception {
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(programManagerService.getInternsWaitingForAssignmentToProf()).thenThrow(InternshipNotFoundException.class);

        // act
        MvcResult mvcResult = mockMvc.perform(get("/internshipManager/interns/waitingForAssignmentToProf")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<List<InternshipOfferDTO>> result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        // assert
        assertThat(result.getException()).isEqualTo("NoInternsWaitingForAssignmentToProf");

    }



    @Test
    void assignToProf_success() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        InternshipEvaluationDTO internshipEvaluationDTO = new InternshipEvaluationDTO(
                1l,
                new TeacherDTO(1l, "nom", "prenom", "courriel", "adresse", "telephone", Discipline.INFORMATIQUE.getTranslation()),
                new InternshipOfferDTO(1l, null, ApprovalStatus.ACCEPTED, null, null, null),
                null, null
        );

        when(programManagerService.assignToProf(anyLong(), anyLong(),any())).thenReturn(internshipEvaluationDTO);


        // act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/interns/assignToProf")
                        .header("Authorization", token)
                        .param("profId", "1")
                        .param("internshipOfferId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // assert
        ResultValue<InternshipEvaluationDTO> result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(result.getValue().id()).isEqualTo(internshipEvaluationDTO.id());
        assertThat(result.getValue().teacher().id()).isEqualTo(internshipEvaluationDTO.teacher().id());
        assertThat(result.getValue().internshipOffer().id()).isEqualTo(internshipEvaluationDTO.internshipOffer().id());

    }


    @Test
    void assignToProf_internshipNotFound() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(programManagerService.assignToProf(anyLong(), anyLong(),any())).thenThrow(InternshipNotFoundException.class);


        // act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/interns/assignToProf")
                        .header("Authorization", token)
                        .param("profId", "1")
                        .param("internshipOfferId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        // assert
        ResultValue<InternshipEvaluationDTO> result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(result.getException()).isEqualTo("InternshipNotFound");

    }


    @Test
    void assignToProf_noProfFound() throws Exception{
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);


        when(programManagerService.assignToProf(anyLong(), anyLong(),any())).thenThrow(UserNotFoundException.class);


        // act
        MvcResult mvcResult = mockMvc.perform(post("/internshipManager/interns/assignToProf")
                        .header("Authorization", token)
                        .param("profId", "1")
                        .param("internshipOfferId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        // assert
        ResultValue<InternshipEvaluationDTO> result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(result.getException()).isEqualTo("ProfNotFound");

    }
    @Test
    void updateProgramManagerPassword_Success_WhenPasswordIsUpdated() throws Exception {
        // Arrange
        Long programManagerId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "program_manager@example.com";
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";

        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO();
        updatePasswordDTO.setCurrentPassword(currentPassword);
        updatePasswordDTO.setNewPassword(newPassword);

        String jsonContent = """
    {
        "currentPassword": "oldPassword",
        "newPassword": "newPassword"
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(programManagerId, jwtUsername, currentPassword, Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        ProgramManagerDTO updatedProgramManager = new ProgramManagerDTO(programManagerId, "NewFirstName", "NewLastName", "program_manager@example.com",null,null);
        when(programManagerService.updateProgramManagerPassword(eq(programManagerId), any(UpdatePasswordDTO.class))).thenReturn(updatedProgramManager);

        // Act
        MvcResult result = mockMvc.perform(put("/userinfo/projet_manager/password/{id}", programManagerId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("NewFirstName");
        assertThat(responseBody).contains("NewLastName");
        assertThat(responseBody).contains("program_manager@example.com");

        verify(programManagerService).updateProgramManagerPassword(eq(programManagerId), any(UpdatePasswordDTO.class));
    }
    @Test
    void updateProgramManagerProfile_Success_WhenProgramManagerProfileIsUpdated() throws Exception {
        // Arrange
        Long programManagerId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "programmanager@example.com";

        String jsonContent = """
    {
        "id": 1,
        "prenom": "UpdatedFirstName",
        "nom": "UpdatedLastName"
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(programManagerId, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        ProgramManagerDTO updatedProgramManager = new ProgramManagerDTO(programManagerId, "UpdatedFirstName", "UpdatedLastName", "programmanager@example.com",null,null);
        when(programManagerService.updateProgramManager(eq(programManagerId), any(ProgramManagerDTO.class))).thenReturn(updatedProgramManager);

        // Act
        MvcResult result = mockMvc.perform(put("/userinfo/projet_manager/{id}", programManagerId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("UpdatedFirstName");
        assertThat(responseBody).contains("UpdatedLastName");

        verify(programManagerService).updateProgramManager(eq(programManagerId), any(ProgramManagerDTO.class));
    }

    @Test
    void updateProgramManagerProfile_NotFound_WhenProgramManagerDoesNotExist() throws Exception {
        // Arrange
        Long programManagerId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "programmanager@example.com";

        String jsonContent = """
    {
        "id": 1,
        "prenom": "UpdatedFirstName",
        "nom": "UpdatedLastName"
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(programManagerId, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        when(programManagerService.updateProgramManager(eq(programManagerId), any(ProgramManagerDTO.class))).thenThrow(new UserNotFoundException("Program Manager not found"));

        // Act
        ResultActions resultActions = mockMvc.perform(put("/userinfo/projet_manager/{id}", programManagerId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));

        // Assert
        resultActions.andExpect(status().isNotFound());
        verify(programManagerService).updateProgramManager(eq(programManagerId), any(ProgramManagerDTO.class));
    }


    @Test
    void getProfCurrentInterns_success() throws Exception {
        // arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        InternshipEvaluationDTO internshipEvaluationDTO = new InternshipEvaluationDTO(
                1l,
                new TeacherDTO(1l, "nom", "prenom", "courriel", "adresse", "telephone", Discipline.INFORMATIQUE.getTranslation()),
                new InternshipOfferDTO(1l, null, ApprovalStatus.ACCEPTED, null, null, null),
                null, null
        );

        when(programManagerService.getProfCurrentInterns(anyLong())).thenReturn(List.of(internshipEvaluationDTO));

        // act
        MvcResult mvcResult = mockMvc.perform(get("/internshipManager/profs/1/currentInterns")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();


        ResultValue<List<InternshipEvaluationDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<List<InternshipEvaluationDTO>>>() {});

        // assert
        assertThat(result.getValue().size()).isEqualTo(1);
        assertThat(result.getValue().getFirst().id()).isEqualTo(internshipEvaluationDTO.id());
        assertThat(result.getValue().getFirst().teacher().id()).isEqualTo(internshipEvaluationDTO.teacher().id());

    }

    // test errors only
    @Test
    void createEvaluationInternEvaluation_InvalidJWTToken() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employer/evaluations/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Assert
        ResultValue<EvaluationInternDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<EvaluationInternDTO>>() {
                });

        assertThat(result.getException()).isEqualTo("Invalid JWT Token");
    }

    @Test
    void createEvaluationInternEvaluation_UserNotFound() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(jwtUsername)).thenThrow(new UserNotFoundException("User not found"));

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employer/evaluations/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        ResultValue<EvaluationInternDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<EvaluationInternDTO>>() {
                });

        assertThat(result.getException()).isEqualTo("UserNotFound");
    }

    @Test
    void createEvaluationInternEvaluation_Forbidden() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employer/evaluations/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andReturn();

        // Assert
        ResultValue<EvaluationInternDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<EvaluationInternDTO>>() {
                });

        assertThat(result.getException()).isEqualTo("Forbidden");
    }

    @Test
    void getNonValidatedOffers_InvalidJWTToken() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/internshipManager/nonValidatedOffers")
                        .header("Authorization", token)
                        .param("season", "fall")
                        .param("year", "2021")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Assert
        ResultValue<PageResultValueList<JobOfferDTO>> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<PageResultValueList<JobOfferDTO>>>() {
                });

        assertThat(result.getException()).isEqualTo("JWT");
    }





}

