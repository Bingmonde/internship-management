package com.prose.gestiondestage.controleur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prose.entity.*;
import com.prose.entity.ResultValue;
import com.prose.entity.users.auth.Role;
import com.prose.presentation.Controller;
import com.prose.security.JwtTokenProvider;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.*;
import com.prose.service.Exceptions.*;
import com.prose.service.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import com.prose.service.Exceptions.AlreadyExistsException;
import com.prose.service.Exceptions.InvalidUserFormatException;
import com.prose.service.dto.EmployeurDTO;
import com.prose.service.dto.EmployeurRegisterDTO;
import com.prose.service.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
@ContextConfiguration
@WebAppConfiguration
public class EmployeurControlerTests {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private CurriculumVitaeService curriculumVitaeService;

    @MockBean
    private ContractService contractService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JobOfferService jobOfferService;

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
    void testInscriptionEmployeur() throws Exception{
        // Arrange
        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO("nomCompagnie",
                "courriel", "telephone", "adresse", "mdp", "1111111111", "111111111", "fasdfads", "fax");
        EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "telephone", "montreal", "H3H 1P9", "adresse", "fax", "courriel@123.com");
        when(employeurService.createEmployeur(employeurRegisterDTO)).thenReturn(employeurDTO);
        String json = objectMapper.writeValueAsString(employeurRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/employeurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();


        ResultValue<EmployeurDTO> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<ResultValue<EmployeurDTO>>() {});

