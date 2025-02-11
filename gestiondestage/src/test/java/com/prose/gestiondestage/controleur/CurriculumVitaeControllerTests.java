package com.prose.gestiondestage.controleur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prose.entity.ApprovalStatus;
import com.prose.entity.ResultValue;
import com.prose.entity.users.auth.Role;
import com.prose.presentation.Controller;
import com.prose.security.JwtTokenProvider;
import com.prose.service.*;
import com.prose.service.Exceptions.FileNotFoundException;
import com.prose.service.dto.*;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
@ContextConfiguration
@WebAppConfiguration
public class CurriculumVitaeControllerTests {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CurriculumVitaeService curriculumVitaeService;

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
    private ContractService contractService;

    @MockBean
    private LoginService loginService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

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
    void testAddCV_Success() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        String formattedDate = "2024-10-02T18:17:24Z";
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(formattedDate);

        CurriculumVitaeDTO curriculumVitaeDTO = new CurriculumVitaeDTO(
                1L,
                null,
                LocalDateTime.now(),
                ApprovalStatus.WAITING.toString(),
                null
        );

        String cvJson = "{\n" +
                "  \"id\": 1,\n" +
                "  \"pdfDocu\": null,\n" +
                "  \"dateHeureAjout\": \"" + formattedDate + "\"\n" +
                " Bonjour, je suis un étudiant qui étudie\n" +
                "}";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.STUDENT);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(curriculumVitaeService.addCV(any(), any(), eq("test@example.com")))
                .thenReturn(curriculumVitaeDTO);

        MockMultipartFile cvPart = new MockMultipartFile("CV", "", "application/json", cvJson.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart("/students/CV")
                        .file(cvPart)
                        .file(filePart)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<CurriculumVitaeDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue()).isEqualTo(curriculumVitaeDTO);
        //}
    }

    @Test
    void testAddCV_Unauthorized() throws Exception {
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        MockMultipartFile cvPart = new MockMultipartFile("CV", "", "application/json", "{}".getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart("/students/CV")
                        .file(cvPart)
                        .file(filePart)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<CurriculumVitaeDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void testAddCV_Forbidden() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.EMPLOYEUR);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MockMultipartFile cvPart = new MockMultipartFile("CV", "", "application/json", "{}".getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart("/students/CV")
                        .file(cvPart)
                        .file(filePart)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<CurriculumVitaeDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
        //}
    }

    @Test
    void testAddCV_IOException() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        String formattedDate = "2024-10-02T18:17:24Z";
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(formattedDate);

        String cvJson = "{\n" +
                "  \"id\": 1,\n" +
                "  \"pdfDocu\": null,\n" +
                "  \"dateHeureAjout\": \"" + formattedDate + "\"\n" +
                " Bonjour, je suis un étudiant qui étudie\n" +
                "}";

//        CurriculumVitaeDTO curriculumVitaeDTO = new CurriculumVitaeDTO(
//                1L,
//                null,
//                formattedDate
//        );

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.STUDENT);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(curriculumVitaeService.addCV(any(), any(), eq("test@example.com")))
                .thenThrow(new IOException());

        MockMultipartFile cvPart = new MockMultipartFile("CV", "", "application/json", cvJson.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart("/students/CV")
                        .file(cvPart)
                        .file(filePart)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<CurriculumVitaeDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("ReadingPDFFails");
        //}
    }

    @Test
    void testGetMyCVsAsStudent_Success() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.STUDENT);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        String formattedDate = "2024-10-02T18:17:24Z";

        List<CurriculumVitaeDTO> curriculumVitaes = Arrays.asList(
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.WAITING.toString(),null),
                new CurriculumVitaeDTO(2L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null)
        );

        when(curriculumVitaeService.getCVsByStudentId(1L,5,5)).thenReturn(new PageImpl<>(curriculumVitaes,PageRequest.of(5,5),5));

        MvcResult mvcResult = mockMvc.perform(get("/students/CV/all?page=5&size=5")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();;

        ResultValue<List<CurriculumVitaeDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue()).hasSize(2).containsExactlyElementsOf(curriculumVitaes);
    }

    @Test
    void testGetMyCVsAsStudent_Unauthorized() throws Exception {
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(get("/students/CV/all?page=5&size=5")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void testGetMyCVsAsStudent_Forbidden() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.TEACHER);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.TEACHER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MvcResult mvcResult = mockMvc.perform(get("/students/CV/all?page=5&size=5")
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void testGetMyValidatedCVsAsStudent_Success() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.STUDENT);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        String formattedDate = "2024-10-02T18:17:24Z";

        List<CurriculumVitaeDTO> curriculumVitaes = Arrays.asList(
                new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                new CurriculumVitaeDTO(2L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null)
        );

        when(curriculumVitaeService.getValidatedCVsByStudentId(1L)).thenReturn(curriculumVitaes);

        MvcResult mvcResult = mockMvc.perform(get("/students/CV/validated")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getValue()).hasSize(2).containsExactlyElementsOf(curriculumVitaes);
    }

    @Test
    void testGetMyValidatedCVsAsStudent_Unauthorized() throws Exception {
        String token = "Bearer invalidToken";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(get("/students/CV/validated")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void testGetMyValidatedCVsAsStudent_Forbidden() throws Exception {
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        /*try (MockedStatic<AuthUserUnifier> authUserUnifierMock = Mockito.mockStatic(AuthUserUnifier.class)) {
            authUserUnifierMock.when(() -> AuthUserUnifier.getUserType(jwtUsername)).thenReturn(Role.TEACHER);*/
        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.TEACHER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        MvcResult mvcResult = mockMvc.perform(get("/students/CV/validated")
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andReturn();

        ResultValue<List<CurriculumVitaeDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        assertThat(resultValue.getException()).isEqualTo("Unauthorized");
    }

    @Test
    void getCV_ShouldReturnCV_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        UserDTO userDTO = new UserDTO(1L, jwtUsername, "password", Role.STUDENT);  // Valid STUDENT role
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        CurriculumVitaeDTO curriculumVitaeDTO = new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(), null);
        when(curriculumVitaeService.getCVById(1L)).thenReturn(curriculumVitaeDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/cvs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<CurriculumVitaeDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
        assertThat(resultValue.getValue()).isNotNull();
        assertThat(resultValue.getValue().id()).isEqualTo(1L);

    }

    @Test
    void getPDF_ShouldReturnPDF_WhenRequestIsValid() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        byte[] pdf = new byte[10];
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);

        when(curriculumVitaeService.getPDF("test.pdf")).thenReturn(pdf);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/download/file/test.pdf")
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
