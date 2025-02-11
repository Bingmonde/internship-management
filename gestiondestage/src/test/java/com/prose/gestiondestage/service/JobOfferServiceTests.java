package com.prose.gestiondestage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prose.entity.*;
import com.prose.entity.users.Employeur;
import com.prose.entity.users.Student;
import com.prose.repository.*;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.EvaluationEmployerService;
import com.prose.service.Exceptions.*;
import com.prose.service.JobOfferService;
import com.prose.service.NotificationService;
import com.prose.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class JobOfferServiceTests {
    private JobOfferRepository jobOfferRepository;
    private EmployeurRepository employeurRepository;
    private PDFDocuRepository pdfDocuRepository;
    private JobOfferService jobOfferService;
    PasswordEncoder passwordEncoder;

    private StudentRepository studentRepository;

    private CurriculumVitaeRepository curriculumVitaeRepository;
    @Mock
    private InternshipOfferRepository internshipOfferRepository;
    private JobPermissionRepository jobPermissionRepository;
    private JobOfferApplicationRepository jobOfferApplicationRepository;
    public JobInterviewRepository jobInterviewRepository;
    public AcademicSessionRepository academicSessionRepository;

    private NotificationService notificationService;


    private EvaluationEmployerService evaluationEmployerService;
    /*@InjectMocks
    private InternshipOfferService internshipOfferService;*/



    @BeforeEach
    void setup() {
        jobOfferRepository = mock(JobOfferRepository.class);
        employeurRepository = mock(EmployeurRepository.class);
        pdfDocuRepository = mock(PDFDocuRepository.class);
        studentRepository = mock(StudentRepository.class);
        curriculumVitaeRepository = mock(CurriculumVitaeRepository.class);
        jobPermissionRepository = mock(JobPermissionRepository.class);
        jobOfferApplicationRepository = mock(JobOfferApplicationRepository.class);
        internshipOfferRepository = mock(InternshipOfferRepository.class);
        jobInterviewRepository = mock(JobInterviewRepository.class);
        academicSessionRepository = mock(AcademicSessionRepository.class);

        notificationService = mock(NotificationService.class);

        jobOfferService = new JobOfferService(jobOfferRepository, employeurRepository, pdfDocuRepository, studentRepository, curriculumVitaeRepository, jobPermissionRepository, jobOfferApplicationRepository, internshipOfferRepository, notificationService, jobInterviewRepository, academicSessionRepository);
        //MockitoAnnotations.openMocks(this); // Breaks some other, random test
    }

    @Test
    void createJobOfferShouldReturnJobOfferDTOWhenSuccess() throws IOException {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Developer", "2023-01-01", "2023-12-31", "Remote", "Full-time", 3, 5000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop software");
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@test.com", "mdp");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Developer");
        jobOffer.setEmployeur(employeur);
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());
        jobOffer.setEmployeur(employeur);

        when(employeurRepository.findUserAppByEmail(employeur.getEmail())).thenReturn(Optional.of(employeur));
        when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(jobOffer);

        // Act
        JobOfferDTO result = jobOfferService.createJobOffer(registerDTO, file, employeur.getEmail());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.titre()).isEqualTo("Developer");
    }

    @Test
    void createJobOfferShouldThrowUserNotFoundExceptionIfEmployeurNotFound() {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Developer", "2023-01-01", "2023-12-31", "Remote", "Full-time", 3, 5000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop software");
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        when(employeurRepository.findUserAppByEmail("a")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobOfferService.createJobOffer(registerDTO, file, "a"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Employeur with email a not found");
    }

    @Test
    void getJobOffersByEmployeurShouldReturnListOfJobOfferDTOs() {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa", "dasda");

        JobOffer jobOffer1 = new JobOffer();
        jobOffer1.setId(1L);
        jobOffer1.setTitre("Developer");
        jobOffer1.setEmployeur(employeur);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Designer");
        jobOffer2.setEmployeur(employeur);

        when(jobOfferRepository.findByEmployeurId(1L, 2L)).thenReturn(Arrays.asList(jobOffer1, jobOffer2));

        // Act
        List<JobOfferDTO> result = jobOfferService.getJobOffersByEmployeur(1L, 2L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).titre()).isEqualTo("Developer");
        assertThat(result.get(1).titre()).isEqualTo("Designer");
    }

    @Test
    void updateJobOfferShouldReturnUpdatedJobOfferDTO() throws MissingPermissionsExceptions {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Senior Developer", "2023-01-01", "2023-12-31", "Remote", "Full-time", 4, 7000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop complex software");
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@test.com", "mdp");

        jobOffer.setEmployeur(employeur);

        when(employeurRepository.findUserAppByEmail(employeur.getEmail())).thenReturn(Optional.of(employeur));
        when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
        when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(jobOffer);

        // Act
        JobOfferDTO result = jobOfferService.updateJobOffer(1L, registerDTO, employeur.getEmail());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.titre()).isEqualTo("Senior Developer");
    }

    @Test
    void updateJobOffer_MissingPermisson() throws MissingPermissionsExceptions {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Senior Developer", "2023-01-01", "2023-12-31", "Remote", "Full-time", 4, 7000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop complex software");
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@test.com", "mdp");

        Employeur employeur2 = new Employeur();
        employeur2.setId(2L);
        employeur2.setCredentials("test2@test.com", "mdp");

        jobOffer.setEmployeur(employeur);

        when(employeurRepository.findUserAppByEmail(employeur2.getEmail())).thenReturn(Optional.of(employeur2));
        when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
        when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(jobOffer);

        try {
            jobOfferService.updateJobOffer(1L, registerDTO, employeur2.getEmail());
        } catch (MissingPermissionsExceptions e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void updateJobOfferShouldThrowUserNotFoundExceptionIfNotFound() {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Senior Developer", "2023-01-01", "2023-12-31", "Remote", "Full-time", 4, 7000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop complex software");

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@test.com", "mdp");

        when(employeurRepository.findUserAppByEmail(employeur.getEmail())).thenReturn(Optional.of(employeur));
        when(jobOfferRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobOfferService.updateJobOffer(1L, registerDTO, employeur.getEmail()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Job Offer with ID 1 not found");
    }

    @Test
    void createJobOfferShouldThrowIOExceptionIfFileInvalid() {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Developer", "2023-01-01", "2023-12-31", "Remote", "Full-time", 3, 5000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop software");
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@test.com", "mdp");
        MultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "Invalid content".getBytes());

        when(employeurRepository.findUserAppByEmail(employeur.getEmail())).thenReturn(Optional.of(employeur));

        // Act & Assert
        assertThatThrownBy(() -> jobOfferService.createJobOffer(registerDTO, invalidFile, employeur.getEmail()))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Invalid file type, only PDF files are allowed");
    }

    @Test
    void createJobOfferShouldThrowIOExceptionForEmptyFile() throws IOException {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Developer", "2023-01-01", "2023-12-31", "Remote", "Full-time", 3, 5000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop software");
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]); // 模拟一个空文件

        Employeur mockEmployeur = new Employeur();
        mockEmployeur.setId(1L);
        mockEmployeur.setNomCompagnie("TestCompany");

        when(employeurRepository.findUserAppByEmail(anyString())).thenReturn(Optional.of(mockEmployeur));

        when(jobOfferRepository.save(any(JobOffer.class))).thenAnswer(invocation -> {
            JobOffer jobOffer = invocation.getArgument(0);
            jobOffer.setId(1L);
            return jobOffer;
        });

        // Act
        Throwable thrown = catchThrowable(() -> jobOfferService.createJobOffer(registerDTO, emptyFile, "test@test.com"));

        // Assert
        assertThat(thrown).isInstanceOf(IOException.class)
                .hasMessageContaining("Empty file or no file uploaded");
    }


    @Test
    void createJobOfferShouldThrowIOExceptionForLargeFile() throws IOException {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Developer", "2023-01-01", "2023-12-31", "Remote", "Full-time", 3, 5000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop software");
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@test.com", "mdp");

        byte[] largeContent = new byte[11 * 1024 * 1024]; // Plus 10MB
        MultipartFile largeFile = new MockMultipartFile("file", "test.pdf", "application/pdf", largeContent);

        // Mock
        when(employeurRepository.findUserAppByEmail(employeur.getEmail())).thenReturn(Optional.of(employeur));
        when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(new JobOffer());

        // Act
        Throwable thrown = catchThrowable(() -> jobOfferService.createJobOffer(registerDTO, largeFile, employeur.getEmail()));

        // Assert
        assertThat(thrown).isInstanceOf(IOException.class)
                .hasMessageContaining("File is too large");
    }

    @Test
    void updateJobOfferShouldThrowUserNotFoundExceptionIfEmployeurNotFound() {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Senior Developer", "2024-01-01", "2024-12-31",
                "Remote", "Full-time", 4, 7000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop complex software");

        when(employeurRepository.findUserAppByEmail("test@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobOfferService.updateJobOffer(1L, registerDTO, "test@test.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Employeur with email test@test.com not found");
    }

    @Test
    void createJobOfferShouldThrowIOExceptionWhenSavePDFFails() throws IOException {
        // Arrange
        JobOfferRegisterDTO registerDTO = new JobOfferRegisterDTO("Developer", "2023-01-01", "2023-12-31",
                "Remote", "Full-time", 3, 5000.0, 32,
                LocalTime.of(8, 0), LocalTime.of(16, 0), "Develop software");

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@test.com", "mdp");
        employeur.setNomCompagnie("TestCompany");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getBytes()).thenThrow(new IOException("Simulated IOException"));

        when(employeurRepository.findUserAppByEmail(employeur.getEmail())).thenReturn(Optional.of(employeur));

        // Act & Assert
        assertThatThrownBy(() -> jobOfferService.createJobOffer(registerDTO, file, employeur.getEmail()))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Reading PDF file failed");
    }

    @Test
    void getJobOffersForStudentValidatedCVOnlyOneOfferValidated() {
        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa", "dasda");

        JobOffer jobOffer1 = new JobOffer();
        jobOffer1.setId(1L);
        jobOffer1.setTitre("Junior Developer");
        jobOffer1.setApproved(true);
        jobOffer1.setEmployeur(employeur);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Senior Developer");
        jobOffer2.setApproved(true);
        jobOffer2.setEmployeur(employeur);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("test@test.com", "mdp");
        student.setDiscipline(Discipline.INFORMATIQUE);

        // cv validé
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        // cv validé
        CurriculumVitae cv2 = new CurriculumVitae();
        cv2.setId(2L);
        cv2.setStudent(student);
        cv2.setStatus(ApprovalStatus.WAITING);

        // approuver les offres de stage
        JobPermission jobPermission = new JobPermission();
        jobPermission.setId(1L);
        jobPermission.setJobOffer(jobOffer1);
        jobPermission.setDisciplines("informatique");
        jobPermission.setStudents(List.of(student));
        jobPermission.setExpirationDate(null);

        when(jobOfferRepository.findAll()).thenReturn(Arrays.asList(jobOffer1, jobOffer2));
        when(studentRepository.findUserAppByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(curriculumVitaeRepository.findByStudentId(student.getId())).thenReturn(List.of(cv, cv2));
        when(jobPermissionRepository.findBySessionIdAndSearch(anyLong(), anyString(), any(), anyString(), any())).thenReturn(new PageImpl<>(List.of(jobPermission), PageRequest.of(5, 5), 5));


        try {
            List<JobOfferDTO> result = jobOfferService.getJobOffersForStudent(student.getEmail(), anyLong(), "", 5, 5).getContent();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).titre()).isEqualTo("Junior Developer");
        } catch (MissingPermissionsExceptions e) {
            e.printStackTrace();
        }
    }

    @Test
    void getJobOffersForStudentNoValidatedCV() {
        JobOffer jobOffer1 = new JobOffer();
        jobOffer1.setId(1L);
        jobOffer1.setTitre("Junior Developer");
        jobOffer1.setApproved(true);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Senior Developer");
        jobOffer2.setApproved(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("test@test.com", "mdp");

        // cv non validé
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.WAITING);

        when(jobOfferRepository.findAll()).thenReturn(Arrays.asList(jobOffer1, jobOffer2));
        when(studentRepository.findUserAppByEmail(student.getEmail())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> jobOfferService.getJobOffersForStudent(student.getEmail(), anyLong(), "", 5, 5))
                .isInstanceOf(MissingPermissionsExceptions.class)
                .hasMessageContaining("Student with email " + student.getEmail() + " does not have a validated CV");
    }

    @Test
    void getJobOffersForStudentNoCV() {
        JobOffer jobOffer1 = new JobOffer();
        jobOffer1.setId(1L);
        jobOffer1.setTitre("Junior Developer");
        jobOffer1.setApproved(true);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Senior Developer");
        jobOffer2.setApproved(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("test@test.com", "mdp");

        when(jobOfferRepository.findAll()).thenReturn(Arrays.asList(jobOffer1, jobOffer2));
        when(studentRepository.findUserAppByEmail(student.getEmail())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> jobOfferService.getJobOffersForStudent(student.getEmail(), anyLong(), "", 5, 5))
                .isInstanceOf(MissingPermissionsExceptions.class)
                .hasMessageContaining("Student with email " + student.getEmail() + " does not have a validated CV");
    }

    @Test
    void getJobOffersForStudentNonExistentStudent() {
        JobOffer jobOffer1 = new JobOffer();
        jobOffer1.setId(1L);
        jobOffer1.setTitre("Junior Developer");
        jobOffer1.setApproved(true);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Senior Developer");
        jobOffer2.setApproved(true);

        when(jobOfferRepository.findAll()).thenReturn(Arrays.asList(jobOffer1, jobOffer2));
        when(studentRepository.findUserAppByEmail("test@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobOfferService.getJobOffersForStudent("test@test.com", anyLong(), "", 5, 5))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Student with email test@test.com not found");
    }

    @Test
    void getJobOffersForStudentOnlyOneApprovedJobOffer() {
        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa", "dasda");

        JobOffer jobOffer1 = new JobOffer();
        jobOffer1.setId(1L);
        jobOffer1.setTitre("Junior Developer");
        jobOffer1.setApproved(false);
        jobOffer1.setEmployeur(employeur);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Senior Developer");
        jobOffer2.setApproved(true);
        jobOffer2.setEmployeur(employeur);

        JobOffer jobOffer3 = new JobOffer();
        jobOffer3.setId(3L);
        jobOffer3.setTitre("Jenior Developer");
        jobOffer3.setApproved(false);
        jobOffer3.setEmployeur(employeur);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("test@test.com", "mdp");
        student.setDiscipline(Discipline.INFORMATIQUE);

        // approuver les offres de stage
        JobPermission jobPermission = new JobPermission();
        jobPermission.setId(1L);
        jobPermission.setJobOffer(jobOffer2);
        jobPermission.setDisciplines("informatique");
        jobPermission.setStudents(List.of(student));
        jobPermission.setExpirationDate(null);

        // cv validé
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        when(jobOfferRepository.findAll()).thenReturn(Arrays.asList(jobOffer1, jobOffer2, jobOffer3));
        when(studentRepository.findUserAppByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(curriculumVitaeRepository.findByStudentId(student.getId())).thenReturn(Arrays.asList(cv));
        when(jobPermissionRepository.findAll()).thenReturn(Arrays.asList(jobPermission));
        when(jobPermissionRepository.findBySessionIdAndSearch(anyLong(), anyString(), any(), anyString(), any())).thenReturn(new PageImpl<>(List.of(jobPermission), PageRequest.of(5, 5), 5));

        try {
            List<JobOfferDTO> result = jobOfferService.getJobOffersForStudent(student.getEmail(), anyLong(), "", 5, 5).getContent();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).titre()).isEqualTo("Senior Developer");
        } catch (MissingPermissionsExceptions e) {
            e.printStackTrace();
            org.junit.jupiter.api.Assertions.fail(e.getMessage());
        }
    }

    @Test
    void applyToJobOfferTest() throws JobNotFoundException, MissingPermissionsExceptions, TooManyApplicationsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("kjhaskdj", "jshadkjahsd");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employeur);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setStudent(student);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobPermission jobPermission = new JobPermission();
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setStudents(new ArrayList<>());
        jobPermission.setDisciplines(student.getDiscipline().toString());

        when(curriculumVitaeRepository.findById(cv.getId())).thenReturn(Optional.of(cv));
        when(jobOfferRepository.findById(jobOffer.getId())).thenReturn(Optional.of(jobOffer));
        when(jobPermissionRepository.findByJobOfferId(jobOffer.getId())).thenReturn(Optional.of(jobPermission));
        when(jobOfferApplicationRepository.findJobOfferApplicationByStudentAndOffer(student.getId(), jobOffer.getId())).thenReturn(List.of());
        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(true);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);
        when(jobOfferApplicationRepository.save(any())).thenReturn(jobOfferApplication);

        UserDTO userDTO = UserDTO.toDTO(student);

        // Act
        JobOfferApplicationDTO applicationDTO = jobOfferService.applyToJobOffer(jobOffer.getId(), cv.getId(), userDTO);
        // Assert
        assertThat(applicationDTO.jobOffer().id()).isEqualTo(jobOffer.getId());
        assertThat(applicationDTO.CV().id()).isEqualTo(cv.getId());
    }

    @Test
    void applyToJobOfferStudentAllowedTest() throws JobNotFoundException, MissingPermissionsExceptions, TooManyApplicationsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa", "dasda");
        ;

        JobOffer jobOffer = new JobOffer();
        jobOffer.setDateDebut("huh");
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employeur);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setStudent(student);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobPermission jobPermission = new JobPermission();
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setStudents(List.of(student));
        jobPermission.setDisciplines(student.getDiscipline().toString());

        when(curriculumVitaeRepository.findById(cv.getId())).thenReturn(Optional.of(cv));
        when(jobOfferRepository.findById(jobOffer.getId())).thenReturn(Optional.of(jobOffer));
        when(jobPermissionRepository.findByJobOfferId(jobOffer.getId())).thenReturn(Optional.of(jobPermission));
        when(jobOfferApplicationRepository.findJobOfferApplicationByStudentAndOffer(student.getId(), jobOffer.getId())).thenReturn(List.of());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(true);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);
        when(jobOfferApplicationRepository.save(any())).thenReturn(jobOfferApplication);

        UserDTO userDTO = UserDTO.toDTO(student);

        // Act
        JobOfferApplicationDTO applicationDTO = jobOfferService.applyToJobOffer(jobOffer.getId(), cv.getId(), userDTO);
        // Assert
        assertThat(applicationDTO.jobOffer().id()).isEqualTo(jobOffer.getId());
        assertThat(applicationDTO.CV().id()).isEqualTo(cv.getId());
    }

    @Test
    void applyToJobOfferPreviousCancelledOffersTest() throws JobNotFoundException, MissingPermissionsExceptions, TooManyApplicationsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("email", "lkashjd");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employeur);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobPermission jobPermission = new JobPermission();
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setStudents(new ArrayList<>());
        jobPermission.setDisciplines(student.getDiscipline().toString());

        when(curriculumVitaeRepository.findById(cv.getId())).thenReturn(Optional.of(cv));
        when(jobOfferRepository.findById(jobOffer.getId())).thenReturn(Optional.of(jobOffer));
        when(jobPermissionRepository.findByJobOfferId(jobOffer.getId())).thenReturn(Optional.of(jobPermission));

        JobOfferApplication jobOfferApplicationCancelled1 = new JobOfferApplication();
        jobOfferApplicationCancelled1.setActive(false);
        jobOfferApplicationCancelled1.setCurriculumVitae(cv);
        jobOfferApplicationCancelled1.setJobOffer(jobOffer);
        JobOfferApplication jobOfferApplicationCancelled2 = new JobOfferApplication();
        jobOfferApplicationCancelled2.setActive(false);
        jobOfferApplicationCancelled2.setCurriculumVitae(cv);
        jobOfferApplicationCancelled2.setJobOffer(jobOffer);
        when(jobOfferApplicationRepository.findJobOfferApplicationByStudentAndOffer(student.getId(), jobOffer.getId())).thenReturn(List.of(jobOfferApplicationCancelled1, jobOfferApplicationCancelled2));

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(true);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);
        when(jobOfferApplicationRepository.save(any())).thenReturn(jobOfferApplication);

        UserDTO userDTO = UserDTO.toDTO(student);

        // Act
        JobOfferApplicationDTO applicationDTO = jobOfferService.applyToJobOffer(jobOffer.getId(), cv.getId(), userDTO);
        // Assert
        assertThat(applicationDTO.jobOffer().id()).isEqualTo(jobOffer.getId());
        assertThat(applicationDTO.CV().id()).isEqualTo(cv.getId());
    }

    @Test
    void applyToJobOfferCVNotOwnedByStudentTest() throws JobNotFoundException, MissingPermissionsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobPermission jobPermission = new JobPermission();
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setStudents(new ArrayList<>());
        jobPermission.setDisciplines(student.getDiscipline().toString());

        when(curriculumVitaeRepository.findById(cv.getId())).thenReturn(Optional.of(cv));
        when(jobOfferRepository.findById(jobOffer.getId())).thenReturn(Optional.of(jobOffer));
        when(jobPermissionRepository.findByJobOfferId(jobOffer.getId())).thenReturn(Optional.of(jobPermission));

        UserDTO userDTO = new UserDTO(student.getId()+1,student.getEmail(),student.getPassword(),student.getRole());

        // Act
        assertThatThrownBy(() -> jobOfferService.applyToJobOffer(jobOffer.getId(), cv.getId(), userDTO))
                .isInstanceOf(MissingPermissionsExceptions.class); // Assert

    }

    @Test
    void applyToJobOfferInvalidCVTest() throws JobNotFoundException, MissingPermissionsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.WAITING);

        JobPermission jobPermission = new JobPermission();
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setStudents(new ArrayList<>());
        jobPermission.setDisciplines(student.getDiscipline().toString());

        when(curriculumVitaeRepository.findById(cv.getId())).thenReturn(Optional.of(cv));
        when(jobOfferRepository.findById(jobOffer.getId())).thenReturn(Optional.of(jobOffer));
        when(jobPermissionRepository.findByJobOfferId(jobOffer.getId())).thenReturn(Optional.of(jobPermission));

        UserDTO userDTO = UserDTO.toDTO(student);

        // Act
        assertThatThrownBy(() -> jobOfferService.applyToJobOffer(jobOffer.getId(), cv.getId(), userDTO))
                .isInstanceOf(MissingPermissionsExceptions.class); // Assert
    }

    @Test
    void applyToJobOfferWrongDisciplineTest() throws JobNotFoundException, MissingPermissionsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobPermission jobPermission = new JobPermission();
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setStudents(new ArrayList<>());
        jobPermission.setDisciplines(Discipline.MARKETING.toString());

        when(curriculumVitaeRepository.findById(cv.getId())).thenReturn(Optional.of(cv));
        when(jobOfferRepository.findById(jobOffer.getId())).thenReturn(Optional.of(jobOffer));
        when(jobPermissionRepository.findByJobOfferId(jobOffer.getId())).thenReturn(Optional.of(jobPermission));

        UserDTO userDTO = UserDTO.toDTO(student);

        // Act
        assertThatThrownBy(() -> jobOfferService.applyToJobOffer(jobOffer.getId(), cv.getId(), userDTO))
                .isInstanceOf(MissingPermissionsExceptions.class); // Assert
    }

    @Test
    void applyToJobOfferStudentNotAllowedTest() throws JobNotFoundException, MissingPermissionsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");

        Student studentAllowed = new Student();
        studentAllowed.setId(2L);
        studentAllowed.setNom("Monroe");
        studentAllowed.setPrenom("Marilyn");
        studentAllowed.setAdresse("test");
        studentAllowed.setTelephone("1234567890");
        studentAllowed.setDiscipline(Discipline.INFORMATIQUE);
        studentAllowed.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobPermission jobPermission = new JobPermission();
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setStudents(List.of(studentAllowed));
        jobPermission.setDisciplines(student.getDiscipline().toString());

        when(curriculumVitaeRepository.findById(cv.getId())).thenReturn(Optional.of(cv));
        when(jobOfferRepository.findById(jobOffer.getId())).thenReturn(Optional.of(jobOffer));
        when(jobPermissionRepository.findByJobOfferId(jobOffer.getId())).thenReturn(Optional.of(jobPermission));

        UserDTO userDTO = UserDTO.toDTO(student);

        // Act
        assertThatThrownBy(() -> jobOfferService.applyToJobOffer(jobOffer.getId(), cv.getId(), userDTO))
                .isInstanceOf(MissingPermissionsExceptions.class); // Assert
    }

    @Test
    void applyToJobOfferOtherActiveOffersTest() throws JobNotFoundException, MissingPermissionsExceptions, TooManyApplicationsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobPermission jobPermission = new JobPermission();
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setStudents(new ArrayList<>());
        jobPermission.setDisciplines(student.getDiscipline().toString());

        when(curriculumVitaeRepository.findById(cv.getId())).thenReturn(Optional.of(cv));
        when(jobOfferRepository.findById(jobOffer.getId())).thenReturn(Optional.of(jobOffer));
        when(jobPermissionRepository.findByJobOfferId(jobOffer.getId())).thenReturn(Optional.of(jobPermission));

        JobOfferApplication jobOfferApplicationCancelled1 = new JobOfferApplication();
        jobOfferApplicationCancelled1.setActive(false);
        jobOfferApplicationCancelled1.setCurriculumVitae(cv);
        jobOfferApplicationCancelled1.setJobOffer(jobOffer);
        JobOfferApplication jobOfferApplicationCancelled2 = new JobOfferApplication();
        jobOfferApplicationCancelled2.setActive(true);
        jobOfferApplicationCancelled2.setCurriculumVitae(cv);
        jobOfferApplicationCancelled2.setJobOffer(jobOffer);
        when(jobOfferApplicationRepository.findJobOfferApplicationByStudentAndOffer(student.getId(), jobOffer.getId())).thenReturn(List.of(jobOfferApplicationCancelled1, jobOfferApplicationCancelled2));

        UserDTO userDTO = UserDTO.toDTO(student);

        // Act
        assertThatThrownBy(() -> jobOfferService.applyToJobOffer(jobOffer.getId(), cv.getId(), userDTO))
                .isInstanceOf(TooManyApplicationsExceptions.class); // Assert
    }

    @Test
    void getJobOffersApplicationFromStudentEmptyTest() {
        // Arrange
        Student student = new Student();
        student.setId(1L);

        when(jobOfferApplicationRepository.findJobOfferApplicationByStudent(eq(student.getId()), anyLong())).thenReturn(List.of());
        // Act
        List<JobOfferApplicationDTO> jobOfferApplicationDTOS = jobOfferService.getJobOffersApplicationsFromStudent(student.getId(), 1L);
        // Assert
        assertThat(jobOfferApplicationDTOS.isEmpty()).isTrue();
    }

    @Test
    void getJobOffersApplicationFromStudentTest() {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa", "dasda");

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employeur);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication1 = new JobOfferApplication();
        jobOfferApplication1.setActive(false);
        jobOfferApplication1.setId(1L);
        jobOfferApplication1.setCurriculumVitae(cv);
        jobOfferApplication1.setJobOffer(jobOffer);
        JobOfferApplication jobOfferApplication2 = new JobOfferApplication();
        jobOfferApplication2.setActive(true);
        jobOfferApplication2.setId(2L);
        jobOfferApplication2.setCurriculumVitae(cv);
        jobOfferApplication2.setJobOffer(jobOffer);

        when(jobOfferApplicationRepository.findJobOfferApplicationByStudent(eq(student.getId()), anyLong())).thenReturn(List.of(jobOfferApplication1, jobOfferApplication2));
        // Act
        List<JobOfferApplicationDTO> jobOfferApplicationDTOS = jobOfferService.getJobOffersApplicationsFromStudent(student.getId(), 1L);
        // Assert
        assertThat(jobOfferApplicationDTOS.size()).isEqualTo(2);
        assertThat(jobOfferApplicationDTOS.getFirst().id()).isEqualTo(jobOfferApplication1.getId());
        assertThat(jobOfferApplicationDTOS.getLast().id()).isEqualTo(jobOfferApplication2.getId());
    }

    @Test
    void cancelJobOfferApplicationStudentTest() throws JobNotFoundException, MissingPermissionsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        when(jobOfferApplicationRepository.findById(jobOfferApplication.getId())).thenReturn(Optional.of(jobOfferApplication));

        ArgumentCaptor<JobOfferApplication> captor = ArgumentCaptor.forClass(JobOfferApplication.class);

        // Act
        jobOfferService.cancelJobOfferApplicationStudent(jobOfferApplication.getId(), student.getId());
        // Assert
        verify(jobOfferApplicationRepository).save(captor.capture());
        JobOfferApplication captedApplication = captor.getValue();

        assertThat(captedApplication.isActive()).isFalse();
    }

    @Test
    void cancelJobOfferApplicationWrongStudentTest() throws JobNotFoundException, MissingPermissionsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        when(jobOfferApplicationRepository.findById(jobOfferApplication.getId())).thenReturn(Optional.of(jobOfferApplication));

        // Act
        assertThatThrownBy(() -> jobOfferService.cancelJobOfferApplicationStudent(jobOfferApplication.getId(), student.getId() + 1))
                .isInstanceOf(MissingPermissionsExceptions.class); // Assert

    }

    @Test
    void cancelJobOfferApplicationWrongApplicationTest() throws JobNotFoundException, MissingPermissionsExceptions {
        // Arrange

        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        assertThatThrownBy(() -> jobOfferService.cancelJobOfferApplicationStudent(1L, 1L))
                .isInstanceOf(JobNotFoundException.class); // Assert

    }

    @Test
    void getJobOfferApplicationsFromJobOfferId() throws JobNotFoundException {
        //Arrange
        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa", "dasda");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);
        jobOffer.setEmployeur(employeur);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("me@mail.com", "1234567");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Student student2 = new Student();
        student2.setId(1L);
        student2.setPrenom("Michel");
        student2.setNom("Smith");
        student2.setCredentials("me2@mail.com", "1234567");
        student2.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        CurriculumVitae cv2 = new CurriculumVitae();
        cv2.setId(2L);
        cv2.setStudent(student2);
        cv2.setStatus(ApprovalStatus.VALIDATED);
        cv2.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setCurriculumVitae(cv);

        JobOfferApplication jobOfferApplication2 = new JobOfferApplication();
        jobOfferApplication2.setId(2L);
        jobOfferApplication2.setActive(true);
        jobOfferApplication2.setJobOffer(jobOffer);
        jobOfferApplication2.setCurriculumVitae(cv2);

        //Act
        when(jobOfferApplicationRepository.findJobOfferApplicationByJobOfferId(anyLong(), anyLong(), anyString(), any(), any())).thenReturn(new PageImpl<>(List.of(jobOfferApplication, jobOfferApplication2), PageRequest.of(5, 5), 5));

        //Act
        List<JobOfferApplicationDTO> result = jobOfferService.getJobOfferApplicationsFromJobOfferId(1L, 2L, "", CandidatesStatusFilterForJobApplicationsFull.ALL,5, 5).getContent();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);

    }

