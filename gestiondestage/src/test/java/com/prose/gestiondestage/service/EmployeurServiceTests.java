package com.prose.gestiondestage.service;

import com.prose.entity.embedded.InterpersonalRelationshipsEvaluation;
import com.prose.entity.embedded.PersonalSkillsEvaluation;
import com.prose.entity.embedded.ProductivityEvaluation;
import com.prose.entity.embedded.QualityOfWorkEvaluation;
import com.prose.entity.users.Employeur;
import com.prose.entity.*;
import com.prose.entity.users.Student;
import com.prose.entity.users.Teacher;
import com.prose.entity.users.auth.Credentials;
import com.prose.entity.users.auth.Role;
import com.prose.repository.*;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.EmployeurService;
import com.prose.service.EvaluationEmployerService;
import com.prose.service.Exceptions.*;
import com.prose.service.NotificationService;
import com.prose.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EmployeurServiceTests {
    EmployeurRepository employeurRepository;
    EmployeurService employeurService;
    JobOfferApplicationRepository jobOfferApplicationRepository;
    JobInterviewRepository jobInterviewRepository;
    NotificationService notificationService;
    InternshipOfferRepository internshipOfferRepository;
    InternshipEvaluationRepository internshipEvaluationRepository;
    EvaluationInternRepository evaluationInternRepository;
    AcademicSessionRepository academicSessionRepository;

    EvaluationEmployerService evaluationEmployerService;
    @BeforeEach
    public void beforeEach() {
        academicSessionRepository = mock(AcademicSessionRepository.class);
        employeurRepository = mock(EmployeurRepository.class);
        jobOfferApplicationRepository = mock(JobOfferApplicationRepository.class);
        jobInterviewRepository = mock(JobInterviewRepository.class);
        notificationService = mock(NotificationService.class);
        internshipOfferRepository = mock(InternshipOfferRepository.class);
        internshipEvaluationRepository = mock(InternshipEvaluationRepository.class);
        evaluationInternRepository = mock(EvaluationInternRepository.class);
        employeurService = new EmployeurService(employeurRepository,NoOpPasswordEncoder.getInstance(), jobOfferApplicationRepository, jobInterviewRepository, notificationService,internshipOfferRepository, internshipEvaluationRepository, evaluationInternRepository, academicSessionRepository);
    }

    @Test
    public void createEmployeurTest() throws InvalidUserFormatException, AlreadyExistsException {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("NomCompagnie");
        employeur.setCredentials("hi@error.com", "mdp");
        employeur.setAdresse("123 Main Street");
        employeur.setTelephone("555-555-5555");
        employeur.setCity("Montreal");
        employeur.setPostalCode("H3H1P9");
        employeur.setFax("123-456-7890");

        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO(
                "NomCompagnie",
                "hi@error.com",
                "John Doe",
                "Montreal",
                "H3H1P9",
                "123-456-7890",
                "5555555555",
                "123 Main Street",
                "mdp"
        );

        when(employeurRepository.save(any(Employeur.class))).thenReturn(employeur);

        // Act
        EmployeurDTO employeurDTO = employeurService.createEmployeur(employeurRegisterDTO);

        // Assert
        assertThat(employeurDTO).isNotNull();
        assertThat(employeurDTO.id()).isEqualTo(1L);
        assertThat(employeurDTO.nomCompagnie()).isEqualTo("NomCompagnie");
        assertThat(employeurDTO.courriel()).isEqualTo("hi@error.com");
        assertThat(employeurDTO.adresse()).isEqualTo("123 Main Street");
        assertThat(employeurDTO.telephone()).isEqualTo("555-555-5555");
    }



    @Test
    public void createEmployeurTestAlreadyExists() {
        //Arrange
        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO(
                "NomCompagnie",
                "hi@error.com",
                "5555555555",
                "ici, chez moi",
                "mdp",
                "1234567890",
                "5555555555",
                "cityasfda",
                "postalCode");

        when(employeurRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        //Act
        assertThatThrownBy(() -> employeurService.createEmployeur(employeurRegisterDTO))
            .isInstanceOf(AlreadyExistsException.class);

    }

    @Test
    public void createEmployeurCourrielInvalidStart() {
        //Arrange
        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO(
                "NomCompagnie",
                "@error.com",
                "5555555555",
                "ici, chez moi",
                "mdp",
                "fax",
                "1234567890",
                "cityasfda",
                "postalCode");

        assertThatThrownBy(() -> employeurService.createEmployeur(employeurRegisterDTO))
            .isInstanceOf(InvalidUserFormatException.class);

    }

    @Test
    public void createEmployeurCourrielInvalidEnd() {
        //Arrange
        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO(
                "NomCompagnie",
                "hi@error.com",
                "5555555555",
                "ici, chez moi",
                "mdp",
                "fax",
                "111111111",
                "cityasfda",
                "postalCode");

        assertThatThrownBy(() -> employeurService.createEmployeur(employeurRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class);

    }

    @Test
    public void createEmployeurTelephoneInvalid() {
        //Arrange
        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO(
                "NomCompagnie",
                "hi@error.com",
                "5555555555",
                "ici, chez moi",
                "mdp",
                "111111111",
                "fax",
                "cityasfda",
                "postalCode");

        assertThatThrownBy(() -> employeurService.createEmployeur(employeurRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class);
    }

    @Test
    public void createEmployeurNomCompagnieInvalid() {
        //Arrange
        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO(
                "   ",

                "hi@error.com",
                "5555555555",
                "ici, chez moi",
                "mdp",
                "111111111",
                "fax",
                "cityasfda",
                "postalCode");

        assertThatThrownBy(() -> employeurService.createEmployeur(employeurRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class);
    }

    @Test
    public void createEmployeurAdresseInvalid() {
        //Arrange
        EmployeurRegisterDTO employeurRegisterDTO = new EmployeurRegisterDTO(
                "NomCompagnie",
                "hi@error.com",
                "5555555555",
                "  ",
                "mdp",
                "111111111",
                "fax",
                "cityasfda",
                "postalCode");

        assertThatThrownBy(() -> employeurService.createEmployeur(employeurRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class);
    }

    @Test
    public void getEmployeurTest() {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("NomCompagnie");
        employeur.setAdresse("adresse");
        employeur.setTelephone("telephone");
        employeur.setCredentials("courriel","mdp");

        when(employeurRepository.findById(anyLong())).thenReturn(Optional.of(employeur));
        // Act
        EmployeurDTO result = employeurService.getEmployeur(0);
        // Assert
        assertThat(result.id()).isEqualTo(employeur.getId());
        assertThat(result.nomCompagnie()).isEqualTo(employeur.getNomCompagnie());
    }

    @Test
    public void getEmployeurNotFoundTest() {
        // Arrange
        when(employeurRepository.findById(anyLong())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> employeurService.getEmployeur(0))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }

    @Test
    public void getEmployeurEmailTest() {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("NomCompagnie");
        employeur.setAdresse("adresse");
        employeur.setTelephone("telephone");
        employeur.setCredentials("courriel","mdp");

        when(employeurRepository.findUserAppByEmail(anyString())).thenReturn(Optional.of(employeur));
        // Act
        EmployeurDTO result = employeurService.getEmployeurByEmail("hi");
        // Assert
        assertThat(result.id()).isEqualTo(employeur.getId());
        assertThat(result.nomCompagnie()).isEqualTo(employeur.getNomCompagnie());
    }

    @Test
    public void getEmployeurEmailNotFoundTest() {
        // Arrange
        when(employeurRepository.findUserAppByEmail(anyString())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> employeurService.getEmployeurByEmail("no"))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }

    @Test
    public void getAllEmployeursTest() {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("NomCompagnie");
        employeur.setAdresse("adresse");
        employeur.setTelephone("telephone");
        employeur.setCredentials("courriel","mdp");

        when(employeurRepository.findAll()).thenReturn(List.of(employeur));
        // Act
        List<EmployeurDTO> result = employeurService.getAllEmployeurs();
        // Assert
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getFirst().id()).isEqualTo(employeur.getId());
        assertThat(result.getFirst().nomCompagnie()).isEqualTo(employeur.getNomCompagnie());
    }

    @Test
    public void getAllEmployeursEmptyTest() {
        // Arrange
        when(employeurRepository.findAll()).thenReturn(List.of());
        // Act
        List<EmployeurDTO> result = employeurService.getAllEmployeurs();
        // Assert
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void createJobInterview_success() throws Exception{
        // arrange
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-02-23T18:00",
                "Online",
                "https://xxxx",
                1l);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@email.com","mdp");
        jobOffer.setEmployeur(employeur);

        Student student = new Student();
        student.setId(5L);
        student.setCredentials("ljajd","dlijalkd");
        student.setDiscipline(Discipline.INFORMATIQUE);

        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setId(1L);
        curriculumVitae.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae.setStatus(ApprovalStatus.VALIDATED);
        curriculumVitae.setStudent(student);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        LocalDateTime interviewDate = LocalDateTime.of(2025, 1, 1, 10, 30);
        jobInterview.setInterviewDate(interviewDate);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://xxxx");
        jobInterview.setJobOfferApplication(jobOfferApplication);

        UserDTO employerUserDTO = UserDTO.toDTO(employeur);


        // act
        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
        when(jobInterviewRepository.save(any())).thenReturn(jobInterview);

        // assert
        assertThat(employeurService.createJobInterview(jobInterviewRegisterDTO, employerUserDTO).id()).isEqualTo(jobInterview.getId());
    }

    @Test
    void createJobInterview_invalidDateTimeFormat() throws Exception{
        // arrange
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-01-01T10:30:00.000Z",
                "Online",
                "https://xxxx",
                1l);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@email.com","mdp");
        jobOffer.setEmployeur(employeur);

        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setId(1L);
        curriculumVitae.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae.setStatus(ApprovalStatus.VALIDATED);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        LocalDateTime interviewDate = LocalDateTime.of(2025, 1, 1, 10, 30);
        jobInterview.setInterviewDate(interviewDate);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://xxxx");
        jobInterview.setJobOfferApplication(jobOfferApplication);

        UserDTO employerUserDTO = UserDTO.toDTO(employeur);

        // act
        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
        when(jobInterviewRepository.save(any())).thenReturn(jobInterview);

        // assert
       assertThatThrownBy(() -> employeurService.createJobInterview(jobInterviewRegisterDTO, employerUserDTO)).isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void createJobInterview_invalidInterviewType() throws Exception{
        // arrange
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-02-23T18:00",
                "Invalid",
                "https://xxxx",
                1l);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@email.com","mdp");
        jobOffer.setEmployeur(employeur);

        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setId(1L);
        curriculumVitae.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae.setStatus(ApprovalStatus.VALIDATED);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        LocalDateTime interviewDate = LocalDateTime.of(2025, 1, 1, 10, 30);
        jobInterview.setInterviewDate(interviewDate);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://xxxx");
        jobInterview.setJobOfferApplication(jobOfferApplication);

        UserDTO employerUserDTO = UserDTO.toDTO(employeur);

        // act
        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
        when(jobInterviewRepository.save(any())).thenReturn(jobInterview);

        // assert
        assertThatThrownBy(() -> employeurService.createJobInterview(jobInterviewRegisterDTO, employerUserDTO)).isInstanceOf(InvalideInterviewTypeException.class);

    }


    // Pas implémenté dans le code... et aussi un peu tard pour l'implémenter =/
    /*@Test
    void createJobInterview_invalidInterviewLink() throws Exception{
        // arrange
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-02-23T18:00",
                "Online",
                "xxxx",
                1l);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@email.com","mdp");
        jobOffer.setEmployeur(employeur);

        Student student = new Student();
        student.setId(6L);
        student.setCredentials("email","password");
        student.setDiscipline(Discipline.INFORMATIQUE);

        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setId(1L);
        curriculumVitae.setDateHeureAjout(new Date());
        curriculumVitae.setStatus(ApprovalStatus.VALIDATED);
        curriculumVitae.setStudent(student);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        LocalDateTime interviewDate = LocalDateTime.of(2025, 1, 1, 10, 30);
        jobInterview.setInterviewDate(interviewDate);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("xxxx");
        jobInterview.setJobOfferApplication(jobOfferApplication);

        UserDTO employerUserDTO = UserDTO.toDTO(employeur);

        // act
        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
        when(jobInterviewRepository.save(any())).thenReturn(jobInterview);

        // assert
        assertThatThrownBy(() -> employeurService.createJobInterview(jobInterviewRegisterDTO, employerUserDTO)).isInstanceOf(InvalideInterviewLinkException.class);

    }*/

    @Test
    void createJobInterview_jobApplicationNotFound() throws Exception{
        // arrange
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-02-23T18:00",
                "Online",
                "https://xxxx",
                1l);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@email.com","mdp");
        jobOffer.setEmployeur(employeur);

        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setId(1L);
        curriculumVitae.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae.setStatus(ApprovalStatus.VALIDATED);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        LocalDateTime interviewDate = LocalDateTime.of(2025, 1, 1, 10, 30);
        jobInterview.setInterviewDate(interviewDate);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://xxxx");
        jobInterview.setJobOfferApplication(jobOfferApplication);

        UserDTO employerUserDTO = UserDTO.toDTO(employeur);

        // act
//        when(jobOfferApplicationRepository.findById(anyLong())).thenThrow(JobApplicationNotFoundException.class);
        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // assert
        assertThatThrownBy(() -> employeurService.createJobInterview(jobInterviewRegisterDTO, employerUserDTO)).isInstanceOf(JobApplicationNotFoundException.class);

    }

    @Test
    void createJobInterview_jobApplicationNotActive() throws Exception{
        // arrange
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-02-23T18:00",
                "Online",
                "https://xxxx",
                1l);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@email.com","mdp");
        jobOffer.setEmployeur(employeur);

        jobOfferApplication.setActive(false);
        jobOfferApplication.setJobOffer(jobOffer);
        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setId(1L);
        curriculumVitae.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae.setStatus(ApprovalStatus.VALIDATED);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        LocalDateTime interviewDate = LocalDateTime.of(2025, 1, 1, 10, 30);
        jobInterview.setInterviewDate(interviewDate);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://xxxx");
        jobInterview.setJobOfferApplication(jobOfferApplication);

        UserDTO employerUserDTO = UserDTO.toDTO(employeur);

        // act
        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
        when(jobInterviewRepository.save(any())).thenReturn(jobInterview);

        // assert
        assertThatThrownBy(() -> employeurService.createJobInterview(jobInterviewRegisterDTO, employerUserDTO)).isInstanceOf(JobApplicationNotActiveException.class);

    }

    @Test
    void createJobInterview_jobApplicationNotEmployers() throws Exception{
        // arrange
        JobInterviewRegisterDTO jobInterviewRegisterDTO = new JobInterviewRegisterDTO("2025-02-23T18:00",
                "Online",
                "https://xxxx",
                1l);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("test@email.com","mdp");
        jobOffer.setEmployeur(employeur);

        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setId(1L);
        curriculumVitae.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae.setStatus(ApprovalStatus.VALIDATED);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        LocalDateTime interviewDate = LocalDateTime.of(2025, 1, 1, 10, 30);
        jobInterview.setInterviewDate(interviewDate);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://xxxx");
        jobInterview.setJobOfferApplication(jobOfferApplication);

        UserDTO employerUserDTO = new UserDTO(employeur.getId()+1,employeur.getEmail(),employeur.getPassword(),employeur.getRole());

        // act
        when(jobOfferApplicationRepository.findById(anyLong())).thenReturn(Optional.of(jobOfferApplication));
        when(jobInterviewRepository.save(any())).thenReturn(jobInterview);

        // assert
        assertThatThrownBy(() -> employeurService.createJobInterview(jobInterviewRegisterDTO, employerUserDTO)).isInstanceOf(MissingPermissionsExceptions.class);

    }

    @Test
    void getJobInterviewsFromEmployerEmptyTest() throws InvalidUserFormatException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2024", "", "");

        when(jobInterviewRepository.findJobInterviewByEmployerId(eq(employer.getId()), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(),PageRequest.of(5,5),5));
        // Act
        List<JobInterviewDTO> jobInterview = employeurService.getJobInterview(employer.getId(),sessionDTO.id(),"","","",5,5).getContent();
        // Assert
        assertThat(jobInterview.isEmpty()).isTrue();
    }

    @Test
    void getJobInterviewsFromEmployerTest() throws InvalidUserFormatException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2024", "", "");

        when(jobInterviewRepository.findJobInterviewByEmployerId(eq(employer.getId()), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        List<JobInterviewDTO> jobInterviewDTO = employeurService.getJobInterview(employer.getId(),sessionDTO.id(),"","","",5,5).getContent();
        // Assert
        assertThat(jobInterviewDTO.size()).isEqualTo(1);
        assertThat(jobInterviewDTO.getFirst().id()).isEqualTo(jobInterview.getId());
    }

    @Test
    void getJobInterviewsFromEmployerDateTest() throws InvalidUserFormatException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2024", "", "");

        when(jobInterviewRepository.findJobInterviewByEmployerId(eq(employer.getId()), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        List<JobInterviewDTO> jobInterviewDTO = employeurService.getJobInterview(employer.getId(),sessionDTO.id(),"2011-12-13","2011-12-14","",5,5).getContent();
        // Assert
        assertThat(jobInterviewDTO.size()).isEqualTo(1);
        assertThat(jobInterviewDTO.getFirst().id()).isEqualTo(jobInterview.getId());
    }

    @Test
    void getJobInterviewsFromEmployerNotCancelledTest() throws InvalidUserFormatException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2024", "", "");

        when(jobInterviewRepository.findJobInterviewByEmployerIdNotCancelled(eq(employer.getId()), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        List<JobInterviewDTO> jobInterviewDTO = employeurService.getJobInterview(employer.getId(),sessionDTO.id(),"2011-12-13","2011-12-14","valide",5,5).getContent();
        // Assert
        assertThat(jobInterviewDTO.size()).isEqualTo(1);
        assertThat(jobInterviewDTO.getFirst().id()).isEqualTo(jobInterview.getId());
    }

    @Test
    void getJobInterviewsFromEmployerCancelledTest() throws InvalidUserFormatException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2024", "", "");

        when(jobInterviewRepository.findJobInterviewByEmployerIdCancelled(eq(employer.getId()), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        List<JobInterviewDTO> jobInterviewDTO = employeurService.getJobInterview(employer.getId(),sessionDTO.id(),"2011-12-13","2011-12-14","cancelled",5,5).getContent();
        // Assert
        assertThat(jobInterviewDTO.size()).isEqualTo(1);
        assertThat(jobInterviewDTO.getFirst().id()).isEqualTo(jobInterview.getId());
    }

    @Test
    void getJobInterviewsFromEmployerConfirmedTest() throws InvalidUserFormatException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2024", "", "");

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);

        when(jobInterviewRepository.findJobInterviewByEmployerIdConfirmedByStudent(eq(employer.getId()), anyLong(),any(),any(),captor.capture(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        List<JobInterviewDTO> jobInterviewDTO = employeurService.getJobInterview(employer.getId(),sessionDTO.id(),"2011-12-13","2011-12-14","confirmed",5,5).getContent();
        // Assert
        assertThat(jobInterviewDTO.size()).isEqualTo(1);
        assertThat(jobInterviewDTO.getFirst().id()).isEqualTo(jobInterview.getId());
        assertThat(captor.getValue()).isTrue();
    }

    @Test
    void getJobInterviewsFromEmployerNotConfirmedTest() throws InvalidUserFormatException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2024", "", "");

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);

        when(jobInterviewRepository.findJobInterviewByEmployerIdConfirmedByStudent(eq(employer.getId()), anyLong(),any(),any(),captor.capture(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        List<JobInterviewDTO> jobInterviewDTO = employeurService.getJobInterview(employer.getId(),sessionDTO.id(),"2011-12-13","2011-12-14","nonConfirmed",5,5).getContent();
        // Assert
        assertThat(jobInterviewDTO.size()).isEqualTo(1);
        assertThat(jobInterviewDTO.getFirst().id()).isEqualTo(jobInterview.getId());
        assertThat(captor.getValue()).isFalse();
    }

    @Test
    void getJobInterviewsFromEmployerDateWrongTest() throws InvalidUserFormatException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2024", "", "");

        when(jobInterviewRepository.findJobInterviewByEmployerId(eq(employer.getId()), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        assertThatThrownBy(() -> employeurService.getJobInterview(employer.getId(),sessionDTO.id(),"20asd11-1ada2-13","2011-12-14","",5,5).getContent())
                .isInstanceOf(InvalidUserFormatException.class); // Assert
    }

    @Test
    void getJobInterviewsFromEmployerStatusWrongTest() throws InvalidUserFormatException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        SessionDTO sessionDTO = new SessionDTO(1L, "Automne", "2024", "", "");

        when(jobInterviewRepository.findJobInterviewByEmployerId(eq(employer.getId()), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        assertThatThrownBy(() -> employeurService.getJobInterview(employer.getId(),sessionDTO.id(),"","","askfblasdkfhlah",5,5).getContent())
                .isInstanceOf(InvalidUserFormatException.class); // Assert
    }

    @Test
    void cancelInterviewTest() throws AlreadyExistsException, MissingPermissionsExceptions, InterviewNotFoundException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        when(jobInterviewRepository.findById(jobInterview.getId())).thenReturn(Optional.of(jobInterview));
        when(jobInterviewRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

        assertThat(jobInterview.getCancelledDate()).isNull(); // Le test brise si la valeur par défaut change. force un fail
        // Act
        JobInterviewDTO jobInterviewDTO = employeurService.cancelInterview(jobInterview.getId(),employer.getId());
        //Assert
        assertThat(jobInterviewDTO.cancelledDate()).isNotNull();
    }

    @Test
    void cancelInterviewAlreadyCancelledTest() throws AlreadyExistsException, MissingPermissionsExceptions, InterviewNotFoundException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());
        jobInterview.setCancelledDate(LocalDateTime.now());

        when(jobInterviewRepository.findById(jobInterview.getId())).thenReturn(Optional.of(jobInterview));
        when(jobInterviewRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

        // Act
        assertThatThrownBy(() -> employeurService.cancelInterview(jobInterview.getId(),employer.getId()))
                .isInstanceOf(AlreadyExistsException.class); //Assert
    }

    @Test
    void cancelInterviewWrongEmployerTest() throws AlreadyExistsException, MissingPermissionsExceptions, InterviewNotFoundException {
        // Arrange
        Employeur employer = new Employeur();
        employer.setId(1L);
        employer.setCredentials("test@test.com","adasd");
        employer.setNomCompagnie("aa");
        employer.setAdresse("bb");
        employer.setTelephone("1234567890");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(2L);
        jobOffer.setEmployeur(employer);

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());

        when(jobInterviewRepository.findById(jobInterview.getId())).thenReturn(Optional.of(jobInterview));
        when(jobInterviewRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

        // Act
        assertThatThrownBy(() -> employeurService.cancelInterview(jobInterview.getId(),employer.getId()+1))
                .isInstanceOf(MissingPermissionsExceptions.class); //Assert
    }

    @Test
    public void getAwaitingContractTest() {
        // Arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);

        Session session = new Session();
        session.setId(5L);
        jobOffer.setSession(session);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com","123456");
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
        employeur.setId(1L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);
        internshipOffer.setConfirmationDate(null);

        when(internshipOfferRepository.findInternshipOfferWithContractEmployer(eq(employeur.getId()),eq(session.getId()),any())).thenReturn(new PageImpl<>(List.of(internshipOffer), PageRequest.of(5,5), 5));

        // Act
        List<InternshipOfferDTO> result = employeurService.getContracts(employeur.getId(),session.getId(),5,5).getContent();
        // Assert
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getFirst().contractSignatureDTO()).isNull();
    }

    @Test
    public void getAwaitingContractEmptyTest() {
        // Arrange
        long employerId = 2L;
        when(internshipOfferRepository.findInternshipOfferWithContractEmployer(eq(employerId),eq(3L),any())).thenReturn(Page.empty());

        // Act
        List<InternshipOfferDTO> result = employeurService.getContracts(employerId,3L,5,5).getContent();
        // Assert
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void getEmployeur_Success() {

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("Test Company");


        Credentials credentials = new Credentials("test@company.com", "password123");
        employeur.setCredentials(credentials);


        when(employeurRepository.findById(1L)).thenReturn(Optional.of(employeur));


        EmployeurDTO result = employeurService.getEmployeur(1L);

        // 验证结果
        assertThat(result.id()).isEqualTo(employeur.getId());
        assertThat(result.nomCompagnie()).isEqualTo(employeur.getNomCompagnie());
    }

    @Test
    void getStudentsWithOffersByEmployerId(){
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setDiscipline(Discipline.INFORMATIQUE);
        teacher.setCredentials("a@mail.com", "123456");

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("e@mail.com", "123456");

        CurriculumVitae cv = new CurriculumVitae();
        cv.setStudent(new Student());
        cv.getStudent().setNom("Nom");
        cv.getStudent().setPrenom("Prenom");
        cv.getStudent().setAdresse("addresse");
        cv.getStudent().setDiscipline(Discipline.INFORMATIQUE);
        cv.getStudent().setTelephone("5555555555");
        cv.getStudent().setCredentials("ab@email.com", "123456");
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStatus(ApprovalStatus.VALIDATED);

        Session currentSession = new Session();
        currentSession.setId(2L);
        currentSession.setYear("2024");
        currentSession.setSeason("Automne");

        Session nextSession = new Session();
        nextSession.setId(3L);
        nextSession.setYear("2025");
        nextSession.setSeason("Hiver");

        Contract contract = new Contract();
        contract.setEmployerSign(LocalDateTime.now());
        contract.setManagerSign(LocalDateTime.now());
        contract.setStudentSign(LocalDateTime.now());

        JobOffer jobOffer = new JobOffer();
        jobOffer.setSession(currentSession);
        jobOffer.setEmployeur(employeur);
        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setCurriculumVitae(cv);
        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setContract(contract);


        when(academicSessionRepository.getReferenceById(any())).thenReturn(nextSession);
        when(internshipOfferRepository.findByEmployerId(employeur.getId())).thenReturn(List.of(internshipOffer));

        // Act
        assertThat(employeurService.getStudentsWithOffersByEmployerId(employeur.getId(), nextSession.getId()).size()).isEqualTo(1);
    }

    @Test
    void getStudentsWithOffersByEmployerId_ContractNotSigned(){
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setDiscipline(Discipline.INFORMATIQUE);
        teacher.setCredentials("a@mail.com", "123456");

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("e@mail.com", "123456");

        CurriculumVitae cv = new CurriculumVitae();
        cv.setStudent(new Student());
        cv.getStudent().setNom("Nom");
        cv.getStudent().setPrenom("Prenom");
        cv.getStudent().setAdresse("addresse");
        cv.getStudent().setDiscipline(Discipline.INFORMATIQUE);
        cv.getStudent().setTelephone("5555555555");
        cv.getStudent().setCredentials("ab@email.com", "123456");
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStatus(ApprovalStatus.VALIDATED);

        Session currentSession = new Session();
        currentSession.setId(2L);
        currentSession.setYear("2024");
        currentSession.setSeason("Automne");

        Session nextSession = new Session();
        nextSession.setId(3L);
        nextSession.setYear("2025");
        nextSession.setSeason("Hiver");


        JobOffer jobOffer = new JobOffer();
        jobOffer.setSession(currentSession);
        jobOffer.setEmployeur(employeur);
        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setCurriculumVitae(cv);
        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setJobOfferApplication(jobOfferApplication);

        when(academicSessionRepository.getReferenceById(any())).thenReturn(nextSession);
        when(internshipOfferRepository.findByEmployerId(employeur.getId())).thenReturn(List.of(internshipOffer));

        // Act
        assertThat(employeurService.getStudentsWithOffersByEmployerId(employeur.getId(), nextSession.getId()).size()).isEqualTo(0);
    }

    @Test
    void saveEvaluation_ErrorSignature() throws InternshipNotFoundException, InvalidBase64Exception, IOException {
        LocalDate date = LocalDate.now();
        EvaluationInternDTO e = new EvaluationInternDTO();
        e.setName("test");
        e.setDate(date);
        e.setSupervisorName("ociwbh");
        e.setCompanyName("pollaas");
        e.setTelephone("1234567899");
        e.setEmployerSignature("wefwef");

        ProductivityEvaluation pre = new ProductivityEvaluation();
        pre.setProduction_a(EvaluationOption.TOTAL_AGREEMENT);
        pre.setProduction_b(EvaluationOption.TOTAL_AGREEMENT);
        pre.setProduction_d(EvaluationOption.TOTAL_AGREEMENT);
        pre.setProduction_e(EvaluationOption.TOTAL_AGREEMENT);
        e.setProductivityEvaluation(pre);
        e.setProductivityComments("cwaec");

        QualityOfWorkEvaluation qu = new QualityOfWorkEvaluation();
        qu.setQuality_a(EvaluationOption.TOTAL_AGREEMENT);
        qu.setQuality_b(EvaluationOption.TOTAL_AGREEMENT);
        qu.setQuality_c(EvaluationOption.TOTAL_AGREEMENT);
        qu.setQuality_d(EvaluationOption.TOTAL_AGREEMENT);
        qu.setQuality_e(EvaluationOption.TOTAL_AGREEMENT);
        e.setQualityOfWorkEvaluation(qu);
        e.setQualityOfWorkComments("cwds");

        InterpersonalRelationshipsEvaluation in = new InterpersonalRelationshipsEvaluation();
        in.setInterPersonal_a(EvaluationOption.TOTAL_AGREEMENT);
        in.setInterPersonal_b(EvaluationOption.TOTAL_AGREEMENT);
        in.setInterPersonal_c(EvaluationOption.TOTAL_AGREEMENT);
        in.setInterPersonal_d(EvaluationOption.TOTAL_AGREEMENT);
        e.setInterpersonalRelationshipsEvaluation(in);
        e.setInterpersonalRelationshipsComments("cnjbsk");

        PersonalSkillsEvaluation pe = new PersonalSkillsEvaluation();
        pe.setPersonalStills_a(EvaluationOption.TOTAL_AGREEMENT);
        pe.setPersonalStills_b(EvaluationOption.TOTAL_AGREEMENT);
        pe.setPersonalStills_c(EvaluationOption.TOTAL_AGREEMENT);
        pe.setPersonalStills_d(EvaluationOption.TOTAL_AGREEMENT);
        e.setPersonalSkillsEvaluation(pe);

        e.setOverallAppreciation(OverallAppreciation.SOMEWHAT_DISAGREEMENT);
        e.setOverallComments("cwkjndsc");
        e.setProgram("Polop");
        e.setEvaluationDiscussedWithIntern(true);
        e.setSupervisionHoursPerWeek(12.0);

        e.setWillingnessToRehire(WillingnessToRehire.YES);
        e.setTechnicalTrainingComments("mcnodih");
        e.setFunction("wefw");
        e.setReturnFormToEmail("owpdcs");
        e.setReturnFormToName("kwljd");

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("NomCompagnie");
        employeur.setCredentials("hi@error.com", "mdp");
        employeur.setAdresse("123 Main Street");
        employeur.setTelephone("555-555-5555");
        employeur.setCity("Montreal");
        employeur.setPostalCode("H3H1P9");
        employeur.setFax("123-456-7890");

        Student student = new Student();
        student.setId(1L);
        student.setNom("Monroe");
        student.setPrenom("Marilyn");
        student.setAdresse("test");
        student.setTelephone("1234567890");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("test@email.com", "passwordEncoder.encode(\"test\")");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(3L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setEmployeur(employeur);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setActive(false);
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);
        jobOfferApplication.setJobOffer(jobOffer);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setId(3L);
        jobInterview.setJobOfferApplication(jobOfferApplication);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setInterviewType(InterviewType.ONLINE);
        jobInterview.setInterviewLocationOrLink("https://woah.com");
        jobInterview.setCreationDate(LocalDateTime.now());


        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);
        internshipOffer.setConfirmationDate(null);
        internshipOffer.setJobOfferApplication(jobOfferApplication);

        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setId(1L);

        UserDTO user = new UserDTO(1L, "aw@mail.com", "poewnfdk", Role.EMPLOYEUR);

        when(employeurRepository.findById(anyLong())).thenReturn(Optional.of(employeur));
        when(internshipEvaluationRepository.findByInternshipOfferId(anyLong())).thenReturn(Optional.of(internshipEvaluation));
        when(evaluationInternRepository.save(any())).thenReturn(new EvaluationIntern());


        // Act
        try {
            employeurService.saveEvaluation(internshipOffer.getId(), e, user);
        } catch (InvalidBase64Exception er){
            System.err.println(er.getMessage());
        }

    }

    @Test
    void saveEvaluation_IsNull() throws InternshipNotFoundException, InvalidBase64Exception, IOException {
        UserDTO user = new UserDTO(1L, "aw@mail.com", "poewnfdk", Role.EMPLOYEUR);
        try {
            employeurService.saveEvaluation(1L, null, user);
        } catch (IllegalArgumentException e){
            assertThat(e.getMessage()).isEqualTo("Evaluation data cannot be null");
        }
    }

    @Test
    void update_Employeur() throws InvalidPasswordException {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("NomCompagnie");
        employeur.setCredentials("hi@error.com", "mdp");
        employeur.setAdresse("123 Main Street");
        employeur.setTelephone("555-555-5555");
        employeur.setCity("Montreal");
        employeur.setPostalCode("H3H1P9");
        employeur.setFax("123-456-7890");

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(employeurRepository.findById(any())).thenReturn(Optional.of(employeur));
        when(employeurRepository.save(employeur)).thenReturn(employeur);

        EmployeurDTO updateDTO = new EmployeurDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", "1234657899", "123 Main Street",
                "hi@error.com");

        // Act
        EmployeurDTO result = employeurService.updateEmployeur(1L, updateDTO);
        assertThat(result.nomCompagnie()).isEqualTo(updateDTO.nomCompagnie());
    }

    @Test
    void update_Employeur_Password() throws InvalidPasswordException {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("NomCompagnie");
        employeur.setCredentials("hi@error.com", "mdp");
        employeur.setAdresse("123 Main Street");
        employeur.setTelephone("555-555-5555");
        employeur.setCity("Montreal");
        employeur.setPostalCode("H3H1P9");
        employeur.setFax("123-456-7890");

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(employeurRepository.findById(any())).thenReturn(Optional.of(employeur));
        when(employeurRepository.save(employeur)).thenReturn(employeur);

        EmployeurDTO updateDTO = new EmployeurDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", "1234657899", "123 Main Street",
                "hi@error.com");

        UpdatePasswordDTO updatePasswordDTO;
        updatePasswordDTO = new UpdatePasswordDTO();
        updatePasswordDTO.setCurrentPassword(NoOpPasswordEncoder.getInstance().encode("mdp"));
        updatePasswordDTO.setNewPassword(NoOpPasswordEncoder.getInstance().encode("1234567"));

        // Act
        employeurService.updateEmployerPassword(1L, updatePasswordDTO);
    }

    @Test
    void update_Employeur_Password_Incorrect() throws InvalidPasswordException {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("NomCompagnie");
        employeur.setCredentials("hi@error.com", "mdp");
        employeur.setAdresse("123 Main Street");
        employeur.setTelephone("555-555-5555");
        employeur.setCity("Montreal");
        employeur.setPostalCode("H3H1P9");
        employeur.setFax("123-456-7890");

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(employeurRepository.findById(any())).thenReturn(Optional.of(employeur));
        when(employeurRepository.save(employeur)).thenReturn(employeur);

        EmployeurDTO updateDTO = new EmployeurDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", "1234657899", "123 Main Street",
                "hi@error.com");

        UpdatePasswordDTO updatePasswordDTO;
        updatePasswordDTO = new UpdatePasswordDTO();
        updatePasswordDTO.setCurrentPassword(NoOpPasswordEncoder.getInstance().encode("pdf"));
        updatePasswordDTO.setNewPassword(NoOpPasswordEncoder.getInstance().encode("1234567"));

        // Act
        try {
            employeurService.updateEmployerPassword(1L, updatePasswordDTO);
        } catch (InvalidPasswordException e){
            assertThat(e.getMessage()).isEqualTo("Current password is incorrect.");
        }
    }
}
