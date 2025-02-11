package com.prose.gestiondestage.service;

import com.prose.entity.Discipline;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Employeur;
import com.prose.entity.users.Student;
import com.prose.entity.*;
import com.prose.entity.users.Teacher;
import com.prose.repository.EmployeurRepository;
import com.prose.repository.InternshipOfferRepository;
import com.prose.repository.JobInterviewRepository;
import com.prose.repository.StudentRepository;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.*;
import com.prose.service.NotificationService;
import com.prose.service.StudentService;
import com.prose.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class StudentServiceTests {
    StudentRepository studentRepository;
    StudentService studentService;
    JobInterviewRepository jobInterviewRepository;

    NotificationService notificationService;
    InternshipOfferRepository internshipOfferRepository;
    EmployeurRepository employeurRepository;


    @BeforeEach
    public void beforeEach() {
        studentRepository = mock(StudentRepository.class);
        notificationService = mock(NotificationService.class);
        jobInterviewRepository = mock(JobInterviewRepository.class);
        internshipOfferRepository = mock(InternshipOfferRepository.class);

        studentService = new StudentService(studentRepository, NoOpPasswordEncoder.getInstance(),jobInterviewRepository,notificationService,internshipOfferRepository,employeurRepository);
    }

    @Test
    public void createStudentTest() throws InvalidUserFormatException, AlreadyExistsException {
        // Arrange

        Student student = new Student();
        student.setId(0L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("hi@error.com","a");

        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp",
                Discipline.INFORMATIQUE.toString()

        );


        when(studentRepository.save(any())).thenReturn(student);
        // Act
        StudentDTO studentDTO1 = studentService.createStudent(studentRegisterDTO);
        // Assert
        assertThat(studentDTO1.nom()).isEqualTo(studentRegisterDTO.nom());
        assertThat(studentDTO1.courriel()).isEqualTo(studentRegisterDTO.courriel());
    }

    @Test
    public void createStudentAlreadyExistsTest(){
        // Arrange

        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp",
                Discipline.INFORMATIQUE.toString()

        );


        when(studentRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
        // Act
        assertThatThrownBy(() -> studentService.createStudent(studentRegisterDTO))
                .isInstanceOf(AlreadyExistsException.class); // Assert

    }

    @Test
    public void createStudentInvalidDisciplineTest() {
        // Arrange

        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp",
                "Nope"

        );

        // Act
        assertThatThrownBy(() -> studentService.createStudent(studentRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }


    @Test
    public void createStudentInvalidPhoneTest()  {
        // Arrange

        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "(555-555-5555",
                "mdp",
                Discipline.INFORMATIQUE.toString()

        );

        // Act
        assertThatThrownBy(() -> studentService.createStudent(studentRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createStudentInvalidEmailStartTest()  {
        // Arrange

        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO(
                "Nom",
                "prenom",
                "@error.com",
                "addresse",
                "5555555555",
                "mdp",
                Discipline.INFORMATIQUE.toString()

        );

        // Act
        assertThatThrownBy(() -> studentService.createStudent(studentRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createStudentInvalidEmailEndTest()  {
        // Arrange

        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO(
                "Nom",
                "prenom",
                "hi@.com",
                "addresse",
                "5555555555",
                "mdp",
                Discipline.INFORMATIQUE.toString()

        );

        // Act
        assertThatThrownBy(() -> studentService.createStudent(studentRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createStudentNameEmptyTest()  {
        // Arrange

        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO(
                "   ",
                "prenom",
                "hi@.com",
                "addresse",
                "5555555555",
                "mdp",
                Discipline.INFORMATIQUE.toString()

        );

        // Act
        assertThatThrownBy(() -> studentService.createStudent(studentRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createStudentFirstNameEmptyTest() {
        // Arrange

        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO(
                "Nom",
                "",
                "hi@.com",
                "addresse",
                "5555555555",
                "mdp",
                Discipline.INFORMATIQUE.toString()

        );

        // Act
        assertThatThrownBy(() -> studentService.createStudent(studentRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createStudentAdressEmptyTest()  {
        // Arrange

        StudentRegisterDTO studentRegisterDTO = new StudentRegisterDTO(
                "Nom",
                "Prenom",
                "hi@.com",
                " ",
                "5555555555",
                "mdp",
                Discipline.INFORMATIQUE.toString()

        );

        // Act
        assertThatThrownBy(() -> studentService.createStudent(studentRegisterDTO))
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }


    @Test
    public void getStudentTest() {
        // Arrange
        Student student = new Student();
        student.setId(0L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        student.setCredentials("hi@error.com","a");

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        // Act
        StudentDTO result = studentService.getStudent(0);
        // Assert
        assertThat(result.id()).isEqualTo(student.getId());
        assertThat(result.nom()).isEqualTo(student.getNom());
    }

    @Test
    public void getStudentNotFoundTest() {
        // Arrange
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> studentService.getStudent(0))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }

    @Test
    public void getStudentEmailTest() {
        // Arrange
        Student student = new Student();
        student.setId(0L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        student.setCredentials("hi@error.com","a");

        when(studentRepository.findUserAppByEmail(anyString())).thenReturn(Optional.of(student));
        // Act
        StudentDTO result = studentService.getStudentByEmail("hi");
        // Assert
        assertThat(result.id()).isEqualTo(student.getId());
        assertThat(result.nom()).isEqualTo(student.getNom());
    }

    @Test
    public void getStudentEmailNotFoundTest() {
        // Arrange
        when(studentRepository.findUserAppByEmail(anyString())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> studentService.getStudentByEmail("no"))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }

    @Test
    public void getStudentsByDisciplineTest_success() throws DisciplineNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("example@hotmail.com", "a");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        // Act
        when(studentRepository.findByDiscipline(any(),eq(""),any())).thenReturn(new PageImpl<>(java.util.List.of(student),PageRequest.of(5,5),5));
        when(studentRepository.findByDiscipline(any(),eq("_%"),any())).thenReturn(new PageImpl<>(java.util.List.of(student),PageRequest.of(5,5),5));

        // Assert
        assertThat(studentService.getStudentsByDiscipline(Discipline.INFORMATIQUE.toString(),"",5,5).getContent()).isNotEmpty();
        assertThat(studentService.getStudentsByDiscipline(Discipline.INFORMATIQUE.toString(),"_%",5,5).getContent().get(0).id()).isEqualTo(student.getId());
    }

    @Test
    public void getStudentsByDisciplineTest_disciplineNotFound() {
        // Arrange
        String discipline = "Nope";

        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentsByDiscipline(discipline,"",5,5))
                .isInstanceOf(DisciplineNotFoundException.class); // Assert
    }

    @Test
    public void getStudentsByDisciplineTest_studentsNotFound(){
        // Act
        when(studentRepository.findByDiscipline(any(),anyString(),any())).thenReturn(Page.empty());

        // Assert
        assertThatThrownBy(() -> studentService.getStudentsByDiscipline(Discipline.INFORMATIQUE.toString(),"",5,5))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }

    @Test
    void getListJobInterviewEmptyFilter() throws JobNotFoundException, InvalidUserFormatException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

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

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.existsById(anyLong())).thenReturn(true);
        when(jobInterviewRepository.findJobInterviewByStudentIdPaged(anyLong(), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));

        List<JobInterviewDTO> jobInterviewDTOS = studentService.getJobInterviews(student.getId(), 2L,"","","",5,5).getContent();

        // Act
        assertThat(jobInterviewDTOS).isNotEmpty();
        assertThat(jobInterviewDTOS.getFirst().id()).isEqualTo(jobInterview.getId());
    }

    @Test
    void getListJobInterviewDatesFilter() throws JobNotFoundException, InvalidUserFormatException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

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

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.existsById(anyLong())).thenReturn(true);
        when(jobInterviewRepository.findJobInterviewByStudentIdPaged(anyLong(), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));

        List<JobInterviewDTO> jobInterviewDTOS = studentService.getJobInterviews(student.getId(), 2L,"2011-12-03","2011-12-04","",5,5).getContent();

        // Act
        assertThat(jobInterviewDTOS).isNotEmpty();
        assertThat(jobInterviewDTOS.getFirst().id()).isEqualTo(jobInterview.getId());
    }

    @Test
    void getListJobInterviewConfirmedFilter() throws JobNotFoundException, InvalidUserFormatException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

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

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.existsById(anyLong())).thenReturn(true);
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        when(jobInterviewRepository.findJobInterviewByStudentIdConfirmedByStudent(anyLong(), anyLong(),any(),any(),argumentCaptor.capture(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));

        List<JobInterviewDTO> jobInterviewDTOS = studentService.getJobInterviews(student.getId(), 2L,"","","confirmed",5,5).getContent();

        // Act
        assertThat(jobInterviewDTOS).isNotEmpty();
        assertThat(jobInterviewDTOS.getFirst().id()).isEqualTo(jobInterview.getId());
        assertThat(argumentCaptor.getValue()).isTrue();
    }

    @Test
    void getListJobInterviewNotConfirmedFilter() throws JobNotFoundException, InvalidUserFormatException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

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

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.existsById(anyLong())).thenReturn(true);
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        when(jobInterviewRepository.findJobInterviewByStudentIdConfirmedByStudent(anyLong(), anyLong(),any(),any(),argumentCaptor.capture(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));

        List<JobInterviewDTO> jobInterviewDTOS = studentService.getJobInterviews(student.getId(), 2L,"","","nonConfirmed",5,5).getContent();

        // Act
        assertThat(jobInterviewDTOS).isNotEmpty();
        assertThat(jobInterviewDTOS.getFirst().id()).isEqualTo(jobInterview.getId());
        assertThat(argumentCaptor.getValue()).isFalse();
    }

    @Test
    void getListJobInterviewNotCancelledFilter() throws JobNotFoundException, InvalidUserFormatException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

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

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.existsById(anyLong())).thenReturn(true);
        when(jobInterviewRepository.findJobInterviewByStudentIdNotCancelled(anyLong(), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));

        List<JobInterviewDTO> jobInterviewDTOS = studentService.getJobInterviews(student.getId(), 2L,"","","valide",5,5).getContent();

        // Act
        assertThat(jobInterviewDTOS).isNotEmpty();
        assertThat(jobInterviewDTOS.getFirst().id()).isEqualTo(jobInterview.getId());
    }

    @Test
    void getListJobInterviewCancelledFilter() throws JobNotFoundException, InvalidUserFormatException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

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

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.existsById(anyLong())).thenReturn(true);
        when(jobInterviewRepository.findJobInterviewByStudentIdCancelled(anyLong(), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));

        List<JobInterviewDTO> jobInterviewDTOS = studentService.getJobInterviews(student.getId(), 2L,"","","cancelled",5,5).getContent();

        // Act
        assertThat(jobInterviewDTOS).isNotEmpty();
        assertThat(jobInterviewDTOS.getFirst().id()).isEqualTo(jobInterview.getId());
    }

    @Test
    void getListJobInterviewBadFilter() throws JobNotFoundException, InvalidUserFormatException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

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

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.existsById(anyLong())).thenReturn(true);
        when(jobInterviewRepository.findJobInterviewByStudentIdCancelled(anyLong(), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        assertThatThrownBy(() -> studentService.getJobInterviews(student.getId(), 2L,"","","badFilterwiolajhwedklahsj",5,5).getContent())
                .isInstanceOf(InvalidUserFormatException.class); // Assert
    }

    @Test
    void getListJobInterviewBadDate() throws JobNotFoundException, InvalidUserFormatException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("a@ml.com", "123456");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

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

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        when(jobInterviewRepository.existsById(anyLong())).thenReturn(true);
        when(jobInterviewRepository.findJobInterviewByStudentIdCancelled(anyLong(), anyLong(),any(),any(),any())).thenReturn(new PageImpl<>(List.of(jobInterview),PageRequest.of(5,5),5));
        // Act
        assertThatThrownBy(() -> studentService.getJobInterviews(student.getId(), 2L,"woah","date","",5,5).getContent())
                .isInstanceOf(InvalidUserFormatException.class); // Assert
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

        when(internshipOfferRepository.findInternshipOfferWithContractStudent(eq(student.getId()),eq(session.getId()),any())).thenReturn(new PageImpl<>(List.of(internshipOffer), PageRequest.of(5,5),5));

        // Act
        List<InternshipOfferDTO> result = studentService.getContracts(student.getId(),session.getId(),5,5).getContent();
        // Assert
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getFirst().contractSignatureDTO()).isNull();
    }

    @Test
    public void getAwaitingContractEmptyTest() {
        // Arrange
        long studentId = 2L;
        when(internshipOfferRepository.findInternshipOfferWithContractStudent(eq(studentId),eq(5L),any())).thenReturn(Page.empty());

        // Act
        List<InternshipOfferDTO> result = studentService.getContracts(studentId,5L,5,5).getContent();
        // Assert
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void student_LastConnection(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("as@mail.com", "123456");
        student.setLastConnection(LocalDateTime.now());

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        // Act
        studentService.setLastConnection(student.getId());
        assertThat(student.getLastConnection()).isNotNull();
    }

    @Test
    void student_LastConnection_NotFound(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setCredentials("as@mail.com", "123456");
        student.setLastConnection(LocalDateTime.now());

        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());
        // Act
        try {
            studentService.setLastConnection(student.getId());
        } catch (UserNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("User not found");
        }
    }

    @Test
    void updateStudent() throws InvalidPasswordException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("va@mail.com", "123456");

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(studentRepository.findById(any())).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        StudentDTO updateDTO = new StudentDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", discipline);

        // Act
        StudentDTO result = studentService.updateStudent(1L, updateDTO);
        // Assert
        assertThat(result.nom()).isEqualTo(updateDTO.nom());
    }

    @Test
    void updateTeacher_Password() throws InvalidPasswordException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("va@mail.com", "123456");

        student.setCredentials("va@mail.com", NoOpPasswordEncoder.getInstance().encode("123456"));

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(studentRepository.findById(any())).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        TeacherDTO updateDTO = new TeacherDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", discipline);

        UpdatePasswordDTO updatePasswordDTO;
        updatePasswordDTO = new UpdatePasswordDTO();
        updatePasswordDTO.setCurrentPassword(NoOpPasswordEncoder.getInstance().encode("123456"));
        updatePasswordDTO.setNewPassword(NoOpPasswordEncoder.getInstance().encode("1234567"));

        // Act
        StudentDTO result = studentService.updateStudentPassword(1L,updatePasswordDTO);

        assertThat(result.nom().equalsIgnoreCase("Nom"));
    }

    @Test
    void updateTeacher_Password_Incorrect() throws InvalidPasswordException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setTelephone("5555555555");
        student.setDiscipline(Discipline.INFORMATIQUE);
        student.setCredentials("va@mail.com", "123456");

        student.setCredentials("va@mail.com", NoOpPasswordEncoder.getInstance().encode("1234567"));

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(studentRepository.findById(any())).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        TeacherDTO updateDTO = new TeacherDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", discipline);

        UpdatePasswordDTO updatePasswordDTO;
        updatePasswordDTO = new UpdatePasswordDTO();
        updatePasswordDTO.setCurrentPassword(NoOpPasswordEncoder.getInstance().encode("123456"));
        updatePasswordDTO.setNewPassword(NoOpPasswordEncoder.getInstance().encode("1234567"));

        try {
            studentService.updateStudentPassword(1L,updatePasswordDTO);
        } catch (InvalidPasswordException e) {
            assertThat(e.getMessage()).isEqualTo("Current password is incorrect.");
        }

    }



    // TODO : Retirer car bogue... mais aussi est-ce que on devrait vraiment retourner un erreur pour une liste vide??? - Nathan_Nino
    /*@Test
    void getListJobInterview_NotFound() throws JobNotFoundException {
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

        jobOfferApplication.setJobInterviews(List.of(jobInterview));

        // Act
        when(jobInterviewRepository.existsById(anyLong())).thenReturn(false);

        // Assert
        assertThatThrownBy(() -> studentService.getJobInterviews(student.getId()))
                .isInstanceOf(JobNotFoundException.class); //

    }*/


}