//    @Test
//    void offerInternshipToStudent() throws JobNotFoundException, MissingPermissionsExceptions, JsonProcessingException {
//        //Arrange
//        JobOffer jobOffer = new JobOffer();
//        jobOffer.setId(1L);
//        jobOffer.setTitre("Junior Developer");
//        jobOffer.setApproved(true);
//        jobOffer.setActivated(true);
//
//        Student student = new Student();
//        student.setId(1L);
//        student.setPrenom("Bob");
//        student.setNom("Smith");
//        student.setCredentials("stu@email.com","123456");
//
//        CurriculumVitae cv = new CurriculumVitae();
//        cv.setId(1L);
//        cv.setStudent(student);
//        cv.setStatus(ApprovalStatus.VALIDATED);
//        cv.setDateHeureAjout(new Date());
//
//        JobOfferApplication jobOfferApplication = new JobOfferApplication();
//        jobOfferApplication.setId(1L);
//        jobOfferApplication.setActive(true);
//        jobOfferApplication.setJobOffer(jobOffer);
//        jobOfferApplication.setApplicationDate(LocalDateTime.now());
//        jobOfferApplication.setCurriculumVitae(cv);
//
//        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));
//
//        Employeur employeur = new Employeur();
//        employeur.setId(1L);
//        employeur.setNomCompagnie("TestCompany");
//        employeur.setCredentials("me@mail.com", "123456");
//        jobOffer.setEmployeur(employeur);
//
//        InternshipOffer internshipOffer = new InternshipOffer();
//        internshipOffer.setId(1L);
//        internshipOffer.setJobOfferApplication(jobOfferApplication);
//        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
//        internshipOffer.setConfirmationStatus(ApprovalStatus.WAITING);
//        internshipOffer.setConfirmationDate(null);
//
//        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
//        when(employeurRepository.findById(anyLong())).thenReturn(Optional.of(employeur));
//        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenReturn(internshipOffer);
//
//        //Act
////        InternshipOfferDTO result = jobOfferService.offerInternshipToStudent(1L, 1L, 7L);
//
//        //Assert
//        assertThat(result).isNotNull();
//        assertThat(result.id()).isEqualTo(1L);
//        assertThat(result).isEqualTo(InternshipOfferDTO.toDTO(internshipOffer));
//        assertThat(result.jobOfferApplicationDTO()).isEqualTo(JobOfferApplicationDTO.toDTO(jobOfferApplication));
//    }

    @Test
    void offerInternshipToStudent_EmptyEmployeur() throws JobNotFoundException, MissingPermissionsExceptions, JsonProcessingException {
        //Arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.WAITING);
        internshipOffer.setConfirmationDate(null);

        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
        when(employeurRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenReturn(internshipOffer);

        UserDTO userDTO = UserDTO.toDTO(employeur);

        assertThatThrownBy(() -> jobOfferService.offerInternshipToStudent(1L, userDTO, 7L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void offerInternshipToStudent_EmptyJobApplication() throws JobNotFoundException, MissingPermissionsExceptions, JsonProcessingException {
        //Arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.WAITING);
        internshipOffer.setConfirmationDate(null);

        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(employeurRepository.findById(anyLong())).thenReturn(Optional.of(employeur));
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenReturn(internshipOffer);

        UserDTO userDTO = UserDTO.toDTO(employeur);

        assertThatThrownBy(() -> jobOfferService.offerInternshipToStudent(1L, userDTO, 7L))
                .isInstanceOf(JobNotFoundException.class);
    }

    @Test
    void offerInternshipToStudent_DifferentEmployeur() throws JobNotFoundException, MissingPermissionsExceptions, JsonProcessingException {
        //Arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.WAITING);
        internshipOffer.setConfirmationDate(null);

        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
        when(employeurRepository.findById(anyLong())).thenReturn(Optional.of(employeur));
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenReturn(internshipOffer);

        UserDTO userDTO = new UserDTO(employeur.getId()+1,"","",employeur.getRole());

        assertThatThrownBy(() -> jobOfferService.offerInternshipToStudent(1L, userDTO, 7L))
                .isInstanceOf(MissingPermissionsExceptions.class);
    }

    @Test
    void offerInternshipToStudent_NotEnoughTimeAllocated() throws JobNotFoundException, MissingPermissionsExceptions, JsonProcessingException {
        //Arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.WAITING);
        internshipOffer.setConfirmationDate(null);

        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
        when(employeurRepository.findById(anyLong())).thenReturn(Optional.of(employeur));
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenReturn(internshipOffer);

        UserDTO userDTO = UserDTO.toDTO(student);

        assertThatThrownBy(() -> jobOfferService.offerInternshipToStudent(1L, userDTO, 0L))
                .isInstanceOf(NotEnoughTimeAllocatedException.class);
    }

    @Test
    void getMyInternshipOffers_success() throws Exception {
        // arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);
        internshipOffer.setConfirmationDate(LocalDateTime.now());

        // act
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findInternshipOffersByStudentId(anyLong(), anyLong(), anyString(), any())).thenReturn(new PageImpl<>(List.of(internshipOffer), PageRequest.of(5, 5), 5));

        // assert
        assertThat(jobOfferService.getMyInternshipOffers(1L, 4L, "woah", 5, 5).getContent().get(0).id()).isEqualTo(1l);
    }

    //    @Test
//    void getMyInternshipOffers_userNotFound() throws Exception{
//        // act
//        when(studentRepository.findById(anyLong())).thenThrow(UserNotFoundException.class);
//
//        // assert
//        assertThatThrownBy(() -> jobOfferService.getMyInternshipOffers(anyLong())).isInstanceOf(UserNotFoundException.class);
//    }
    @Test
    void getMyInternshipOffers_noInternshipOffer() throws Exception {
        // arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");

        // act
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findInternshipOffersByStudentId(anyLong(), anyLong(), anyString(), any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(5, 5), 5));

        // assert
        assertThatThrownBy(() -> jobOfferService.getMyInternshipOffers(1L, 4L, "", 5, 5)).isInstanceOf(InternshipNotFoundException.class);
    }


    @Test
    void confirmInternshipOffer_success() throws Exception {
        // arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);
        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        jobOffer.setEmployeur(employeur);
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));


        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);
