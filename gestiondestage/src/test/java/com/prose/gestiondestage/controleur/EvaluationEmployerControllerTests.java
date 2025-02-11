package com.prose.gestiondestage.controleur;

import com.prose.entity.*;
import com.prose.entity.users.Employeur;
import com.prose.entity.users.Teacher;
import com.prose.entity.users.auth.Role;
import com.prose.presentation.Controller;
import com.prose.security.JwtTokenProvider;
import com.prose.service.*;
import com.prose.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static com.prose.entity.EvaluationOption.TOTAL_AGREEMENT;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class EvaluationEmployerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private LoginService loginService;

    @MockBean
    private EvaluationEmployerService evaluationEmployerService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private EmployeurService employeurService;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private ProgramManagerService programManagerService;

    @MockBean
    private JobOfferService jobOfferService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CurriculumVitaeService curriculumVitaeService;

    @MockBean
    private ContractService contractService;


    @MockBean
    private PDFService pdfService;


    private EvaluationEmployerDTO evaluationDTO;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        evaluationDTO = new EvaluationEmployerDTO(
                1L,
                StageNumber.STAGE_1,
                "40", "40", "40",
                StagePreference.PREMIER_STAGE, // Preferred stage
                NumberOfInterns.UN_ETUDIANT, // Number of interns
                WillingnessToRehire.YES, // Willing to rehire
                "08:00", "09:00",
                "10:00", "12:00",
                "14:00", "16:00",
                "Excellent intern",
                "No issues observed",
                "Prof. John",
                "2024-11-05",
                // Evaluation options
                TOTAL_AGREEMENT, TOTAL_AGREEMENT, TOTAL_AGREEMENT, TOTAL_AGREEMENT,
                TOTAL_AGREEMENT, TOTAL_AGREEMENT, TOTAL_AGREEMENT, TOTAL_AGREEMENT,
                TOTAL_AGREEMENT, TOTAL_AGREEMENT // Evaluation fields
        );
    }

    @Test
    public void testCreateEvaluation_Success() throws Exception {
        // Arrange
        String token = "valid-token";
        String username = "testuser@example.com";
        String json = objectMapper.writeValueAsString(evaluationDTO);

        UserDTO userDTO = new UserDTO(1L, "Test User", "testuser@example.com", Role.TEACHER);

        Employeur employeur = new Employeur(1L, "Test Company", "Test Contact Person", "test@example.com", "password", "123 Test Street", "Test City", "A1B 2C3", "1234567890", "0987654321");
        Teacher teacher = new Teacher();
        teacher.setId(1L);

        EvaluationEmployer evaluationEmployer = new EvaluationEmployer();
        evaluationEmployer.setEmployeur(employeur);
        // Act
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(username);
        when(loginService.getUserByEmail(username)).thenReturn(userDTO);
        when(evaluationEmployerService.createEvaluation(anyLong(), any(EvaluationEmployerDTO.class), eq(userDTO)))
                .thenReturn(evaluationEmployer);
        // Assert
        mockMvc.perform(post("/teacher/evaluations/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").exists());
    }

    @Test
    public void testCreateEvaluation_MissingToken() throws Exception {
        String json = objectMapper.writeValueAsString(evaluationDTO);

        mockMvc.perform(post("/teacher/evaluations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exception").value("JWT"));
    }

    @Test
    public void testCreateEvaluation_InvalidToken() throws Exception {
        String json = objectMapper.writeValueAsString(evaluationDTO);
        String token = "invalid-token";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        mockMvc.perform(post("/teacher/evaluations/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exception").value("JWT"));
    }

    /*@Test //Pas un test du controlleur
    public void testCreateEvaluation_UserNotFound() throws Exception {
        // Arrange
        String token = "valid-token";
        String username = "test@example.com";
        String json = objectMapper.writeValueAsString(evaluationDTO);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(username);
        when(loginService.getUserByEmail(username)).thenThrow(new UserNotFoundException("User not found"));
        // Act and Assert
        mockMvc.perform(post("/teacher/evaluations/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exception").value("User not found"));
    }*/

    @Test
    public void testCreateEvaluation_NonTeacherRole() throws Exception {
        // Arrange
        String token = "valid-token";
        String username = "nonteacher@example.com";
        String json = objectMapper.writeValueAsString(evaluationDTO);

        UserDTO userDTO = new UserDTO(2L, "Non Teacher User", username, Role.EMPLOYEUR);
        //act
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(username);
        when(loginService.getUserByEmail(username)).thenReturn(userDTO);

        mockMvc.perform(post("/teacher/evaluations/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.exception").value("User is not authorized to access this resource"));
    }

    @Test
    public void testGetInternshipEvaluations_MissingToken() throws Exception {
        mockMvc.perform(get("/teacher/internshipEvaluations?season=Hiver&year=2025"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exception").value("JWT"));
    }
    @Test
    public void testGetInternshipEvaluations_Success_NoEvaluations() throws Exception {
        String token = "valid-token";
        String username = "teacher@example.com";

        UserDTO userDTO = new UserDTO(1L, "Teacher User", username, Role.TEACHER);
        SessionDTO sessionDTO = new SessionDTO(1l, "Hiver", "2025", "2024-12-1", "2024-12-21");

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(username);
        when(loginService.getUserByEmail(username)).thenReturn(userDTO);
        when(teacherService.getEmployeurByTeacher(anyLong(), anyLong())).thenReturn(List.of());
        when(jobOfferService.getSessionFromDB(anyString(), anyString())).thenReturn(sessionDTO);

        mockMvc.perform(get("/teacher/internshipEvaluations?season=Hiver&year=2025")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").isArray())
                .andExpect(jsonPath("$.value").isEmpty());
    }
    @Test
    public void testGetInternshipEvaluations_InvalidToken() throws Exception {
        String token = "invalid-token";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        mockMvc.perform(get("/teacher/internshipEvaluations?season=Hiver&year=2025")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exception").value("JWT"));
    }


    @Test
    public void testGetInternshipEvaluations_NonTeacherRole() throws Exception {
        String token = "valid-token";
        String username = "nonteacher@example.com";

        UserDTO userDTO = new UserDTO(2L, "Non Teacher User", username, Role.EMPLOYEUR);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(username);
        when(loginService.getUserByEmail(username)).thenReturn(userDTO);

        mockMvc.perform(get("/teacher/internshipEvaluations?season=Hiver&year=2025")
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.exception").value("Unauthorized"));
    }

    @Test
    public void testGetInternshipEvaluations_EvaluationsNotFound() throws Exception {
        String token = "valid-token";
        String username = "teacher@example.com";

        UserDTO userDTO = new UserDTO(1L, "Teacher User", username, Role.TEACHER);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(username);
        when(loginService.getUserByEmail(username)).thenReturn(userDTO);
        when(teacherService.getEmployeurByTeacher(anyLong(), anyLong())).thenThrow(new RuntimeException("EvaluationsNotFound"));

        mockMvc.perform(get("/teacher/internshipEvaluations?season=Hiver&year=2025")
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exception").value("EvaluationsNotFound"));
    }
    @Test
    public void testCreateEvaluation_InvalidRoleException() throws Exception {
        String token = "valid-token";
        String username = "nonteacher@example.com";

        UserDTO userDTO = new UserDTO(2L, "Non Teacher User", username, Role.STUDENT);
        String json = objectMapper.writeValueAsString(evaluationDTO);

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(username);
        when(loginService.getUserByEmail(username)).thenReturn(userDTO);

        mockMvc.perform(post("/teacher/evaluations/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.exception").value("User is not authorized to access this resource"));
    }

}
