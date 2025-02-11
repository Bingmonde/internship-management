package com.prose.gestiondestage.controleur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prose.entity.ResultValue;
import com.prose.entity.users.auth.Role;
import com.prose.presentation.Controller;
import com.prose.security.JwtTokenProvider;
import com.prose.service.*;
import com.prose.service.dto.UserDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
@ContextConfiguration
@WebAppConfiguration
public class PdfServiceControllerTests {
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
    void printContract_ShouldReturnContract_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        byte[] contract = new byte[10];
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);  // Valid STUDENT role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        when(pdfService.printContract(1L, userDTO)).thenReturn(contract);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/print/contracts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<byte[]> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        assertThat(resultValue.getValue()).isNotNull();
    }

    @Test
    void printEvaluationEmployer_ShouldReturnEvaluationEmployer_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        byte[] evaluationEmployer = new byte[10];
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);  // Valid PROGRAM_MANAGER role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        when(pdfService.printEmployerEvaluation(1L, userDTO)).thenReturn(evaluationEmployer);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/print/evaluationEmployer/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<byte[]> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        assertThat(resultValue.getValue()).isNotNull();
    }

    @Test
    void printEvaluationIntern_ShouldReturnEvaluationIntern_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        byte[] evaluationIntern = new byte[10];
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.PROGRAM_MANAGER);  // Valid PROGRAM_MANAGER role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        when(pdfService.printInternEvaluation(1L, userDTO.id())).thenReturn(evaluationIntern);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/print/evaluationIntern/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<byte[]> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        assertThat(resultValue.getValue()).isNotNull();
    }
}