//        internshipOffer.setConfirmationDate(LocalDateTime.now());

        InternshipOfferDTO internshipOfferDTO = InternshipOfferDTO.toDTO(internshipOffer);


        InternshipOffer internshipOfferDB = new InternshipOffer();
        internshipOfferDB.setId(1L);
        internshipOfferDB.setJobOfferApplication(jobOfferApplication);
        internshipOfferDB.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOfferDB.setConfirmationStatus(ApprovalStatus.WAITING);
//        internshipOfferDB.setConfirmationDate(LocalDateTime.now());


        JobOffer jobOfferDB = new JobOffer();
        jobOfferDB.setId(1L);
        jobOfferDB.setTitre("Junior Developer");
        jobOfferDB.setNombreStagiaire(1);
        jobOfferDB.setApproved(true);
        jobOfferDB.setActivated(false);
        jobOfferDB.setEmployeur(employeur);

        // act
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(anyLong())).thenReturn(Optional.of(internshipOfferDB));
        when(jobOfferRepository.findById(anyLong())).thenReturn(Optional.of(jobOffer));
        when(internshipOfferRepository.countByJobOfferApplication_JobOfferAndConfirmationStatusIn(jobOffer, List.of(ApprovalStatus.ACCEPTED))).thenReturn(1);
        when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(jobOfferDB);
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenReturn(internshipOffer);


        InternshipOfferDTO result = jobOfferService.confirmInternshipOffer(1l, "accepted", 1L);

        // assert
        assertThat(result.id()).isEqualTo(1l);
        assertThat(result.studentsApprovalStatus()).isEqualTo(ApprovalStatus.ACCEPTED);
        assertThat(result.jobOfferApplicationDTO().jobOffer().isActivated()).isEqualTo(false);
    }

    @Test
    void confirmInternshipOffer_noPermission() throws Exception {
        // arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        InternshipOfferDTO internshipOfferDTO = InternshipOfferDTO.toDTO(internshipOffer);


        InternshipOffer internshipOfferDB = new InternshipOffer();
        internshipOfferDB.setId(1L);
        internshipOfferDB.setJobOfferApplication(jobOfferApplication);
        internshipOfferDB.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOfferDB.setConfirmationStatus(ApprovalStatus.WAITING);


        JobOffer jobOfferDB = new JobOffer();
        jobOfferDB.setId(1L);
        jobOfferDB.setTitre("Junior Developer");
        jobOfferDB.setNombreStagiaire(1);
        jobOfferDB.setApproved(true);
        jobOfferDB.setActivated(false);

        // act
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(anyLong())).thenReturn(Optional.of(internshipOfferDB));
        when(jobOfferRepository.findById(anyLong())).thenReturn(Optional.of(jobOffer));
        when(internshipOfferRepository.countByJobOfferApplication_JobOfferAndConfirmationStatusIn(jobOffer, List.of(ApprovalStatus.ACCEPTED))).thenReturn(1);
        when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(jobOfferDB);
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenReturn(internshipOffer);

        // assert
        assertThatThrownBy(() -> jobOfferService.confirmInternshipOffer(1l, "accepted", 2L)).isInstanceOf(MissingPermissionsExceptions.class);
    }

    @Test
    void confirmInternshipOffer_internshipAlreadyConfirmed() throws Exception {
        // arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        InternshipOfferDTO internshipOfferDTO = InternshipOfferDTO.toDTO(internshipOffer);


        InternshipOffer internshipOfferDB = new InternshipOffer();
        internshipOfferDB.setId(1L);
        internshipOfferDB.setJobOfferApplication(jobOfferApplication);
        internshipOfferDB.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOfferDB.setConfirmationStatus(ApprovalStatus.ACCEPTED);


        JobOffer jobOfferDB = new JobOffer();
        jobOfferDB.setId(1L);
        jobOfferDB.setTitre("Junior Developer");
        jobOfferDB.setNombreStagiaire(1);
        jobOfferDB.setApproved(true);
        jobOfferDB.setActivated(false);

        // act
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(anyLong())).thenReturn(Optional.of(internshipOfferDB));
        when(jobOfferRepository.findById(anyLong())).thenReturn(Optional.of(jobOffer));
        when(internshipOfferRepository.countByJobOfferApplication_JobOfferAndConfirmationStatusIn(jobOffer, List.of(ApprovalStatus.ACCEPTED))).thenReturn(1);
        when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(jobOfferDB);
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenReturn(internshipOffer);

        // assert
        assertThatThrownBy(() -> jobOfferService.confirmInternshipOffer(1l, "accepted", 1L)).isInstanceOf(InternshipOfferAlreadyConfirmedException.class);
    }

    @Test
    void confirmInternshipOffer_intershipOfferExpired() throws Exception {
        // arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().minusDays(3L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        InternshipOfferDTO internshipOfferDTO = InternshipOfferDTO.toDTO(internshipOffer);


        InternshipOffer internshipOfferDB = new InternshipOffer();
        internshipOfferDB.setId(1L);
        internshipOfferDB.setJobOfferApplication(jobOfferApplication);
        internshipOfferDB.setExpirationDate(LocalDateTime.now().minusDays(3l));
        internshipOfferDB.setConfirmationStatus(ApprovalStatus.WAITING);


        JobOffer jobOfferDB = new JobOffer();
        jobOfferDB.setId(1L);
        jobOfferDB.setTitre("Junior Developer");
        jobOfferDB.setNombreStagiaire(1);
        jobOfferDB.setApproved(true);
        jobOfferDB.setActivated(false);

        // act
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(anyLong())).thenReturn(Optional.of(internshipOfferDB));
        when(jobOfferRepository.findById(anyLong())).thenReturn(Optional.of(jobOffer));
        when(internshipOfferRepository.countByJobOfferApplication_JobOfferAndConfirmationStatusIn(jobOffer, List.of(ApprovalStatus.ACCEPTED))).thenReturn(1);
        when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(jobOfferDB);
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenReturn(internshipOffer);

        // assert
        assertThatThrownBy(() -> jobOfferService.confirmInternshipOffer(1l, "accepted", 1L)).isInstanceOf(DateExpiredException.class);

    }

    @Test
    void confirmInternshipOffer_jobOfferNotFound() throws Exception {
        // arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(3L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        InternshipOfferDTO internshipOfferDTO = InternshipOfferDTO.toDTO(internshipOffer);


        InternshipOffer internshipOfferDB = new InternshipOffer();
        internshipOfferDB.setId(1L);
        internshipOfferDB.setJobOfferApplication(jobOfferApplication);
        internshipOfferDB.setExpirationDate(LocalDateTime.now().plusDays(3l));
        internshipOfferDB.setConfirmationStatus(ApprovalStatus.WAITING);

        JobOffer jobOfferDB = new JobOffer();
        jobOfferDB.setId(1L);
        jobOfferDB.setTitre("Junior Developer");
        jobOfferDB.setNombreStagiaire(1);
        jobOfferDB.setApproved(true);
        jobOfferDB.setActivated(false);

        // act
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(anyLong())).thenReturn(Optional.of(internshipOfferDB));
        when(jobOfferRepository.findById(anyLong())).thenReturn(Optional.empty());

        // assert
        assertThatThrownBy(() -> jobOfferService.confirmInternshipOffer(1l, "accepted", 1L)).isInstanceOf(JobNotFoundException.class);
    }


    @Test
    void confirmInternshipOffer_invalidApprovalStatus() throws Exception {
        // arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(3L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        InternshipOfferDTO internshipOfferDTO = InternshipOfferDTO.toDTO(internshipOffer);


        InternshipOffer internshipOfferDB = new InternshipOffer();
        internshipOfferDB.setId(1L);
        internshipOfferDB.setJobOfferApplication(jobOfferApplication);
        internshipOfferDB.setExpirationDate(LocalDateTime.now().plusDays(3l));
        internshipOfferDB.setConfirmationStatus(ApprovalStatus.WAITING);

        JobOffer jobOfferDB = new JobOffer();
        jobOfferDB.setId(1L);
        jobOfferDB.setTitre("Junior Developer");
        jobOfferDB.setNombreStagiaire(1);
        jobOfferDB.setApproved(true);
        jobOfferDB.setActivated(false);

        // act
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(anyLong())).thenReturn(Optional.of(internshipOfferDB));
        when(jobOfferRepository.findById(anyLong())).thenReturn(Optional.empty());

        // assert
        assertThatThrownBy(() -> jobOfferService.confirmInternshipOffer(1l, "unvalideStatus", 1L)).isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void studentApproveJobInterView() throws JobNotFoundException, MissingPermissionsExceptions {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setId(1L);
        curriculumVitae.setStudent(student);
        curriculumVitae.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae.setStatus(ApprovalStatus.VALIDATED);
        curriculumVitae.setPdfDocu(null);

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa", "dasda");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setActivated(true);
        jobOffer.setApproved(true);
        jobOffer.setPdfDocu(null);
        jobOffer.setEmployeur(employeur);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(null);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("link");
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setConfirmationDate(LocalDateTime.now());

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.findById(anyLong())).thenReturn(Optional.of(jobInterview));
        when((jobInterviewRepository.save(any(JobInterview.class)))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO userDTO = UserDTO.toDTO(student);

        ArgumentCaptor<JobInterview> captor = ArgumentCaptor.forClass(JobInterview.class);

        // Act
        jobOfferService.approveJobInterview(1L,userDTO);
        // Assert
        verify(jobInterviewRepository).save(captor.capture());


        JobInterview captedJobInterview = captor.getValue();

        assertThat(captedJobInterview.isConfirmedByStudent()).isTrue();

    }

    @Test
    void studentApproveJobInterView_NotFound() throws JobNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setId(1L);
        curriculumVitae.setStudent(student);
        curriculumVitae.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae.setStatus(ApprovalStatus.VALIDATED);
        curriculumVitae.setPdfDocu(null);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setActivated(true);
        jobOffer.setApproved(true);
        jobOffer.setPdfDocu(null);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(null);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("link");
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setConfirmationDate(LocalDateTime.now());

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        UserDTO userDTO = UserDTO.toDTO(student);

        // Act & Assert
        assertThatThrownBy(() -> jobOfferService.approveJobInterview(1L,userDTO))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("JobNotFoundException");
    }

    @Test
    void testGetJobOfferDTOById_NotFound() {
        // Arrange
        Long jobOfferId = 1L;
        when(jobOfferRepository.findById(jobOfferId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(JobNotFoundException.class, () -> jobOfferService.getJobOfferDTOById(jobOfferId));
        verify(jobOfferRepository, times(1)).findById(jobOfferId);
    }

    @Test
    void testGetJobOfferDTOById_Found() throws JobNotFoundException {
        // Arrange
        Long jobOfferId = 1L;
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@test.com", "password");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(jobOfferId);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setEmployeur(employeur);

        when(jobOfferRepository.findById(jobOfferId)).thenReturn(Optional.of(jobOffer));

        // Act
        JobOfferDTO result = jobOfferService.getJobOfferDTOById(jobOfferId);

        // Assert
        assertNotNull(result);
        assertEquals(jobOfferId, result.id());
        assertEquals(employeur.getId(), result.employeurDTO().id()); // Updated to use getEmployeurId() method
        verify(jobOfferRepository, times(1)).findById(jobOfferId);
    }

    @Test
    void testGetJobOfferApplication_notFound() {
        Long applicationId = 1L;

        when(jobOfferApplicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(JobApplicationNotFoundException.class, () -> jobOfferService.getJobOfferApplication(applicationId));

        verify(jobOfferApplicationRepository).findById(applicationId);
    }


    @Test
    void testGetInterviewFromId_notFound() {
        long id = 1L;

        when(jobInterviewRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(InterviewNotFoundException.class, () -> jobOfferService.getInterviewFromId(id));

        verify(jobInterviewRepository).findById(id);
    }

    @Test
    void testGetInternshipOffer_NotFound() {
        // Arrange
        Long internshipId = 1L;
        when(internshipOfferRepository.getInternshipOfferById(internshipId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InternshipNotFoundException.class, () -> jobOfferService.getInternshipOffer(internshipId));
        verify(internshipOfferRepository, times(1)).getInternshipOfferById(internshipId);
    }

    @Test
    void testGetJobOffersStats() {
        // Mock data
        Long employerId = 1L;
        Long sessionId = 1L;

        // Create JobOffer objects
        JobOffer jobOffer1 = new JobOffer();
        jobOffer1.setId(1L);
        jobOffer1.setNombreStagiaire(3);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setNombreStagiaire(2);

        List<JobOffer> jobOffers = List.of(jobOffer1, jobOffer2);

        // Create InternshipOffer objects for each JobOffer
        InternshipOffer internshipOffer1 = new InternshipOffer();
        internshipOffer1.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        InternshipOffer internshipOffer2 = new InternshipOffer();
        internshipOffer2.setConfirmationStatus(ApprovalStatus.WAITING);

        InternshipOffer internshipOffer3 = new InternshipOffer();
        internshipOffer3.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        List<InternshipOffer> internshipOffersForJobOffer1 = List.of(internshipOffer1, internshipOffer2, internshipOffer3);

        InternshipOffer internshipOffer4 = new InternshipOffer();
        internshipOffer4.setConfirmationStatus(ApprovalStatus.WAITING);
        List<InternshipOffer> internshipOffersForJobOffer2 = List.of(internshipOffer4);

        // Mock repository behavior
        when(jobOfferRepository.findByEmployeurId(employerId, sessionId)).thenReturn(jobOffers);
        when(internshipOfferRepository.getInternshipOffersByJobOfferIdAndSessionId(1L, sessionId))
                .thenReturn(internshipOffersForJobOffer1);
        when(internshipOfferRepository.getInternshipOffersByJobOfferIdAndSessionId(2L, sessionId))
                .thenReturn(internshipOffersForJobOffer2);

        // Call the method under test
        Map<Long, JobOfferStatsDTO> result = jobOfferService.getJobOffersStats(employerId, sessionId);

        // Assert results
        assertEquals(2, result.size());

        JobOfferStatsDTO stats1 = result.get(1L);
        assertEquals(3L, stats1.nbInternsNeeded());
        assertEquals(3L, stats1.nbInternshipOffersSent());
        assertEquals(2L, stats1.nbInternshipOffersAccepted());

        JobOfferStatsDTO stats2 = result.get(2L);
        assertEquals(2L, stats2.nbInternsNeeded());
        assertEquals(1L, stats2.nbInternshipOffersSent());
        assertEquals(0L, stats2.nbInternshipOffersAccepted());

        // Verify interactions
        verify(jobOfferRepository).findByEmployeurId(employerId, sessionId);
        verify(internshipOfferRepository).getInternshipOffersByJobOfferIdAndSessionId(1L, sessionId);
        verify(internshipOfferRepository).getInternshipOffersByJobOfferIdAndSessionId(2L, sessionId);
    }

    @Test
    void testGetJobOffersStats_NoJobOffers() {
        // Mock data
        Long employerId = 1L;
        Long sessionId = 1L;

        // No job offers for the employer
        List<JobOffer> jobOffers = List.of();

        // Mock repository behavior
        when(jobOfferRepository.findByEmployeurId(employerId, sessionId)).thenReturn(jobOffers);

        // Call the method under test
        Map<Long, JobOfferStatsDTO> result = jobOfferService.getJobOffersStats(employerId, sessionId);

        // Assert results
        assertTrue(result.isEmpty()); // No job offers, so the result should be empty

        // Verify interactions
        verify(jobOfferRepository).findByEmployeurId(employerId, sessionId);
    }

    @Test
    void testGetJobOffersStats_NoInternshipOffersSent() {
        // Mock data
        Long employerId = 1L;
        Long sessionId = 1L;

        JobOffer jobOffer1 = new JobOffer();
        jobOffer1.setId(1L);
        jobOffer1.setNombreStagiaire(3); // 3 interns needed

        List<JobOffer> jobOffers = List.of(jobOffer1);

        // No internship offers sent for the job offer
        List<InternshipOffer> internshipOffersForJobOffer1 = List.of();

        // Mock repository behavior
        when(jobOfferRepository.findByEmployeurId(employerId, sessionId)).thenReturn(jobOffers);
        when(internshipOfferRepository.getInternshipOffersByJobOfferIdAndSessionId(1L, sessionId))
                .thenReturn(internshipOffersForJobOffer1);

        // Call the method under test
        Map<Long, JobOfferStatsDTO> result = jobOfferService.getJobOffersStats(employerId, sessionId);

        // Assert results
        assertEquals(1, result.size());

        JobOfferStatsDTO stats1 = result.get(1L);
        assertEquals(3L, stats1.nbInternsNeeded());
        assertEquals(0L, stats1.nbInternshipOffersSent());
        assertEquals(0L, stats1.nbInternshipOffersAccepted());

        // Verify interactions
        verify(jobOfferRepository).findByEmployeurId(employerId, sessionId);
        verify(internshipOfferRepository).getInternshipOffersByJobOfferIdAndSessionId(1L, sessionId);
    }

    @Test
    void testGetJobOffersStats_AllInternshipOffersAccepted() {
        // Mock data
        Long employerId = 1L;
        Long sessionId = 1L;

        JobOffer jobOffer1 = new JobOffer();
        jobOffer1.setId(1L);
        jobOffer1.setNombreStagiaire(3); // 3 interns needed

        List<JobOffer> jobOffers = List.of(jobOffer1);

        // All internship offers are accepted
        InternshipOffer internshipOffer1 = new InternshipOffer();
        internshipOffer1.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        InternshipOffer internshipOffer2 = new InternshipOffer();
        internshipOffer2.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        List<InternshipOffer> internshipOffersForJobOffer1 = List.of(internshipOffer1, internshipOffer2);

        // Mock repository behavior
        when(jobOfferRepository.findByEmployeurId(employerId, sessionId)).thenReturn(jobOffers);
        when(internshipOfferRepository.getInternshipOffersByJobOfferIdAndSessionId(1L, sessionId))
                .thenReturn(internshipOffersForJobOffer1);

        // Call the method under test
        Map<Long, JobOfferStatsDTO> result = jobOfferService.getJobOffersStats(employerId, sessionId);

        // Assert results
        assertEquals(1, result.size());

        JobOfferStatsDTO stats1 = result.get(1L);
        assertEquals(3L, stats1.nbInternsNeeded());
        assertEquals(2L, stats1.nbInternshipOffersSent());
        assertEquals(2L, stats1.nbInternshipOffersAccepted());

        // Verify interactions
        verify(jobOfferRepository).findByEmployeurId(employerId, sessionId);
        verify(internshipOfferRepository).getInternshipOffersByJobOfferIdAndSessionId(1L, sessionId);
    }

    @Test
    void getSessionFromDB(){
        // Arrange
        Long sessionId = 1L;
        Session session = new Session();
        session.setId(sessionId);
        session.setSeason("Automne");
        session.setYear("2021");
        session.setStartDate(LocalDateTime.now());
        session.setEndDate(LocalDateTime.now());
        when(academicSessionRepository.findAll()).thenReturn(List.of(session));

        // Act
        SessionDTO result = jobOfferService.getSessionFromDB("Automne", "2021");

        // Assert
        assertNotNull(result);
        assertEquals(sessionId, result.id());
    }

    @Test
    void getSessionFromDB_NotFound(){
        // Arrange
        Long sessionId = 1L;
        Session session = new Session();
        session.setId(sessionId);
        session.setSeason("Automne");
        session.setYear("2021");
        session.setStartDate(LocalDateTime.now());
        session.setEndDate(LocalDateTime.now());
        when(academicSessionRepository.findAll()).thenReturn(List.of(session));

        try {
            jobOfferService.getSessionFromDB("Automne", "2020");
        } catch (SessionNotFoundException e) {
            assertEquals("SessionNotFoundException", e.getMessage());
        }
    }

    @Test
    void getAcademicSession(){
        // Arrange
        Long sessionId = 1L;
        Session session = new Session();
        session.setId(sessionId);
        session.setSeason("Automne");
        session.setYear("2021");
        session.setStartDate(LocalDateTime.now());
        session.setEndDate(LocalDateTime.now());
        when(academicSessionRepository.findAll()).thenReturn(List.of(session));

        // Act
        List<SessionDTO> result = jobOfferService.getAcademicSessions();

        assertNotNull(result);
        assertThat(!result.isEmpty());
    }

    @Test
    void getInternshipOfferFromJobOfferApplication() throws InternshipNotFoundException {
        // Arrange

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("as@mail.com", "123456");

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("test@test.com", "mdp");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        Date d = new Date(2);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setEmployeur(employeur);
        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));
        when(internshipOfferRepository.getInternshipOfferByJobOfferApplicationId(jobOfferApplication.getId())).thenReturn(Optional.of(internshipOffer));

        // Act
        InternshipOfferDTO result = jobOfferService.getInternshipOfferFromJobOfferApplication(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getInternshipOffer() throws InternshipNotFoundException {
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("as@mail.com", "123456");

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("test@test.com", "mdp");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        Date d = new Date(2);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setEmployeur(employeur);
        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        jobOfferApplication.setJobOffer(jobOffer);

        when(internshipOfferRepository.getInternshipOfferById(any())).thenReturn(Optional.of(internshipOffer));

        InternshipOfferDTO result = jobOfferService.getInternshipOffer(1L);
    }

    @Test
    void getInternshipOffer_Empty() throws InternshipNotFoundException {
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("as@mail.com", "123456");

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("test@test.com", "mdp");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        Date d = new Date(2);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setEmployeur(employeur);
        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        jobOfferApplication.setJobOffer(jobOffer);

        when(internshipOfferRepository.getInternshipOfferById(any())).thenReturn(Optional.empty());

        try {
            jobOfferService.getInternshipOffer(1L);
        } catch (InternshipNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
