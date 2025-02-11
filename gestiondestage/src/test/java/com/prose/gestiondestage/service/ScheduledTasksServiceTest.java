package com.prose.gestiondestage.service;

import com.prose.entity.*;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Student;
import com.prose.repository.InternshipEvaluationRepository;
import com.prose.service.*;
import com.prose.service.dto.EmployeurDTO;
import com.prose.service.dto.TeacherDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class ScheduledTasksServiceTest {

    private NotificationService notificationService;

    private TeacherService teacherService;

    private EmployeurService employerService;

    private ProgramManagerService programManagerService;

    private InternshipEvaluationRepository internshipEvaluationRepository;

    private ScheduledTasksService scheduledTasksService;

    @BeforeEach
    public void setUp() {
        notificationService = mock(NotificationService.class);
        teacherService = mock(TeacherService.class);
        internshipEvaluationRepository = mock(InternshipEvaluationRepository.class);
        employerService = mock(EmployeurService.class);
        programManagerService = mock(ProgramManagerService.class);
        scheduledTasksService = new ScheduledTasksService(teacherService, internshipEvaluationRepository, notificationService, employerService, programManagerService);
    }

    @Test
    public void testOperationNotifyConcernedTeachersEvaluateInternshipEnvironment() {
        // Arrange
        TeacherDTO teacher = new TeacherDTO(1L, "John", "Doe", "jo@do.com", "1234567", "1234567890", Discipline.INFORMATIQUE.getTranslation());
        List<TeacherDTO> teachers = Collections.singletonList(teacher);
        when(teacherService.getAllProfs()).thenReturn(teachers);

        InternshipEvaluation evaluation = mock(InternshipEvaluation.class);
        InternshipOffer internshipOffer = mock(InternshipOffer.class);
        JobOfferApplication jobOfferApplication = mock(JobOfferApplication.class);
        JobOffer jobOffer = mock(JobOffer.class);
        CurriculumVitae curriculumVitae = mock(CurriculumVitae.class);
        Student student = new Student(1L, "Jane", "Doe", "jand@oe.com", "mdpppppp", "1234567", "1234567890", Discipline.INFORMATIQUE);

        when(evaluation.getInternshipOffer()).thenReturn(internshipOffer);
        when(internshipOffer.getJobOfferApplication()).thenReturn(jobOfferApplication);
        when(jobOfferApplication.getJobOffer()).thenReturn(jobOffer);
        when(jobOffer.getDateDebut()).thenReturn(LocalDate.now().toString());
        when(jobOfferApplication.getCurriculumVitae()).thenReturn(curriculumVitae);
        when(curriculumVitae.getStudent()).thenReturn(student);

        List<InternshipEvaluation> evaluations = Collections.singletonList(evaluation);
        when(internshipEvaluationRepository.findByTeacherId(teacher.id())).thenReturn(evaluations);

        // Act
        scheduledTasksService.operationNotifyConcernedTeachersEvaluateInternshipEnvironment();

        // Assert
//        verify(notificationService, times(1)).addNotification(eq(NotificationCode.INTERNSHIP_ENVIRONMENT_TO_REVIEW), eq(teacher.id()), eq("Jane Doe"));
    }

    @Test
    public void testOperationNotifyConcernedTeachersEvaluateInternshipEnvironmentNoTeacher() {
        // Arrange
        List<TeacherDTO> teachers = Collections.emptyList();
        when(teacherService.getAllProfs()).thenReturn(teachers);

        // Act
        scheduledTasksService.operationNotifyConcernedTeachersEvaluateInternshipEnvironment();

        // Assert
//        verify(notificationService, never()).addUserNotification(any(NotificationCode.class), any(Long.class), any(String.class));
    }

    @Test
    public void testOperationNotifyConcernedTeachersEvaluateInternshipEnvironmentNoEvaluation() {
        // Arrange
        TeacherDTO teacher = new TeacherDTO(1L, "John", "Doe", "jo@do.com", "1234567", "1234567890", Discipline.INFORMATIQUE.getTranslation());
        List<TeacherDTO> teachers = Collections.singletonList(teacher);
        when(teacherService.getAllProfs()).thenReturn(teachers);

        when(internshipEvaluationRepository.findByTeacherId(teacher.id())).thenReturn(Collections.emptyList());

        // Act
        scheduledTasksService.operationNotifyConcernedTeachersEvaluateInternshipEnvironment();

        // Assert
//        verify(notificationService, never()).addUserNotification(any(NotificationCode.class), any(Long.class), any(String.class));
    }

    @Test
    public void testOperationNotifyConcernedTeachersEvaluateInternshipEnvironmentJobOfferStartsLater() {
        // Arrange
        TeacherDTO teacher = new TeacherDTO(1L, "John", "Doe", "jo@do.com", "1234567", "1234567890", Discipline.INFORMATIQUE.getTranslation());

        List<TeacherDTO> teachers = Collections.singletonList(teacher);
        when(teacherService.getAllProfs()).thenReturn(teachers);

        InternshipEvaluation evaluation = mock(InternshipEvaluation.class);
        InternshipOffer internshipOffer = mock(InternshipOffer.class);
        JobOfferApplication jobOfferApplication = mock(JobOfferApplication.class);
        JobOffer jobOffer = mock(JobOffer.class);
        CurriculumVitae curriculumVitae = mock(CurriculumVitae.class);
        Student student = new Student(1L, "Jane", "Doe", "ja@do.com", "mdp", "1234567", "1234567890", Discipline.INFORMATIQUE);

        when(evaluation.getInternshipOffer()).thenReturn(internshipOffer);
        when(internshipOffer.getJobOfferApplication()).thenReturn(jobOfferApplication);
        when(jobOfferApplication.getJobOffer()).thenReturn(jobOffer);
        when(jobOffer.getDateDebut()).thenReturn(LocalDate.now().plusDays(1).toString());
        when(jobOfferApplication.getCurriculumVitae()).thenReturn(curriculumVitae);
        when(curriculumVitae.getStudent()).thenReturn(student);

        List<InternshipEvaluation> evaluations = Collections.singletonList(evaluation);
        when(internshipEvaluationRepository.findByTeacherId(teacher.id())).thenReturn(evaluations);

        // Act
        scheduledTasksService.operationNotifyConcernedTeachersEvaluateInternshipEnvironment();

        // Assert
//        verify(notificationService, never()).addUserNotification(any(NotificationCode.class), any(Long.class), any(String.class));
    }

    @Test
    public void testOperationNotifyConcernedTeachersEvaluateInternshipEnvironmentJobOfferStartedYesterday() {
        // Arrange
        TeacherDTO teacher = new TeacherDTO(1L, "John", "Doe", "jo@do.com", "1234567", "1234567890", Discipline.INFORMATIQUE.getTranslation());

        List<TeacherDTO> teachers = Collections.singletonList(teacher);
        when(teacherService.getAllProfs()).thenReturn(teachers);

        InternshipEvaluation evaluation = mock(InternshipEvaluation.class);
        InternshipOffer internshipOffer = mock(InternshipOffer.class);
        JobOfferApplication jobOfferApplication = mock(JobOfferApplication.class);
        JobOffer jobOffer = mock(JobOffer.class);
        CurriculumVitae curriculumVitae = mock(CurriculumVitae.class);
        Student student = new Student(1L, "Jane", "Doe", "ja@do.com", "mdp", "1234567", "1234567890", Discipline.INFORMATIQUE);

        when(evaluation.getInternshipOffer()).thenReturn(internshipOffer);
        when(internshipOffer.getJobOfferApplication()).thenReturn(jobOfferApplication);
        when(jobOfferApplication.getJobOffer()).thenReturn(jobOffer);
        when(jobOffer.getDateDebut()).thenReturn(LocalDate.now().minusDays(1).toString());
        when(jobOfferApplication.getCurriculumVitae()).thenReturn(curriculumVitae);
        when(curriculumVitae.getStudent()).thenReturn(student);

        List<InternshipEvaluation> evaluations = Collections.singletonList(evaluation);
        when(internshipEvaluationRepository.findByTeacherId(teacher.id())).thenReturn(evaluations);

        // Act
        scheduledTasksService.operationNotifyConcernedTeachersEvaluateInternshipEnvironment();

        // Assert
//        verify(notificationService, never()).addUserNotification(any(NotificationCode.class), any(Long.class), any(String.class));
    }


    @Test
    public void operationNotifyConcernedEmployersEvaluateInterns() {
        EmployeurDTO employer = new EmployeurDTO(
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
        List<EmployeurDTO> employers = Collections.singletonList(employer);
        when(employerService.getAllEmployeurs()).thenReturn(employers);

        InternshipEvaluation evaluation = mock(InternshipEvaluation.class);
        InternshipOffer internshipOffer = mock(InternshipOffer.class);
        JobOfferApplication jobOfferApplication = mock(JobOfferApplication.class);
        JobOffer jobOffer = mock(JobOffer.class);
        CurriculumVitae curriculumVitae = mock(CurriculumVitae.class);
        Student student = new Student(1L, "Jane", "Doe", "jane@doe.com", "password", "1234567", "1234567890", Discipline.INFORMATIQUE);

        when(evaluation.getInternshipOffer()).thenReturn(internshipOffer);
        when(internshipOffer.getJobOfferApplication()).thenReturn(jobOfferApplication);
        when(jobOfferApplication.getJobOffer()).thenReturn(jobOffer);
        when(jobOffer.getDateFin()).thenReturn(LocalDate.now().plusWeeks(2).toString());
        when(jobOfferApplication.getCurriculumVitae()).thenReturn(curriculumVitae);
        when(curriculumVitae.getStudent()).thenReturn(student);

        List<InternshipEvaluation> evaluations = Collections.singletonList(evaluation);
        when(internshipEvaluationRepository.findCurrentEvaluationsByEmployerId(employer.id())).thenReturn(evaluations);

        scheduledTasksService.operationNotifyConcernedEmployersEvaluateInterns();

//        verify(notificationService, times(1)).addUserNotification(eq(NotificationCode.INTERN_TO_REVIEW), eq(employer.id()), eq("Jane Doe"));
    }

    @Test
    public void operationNotifyConcernedEmployersEvaluateInternsNoEmployer() {
        List<EmployeurDTO> employers = Collections.emptyList();
        when(employerService.getAllEmployeurs()).thenReturn(employers);

        scheduledTasksService.operationNotifyConcernedEmployersEvaluateInterns();

//        verify(notificationService, never()).addUserNotification(any(NotificationCode.class), any(Long.class), any(String.class));
    }

    @Test
    public void operationNotifyConcernedEmployersEvaluateInternsNoEvaluation() {
        EmployeurDTO employer = new EmployeurDTO(
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
        List<EmployeurDTO> employers = Collections.singletonList(employer);
        when(employerService.getAllEmployeurs()).thenReturn(employers);

        when(internshipEvaluationRepository.findCurrentEvaluationsByEmployerId(employer.id())).thenReturn(Collections.emptyList());

        scheduledTasksService.operationNotifyConcernedEmployersEvaluateInterns();

//        verify(notificationService, never()).addUserNotification(any(NotificationCode.class), any(Long.class), any(String.class));
    }

    @Test
    public void operationNotifyConcernedEmployersEvaluateInternsJobOfferEndsInMoreThanTwoWeeks() {
        EmployeurDTO employer = new EmployeurDTO(
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
        List<EmployeurDTO> employers = Collections.singletonList(employer);
        when(employerService.getAllEmployeurs()).thenReturn(employers);

        InternshipEvaluation evaluation = mock(InternshipEvaluation.class);
        InternshipOffer internshipOffer = mock(InternshipOffer.class);
        JobOfferApplication jobOfferApplication = mock(JobOfferApplication.class);
        JobOffer jobOffer = mock(JobOffer.class);
        CurriculumVitae curriculumVitae = mock(CurriculumVitae.class);
        Student student = new Student(1L, "Jane", "Doe", "jane@doe.com", "password", "1234567", "1234567890", Discipline.INFORMATIQUE);

        when(evaluation.getInternshipOffer()).thenReturn(internshipOffer);
        when(internshipOffer.getJobOfferApplication()).thenReturn(jobOfferApplication);
        when(jobOfferApplication.getJobOffer()).thenReturn(jobOffer);
        when(jobOffer.getDateFin()).thenReturn(LocalDate.now().plusWeeks(3).toString());
        when(jobOfferApplication.getCurriculumVitae()).thenReturn(curriculumVitae);
        when(curriculumVitae.getStudent()).thenReturn(student);

        List<InternshipEvaluation> evaluations = Collections.singletonList(evaluation);
        when(internshipEvaluationRepository.findCurrentEvaluationsByEmployerId(employer.id())).thenReturn(evaluations);

        scheduledTasksService.operationNotifyConcernedEmployersEvaluateInterns();

//        verify(notificationService, never()).addUserNotification(any(NotificationCode.class), any(Long.class), any(String.class));
    }

    @Test
    public void operationNotifyConcernedEmployersEvaluateInternsJobOfferEndedYesterday() {
        EmployeurDTO employer = new EmployeurDTO(
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
        List<EmployeurDTO> employers = Collections.singletonList(employer);
        when(employerService.getAllEmployeurs()).thenReturn(employers);

        InternshipEvaluation evaluation = mock(InternshipEvaluation.class);
        InternshipOffer internshipOffer = mock(InternshipOffer.class);
        JobOfferApplication jobOfferApplication = mock(JobOfferApplication.class);
        JobOffer jobOffer = mock(JobOffer.class);
        CurriculumVitae curriculumVitae = mock(CurriculumVitae.class);
        Student student = new Student(1L, "Jane", "Doe", "jane@doe.com", "password", "1234567", "1234567890", Discipline.INFORMATIQUE);

        when(evaluation.getInternshipOffer()).thenReturn(internshipOffer);
        when(internshipOffer.getJobOfferApplication()).thenReturn(jobOfferApplication);
        when(jobOfferApplication.getJobOffer()).thenReturn(jobOffer);
        when(jobOffer.getDateFin()).thenReturn(LocalDate.now().minusDays(1).toString());
        when(jobOfferApplication.getCurriculumVitae()).thenReturn(curriculumVitae);
        when(curriculumVitae.getStudent()).thenReturn(student);

        List<InternshipEvaluation> evaluations = Collections.singletonList(evaluation);
        when(internshipEvaluationRepository.findCurrentEvaluationsByEmployerId(employer.id())).thenReturn(evaluations);

        scheduledTasksService.operationNotifyConcernedEmployersEvaluateInterns();

//        verify(notificationService, never()).addUserNotification(any(NotificationCode.class), any(Long.class), any(String.class));
    }
}