        // Assert
        assertThat(resultValue.getValue()).isEqualTo(employeurDTO);

    }

    @Test
    void testInscriptionEmployeurInvalidUserFormat() throws Exception{
        // Arrange
        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO("nomCompagnie",
                "courriel", "telephone", "adresse", "mdp","1111111111", "111111111", "fasdfads", "fax");
        when(employeurService.createEmployeur(employeurRegisterDTO)).thenThrow(new InvalidUserFormatException());
        String json = objectMapper.writeValueAsString(employeurRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/employeurs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<EmployeurDTO> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<ResultValue<EmployeurDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("InvalidUserFormat");
    }

    @Test
    void testInscriptionEmployeurAlreadyExists() throws Exception{
        // Arrange
        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO("nomCompagnie",
                "courriel", "telephone", "adresse", "123456", "1111111111", "111111111", "fasdfads", "fax");
        when(employeurService.createEmployeur(employeurRegisterDTO)).thenThrow(new AlreadyExistsException());
        String json = objectMapper.writeValueAsString(employeurRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/employeurs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        ResultValue<EmployeurDTO> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<ResultValue<EmployeurDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("AlreadyExists");
    }

    @Test
    void getUserEmployeurTest() throws Exception {
        // Arrange
                EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "1111 rue", "montreal", "H3H 1P9", "111-111-1111", "fax", "123@123.com");
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(employeurService.getEmployeur(anyLong())).thenReturn(employeurDTO);

        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
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
        assertThat(resultValue.getValue()).isEqualTo(employeurDTO.nomCompagnie());
    }

    @Test
    void createJobInterView_success() throws Exception {
        // Arrange
        StudentDTO studentDTO = new StudentDTO(4L,"Nom","Prenom","a@a.com","adresse","1234567890",Discipline.getAllTranslations().getFirst());
        CurriculumVitaeDTO curriculumVitaeDTO = new CurriculumVitaeDTO(6L,null,LocalDateTime.now(),ApprovalStatus.VALIDATED.toString(),studentDTO);
        EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "1111 rue", "montreal", "H3H 1P9", "111-111-1111", "fax", "123@123.com");
        JobOfferDTO jobOfferDTO = new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, false, false,employeurDTO);
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1l, jobOfferDTO, curriculumVitaeDTO, true, null, ApprovalStatus.NOT_APPLICABLE);

        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Online", "https://xxxx", jobOfferApplicationDTO.id());

        LocalDateTime localDateTime = LocalDateTime.now();
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(1L, localDateTime, "Online", "https://xxxx", jobOfferApplicationDTO,
                false,null, localDateTime,null);
        when(employeurService.createJobInterview(any(), any())).thenReturn(jobInterviewDTO);
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(resultValue.getValue()).isEqualTo(jobInterviewDTO);
    }

    @Test
    void createJobInterView_unauthorized() throws Exception {
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
                        //ApprovalStatus.WAITING,
                        Discipline.ACCOUNTING.getTranslation()
                );
        UserDTO userDTO = new UserDTO(studentDTO.id(),studentDTO.courriel(),"---",Role.STUDENT);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1l, null, null, true, null, ApprovalStatus.NOT_APPLICABLE);
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Online", "https://xxxx", jobOfferApplicationDTO.id());
        LocalDateTime localDateTime = LocalDateTime.now();
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(1L, localDateTime, "Online", "https://xxxx", jobOfferApplicationDTO,
                false,null, localDateTime,null);
        when(employeurService.createJobInterview(any(), any())).thenReturn(jobInterviewDTO);
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);


        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
        ResultValue<JobInterviewDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void createEvaluationInternEvaluation_ShouldCreateEvaluation_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        long internshipId = 1L;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        // Mocking a valid user
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        // Mocking the input and output for the service
        EvaluationInternDTO evaluationDTO = new EvaluationInternDTO();
        EvaluationInternDTO savedEvaluationDTO = new EvaluationInternDTO(); // Returned object
        savedEvaluationDTO.setId(1L); // Set a value to make it non-null
        when(employeurService.saveEvaluation(internshipId, evaluationDTO, userDTO)).thenReturn(savedEvaluationDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employer/evaluations/{id}", internshipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(evaluationDTO)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    void createJobInterView_invalideInterviewType() throws Exception {
        // Arrange
        EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "1111 rue", "montreal", "H3H 1P9", "111-111-1111", "fax", "123@123.com");
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1l, null, null, true, null, ApprovalStatus.NOT_APPLICABLE);
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Telephone", "https://xxxx", jobOfferApplicationDTO.id());
        when(employeurService.createJobInterview(any(), any())).thenThrow(new InvalideInterviewTypeException());
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("InvalidInterviewType");

    }

    /*@Test
    void createJobInterView_interviewDateLessThan48hrs() throws Exception {
        // Arrange
        EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "1111 rue", "montreal", "H3H 1P9", "111-111-1111", "fax", "123@123.com");
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1l, null, null, true, null, ApprovalStatus.NOT_APPLICABLE);
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Telephone", "https://xxxx", jobOfferApplicationDTO.id());
        when(employeurService.createJobInterview(any(), anyLong())).thenThrow(new InvalideDateTimeException());
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("InterviewDateNotMoreThan48hrs");

    }*/

    @Test
    void createJobInterView_InvalideDateFormat() throws Exception{
        // Arrange
        EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "1111 rue", "montreal", "H3H 1P9", "111-111-1111", "fax", "123@123.com");
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1l, null, null, true,null, ApprovalStatus.NOT_APPLICABLE);
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Telephone", "https://xxxx", jobOfferApplicationDTO.id());
        when(employeurService.createJobInterview(any(), any())).thenThrow(new IllegalArgumentException());
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("InvalidDateTimeFormat");

    }

    // Exception not thrown anywhere in code???
    /*@Test
    void createJobInterView_invalideInterviewLink() throws Exception{
        // Arrange
        EmployeurDTO employeurDTO = new EmployeurDTO(1l,"nomCompagnie",
                "courriel", "telephone", "adresse", "H1Y2L5","1111111111", "111111111", "fasdfads@123.com");
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1l, null, null, true, null, ApprovalStatus.NOT_APPLICABLE);
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Telephone", "https://xxxx", jobOfferApplicationDTO.id());
        when(employeurService.createJobInterview(any(), any())).thenThrow(new InvalideInterviewLinkException());
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("InvalidInterviewLink");
    }*/

    @Test
    void createJobInterView_jobApplicationNotFound() throws Exception{
        // Arrange
        EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "1111 rue", "montreal", "H3H 1P9", "111-111-1111", "fax", "123@123.com");
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1l, null, null, true, null, ApprovalStatus.NOT_APPLICABLE);
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Telephone", "https://xxxx", jobOfferApplicationDTO.id());
        when(employeurService.createJobInterview(any(), any())).thenThrow(new JobApplicationNotFoundException());
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("JobApplicationNotFound");
    }

    @Test
    void createJobInterView_jobApplicationNotActive() throws Exception{
        // Arrange
        EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "1111 rue", "montreal", "H3H 1P9", "111-111-1111", "fax", "123@123.com");
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1l, null, null, true, null, ApprovalStatus.NOT_APPLICABLE);
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Telephone", "https://xxxx", jobOfferApplicationDTO.id());
        when(employeurService.createJobInterview(any(), any())).thenThrow(new JobApplicationNotActiveException());
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("JobApplicationNotActive");

    }

    @Test
    void createJobInterView_jobApplicationNotEmployers() throws Exception{
        // Arrange
        EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "1111 rue", "montreal", "H3H 1P9", "111-111-1111", "fax", "123@123.com");
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(1l, null, null, true, null, ApprovalStatus.NOT_APPLICABLE);
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Telephone", "https://xxxx", jobOfferApplicationDTO.id());
        when(employeurService.createJobInterview(any(), any())).thenThrow(new MissingPermissionsExceptions());
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("JobApplicationNotYours");

    }

    @Test
    void createJobInterView_constainsNullFields() throws Exception{
        // Arrange
        EmployeurDTO employeurDTO = new EmployeurDTO(1L, "nomCompagnie",
                "wang", "1111 rue", "montreal", "H3H 1P9", "111-111-1111", "fax", "123@123.com");
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        UserDTO userDTO = new UserDTO(employeurDTO.id(),employeurDTO.courriel(),"---",Role.EMPLOYEUR);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        JobOfferApplicationDTO jobOfferApplicationDTO = new JobOfferApplicationDTO(null, null, null, true, null, ApprovalStatus.NOT_APPLICABLE);
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z", "Telephone", "https://xxxx", jobOfferApplicationDTO.id());
        when(employeurService.createJobInterview(any(), any())).thenThrow(new NullPointerException());
        String json = objectMapper.writeValueAsString(jobInterviewRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/employeur/jobInterview")
                        .header("Authorization", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<ResultValue<JobInterviewDTO>>() {});

        // Assert
        assertThat(resultValue.getException()).isEqualTo("ContainsNullFields");

    }

    @Test
    void employerGetInterviewsListEmptyTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(employeurService.getJobInterview(eq(userDTO.id()), anyLong(),anyString(),anyString(),anyString(),anyInt(),anyInt())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(5,5),5));
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobInterview?season=Automne&year=2023" +
                        "&page=5&size=5" +
                        "&startDate=2011-12-03&endDate=2011-12-04&status=confirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        // Assert

        ResultValue<List<JobInterviewDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getValue().isEmpty()).isTrue();
    }

    @Test
    void employerGetApplicationListFullTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);

        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(employeurService.getJobInterview(eq(userDTO.id()), anyLong(),anyString(),anyString(),anyString(),anyInt(),anyInt())).thenReturn(new PageImpl<>(List.of(new JobInterviewDTO(3L,null,null,null,null,false,null,null,null)),PageRequest.of(5,5),5));
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);
        // Act
        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobInterview?season=Automne&year=2023" +
                        "&page=5&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        // Assert

        ResultValue<List<JobInterviewDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getValue().size()).isEqualTo(1);
        Assertions.assertThat(resultValue.getValue().getFirst().id()).isEqualTo(3L);
    }
    @Test
    void shouldReturnUnauthorizedWhenNoJwtTokenProvided() throws Exception {
        mockMvc.perform(get("/employeur/jobApplications/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenUserRoleIsNotEmployeur() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        mockMvc.perform(get("/employeur/jobApplications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbiddenWhenJobApplicationNotFound() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getJobOfferApplication(any())).thenThrow(new JobApplicationNotFoundException());

        mockMvc.perform(get("/employeur/jobApplications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }


    @Test
    void employerGetInterviewListUnauthorizedTest() throws Exception {
        String token = "Bearer invalidToken";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);


        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobInterview?season=Automne&year=2023" +
                        "&page=5&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<List<JobInterviewDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void employerGetInterviewListForbiddenTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        MvcResult mvcResult = mockMvc.perform(get("/employeur/jobInterview?season=Automne&year=2023" +
                        "&page=5&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<List<JobInterviewDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void employerCancelInterviewUnauthorizedTest() throws Exception {
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);


        MvcResult mvcResult = mockMvc.perform(delete("/employeur/jobInterview/1")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void employerCancelInterviewForbiddenTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MvcResult mvcResult = mockMvc.perform(delete("/employeur/jobInterview/1")
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void employerCancelInterviewTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        long interviewId = 3L;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(employeurService.cancelInterview(interviewId,userDTO.id())).
                thenReturn(new JobInterviewDTO(interviewId,null,null,null,null,false,null,null,LocalDateTime.now()));

        MvcResult mvcResult = mockMvc.perform(delete("/employeur/jobInterview/"+interviewId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        Assertions.assertThat(resultValue.getValue().cancelledDate()).isNotNull();
    }

    @Test
    void employerCancelInterviewAlreadyCancelledTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        long interviewId = 3L;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(employeurService.cancelInterview(interviewId,userDTO.id()))
                .thenThrow(AlreadyExistsException.class);

        MvcResult mvcResult = mockMvc.perform(delete("/employeur/jobInterview/"+interviewId)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("AlreadyCancelled");
    }

    @Test
    void employerCancelInterviewWrongEmployerTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        long interviewId = 3L;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(employeurService.cancelInterview(interviewId,userDTO.id()))
                .thenThrow(MissingPermissionsExceptions.class);

        MvcResult mvcResult = mockMvc.perform(delete("/employeur/jobInterview/"+interviewId)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("MissingPermission");
    }

    @Test
    void employerCancelInterviewNotFoundTest() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        long interviewId = 3L;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(employeurService.cancelInterview(interviewId,userDTO.id()))
                .thenThrow(InterviewNotFoundException.class);

        MvcResult mvcResult = mockMvc.perform(delete("/employeur/jobInterview/"+interviewId)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("NotFound");
    }

    @Test
    void updateEmployerPassword_Success_WhenPasswordIsUpdated() throws Exception {
        // Arrange
        Long employerId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "employer@example.com";
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
        UserDTO userDTO = new UserDTO(employerId, jwtUsername, currentPassword, Role.EMPLOYEUR);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        EmployeurDTO updatedEmployer = new EmployeurDTO(employerId, "Company", "employer@example.com", "UpdatedContactPerson", "1234567890", "Address", "City", "12345", "1234567");
        when(employeurService.updateEmployerPassword(eq(employerId), any(UpdatePasswordDTO.class))).thenReturn(updatedEmployer);

        // Act
        MvcResult result = mockMvc.perform(put("/userinfo/employeur/password/{id}", employerId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("Company");
        assertThat(responseBody).contains("UpdatedContactPerson");

        verify(employeurService).updateEmployerPassword(eq(employerId), any(UpdatePasswordDTO.class));
    }
    @Test
    void updateEmployerProfile_Success_WhenEmployerProfileIsUpdated() throws Exception {
        // Arrange
        Long employerId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "employer@example.com";

        String jsonContent = """
    {
        "id": 1,
        "nomCompagnie": "UpdatedCompany",
        "contactPerson": "UpdatedContactPerson"
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(employerId, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        EmployeurDTO updatedEmployer = new EmployeurDTO(employerId, "UpdatedCompany", "UpdatedContactPerson", "employer@example.com", null, null, null, null, null);
        when(employeurService.updateEmployeur(eq(employerId), any(EmployeurDTO.class))).thenReturn(updatedEmployer);

        // Act
        MvcResult result = mockMvc.perform(put("/userinfo/employeur/{id}", employerId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("UpdatedCompany");
        assertThat(responseBody).contains("UpdatedContactPerson");

        verify(employeurService).updateEmployeur(eq(employerId), any(EmployeurDTO.class));
    }

    @Test
    void updateEmployerProfile_NotFound_WhenEmployerDoesNotExist() throws Exception {
        // Arrange
        Long employerId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "employer@example.com";

        String jsonContent = """
    {
        "id": 1,
        "nomCompagnie": "UpdatedCompany",
        "contactPerson": "UpdatedContactPerson"
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(employerId, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        when(employeurService.updateEmployeur(eq(employerId), any(EmployeurDTO.class))).thenThrow(new UserNotFoundException("Employer not found"));

        // Act
        ResultActions resultActions = mockMvc.perform(put("/userinfo/employeur/{id}", employerId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));

        // Assert
        resultActions.andExpect(status().isNotFound());
        verify(employeurService).updateEmployeur(eq(employerId), any(EmployeurDTO.class));
    }


    @Test
    void getJobInterviewTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        long interviewId = 1L;
        UserDTO userDTO = new UserDTO(3L, jwtUsername, "", Role.STUDENT);
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO( 1L, LocalDateTime.now(), "Online", "https://xxxx", null, false, null, null, null);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);
        when(jobOfferService.getInterviewFromId(interviewId)).thenReturn(jobInterviewDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/jobInterview/{id}", interviewId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        // Assert
        assertThat(resultValue.getValue()).isEqualTo(jobInterviewDTO);
        assertThat(resultValue.getException()).isNull();
    }

    @Test
    void getJobInterviewUnauthorizedTest() throws Exception {
        // Arrange
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/jobInterview/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        // Assert
        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void getJobInterviewNotFoundTest() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";
        long interviewId = 1L;
        UserDTO userDTO = new UserDTO(3L, jwtUsername, "", Role.STUDENT);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);
        when(jobOfferService.getInterviewFromId(interviewId)).thenThrow(new InterviewNotFoundException());

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/jobInterview/{id}", interviewId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        // Assert
        assertThat(resultValue.getException()).isEqualTo("NotFound");
    }

    @Test
    void getStudentsWithOffersByEmployerId_ShouldReturnStudentsWithOffers_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);  // Valid EMPLOYEUR role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2023", "", "");
        when(jobOfferService.getSessionFromDB("Automne", "2023")).thenReturn(sessionDTO);

        List<InternshipOfferDTO> studentOffers = new ArrayList<>();
        studentOffers.add(new InternshipOfferDTO(1L, LocalDateTime.now().plusDays(7), ApprovalStatus.WAITING, null, null, null));
        when(employeurService.getStudentsWithOffersByEmployerId(userDTO.id(), sessionDTO.id())).thenReturn(studentOffers);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/employer/students?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<List<InternshipOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        Assertions.assertThat(resultValue.getValue()).isNotNull();
        Assertions.assertThat(resultValue.getValue().size()).isEqualTo(1);
        Assertions.assertThat(resultValue.getValue().getFirst().id()).isEqualTo(1L);
    }

    @Test
    void getStudentsWithOffersByEmployerId_ShouldReturnForbidden_WhenUserRoleIsNotEmployeur() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);  // Non-EMPLOYEUR role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/employer/students?season=Automne&year=2023")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        // Assert
        ResultValue<List<InternshipOfferDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        Assertions.assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }


}
