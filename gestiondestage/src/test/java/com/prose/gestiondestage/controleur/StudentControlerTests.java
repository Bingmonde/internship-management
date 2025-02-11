package com.prose.gestiondestage.controleur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prose.entity.ApprovalStatus;
import com.prose.entity.Discipline;
import com.prose.entity.ResultValue;
import com.prose.entity.users.auth.Role;
import com.prose.presentation.Controller;
import com.prose.security.JwtTokenProvider;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.*;
import com.prose.service.Exceptions.*;
import com.prose.service.dto.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
@ContextConfiguration
@WebAppConfiguration
public class StudentControlerTests {


    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @MockBean
    private EmployeurService employeurService;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private ProgramManagerService programManagerService;

    @MockBean
    private LoginService loginService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ContractService contractService;

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
    void testInscriptionStudents() throws Exception {
        // Arrange
        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO("nom", "prénom",
                "email", "3030 rue popo", "11111111",
                "mdp", Discipline.INFORMATIQUE.toString());
        StudentDTO studentDTO = new StudentDTO(1L, "prenom",
                "email", "3030 rue popo", "11111111",
                "mdp",Discipline.INFORMATIQUE.getTranslation());
        when(studentService.createStudent(studentRegisterDTO)).thenReturn(studentDTO);
        String json = objectMapper.writeValueAsString(studentRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ResultValue<StudentDTO> result = objectMapper
                .readValue(mvcResult.getResponse()
                        .getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<ResultValue<StudentDTO>>() {});

        // Assert
        assertThat(result.getValue()).isEqualTo(studentDTO);
    }

    @Test
    void testInscriptionStudentsAlreadyExists() throws Exception {
        // Arrange
        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO("nom", "prenom",
                "email", "3030 rue popo", "11111111",
                "mdp", Discipline.INFORMATIQUE.toString());
        StudentDTO studentDTO = new StudentDTO(1L, "prenom",
                "email", "3030 rue popo", "11111111", "matricule", Discipline.INFORMATIQUE.getTranslation());
        when(studentService.createStudent(studentRegisterDTO)).thenThrow(AlreadyExistsException.class);
        String json = objectMapper.writeValueAsString(studentRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        ResultValue<StudentDTO> result = objectMapper
                .readValue(mvcResult
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<ResultValue<StudentDTO>>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("AlreadyExists");
    }

    @Test
    void testInscriptionStudentsInvalidUserFormat() throws Exception {
        // Arrange
        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO("nom", "prenom",
                "email", "3030 rue popo", "11111111",
                "mdp", Discipline.INFORMATIQUE.toString());
        StudentDTO studentDTO = new StudentDTO(1L, "prenom",
                "email", "3030 rue popo", "11111111",
                "mdp",Discipline.INFORMATIQUE.getTranslation());
        when(studentService.createStudent(studentRegisterDTO)).thenThrow(InvalidUserFormatException.class);
        String json = objectMapper.writeValueAsString(studentRegisterDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/inscription/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ResultValue<StudentDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<ResultValue<StudentDTO>>() {});

        // Assert
        assertThat(result.getException()).isEqualTo("InvalidUserFormat");
    }

    @Test
    void testTranslationsDisciplines() throws Exception {
        // Arrange
        List<DisciplineTranslationDTO> translationDTOS = Discipline.getAllTranslations();
        // Act
        MvcResult mvcResult = mockMvc.perform(get("/disciplines")
                                                .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        List<DisciplineTranslationDTO> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {
        });
        // Assert
        assertThat(result).isEqualTo(translationDTOS);
    }

    @Test
    void getUserStudentTest() throws Exception {
        // Arrange
        StudentDTO studentDTO = new StudentDTO(1L, "prenom",
                "email", "3030 rue popo", "11111111",
                "mdp", Discipline.INFORMATIQUE.getTranslation());

        UserDTO userDTO = new UserDTO(studentDTO.id(),studentDTO.courriel(),"---",Role.STUDENT);

        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);
        when(studentService.getStudent(anyLong())).thenReturn(studentDTO);
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
        assertThat(resultValue.getValue()).isEqualTo(studentDTO.prenom() + " " + studentDTO.nom());
    }


    @Test
    public void getStudentsByDisciplineTest_success() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");

        String discipline = "informatique";
        List<StudentDTO> students = List.of(
                new StudentDTO(1L, "prenom1", "email1", "adresse1", "telephone1", "mdp1",Discipline.INFORMATIQUE.getTranslation()),
                new StudentDTO(2L, "prenom2", "email2", "adresse2", "telephone2", "mdp2",Discipline.INFORMATIQUE.getTranslation())
        );
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(studentService.getStudentsByDiscipline(discipline,"",5,5)).thenReturn(new PageImpl<>(students, PageRequest.of(5,5),5));

        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/students/disciplines/" + discipline + "?page=5&size=5")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<List<StudentDTO>> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {});
        assertThat(resultValue.getValue()).isEqualTo(students);

    }

    @Test
    public void getStudentsByDisciplineTest_successQuery() throws Exception {
        // Arrange
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");

        String discipline = "informatique";
        List<StudentDTO> students = List.of(
                new StudentDTO(1L, "prenom1", "email1", "adresse1", "telephone1", "mdp1",Discipline.INFORMATIQUE.getTranslation()),
                new StudentDTO(2L, "prenom2", "email2", "adresse2", "telephone2", "mdp2",Discipline.INFORMATIQUE.getTranslation())
        );
        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(studentService.getStudentsByDiscipline(discipline,"query",5,5)).thenReturn(new PageImpl<>(students, PageRequest.of(5,5),5));

        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/students/disciplines/" + discipline + "?page=5&size=5&q=query")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        ResultValue<List<StudentDTO>> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {});
        assertThat(resultValue.getValue()).isEqualTo(students);

    }

    @Test
    public void getStudentsByDisciplineTest_unauthorized() throws Exception {
        // Arrange
        String discipline = "informatique";
        String token = "Bearer validToken";
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/students/disciplines/" + discipline + "?page=5&size=5")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Assert
        ResultValue<List<StudentDTO>> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {});
        assertThat(resultValue.getException()).isEqualTo("JWT");

    }

