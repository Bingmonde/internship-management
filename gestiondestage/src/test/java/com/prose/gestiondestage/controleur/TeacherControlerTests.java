package com.prose.gestiondestage.controleur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prose.entity.Discipline;
import com.prose.entity.ResultValue;
import com.prose.entity.users.auth.Role;
import com.prose.presentation.Controller;
import com.prose.security.JwtTokenProvider;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.*;
import com.prose.service.Exceptions.AlreadyExistsException;
import com.prose.service.Exceptions.InvalidPasswordException;
import com.prose.service.Exceptions.InvalidUserFormatException;
import com.prose.service.dto.*;
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
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
@ContextConfiguration
@WebAppConfiguration
public class TeacherControlerTests {

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

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JobOfferService jobOfferService;
    @Autowired
    private WebApplicationContext webApplicationContext;
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

    @Test
    void testInscriptionTeachers() throws Exception {
        // Arrange
        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO("nom", "prénom",
                "email", "3030 rue popo", "1111111",
                "mdp", "informatique");
        TeacherDTO teacherDTO = new TeacherDTO(1L, "prenom",
                "email", "3030 rue popo", "1111111",
                "mdp", Discipline.INFORMATIQUE.getTranslation());
        when(teacherService.createTeacher(teacherRegisterDTO)).thenReturn(teacherDTO);
        String json = objectMapper.writeValueAsString(teacherRegisterDTO);
        System.out.println(json);
        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ResultValue<TeacherDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<TeacherDTO>>() {
        });

