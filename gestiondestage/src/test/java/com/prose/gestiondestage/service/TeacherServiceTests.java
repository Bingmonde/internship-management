package com.prose.gestiondestage.service;


import com.prose.entity.*;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Employeur;
import com.prose.entity.users.Student;
import com.prose.entity.users.Teacher;
import com.prose.entity.users.UserState;
import com.prose.repository.*;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.AlreadyExistsException;
import com.prose.service.Exceptions.InvalidPasswordException;
import com.prose.service.Exceptions.InvalidUserFormatException;
import com.prose.service.NotificationService;
import com.prose.service.TeacherService;
import com.prose.service.dto.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TeacherServiceTests {
    TeacherRepository teacherRepository;
    InternshipEvaluationRepository internshipEvaluationRepository;
    AcademicSessionRepository academicSessionRepository;

    TeacherService teacherService;

    NotificationService notificationService;
    InternshipOfferRepository internshipOfferRepository;
    EvaluationEmployerRepository evaluationEmployerRepository;
    EmployeurDTO employeurDTO;

    @BeforeEach
    public void beforeEach() {
        teacherRepository = mock(TeacherRepository.class);
        notificationService = mock(NotificationService.class);
        internshipEvaluationRepository = mock(InternshipEvaluationRepository.class);
        academicSessionRepository = mock(AcademicSessionRepository.class);

        teacherService = new TeacherService(teacherRepository, NoOpPasswordEncoder.getInstance(),notificationService, internshipEvaluationRepository, academicSessionRepository);
    }

    @Test
    public void createTeacherTest() throws InvalidUserFormatException, AlreadyExistsException {
        // Arrange

        Teacher teacher = new Teacher();
        teacher.setId(0L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setCredentials("hi@error.com","a");
        teacher.setDiscipline(Discipline.INFORMATIQUE);

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp",
                "informatique"
        );


        when(teacherRepository.save(any())).thenReturn(teacher);
        // Act
        TeacherDTO teacherDTO = teacherService.createTeacher(teacherRegisterDTO);
        // Assert
        assertThat(teacherDTO.nom()).isEqualTo(teacherRegisterDTO.nom());
        assertThat(teacherDTO.courriel()).isEqualTo(teacherRegisterDTO.courriel());
    }

    @Test
    public void createTeacherTest_BadEnum() throws InvalidUserFormatException, AlreadyExistsException {
        // Arrange

        Teacher teacher = new Teacher();
        teacher.setId(0L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setCredentials("hi@error.com","a");
        teacher.setDiscipline(Discipline.INFORMATIQUE);

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp",
                "blabla"
        );


        when(teacherRepository.save(any())).thenReturn(teacher);
        // Act
        try {
            teacherService.createTeacher(teacherRegisterDTO);
        } catch (InvalidUserFormatException e) {
            assertThat(e.getMessage()).isEqualTo("Enum not found");
        }
    }

    @Test
    public void createTeacherAlreadyExistsTest() {
        // Arrange

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp",
                "informatique"
        );


        when(teacherRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
        // Act
        assertThatThrownBy(() -> {teacherService.createTeacher(teacherRegisterDTO);})
                .isInstanceOf(AlreadyExistsException.class); // Assert

    }

    @Test
    public void createTeacherInvalidPhoneTest() {
        // Arrange

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "(555-555-5555",
                "mdp",
                "informatique"
        );

        // Act
        assertThatThrownBy(() -> {teacherService.createTeacher(teacherRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createTeacherInvalidEmailStartTest() {
        // Arrange

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO(
                "Nom",
                "prenom",
                "@error.com",
                "addresse",
                "5555555555",
                "mdp",
                "informatique"

        );

        // Act
        assertThatThrownBy(() -> {teacherService.createTeacher(teacherRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createTeacherInvalidEmailEndTest() {
        // Arrange

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO(
                "Nom",
                "prenom",
                "hi@.com",
                "addresse",
                "5555555555",
                "mdp",
                "informatique"
        );

        // Act
        assertThatThrownBy(() -> {teacherService.createTeacher(teacherRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createTeacherNameEmptyTest() {
        // Arrange

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO(
                "   ",
                "prenom",
                "hi@.com",
                "addresse",
                "5555555555",
                "mdp",
                "informatique"

        );

        // Act
        assertThatThrownBy(() -> {teacherService.createTeacher(teacherRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createTeacherFirstNameEmptyTest()  {
        // Arrange

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO(
                "Nom",
                "",
                "hi@.com",
                "addresse",
                "5555555555",
                "mdp",
                "informatique"
        );

        // Act
        assertThatThrownBy(() -> {teacherService.createTeacher(teacherRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createTeacherAdresseEmptyTest()  {
        // Arrange

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO(
                "Nom",
                "Prenom",
                "hi@.com",
                " ",
                "5555555555",
                "mdp",
                "informatique"
        );

        // Act
        assertThatThrownBy(() -> {teacherService.createTeacher(teacherRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }


    @Test
    public void getTeacherTest() {
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(0L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setCredentials("hi@error.com","a");
        teacher.setDiscipline(Discipline.INFORMATIQUE);

        when(teacherRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        // Act
        TeacherDTO result = teacherService.getTeacher(0);
        // Assert
        assertThat(result.id()).isEqualTo(teacher.getId());
        assertThat(result.nom()).isEqualTo(teacher.getNom());
    }

    @Test
    public void getTeacherNotFoundTest() {
        // Arrange
        when(teacherRepository.findById(anyLong())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> teacherService.getTeacher(0))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }

    @Test
    public void getTeacherEmailTest() {
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(0L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setCredentials("hi@error.com","a");
        teacher.setDiscipline(Discipline.INFORMATIQUE);

        when(teacherRepository.findUserAppByEmail(anyString())).thenReturn(Optional.of(teacher));
        // Act
        TeacherDTO result = teacherService.getTeacherByEmail("hi");
        // Assert
        assertThat(result.id()).isEqualTo(teacher.getId());
        assertThat(result.nom()).isEqualTo(teacher.getNom());
    }

    @Test
    public void getTeacherEmailNotFoundTest() {
        // Arrange
        when(teacherRepository.findUserAppByEmail(anyString())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> teacherService.getTeacherByEmail("no"))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }

    @Test
    public void getAllProfs_sucess() {
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(0L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setCredentials("hi@error.com", "a");
        teacher.setDiscipline(Discipline.INFORMATIQUE);

        when(teacherRepository.findAll()).thenReturn(List.of(teacher));
        // Act
        List<TeacherDTO> result = teacherService.getAllProfs();
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(teacher.getId());
    }

    @Test
    public void getAllProfs_IsEmpty() {
        // Arrange

        when(teacherRepository.findAll()).thenReturn(List.of());
        try {
            teacherService.getAllProfs();
        } catch (UserNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("No teachers found");
        }
    }

    @Test
    public void getStudentsByTeacherIdSuccess() {
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(0L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setCredentials("pp@pp.com", "a");

        Student student = new Student();
        student.setId(0L);
        student.setNom("Nom");
        student.setPrenom("Prenom");
        student.setAdresse("addresse");
        student.setTelephone("5555555555");
        student.setCredentials("stustu@etu.com", "a");
        student.setDiscipline(Discipline.INFORMATIQUE);

        // connect both using evaluation
        InternshipEvaluation evaluation = new InternshipEvaluation();
        evaluation.setInternshipOffer(new InternshipOffer());
        evaluation.getInternshipOffer().setJobOfferApplication(new JobOfferApplication());
        evaluation.getInternshipOffer().getJobOfferApplication().setCurriculumVitae(new CurriculumVitae());
        evaluation.getInternshipOffer().getJobOfferApplication().getCurriculumVitae().setStudent(student);

        when(internshipEvaluationRepository.findCurrentEvaluationsByTeacherId(anyLong())).thenReturn(List.of(evaluation));

        when(teacherRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        // Act
        List<StudentDTO> result = teacherService.getStudentsByTeacherId(0);
        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    public void getStudentsByTeacherIdNoStudentsSuccess() {
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(0L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setCredentials("pp@pp.com", "a");

        when(teacherRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        // Act
        List<StudentDTO> result = teacherService.getStudentsByTeacherId(0);
        // Assert
        assertThat(result).hasSize(0);
    }

    @Test
    public void getStudentsByTeacherteacherNotFoundFail() {
        // Arrange
        when(teacherRepository.findById(anyLong())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> teacherService.getStudentsByTeacherId(0))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }

    @Test
    void updateTeacher_ThrowsUserNotFoundException_WhenTeacherNotFound() {
        // Arrange
        Long teacherId = 1L;
        TeacherDTO updateDTO = new TeacherDTO(teacherId, "UpdatedFirstName", "UpdatedLastName", "teacher@example.com", null, null, null);
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> teacherService.updateTeacher(teacherId, updateDTO));
        verify(teacherRepository, never()).save(any(Teacher.class));
    }

    @Test
    void updateTeacherPassword_ThrowsUserNotFoundException_WhenTeacherNotFound() {
        // Arrange
        Long teacherId = 1L;
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("oldPassword", "newPassword");
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> teacherService.updateTeacherPassword(teacherId, updatePasswordDTO));
        verify(teacherRepository, never()).save(any(Teacher.class));
    }
    @Test
    void getEmployeurByTeacher_ThrowsUserNotFoundException_WhenTeacherNotFound() {
        // Arrange
        long teacherId = 1L;
        long sessionId = 1L;
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> teacherService.getEmployeurByTeacher(teacherId, sessionId));
        verify(internshipEvaluationRepository, never()).findByTeacherId(anyLong());
    }

    @Test
    void getEmployeurByTeacher() {
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
        cv.setDateHeureAjout((LocalDateTime.now().plusDays(1)));
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

        EvaluationEmployer evaluationEmployer = new EvaluationEmployer();
        evaluationEmployer.setEmployeur(employeur);

        EvaluationIntern evaluationIntern = new EvaluationIntern();
        evaluationIntern.setStudent(cv.getStudent());
        evaluationIntern.setEmployeur(employeur);

        InternshipEvaluation evaluation = new InternshipEvaluation();
        evaluation.setTeacher(teacher);
        evaluation.setEvaluationEmployer(evaluationEmployer);
        evaluation.setEvaluationIntern(evaluationIntern);
        evaluation.setInternshipOffer(internshipOffer);


        when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(academicSessionRepository.getReferenceById(any())).thenReturn(nextSession);
        when(internshipEvaluationRepository.findByTeacherId(teacher.getId())).thenReturn(List.of(evaluation));

        assertThat(teacherService.getEmployeurByTeacher(teacher.getId(), nextSession.getId())).hasSize(1);
    }

    @Test
    void getEmployeurByTeacher_Empty() {
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
        cv.setDateHeureAjout(LocalDateTime.now().plusDays(1));
        cv.setStatus(ApprovalStatus.VALIDATED);

        Session currentSession = new Session();
        currentSession.setId(2L);
        currentSession.setYear("2022");
        currentSession.setSeason("Hiver");

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

        EvaluationEmployer evaluationEmployer = new EvaluationEmployer();
        evaluationEmployer.setEmployeur(employeur);

        EvaluationIntern evaluationIntern = new EvaluationIntern();
        evaluationIntern.setStudent(cv.getStudent());
        evaluationIntern.setEmployeur(employeur);

        InternshipEvaluation evaluation = new InternshipEvaluation();
        evaluation.setTeacher(teacher);
        evaluation.setEvaluationEmployer(evaluationEmployer);
        evaluation.setEvaluationIntern(evaluationIntern);
        evaluation.setInternshipOffer(internshipOffer);


        when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(academicSessionRepository.getReferenceById(any())).thenReturn(nextSession);
        when(internshipEvaluationRepository.findByTeacherId(teacher.getId())).thenReturn(List.of(evaluation));

        assertThat(teacherService.getEmployeurByTeacher(teacher.getId(), nextSession.getId())).hasSize(0);
    }

    @Test
    void updateTeacher() throws InvalidPasswordException {
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setDiscipline(Discipline.INFORMATIQUE);
        teacher.setCredentials("va@mail.com", "123456");

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(teacherRepository.findById(any())).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(teacher)).thenReturn(teacher);

        TeacherDTO updateDTO = new TeacherDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", discipline);

        // Act
        TeacherDTO result = teacherService.updateTeacher(1L, updateDTO);
        assertThat(result.nom()).isEqualTo(updateDTO.nom());
    }

    @Test
    void updateTeacher_Password() throws InvalidPasswordException {
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setDiscipline(Discipline.INFORMATIQUE);
        teacher.setCredentials("va@mail.com", NoOpPasswordEncoder.getInstance().encode("123456"));

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(teacherRepository.findById(any())).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(teacher)).thenReturn(teacher);

        TeacherDTO updateDTO = new TeacherDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", discipline);

        UpdatePasswordDTO updatePasswordDTO;
        updatePasswordDTO = new UpdatePasswordDTO();
        updatePasswordDTO.setCurrentPassword(NoOpPasswordEncoder.getInstance().encode("123456"));
        updatePasswordDTO.setNewPassword(NoOpPasswordEncoder.getInstance().encode("1234567"));

        // Act
        TeacherDTO result = teacherService.updateTeacherPassword(1L,updatePasswordDTO);

        assertThat(result.nom().equalsIgnoreCase("Nom"));
    }

    @Test
    void updateTeacher_Password_Incorrect() throws InvalidPasswordException {
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setNom("Nom");
        teacher.setPrenom("Prenom");
        teacher.setAdresse("addresse");
        teacher.setTelephone("5555555555");
        teacher.setDiscipline(Discipline.INFORMATIQUE);
        teacher.setCredentials("va@mail.com", NoOpPasswordEncoder.getInstance().encode("1234567"));

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(teacherRepository.findById(any())).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(teacher)).thenReturn(teacher);

        TeacherDTO updateDTO = new TeacherDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", discipline);

        UpdatePasswordDTO updatePasswordDTO;
        updatePasswordDTO = new UpdatePasswordDTO();
        updatePasswordDTO.setCurrentPassword(NoOpPasswordEncoder.getInstance().encode("123456"));
        updatePasswordDTO.setNewPassword(NoOpPasswordEncoder.getInstance().encode("1234567"));

        try {
            teacherService.updateTeacherPassword(1L,updatePasswordDTO);
        } catch (InvalidPasswordException e) {
            assertThat(e.getMessage()).isEqualTo("Current password is incorrect.");
        }

    }

}