    @Test
    public void getStudentsByDisciplineTest_disciplineNotFound() throws Exception {
        // Arrange
        String discipline = "discipline_nonExistant";
        ProgramManagerDTO programManagerDTO =
                new ProgramManagerDTO(1L,
                        "nom",
                        "prenom",
                        "scsa",
                        "sax",
                        "xsax");

        when(jwtTokenProvider.getJwtUsername(anyString())).thenReturn("test");
        when(studentService.getStudentsByDiscipline(anyString(),anyString(),eq(5),eq(5))).thenThrow(DisciplineNotFoundException.class);
        UserDTO userDTO = new UserDTO(programManagerDTO.id(),programManagerDTO.courriel(),"---",Role.PROGRAM_MANAGER);
        when(loginService.getUserByEmail(anyString())).thenReturn(userDTO);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/students/disciplines/" + discipline + "?page=5&size=5")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test"))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        ResultValue<List<StudentDTO>> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {});
        assertThat(resultValue.getException()).isEqualTo("DisciplineNotFound");

    }

    @Test
    public void getStudentsByDisciplineTest_studentsNotFound() throws Exception {
        // Arrange
        String discipline = "informatique";
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
        when(studentService.getStudentsByDiscipline(anyString(),anyString(),eq(5),eq(5))).thenThrow(UserNotFoundException.class);

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/students/disciplines/" + discipline + "?page=5&size=5")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "test"))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        ResultValue<List<StudentDTO>> resultValue = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {});
        assertThat(resultValue.getException()).isEqualTo("NoStudentsFound");
    }

    @Test
    void getListJobInterview() throws Exception {
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
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true, employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),studentDTO),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        JobInterviewDTO jobInterviewDTO2 = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true,employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),studentDTO),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        List<JobInterviewDTO> jobInterviewDTOList = List.of(jobInterviewDTO2, jobInterviewDTO);

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        SessionDTO sessionDTO = new SessionDTO(3L,"season","year","30-30-2030","30302023");

        when(studentService.getJobInterviews(anyLong(), anyLong(),anyString(),anyString(),anyString(),anyInt(),anyInt())).thenReturn(new PageImpl<>(jobInterviewDTOList,PageRequest.of(5,5),5));
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        MvcResult mvcResult = mockMvc.perform(get("/student/jobInterview?season=Automne&year=2023"
                                                    + "&page=5&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<List<JobInterviewDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        //Assert
        Assertions.assertThat(resultValue.getValue()).isEqualTo(jobInterviewDTOList);
    }

    @Test
    void getListJobInterview_IsUnauthorized() throws Exception {
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
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true,employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        JobInterviewDTO jobInterviewDTO2 = new JobInterviewDTO(

                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true,employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        List<JobInterviewDTO> jobInterviewDTOList = List.of(jobInterviewDTO2, jobInterviewDTO);

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        SessionDTO sessionDTO = new SessionDTO(3L,"season","year","30-30-2030","30302023");
        when(studentService.getJobInterviews(anyLong(),anyLong(),anyString(),anyString(),anyString(),anyInt(),anyInt())).thenReturn(new PageImpl<>(jobInterviewDTOList,PageRequest.of(5,5),5));
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        mockMvc.perform(get("/student/jobInterview?season=Automne&year=2023"
                        + "&page=5&size=5"
                        + "&startDate=2011-12-03&endDatee=2011-12-04&status=valide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void getListJobInterview_IsForbidden() throws Exception {
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
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true,employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        JobInterviewDTO jobInterviewDTO2 = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true,employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
        List<JobInterviewDTO> jobInterviewDTOList = List.of(jobInterviewDTO2, jobInterviewDTO);

        //Act

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.TEACHER);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        SessionDTO sessionDTO = new SessionDTO(3L,"season","year","30-30-2030","30302023");
        when(studentService.getJobInterviews(anyLong(),anyLong(),anyString(),anyString(),anyString(),anyInt(),anyInt())).thenReturn(new PageImpl<>(jobInterviewDTOList,PageRequest.of(5,5),5));
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);

        mockMvc.perform(get("/student/jobInterview?season=Automne&year=2023"
                        + "&page=5&size=5"
                        + "&startDate=2011-12-03&endDatee=2011-12-04&status=valide")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());

    }

    @Test
    void getListJobInterview_EmptyList() throws Exception {
        //Arrange
        String token = "Bearer validToken";
        String jwtUsername = "test@example.com";

        //Act
        SessionDTO sessionDTO = new SessionDTO(3L,"season","year","30-30-2030","30302023");
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);
        when(jobOfferService.getSessionFromDB(any(),any())).thenReturn(sessionDTO);
        when(studentService.getJobInterviews(eq(userDTO.id()),anyLong(),anyString(),anyString(),anyString(),anyInt(),anyInt())).thenThrow(JobNotFoundException.class);

        MvcResult mvcResult = mockMvc.perform(get("/student/jobInterview?season=Automne&year=2023"
                        + "&page=5&size=5"
                        + "&startDate=2011-12-03&endDatee=2011-12-04&status=valide")
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andReturn();

        ResultValue<List<JobInterviewDTO>> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        //Assert
        Assertions.assertThat(resultValue.getException()).isEqualTo("JobNotFoundExeption");
    }

    @Test
    void studentJobInterviewConfirmation() throws Exception {
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
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true,employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),studentDTO),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        //Act
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.approveJobInterview(anyLong(),any())).thenReturn(jobInterviewDTO);

        MvcResult mvcResult = mockMvc.perform(post("/student/jobInterview/confirmation/" + 2L)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        //Assert
        Assertions.assertThat(resultValue.getValue()).isEqualTo(jobInterviewDTO);
    }

    @Test
    void studentJobInterviewConfirmation_Unauthorized() throws Exception {
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
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true,employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        //Act
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(null);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.approveJobInterview(anyLong(),any())).thenReturn(jobInterviewDTO);

        mockMvc.perform(post("/student/jobInterview/confirmation/"+2L)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentJobInterviewConfirmation_Forbidden() throws Exception {
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
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true,employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        //Act
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.EMPLOYEUR);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.approveJobInterview(anyLong(),any())).thenReturn(jobInterviewDTO);

        mockMvc.perform(post("/student/jobInterview/confirmation/"+2L)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }


    @Test
    void studentJobInterviewConfirmation_NotFound() throws Exception {
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
        JobInterviewDTO jobInterviewDTO = new JobInterviewDTO(
                1L,
                LocalDateTime.now(),
                "type",
                "Location1",
                new JobOfferApplicationDTO(1L,
                        new JobOfferDTO(1L, "Job1", "2023-01-01", "2023-06-01", "Location1", "Full-time", 2, 20.0, 32,
                                LocalTime.of(8, 0), LocalTime.of(16, 0),"Description1", null, true, true,employeurDTO),
                        new CurriculumVitaeDTO(1L, null, LocalDateTime.now(), ApprovalStatus.VALIDATED.toString(),null),
                        true,
                        LocalDateTime.now(),
                        ApprovalStatus.NOT_APPLICABLE),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());

        //Act
        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(7L, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(any())).thenReturn(userDTO);

        when(jobOfferService.approveJobInterview(anyLong(),any())).thenThrow(JobNotFoundException.class);

        MvcResult mvcResult = mockMvc.perform(post("/student/jobInterview/confirmation/" + 2L)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        ResultValue<JobInterviewDTO> resultValue = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );

        //Assert
        Assertions.assertThat(resultValue.getException()).isEqualTo("JobNotFoundException");
    }
    @Test
    void updateStudentPassword_Success_WhenPasswordIsUpdated() throws Exception {
        // Arrange
        Long studentId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "student@example.com";
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
        UserDTO userDTO = new UserDTO(studentId, jwtUsername, currentPassword, Role.STUDENT);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        StudentDTO updatedStudent = new StudentDTO(studentId, "NewFirstName", "NewLastName", "student@example.com", "Mathematics", "1234567890",null);
        when(studentService.updateStudentPassword(eq(studentId), any(UpdatePasswordDTO.class))).thenReturn(updatedStudent);

        // Act
        MvcResult result = mockMvc.perform(put("/userinfo/student/password/{id}", studentId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("NewFirstName");
        assertThat(responseBody).contains("NewLastName");
        assertThat(responseBody).contains("Mathematics");

        verify(studentService).updateStudentPassword(eq(studentId), any(UpdatePasswordDTO.class));
    }
    @Test
    void updateUserProfile_Success_WhenStudentProfileIsUpdated() throws Exception {
        // Arrange
        Long studentId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "student@example.com";

        String jsonContent = """
    {
        "id": 1,
        "prenom": "UpdatedFirstName",
        "nom": "UpdatedLastName"
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(studentId, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        StudentDTO updatedStudent = new StudentDTO(studentId, "UpdatedFirstName", "UpdatedLastName", "student@example.com", null, null,null);
        when(studentService.updateStudent(eq(studentId), any(StudentDTO.class))).thenReturn(updatedStudent);

        // Act
        MvcResult result = mockMvc.perform(put("/userinfo/student/{id}", studentId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("UpdatedFirstName");
        assertThat(responseBody).contains("UpdatedLastName");

        verify(studentService).updateStudent(eq(studentId), any(StudentDTO.class));
    }

    @Test
    void updateUserProfile_NotFound_WhenStudentDoesNotExist() throws Exception {
        // Arrange
        Long studentId = 1L;
        String token = "Bearer validToken";
        String jwtUsername = "student@example.com";

        String jsonContent = """
    {
        "id": 1,
        "prenom": "UpdatedFirstName",
        "nom": "UpdatedLastName"
    }
    """;

        when(jwtTokenProvider.getJwtUsername(token)).thenReturn(jwtUsername);
        UserDTO userDTO = new UserDTO(studentId, jwtUsername, "password", Role.STUDENT);
        when(loginService.getUserByEmail(jwtUsername)).thenReturn(userDTO);

        when(studentService.updateStudent(eq(studentId), any(StudentDTO.class))).thenThrow(new UserNotFoundException("Student not found"));

        // Act
        ResultActions resultActions = mockMvc.perform(put("/userinfo/student/{id}", studentId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));

        // Assert
        resultActions.andExpect(status().isNotFound());
        verify(studentService).updateStudent(eq(studentId), any(StudentDTO.class));
    }


}