        // Assert
        assertThat(result.getValue()).isEqualTo(teacherDTO);
    }

    @Test
    void testInscriptionTeachersAlreadyExists() throws Exception {
        // Arrange
        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO("nom", "prénom",
                "email", "3030 rue popo", "1111111",
                "mdp", "informatique");
        when(teacherService.createTeacher(teacherRegisterDTO)).thenThrow(AlreadyExistsException.class);
        String json = objectMapper.writeValueAsString(teacherRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        ResultValue<StudentDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<StudentDTO>>() {
        });

        // Assert
        assertThat(result.getException()).isEqualTo("AlreadyExists");
    }

    @Test
    void testInscriptionTeachersInvalidUserFormat() throws Exception {
        // Arrange
        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO("nom", "prénom",
                "email", "3030 rue popo", "1111111",
                "mdp", "informatique");
        when(teacherService.createTeacher(teacherRegisterDTO)).thenThrow(InvalidUserFormatException.class);
        String json = objectMapper.writeValueAsString(teacherRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<StudentDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<StudentDTO>>() {
        });

        // Assert
        assertThat(result.getException()).isEqualTo("InvalidUserFormat");
    }

    @Test
    void getUserTeacherTest() throws Exception {
        // Arrange
        TeacherDTO teacherDTO = new TeacherDTO(1L, "prenom",
                "email", "3030 rue popo", "1111111",
                "mdp", Discipline.INFORMATIQUE.getTranslation());
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(teacherService.getTeacher(anyLong())).thenReturn(teacherDTO);

        UserDTO userDTO = new UserDTO(teacherDTO.id(), teacherDTO.courriel(), "---", Role.TEACHER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/userinfo/username")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test"))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<String> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        // Assert
        assertThat(resultValue.getValue()).isEqualTo(teacherDTO.prenom() + " " + teacherDTO.nom());
    }

    @Test
    void getStudentsTestSuccess() throws Exception {
        // Arrange
        TeacherDTO teacherDTO = new TeacherDTO(1L, "prenom",
                "email", "3030 rue popo", "1111111",
                "mdp", Discipline.INFORMATIQUE.getTranslation());

        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(teacherService.getTeacherByEmail(anyString())).thenReturn(teacherDTO);

        List<StudentDTO> studentDTOList = List.of(new StudentDTO(1L, "prenom",
                "email", "3030 rue popo", "1111111",
                "mdp", Discipline.INFORMATIQUE.getTranslation()));
        when(teacherService.getStudentsByTeacherId(anyLong())).thenReturn(studentDTOList);

        UserDTO userDTO = new UserDTO(teacherDTO.id(), teacherDTO.courriel(), "---", Role.TEACHER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/teacher/students")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test"))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<StudentDTO>> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        // Assert
        assertThat(resultValue.getValue()).isEqualTo(studentDTOList);
    }

    @Test
    void getStudentsTestNoStudentsSuccess() throws Exception {
        // empty list, should not throw an error
        // Arrange
        TeacherDTO teacherDTO = new TeacherDTO(1L, "prenom",
                "email", "3030 rue popo", "1111111",
                "mdp", Discipline.INFORMATIQUE.getTranslation());

        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(teacherService.getTeacherByEmail(anyString())).thenReturn(teacherDTO);

        when(teacherService.getStudentsByTeacherId(anyLong())).thenReturn(List.of());

        UserDTO userDTO = new UserDTO(teacherDTO.id(), teacherDTO.courriel(), "---", Role.TEACHER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/teacher/students")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test"))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<StudentDTO>> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        // Assert
        assertThat(resultValue.getValue()).isEqualTo(List.of());
    }

    @Test
    void getStudentsTestUnauthorized() throws Exception {
        // Arrange
        String token = "Bearer validToken";
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/teacher/students")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ResultValue<List<StudentDTO>> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        // Assert
        assertThat(resultValue.getException()).isEqualTo("JWT");
    }

    @Test
    void updateUserPassword_Success_WhenPasswordIsUpdated() throws Exception {
        // Arrange
        Long teacherId = 2L;
        String token = "Bearer validToken";
        String jwtUsername = "teacher@example.com";
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
        UserDTO userDTO = new UserDTO(teacherId, jwtUsername, currentPassword, Role.TEACHER);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        TeacherDTO updatedTeacher = new TeacherDTO(teacherId, "UpdatedFirstName", "UpdatedLastName", jwtUsername, "Mathematics", null, null);
        when(teacherService.updateTeacherPassword(eq(teacherId), any(UpdatePasswordDTO.class))).thenReturn(updatedTeacher);

        // Act
        MvcResult result = mockMvc.perform(put("/userinfo/teacher/password/{id}", teacherId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("UpdatedFirstName");
        assertThat(responseBody).contains("UpdatedLastName");

        verify(teacherService).updateTeacherPassword(eq(teacherId), any(UpdatePasswordDTO.class));
    }
    @Test
    void updateUserProfile_Success_WhenTeacherProfileIsUpdated() throws Exception {
        // Arrange
        Long teacherId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "teacher@example.com";

        String jsonContent = """
    {
        "id": 1,
        "prenom": "UpdatedFirstName",
        "nom": "UpdatedLastName",
        "discipline": {
            "id": "informatics",
            "en": "Informatics",
            "fr": "Informatique"
        }
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(teacherId, jwtUsername, "password", Role.TEACHER);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        TeacherDTO updatedTeacher = new TeacherDTO(teacherId, "UpdatedFirstName", "UpdatedLastName", "teacher@example.com", "Informatics", null, null);
        when(teacherService.updateTeacher(eq(teacherId), any(TeacherDTO.class))).thenReturn(updatedTeacher);

        // Act
        MvcResult result = mockMvc.perform(put("/userinfo/teacher/{id}", teacherId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("UpdatedFirstName");
        assertThat(responseBody).contains("UpdatedLastName");
        assertThat(responseBody).contains("Informatics");

        verify(teacherService).updateTeacher(eq(teacherId), any(TeacherDTO.class));
    }

    @Test
    void updateUserProfile_NotFound_WhenTeacherDoesNotExist() throws Exception {
        // Arrange
        Long teacherId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "teacher@example.com";

        String jsonContent = """
    {
        "id": 1,
        "prenom": "UpdatedFirstName",
        "nom": "UpdatedLastName",
        "discipline": {
            "id": "informatics",
            "en": "Informatics",
            "fr": "Informatique"
        }
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(teacherId, jwtUsername, "password", Role.TEACHER);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        when(teacherService.updateTeacher(eq(teacherId), any(TeacherDTO.class))).thenThrow(new UserNotFoundException("Teacher not found"));

        // Act & Assert
        mockMvc.perform(put("/userinfo/teacher/{id}", teacherId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserProfile_Unauthorized_WhenPasswordInvalid() throws Exception {
        // Arrange
        Long teacherId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "teacher@example.com";

        String jsonContent = """
    {
        "id": 1,
        "prenom": "UpdatedFirstName",
        "nom": "UpdatedLastName",
        "discipline": {
            "id": "informatics",
            "en": "Informatics",
            "fr": "Informatique"
        }
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(teacherId, jwtUsername, "password", Role.TEACHER);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        when(teacherService.updateTeacher(eq(teacherId), any(TeacherDTO.class))).thenThrow(new InvalidPasswordException("Invalid password"));

        // Act & Assert
        mockMvc.perform(put("/userinfo/teacher/{id}", teacherId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isUnauthorized());
    }


}


