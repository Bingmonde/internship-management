package com.prose.gestiondestage.service;

import com.prose.entity.*;
import com.prose.entity.users.Employeur;
import com.prose.entity.users.ProgramManager;
import com.prose.entity.users.Student;
import com.prose.entity.users.Teacher;
import com.prose.entity.users.auth.Role;
import com.prose.repository.InternshipOfferRepository;
import com.prose.repository.AcademicSessionRepository;
import com.prose.repository.JobOfferRepository;
import com.prose.repository.JobPermissionRepository;
import com.prose.repository.ProgramManagerRepository;
import com.prose.repository.*;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.*;
import com.prose.service.Exceptions.*;
//import com.prose.service.JobOfferService;
//import com.prose.service.NotificationService;
//import com.prose.service.ProgramManagerService;
//import com.prose.service.StudentService;
import com.prose.service.JobOfferService;
import com.prose.service.NotificationService;
import com.prose.service.ProgramManagerService;
import com.prose.service.StudentService;
import com.prose.service.dto.InternshipOfferDTO;
import com.prose.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProgramManagerServiceTests {

    ProgramManagerRepository programManagerRepository;
    ProgramManagerService programManagerService;

    JobOfferService jobOfferService;
    StudentService studentService;
    JobPermissionRepository jobPermissionRepository;

    JobOfferRepository jobOfferRepository;
    InternshipOfferRepository internshipOfferRepository;

    TeacherRepository teacherRepository;
    InternshipEvaluationRepository internshipEvaluationRepository;
    AcademicSessionRepository academicSessionRepository;

    NotificationService notificationService;

   EvaluationEmployerService evaluationEmployerService;

    CurriculumVitaeRepository curriculumVitaeRepository;
    StudentRepository studentRepository;

    JobOfferApplicationRepository jobOfferApplicationRepository;
    JobInterviewRepository jobInterviewRepository;


    @BeforeEach
    public void beforeEach(){
        programManagerRepository = mock(ProgramManagerRepository.class);
        jobOfferService = mock(JobOfferService.class);
        studentService = mock(StudentService.class);
        jobPermissionRepository = mock(JobPermissionRepository.class);
        jobOfferRepository = mock(JobOfferRepository.class);
        notificationService = mock(NotificationService.class);
        internshipOfferRepository = mock(InternshipOfferRepository.class);
        academicSessionRepository = mock(AcademicSessionRepository.class);
//        programManagerService = new ProgramManagerService(programManagerRepository, NoOpPasswordEncoder.getInstance(), jobOfferService, studentService, jobPermissionRepository, jobOfferRepository, internshipOfferRepository, teacherRepository, internshipEvaluationRepository, academicSessionRepository,notificationService);
        internshipOfferRepository = mock(InternshipOfferRepository.class);
        teacherRepository = mock(TeacherRepository.class);
        internshipEvaluationRepository = mock(InternshipEvaluationRepository.class);
        curriculumVitaeRepository = mock(CurriculumVitaeRepository.class);
        studentRepository = mock(StudentRepository.class);
        jobInterviewRepository = mock(JobInterviewRepository.class);
        programManagerService = new ProgramManagerService(programManagerRepository, NoOpPasswordEncoder.getInstance(), jobOfferService, studentService, jobPermissionRepository, jobOfferRepository, internshipOfferRepository, teacherRepository, internshipEvaluationRepository, academicSessionRepository,notificationService, curriculumVitaeRepository, studentRepository, jobOfferApplicationRepository,
                jobInterviewRepository);
    }

    @Test
    public void createProgramManagerTest() throws InvalidUserFormatException, AlreadyExistsException {
        // Arrange

        ProgramManager programManager = new ProgramManager();
        programManager.setId(0L);
        programManager.setNom("Nom");
        programManager.setPrenom("Prenom");
        programManager.setAdresse("addresse");
        programManager.setTelephone("5555555555");

        programManager.setCredentials("hi@error.com","a");

        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp"
        );


        when(programManagerRepository.save(any())).thenReturn(programManager);
        // Act
        ProgramManagerDTO programManagerDTO = programManagerService.createProgramManager(programManagerRegisterDTO);
        // Assert
        assertThat(programManagerDTO.nom()).isEqualTo(programManagerRegisterDTO.nom());
        assertThat(programManagerDTO.courriel()).isEqualTo(programManagerRegisterDTO.courriel());
    }

    @Test
    public void createProgramManagerAlreadyExistsTest() {
        // Arrange

        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp"
        );


        when(programManagerRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
        // Act
        assertThatThrownBy(() -> {programManagerService.createProgramManager(programManagerRegisterDTO);})
                .isInstanceOf(AlreadyExistsException.class); // Assert

    }

    @Test
    public void createProgramManagerInvalidPhoneTest() {
        // Arrange

        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO(
                "Nom",
                "prenom",
                "hi@error.com",
                "addresse",
                "(5)555555555",
                "mdp"
        );

        // Act
        assertThatThrownBy(() -> {programManagerService.createProgramManager(programManagerRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createProgramManagerInvalidEmailStartTest() {
        // Arrange

        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO(
                "Nom",
                "prenom",
                "@error.com",
                "addresse",
                "(5)5555555555",
                "mdp"
        );

        // Act
        assertThatThrownBy(() -> {programManagerService.createProgramManager(programManagerRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class); // Assert

    }

    @Test
    public void createProgramManagerInvalidEmailEndTest() {
        // Arrange

        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO(
                "Nom",
                "prenom",
                "hi@.com",
                "addresse",
                "(5)5555555555",
                "mdp"
        );

        // Act
        assertThatThrownBy(() -> {programManagerService.createProgramManager(programManagerRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class);// Assert

    }

    @Test
    public void createProgramManagerNameEmptyTest() {
        // Arrange

        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO(
                "  ",
                "prenom",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp"
        );

        // Act
        assertThatThrownBy(() -> {programManagerService.createProgramManager(programManagerRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class);// Assert

    }

    @Test
    public void createProgramManagerFirstNameEmptyTest() {
        // Arrange

        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO(
                "nom",
                "   ",
                "hi@error.com",
                "addresse",
                "5555555555",
                "mdp"
        );

        // Act
        assertThatThrownBy(() -> {programManagerService.createProgramManager(programManagerRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class);// Assert

    }

    @Test
    public void createProgramManagerAdresseEmptyTest() {
        // Arrange

        ProgramManagerRegisterDTO programManagerRegisterDTO = new ProgramManagerRegisterDTO(
                "nom",
                "prenom",
                "hi@error.com",
                "  ",
                "5555555555",
                "mdp"
        );

        // Act
        assertThatThrownBy(() -> {programManagerService.createProgramManager(programManagerRegisterDTO);})
                .isInstanceOf(InvalidUserFormatException.class);// Assert

    }


    @Test
    public void getProgramManagerTest() {
        // Arrange
        ProgramManager programManager = new ProgramManager();
        programManager.setId(0L);
        programManager.setNom("Nom");
        programManager.setPrenom("Prenom");
        programManager.setAdresse("addresse");
        programManager.setTelephone("5555555555");
        programManager.setCredentials("hi@error.com","a");

        when(programManagerRepository.findById(anyLong())).thenReturn(Optional.of(programManager));
        // Act
        ProgramManagerDTO result = programManagerService.getProgramManager(0);
        // Assert
        assertThat(result.id()).isEqualTo(programManager.getId());
        assertThat(result.nom()).isEqualTo(programManager.getNom());
    }

    @Test
    public void getProgramManagerNotFoundTest() {
        // Arrange
        when(programManagerRepository.findById(anyLong())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> programManagerService.getProgramManager(0))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }

    @Test
    public void getProgramManagerEmailTest() {
        // Arrange
        ProgramManager programManager = new ProgramManager();
        programManager.setId(0L);
        programManager.setNom("Nom");
        programManager.setPrenom("Prenom");
        programManager.setAdresse("addresse");
        programManager.setTelephone("5555555555");

        programManager.setCredentials("hi@error.com","a");

        when(programManagerRepository.findUserAppByEmail(anyString())).thenReturn(Optional.of(programManager));
        // Act
        ProgramManagerDTO result = programManagerService.getProgramManagerByEmail("hi");
        // Assert
        assertThat(result.id()).isEqualTo(programManager.getId());
        assertThat(result.nom()).isEqualTo(programManager.getNom());
    }

    @Test
    public void getProgramManagerEmailNotFoundTest() {
        // Arrange
        when(programManagerRepository.findUserAppByEmail(anyString())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> programManagerService.getProgramManagerByEmail("no"))
                .isInstanceOf(UserNotFoundException.class); // Assert
    }


    @Test
    public void createJobPermissionTest_success() throws DisciplineNotFoundException, JobNotFoundException {
        // Arrange
        JobPermissionRegisterDTO jobPermissionRegisterDTO = new JobPermissionRegisterDTO(
                new ArrayList<String>(List.of("informatique", "accounting")),
                new ArrayList<Long>(List.of(1L, 2L)),
                LocalDate.of(2025, 2, 1),
                true
        );
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setEmployeur(employeur);
        jobOffer.setId(1L);
        jobOffer.setTitre("titre woah");
        jobOffer.setDateDebut("2021-01-01");
        Student student1 = new Student();
        student1.setId(1L);
        student1.setCredentials("stud1@test.com", "123456");
        student1.setDiscipline(Discipline.INFORMATIQUE);
        Student student2 = new Student();
        student2.setId(2L);
        student2.setCredentials("prout2@prout.com", "a22");
        student2.setDiscipline(Discipline.INFORMATIQUE);


        JobPermission jobPermission = new JobPermission();
        jobPermission.setId(1L);
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setDisciplines("informatique;accounting");
        jobPermission.setStudents(new ArrayList<>(List.of(student1, student2)));
        jobPermission.setExpirationDate(LocalDate.of(2025, 2, 1));

        // Act
        when(jobOfferService.getJobOfferById(anyLong())).thenReturn(jobOffer);
        when(studentService.getStudentEntity(1L)).thenReturn(student1);
        when(studentService.getStudentEntity(2L)).thenReturn(student2);
        when(jobPermissionRepository.save(any(JobPermission.class))).thenReturn(jobPermission);

        // Assert
        assertThat(programManagerService.createJobPermission(1l, jobPermissionRegisterDTO,new UserDTO(6L,null,null,Role.PROGRAM_MANAGER)).id()).isEqualTo(jobPermission.getId());
        assertThat(programManagerService.createJobPermission(1l, jobPermissionRegisterDTO,new UserDTO(6L,null,null,Role.PROGRAM_MANAGER)).isApproved()).isEqualTo(jobPermission.getJobOffer().isApproved());
    }

    @Test
    public void createJobPermissionTest_jobNotFound() throws JobNotFoundException {
        // Arrange
        JobPermissionRegisterDTO jobPermissionRegisterDTO = new JobPermissionRegisterDTO(
                new ArrayList<String>(List.of("informatique", "accounting")),
                new ArrayList<Long>(List.of(1L, 2L)),
                LocalDate.of(2025, 2, 1),
                true
        );

        // Act
        when(jobOfferService.getJobOfferById(anyLong())).thenThrow(JobNotFoundException.class);

        // Assert
        assertThatThrownBy(() -> programManagerService.createJobPermission(1l, jobPermissionRegisterDTO,new UserDTO(6L,null,null,Role.PROGRAM_MANAGER)))
                .isInstanceOf(JobNotFoundException.class);
    }


    @Test
    public void createJobPermissionTest_disciplineNotFound() throws JobNotFoundException {
        // Arrange
        JobPermissionRegisterDTO jobPermissionRegisterDTO = new JobPermissionRegisterDTO(
                new ArrayList<String>(List.of("discipline_1", "discipline_2")),
                new ArrayList<Long>(List.of(1L, 2L)),
                LocalDate.of(2025, 2, 1),
                true
        );
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setDateDebut("2021-01-01");

        JobPermission jobPermission = new JobPermission();
        jobPermission.setId(1L);
        jobPermission.setJobOffer(jobOffer);
        jobPermission.setDisciplines("informatique;accounting");
        jobPermission.setExpirationDate(LocalDate.of(2025, 2, 1));

        // Act
        when(jobOfferService.getJobOfferById(anyLong())).thenReturn(jobOffer);

        // Assert
        assertThatThrownBy(() -> programManagerService.createJobPermission(1l, jobPermissionRegisterDTO,new UserDTO(6L,null,null,Role.PROGRAM_MANAGER)))
                .isInstanceOf(DisciplineNotFoundException.class);
    }

    @Test
    public void createJobPermissionTest_containsStudentsNotFound() throws JobNotFoundException {
        // Arrange
        JobPermissionRegisterDTO jobPermissionRegisterDTO = new JobPermissionRegisterDTO(
                new ArrayList<String>(List.of("informatique", "accounting")),
                new ArrayList<Long>(List.of(1L, 2L)),
                LocalDate.of(2025, 2, 1),
                true
        );
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setDateDebut("2021-01-01");
        // Act
        when(jobOfferService.getJobOfferById(anyLong())).thenReturn(jobOffer);
        when(studentService.getStudentEntity(anyLong())).thenThrow(UserNotFoundException.class);

        // Assert
        assertThatThrownBy(() -> programManagerService.createJobPermission(1l, jobPermissionRegisterDTO,new UserDTO(6L,null,null,Role.PROGRAM_MANAGER)))
                .isInstanceOf(UserNotFoundException.class);
    }


    @Test
    public void getJobOffersWaitingForApproval_success() throws JobNotFoundException {
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setApproved(false);
        jobOffer.setEmployeur(employeur);
        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setApproved(false);
        jobOffer2.setEmployeur(employeur);
        List<JobOffer> jobOffers = new ArrayList<>(List.of(jobOffer, jobOffer2));
        Page<JobOffer> jobOfferPage = new PageImpl<>(jobOffers, PageRequest.of(0, 5), 2);
        // Act
        when(jobOfferRepository.findByIsApprovedFalse(any())).thenReturn(jobOffers);
        // Assert
        assertThat(programManagerService.getJobOffersWaitingForApproval(any()).size()).isEqualTo(2);
    }

    @Test
    public void getJobOffersWaitingForApproval_noJobWaitingForApproval() {
        // Act
        when(jobOfferRepository.findByIsApprovedFalse(any())).thenReturn(new ArrayList<>());
        // Assert
        assertThatThrownBy(() -> programManagerService.getJobOffersWaitingForApproval(any()))
                .isInstanceOf(JobNotFoundException.class);
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

        when(internshipOfferRepository.findInternshipOfferWithOrAwaitingContract(eq(session.getId()),any())).thenReturn(new PageImpl<>(List.of(internshipOffer), PageRequest.of(5,5),5));

        // Act
        List<InternshipOfferDTO> result = programManagerService.getContracts(session.getId(),5,5).getContent();
        // Assert
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getFirst().contractSignatureDTO()).isNull();
    }

    @Test
    public void getAwaitingContractEmptyTest() {
        // Arrange

        when(internshipOfferRepository.findInternshipOfferWithOrAwaitingContract(eq(5L),any())).thenReturn(Page.empty());

        // Act
        List<InternshipOfferDTO> result = programManagerService.getContracts(5L,5,5).getContent();
        // Assert
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void beginContractTest() throws InternshipNotFoundException, JobApplicationNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);

        Employeur employeur = new Employeur();
        employeur.setId(2L);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setTitre("hi");
        jobOffer.setEmployeur(employeur);

        CurriculumVitae curriculumVitae = new CurriculumVitae();
        curriculumVitae.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setCurriculumVitae(curriculumVitae);
        jobOfferApplication.setJobOffer(jobOffer);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(3L);
        internshipOffer.setContract(null);

        internshipOffer.setJobOfferApplication(jobOfferApplication);
        jobOfferApplication.setInternshipOffer(internshipOffer);

        ProgramManager programManager = new ProgramManager();
        programManager.setId(7L);


        when(internshipOfferRepository.findById(internshipOffer.getId())).thenReturn(Optional.of(internshipOffer));
        when(programManagerRepository.getReferenceById(programManager.getId())).thenReturn(programManager);
        ArgumentCaptor<InternshipOffer> argumentCaptor = ArgumentCaptor.forClass(InternshipOffer.class);
        // Act
        programManagerService.beginContract(internshipOffer.getId(),new UserDTO(6L,null,null,Role.PROGRAM_MANAGER));
        // Assert
        verify(internshipOfferRepository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isNotNull();

    }

    @Test
    void beginContractNotFoundTest() throws InternshipNotFoundException {
        // Arrange
        long id = 3L;

        ProgramManager programManager = new ProgramManager();
        programManager.setId(4L);

        when(internshipOfferRepository.findById(id)).thenReturn(Optional.empty());
        when(programManagerRepository.getReferenceById(programManager.getId())).thenReturn(programManager);
        // Act
        assertThatThrownBy(() -> programManagerService.beginContract(3L,new UserDTO(6L,null,null,Role.PROGRAM_MANAGER)))
                .isInstanceOf(InternshipNotFoundException.class); // Assert

    }

    @Test
    void getInternsWaitingForAssignmentToProf_success() throws Exception {

        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setCredentials("stu@email.com","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("emp@email.com","123456");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOffer.setEmployeur(employeur);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        Contract contract = new Contract();
        contract.setId(1L);
        contract.setManagerSign(LocalDateTime.now());
        contract.setStudentSign(LocalDateTime.now());
        contract.setEmployerSign(LocalDateTime.now());

        // Act
        when(internshipOfferRepository.findInternshipOfferWithContractAllSigned()).thenReturn(List.of(internshipOffer));

        // Assert
        assertThat(programManagerService.getInternsWaitingForAssignmentToProf().size()).isEqualTo(1);
        assertThat(programManagerService.getInternsWaitingForAssignmentToProf().get(0).id()).isEqualTo(internshipOffer.getId());

    }


    @Test
    void getInternsWaitingForAssignmentToProf_internShipNotFoundException() throws Exception {
        // Act
        when(internshipOfferRepository.findInternshipOfferWithContractAllSigned()).thenReturn(new ArrayList<>());

        // Assert
        assertThatThrownBy(() -> programManagerService.getInternsWaitingForAssignmentToProf())
                .isInstanceOf(InternshipNotFoundException.class);

    }


    @Test
    void assignToProf_success() throws Exception {
        // arrange
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setCredentials("prof@email.com", "123456");
        teacher.setDiscipline(Discipline.INFORMATIQUE);

        Student student = new Student();
        student.setId(1L);
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("emp@email.com", "123456");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOffer.setEmployeur(employeur);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        Contract contract = new Contract();
        contract.setId(1L);
        contract.setManagerSign(LocalDateTime.now());
        contract.setStudentSign(LocalDateTime.now());
        contract.setEmployerSign(LocalDateTime.now());

        internshipOffer.setContract(contract);

        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setId(1L);
        internshipEvaluation.setInternshipOffer(internshipOffer);
        internshipEvaluation.setTeacher(teacher);

        // act
        when(teacherRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        when(internshipOfferRepository.findById(anyLong())).thenReturn(Optional.of(internshipOffer));
        when(internshipEvaluationRepository.save(any())).thenReturn(internshipEvaluation);

        // assert
        assertThat(programManagerService.assignToProf(1L, 1L,new UserDTO(3L,"email","password",Role.PROGRAM_MANAGER)).teacher().id()).isEqualTo(internshipEvaluation.getTeacher().getId());

    }


    @Test
    void assignToProf_InternShipNotFoundException() throws Exception {
        // act
        when(internshipOfferRepository.findById(anyLong())).thenReturn(Optional.empty());
        // assert
        assertThatThrownBy(() -> programManagerService.assignToProf(1L, 1L, new UserDTO(3L,"email","password",Role.PROGRAM_MANAGER)))
                .isInstanceOf(InternshipNotFoundException.class);

    }

    @Test
    void assignToProf_UserNotFoundException() throws Exception {
        // arrange
        Student student = new Student();
        student.setId(1L);
        student.setCredentials("stu@email.com","123456");

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("emp@email.com","123456");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOffer.setEmployeur(employeur);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        Contract contract = new Contract();
        contract.setId(1L);
        contract.setManagerSign(LocalDateTime.now());
        contract.setStudentSign(LocalDateTime.now());
        contract.setEmployerSign(LocalDateTime.now());

        internshipOffer.setContract(contract);


        // act
        when(internshipOfferRepository.findById(anyLong())).thenReturn(Optional.of(internshipOffer));
        when(teacherRepository.findById(anyLong())).thenReturn(Optional.empty());
        // assert

        assertThatThrownBy(() -> programManagerService.assignToProf(1L, 1L, new UserDTO(3L,"email","password",Role.PROGRAM_MANAGER)))
                .isInstanceOf(UserNotFoundException.class);
    }


    @Test
    void getProfCurrentInterns_success() throws Exception {
        // Arrange
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setCredentials("prof@email.com","123456");
        teacher.setDiscipline(Discipline.INFORMATIQUE);

        Student student = new Student();
        student.setId(1L);
        student.setCredentials("stu@email.com","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("emp@email.com","123456");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOffer.setEmployeur(employeur);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setId(1L);
        internshipEvaluation.setInternshipOffer(internshipOffer);
        internshipEvaluation.setTeacher(teacher);

        when(internshipEvaluationRepository.findCurrentEvaluationsByTeacherId(teacher.getId())).thenReturn(List.of(internshipEvaluation));

        // Act
        List<InternshipEvaluationDTO> result = programManagerService.getProfCurrentInterns(1L);


        // Assert
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).id()).isEqualTo(internshipOffer.getId());
        assertThat(result.get(0).teacher().id()).isEqualTo(teacher.getId());
    }

    @Test
    void getAllNonApprovedJob(){
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("s@email.com","dasda");
        employeur.setNomCompagnie("TestCompany");

        Employeur employeur2 = new Employeur();
        employeur2.setId(2L);
        employeur2.setCredentials("s@email.com","dasda");
        employeur2.setNomCompagnie("TestCompany");


        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(false);
        jobOffer.setActivated(true);
        jobOffer.setEmployeur(employeur);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Junior Developer");
        jobOffer2.setApproved(false);
        jobOffer2.setActivated(true);
        jobOffer2.setEmployeur(employeur2);

        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setYear("2024");
        session.setSeason("Hiver");

        List<JobOffer> jobOffers = new ArrayList<>(List.of(jobOffer, jobOffer2));
        Page<JobOffer> jobOfferPage = new PageImpl<>(jobOffers, PageRequest.of(0, 5), 2);
        // Act
        when(jobOfferRepository.findByIsApprovedFalsePage(any(),any(),any())).thenReturn(jobOfferPage);
        // Assert
        assertThat(programManagerService.getAllNonApprovedJobOffers(pageable, query, session.getId()).getContent().size()).isEqualTo(2);
    }

    @Test
    void getAllNonApprovedJob_IsEmpty(){
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("s@email.com","dasda");
        employeur.setNomCompagnie("TestCompany");

        Employeur employeur2 = new Employeur();
        employeur2.setId(2L);
        employeur2.setCredentials("s@email.com","dasda");
        employeur2.setNomCompagnie("TestCompany");


        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);
        jobOffer.setEmployeur(employeur);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Junior Developer");
        jobOffer2.setApproved(true);
        jobOffer2.setActivated(true);
        jobOffer2.setEmployeur(employeur2);

        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setYear("2024");
        session.setSeason("Hiver");

        List<JobOffer> jobOffers = new ArrayList<>(List.of(jobOffer, jobOffer2));
        Page<JobOffer> jobOfferPage = new PageImpl<>(jobOffers, PageRequest.of(0, 5), 2);
        // Act
        when(jobOfferRepository.findByIsApprovedFalsePage(any(),any(),any())).thenReturn(Page.empty());
        // Assert
        assertThat(programManagerService.getAllNonApprovedJobOffers(pageable, query, session.getId()).getContent().isEmpty());
    }

    @Test
    void getAllApprovedJob(){
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("s@email.com","dasda");
        employeur.setNomCompagnie("TestCompany");

        Employeur employeur2 = new Employeur();
        employeur2.setId(2L);
        employeur2.setCredentials("s@email.com","dasda");
        employeur2.setNomCompagnie("TestCompany");


        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);
        jobOffer.setEmployeur(employeur);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Junior Developer");
        jobOffer2.setApproved(true);
        jobOffer2.setActivated(true);
        jobOffer2.setEmployeur(employeur2);

        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setYear("2024");
        session.setSeason("Hiver");

        List<JobOffer> jobOffers = new ArrayList<>(List.of(jobOffer, jobOffer2));
        Page<JobOffer> jobOfferPage = new PageImpl<>(jobOffers, PageRequest.of(0, 5), 2);
        // Act
        when(jobOfferRepository.findByIsApprovedTrue(any(),any(),anyLong())).thenReturn(jobOfferPage);
        // Assert
        assertThat(programManagerService.getAllApprovedJobOffers(pageable, query, session.getId()).getContent().size()).isEqualTo(2);
    }

    @Test
    void getAllApprovedJob_IsEmpty(){
        // Arrange
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setCredentials("s@email.com","dasda");
        employeur.setNomCompagnie("TestCompany");

        Employeur employeur2 = new Employeur();
        employeur2.setId(2L);
        employeur2.setCredentials("s@email.com","dasda");
        employeur2.setNomCompagnie("TestCompany");


        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setApproved(false);
        jobOffer.setActivated(true);
        jobOffer.setEmployeur(employeur);

        JobOffer jobOffer2 = new JobOffer();
        jobOffer2.setId(2L);
        jobOffer2.setTitre("Junior Developer");
        jobOffer2.setApproved(false);
        jobOffer2.setActivated(true);
        jobOffer2.setEmployeur(employeur2);

        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setYear("2024");
        session.setSeason("Hiver");

        List<JobOffer> jobOffers = new ArrayList<>(List.of(jobOffer, jobOffer2));
        Page<JobOffer> jobOfferPage = new PageImpl<>(jobOffers, PageRequest.of(0, 5), 2);
        // Act
        when(jobOfferRepository.findByIsApprovedTrue(any(),any(),anyLong())).thenReturn(Page.empty());
        // Assert
        assertThat(programManagerService.getAllApprovedJobOffers(pageable, query, session.getId()).getContent().isEmpty());
    }

    @Test
    void getAllStudents(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("s@mail.com","dasda");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setYear("2024");
        session.setSeason("Hiver");
        // Act
        when(studentRepository.findAll(pageable, query, session.getId())).thenReturn(new PageImpl<>(List.of(student), pageable, 1));
        // Assert
        assertThat(programManagerService.getAllStudents(pageable, query, session.getId()).getContent().size()).isEqualTo(1);
    }

    @Test
    void getAllStudents_IsEmpty(){
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setYear("2024");
        session.setSeason("Hiver");
        // Act
        when(studentRepository.findAll(pageable, query, session.getId())).thenReturn(new PageImpl<>(new ArrayList<>(), pageable, 1));
        // Assert
        assertThat(programManagerService.getAllStudents(pageable, query, session.getId()).getContent().isEmpty());
    }

    @Test
    void getAllStudentsWithNoCV(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("s@mail.com","dasda");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Student student2 = new Student();
        student2.setId(2L);
        student2.setPrenom("Bab");
        student2.setNom("Sdith");
        student2.setCredentials("d@mail.com","dasda");
        student2.setDiscipline(Discipline.INFORMATIQUE);

        Session session = new Session();
        session.setId(1L);
        session.setYear("2024");
        session.setSeason("Hiver");

        Pageable pageable = PageRequest.of(0, 5);
        List<Student> students = new ArrayList<>(List.of(student, student2));
        Page<Student> studentPage = new PageImpl<>(students, pageable, 2);
        String query = "";
        // Act
        when(curriculumVitaeRepository.getAllStudentsWithNoCV(pageable, query)).thenReturn(studentPage);
        // Assert
        assertThat(programManagerService.getAllStudentsWithNoCV(pageable, query, session.getId()).getContent().size()).isEqualTo(2);
    }

    @Test
    void getAllStudentsWithNoCV_IsEmpty(){
        // Arrange
        Session session = new Session();
        session.setId(1L);
        session.setYear("2024");
        session.setSeason("Hiver");

        Pageable pageable = PageRequest.of(0, 5);
        Page<Student> studentPage = new PageImpl<>(new ArrayList<>(), pageable, 2);
        String query = "";
        // Act
        when(curriculumVitaeRepository.getAllStudentsWithNoCV(pageable, query)).thenReturn(studentPage);
        // Assert
        assertThat(programManagerService.getAllStudentsWithNoCV(pageable, query, session.getId()).getContent().isEmpty());
    }

    @Test
    void getAllStudentsWithCVNotAccepted(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("q@mail.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Student student2 = new Student();
        student2.setId(2L);
        student2.setPrenom("Bab");
        student2.setNom("Sdith");
        student2.setCredentials("w@email.com", "123456");
        student2.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStatus(ApprovalStatus.WAITING);
        cv.setStudent(student);

        CurriculumVitae cv2 = new CurriculumVitae();
        cv2.setId(2L);
        cv2.setStatus(ApprovalStatus.REJECTED);
        cv2.setStudent(student);

        CurriculumVitae cv3 = new CurriculumVitae();
        cv3.setId(3L);
        cv3.setStatus(ApprovalStatus.REJECTED);
        cv3.setStudent(student2);

        CurriculumVitae cv4 = new CurriculumVitae();
        cv4.setId(4L);
        cv4.setStatus(ApprovalStatus.WAITING);
        cv4.setStudent(student2);

        Pageable pageable = PageRequest.of(0, 5);
        List<CurriculumVitae> cvsWaiting = new ArrayList<>(List.of(cv, cv4, cv2, cv3));
        Page<CurriculumVitae> cvPage = new PageImpl<>(cvsWaiting, pageable, 4);


        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");


        // Act
        when(curriculumVitaeRepository.findByNotStatusPage(ApprovalStatus.VALIDATED, pageable, query)).thenReturn(cvPage);

        // Assert
        assertThat(programManagerService.getAllStudentsWithCVNotAccepted(pageable, query, session.getId()).getContent().size()).isEqualTo(4);
    }

    @Test
    void getAllStudentsWithCVNotAccepted_IsEmpty(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("q@mail.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Student student2 = new Student();
        student2.setId(2L);
        student2.setPrenom("Bab");
        student2.setNom("Sdith");
        student2.setCredentials("w@email.com", "123456");
        student2.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStatus(ApprovalStatus.WAITING);
        cv.setStudent(student);

        CurriculumVitae cv2 = new CurriculumVitae();
        cv2.setId(2L);
        cv2.setStatus(ApprovalStatus.REJECTED);
        cv2.setStudent(student);

        CurriculumVitae cv3 = new CurriculumVitae();
        cv3.setId(3L);
        cv3.setStatus(ApprovalStatus.REJECTED);
        cv3.setStudent(student2);

        CurriculumVitae cv4 = new CurriculumVitae();
        cv4.setId(4L);
        cv4.setStatus(ApprovalStatus.WAITING);
        cv4.setStudent(student2);

        Pageable pageable = PageRequest.of(0, 5);
        List<CurriculumVitae> cvsWaiting = new ArrayList<>(List.of(cv, cv4, cv2, cv3));
        Page<CurriculumVitae> cvPage = new PageImpl<>(cvsWaiting, pageable, 4);


        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");


        // Act
        when(curriculumVitaeRepository.findByNotStatusPage(ApprovalStatus.VALIDATED, pageable, query)).thenReturn(Page.empty());

        // Assert
        assertThat(programManagerService.getAllStudentsWithCVNotAccepted(pageable, query, session.getId()).getContent().isEmpty());
    }

    @Test
    void getAllStudentsWithNoInterView(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("a@email.com","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Student student2 = new Student();
        student2.setId(2L);
        student2.setPrenom("Bab");
        student2.setNom("Sdith");
        student2.setCredentials("d@mail.com","123456");
        student2.setDiscipline(Discipline.INFORMATIQUE);

        List<Student> students = new ArrayList<>(List.of(student, student2));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        Page<Student> studentPage = new PageImpl<>(students, pageable, 2);

        // Act
        when(jobInterviewRepository.findAllStudentWithNoInterview(pageable, query, session.getId())).thenReturn(studentPage);

        // Assert
        assertThat(programManagerService.getAllStudentsWithNoInterview(pageable, query, session.getId()).getContent().size()).isEqualTo(2);
    }

    @Test
    void getAllStudentsWithNoInterView_IsEmpty(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("a@email.com","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Student student2 = new Student();
        student2.setId(2L);
        student2.setPrenom("Bab");
        student2.setNom("Sdith");
        student2.setCredentials("d@mail.com","123456");
        student2.setDiscipline(Discipline.INFORMATIQUE);

        List<Student> students = new ArrayList<>(List.of(student, student2));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        Page<Student> studentPage = new PageImpl<>(students, pageable, 2);

        // Act
        when(jobInterviewRepository.findAllStudentWithNoInterview(pageable, query, session.getId())).thenReturn(Page.empty());

        // Assert
        assertThat(programManagerService.getAllStudentsWithNoInterview(pageable, query, session.getId()).getContent().isEmpty());
    }

    @Test
    void getAllStudentWaitingForInterview(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);



        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        Page<JobInterview> studentPage = new PageImpl<>(jbInterview, pageable, 1);

        // Act
        when(jobInterviewRepository.findAllStudentWaitingForInterview(pageable, query, session.getId())).thenReturn(studentPage);

        // Assert
        assertThat(programManagerService.getAllStudentsWaitingForInterview(pageable, query, session.getId()).getContent().size()).isEqualTo(1);
    }

    @Test
    void getAllStudentWaitingForInterview_IsEmpty(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);



        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        Page<JobInterview> studentPage = new PageImpl<>(jbInterview, pageable, 1);

        // Act
        when(jobInterviewRepository.findAllStudentWaitingForInterview(pageable, query, session.getId())).thenReturn(Page.empty());

        // Assert
        assertThat(programManagerService.getAllStudentsWaitingForInterview(pageable, query, session.getId()).getContent().isEmpty());
    }

    @Test
    void getAllStudentsWaitingForInterviewAnswer() throws JobApplicationNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);

        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        Page<JobInterview> studentPage = new PageImpl<>(jbInterview, pageable, 1);

        // Act
        when(jobInterviewRepository.findAllStudentWaitingForInterviewAnswer(pageable, query, session.getId())).thenReturn(studentPage);

        // Assert
        assertThat(programManagerService.getAllStudentsWaitingForInterviewAnswer(pageable, query, session.getId()).getContent().size()).isEqualTo(1);
    }

    @Test
    void getAllStudentsWaitingForInterviewAnswer_empty() throws JobApplicationNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);

        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        Page<JobInterview> studentPage = new PageImpl<>(jbInterview, pageable, 1);

        // Act
        when(jobInterviewRepository.findAllStudentWaitingForInterviewAnswer(pageable, query, session.getId())).thenReturn(Page.empty());

        // Assert
        assertThat(programManagerService.getAllStudentsWaitingForInterviewAnswer(pageable, query, session.getId()).getContent().size()).isEqualTo(0);
    }

    @Test
    void getAllStudentsWithInternship() throws JobApplicationNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);

        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setInternshipEvaluation(new InternshipEvaluation());
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(30L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        List<InternshipOffer> internshipOffers = new ArrayList<>(List.of(internshipOffer));

        Page<InternshipOffer> studentPage = new PageImpl<>(internshipOffers, pageable, 1);

        // Act
        when(internshipOfferRepository.findInternshipOfferByStatus(ApprovalStatus.ACCEPTED, pageable, query, session.getId())).thenReturn(studentPage);

        // Assert
        assertThat(programManagerService.getAllStudentsWithInternship(pageable, query, session.getId()).getContent().size()).isEqualTo(1);
    }

    @Test
    void getAllStudentsWithInternship_Empty() throws JobApplicationNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);

        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setInternshipEvaluation(new InternshipEvaluation());
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(30L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        List<InternshipOffer> internshipOffers = new ArrayList<>(List.of(internshipOffer));

        Page<InternshipOffer> studentPage = new PageImpl<>(internshipOffers, pageable, 1);

        // Act
        when(internshipOfferRepository.findInternshipOfferByStatus(ApprovalStatus.ACCEPTED, pageable, query, session.getId())).thenReturn(Page.empty());

        // Assert
        assertThat(programManagerService.getAllStudentsWithInternship(pageable, query, session.getId()).getContent().size()).isEqualTo(0);
    }


    @Test
    void getAllStudentsNotYetAssessedByTeacher() throws JobApplicationNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);

        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setInternshipEvaluation(new InternshipEvaluation());
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(30L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);


        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

        EvaluationIntern evaluationIntern = new EvaluationIntern();
        evaluationIntern.setId(1L);
        evaluationIntern.setEmployeur(employeur);

        EvaluationEmployer evaluationEmployer = new EvaluationEmployer();
        evaluationEmployer.setId(1L);
        evaluationEmployer.setEmployeur(employeur);


        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setInternshipOffer(internshipOffer);
        internshipEvaluation.setEvaluationIntern(null);
        internshipEvaluation.setEvaluationEmployer(evaluationEmployer);

        List<InternshipEvaluation> internshipEvaluations = new ArrayList<>(List.of(internshipEvaluation));

        Page<InternshipEvaluation> studentPage = new PageImpl<>(internshipEvaluations, pageable, 1);
        // Act
        when(internshipEvaluationRepository.findAllStudentsNotYetEvaluatedByTeacher(query,pageable, session.getId())).thenReturn(studentPage);

        // Assert
        assertThat(programManagerService.getAllStudentsNotYetAssessedByTeacher(pageable, query, session.getId()).getContent().size()).isEqualTo(1);
    }

    @Test
    void getAllStudentsNotYetAssessedByTeacher_Empty() throws JobApplicationNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);

        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setInternshipEvaluation(new InternshipEvaluation());
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(30L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);


        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

        EvaluationIntern evaluationIntern = new EvaluationIntern();
        evaluationIntern.setId(1L);
        evaluationIntern.setEmployeur(employeur);

        EvaluationEmployer evaluationEmployer = new EvaluationEmployer();
        evaluationEmployer.setId(1L);
        evaluationEmployer.setEmployeur(employeur);


        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setInternshipOffer(internshipOffer);
        internshipEvaluation.setEvaluationIntern(null);
        internshipEvaluation.setEvaluationEmployer(evaluationEmployer);

        List<InternshipEvaluation> internshipEvaluations = new ArrayList<>(List.of(internshipEvaluation));

        Page<InternshipEvaluation> studentPage = new PageImpl<>(internshipEvaluations, pageable, 1);
        // Act
        when(internshipEvaluationRepository.findAllStudentsNotYetEvaluatedByTeacher(query,pageable, session.getId())).thenReturn(Page.empty());

        // Assert
        assertThat(programManagerService.getAllStudentsNotYetAssessedByTeacher(pageable, query, session.getId()).getContent().size()).isEqualTo(0);
    }


    @Test
    void getAllStudentsNotYetAssessedBySupervisor() throws JobApplicationNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);

        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setInternshipEvaluation(new InternshipEvaluation());
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(30L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);


        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

        EvaluationIntern evaluationIntern = new EvaluationIntern();
        evaluationIntern.setId(1L);
        evaluationIntern.setEmployeur(employeur);

        EvaluationEmployer evaluationEmployer = new EvaluationEmployer();
        evaluationEmployer.setId(1L);
        evaluationEmployer.setEmployeur(employeur);


        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setInternshipOffer(internshipOffer);
        internshipEvaluation.setEvaluationIntern(evaluationIntern);
        internshipEvaluation.setEvaluationEmployer(null);

        List<InternshipEvaluation> internshipEvaluations = new ArrayList<>(List.of(internshipEvaluation));

        Page<InternshipEvaluation> studentPage = new PageImpl<>(internshipEvaluations, pageable, 1);
        // Act
        when(internshipEvaluationRepository.findAllStudentsEmployeurNotYetEvaluatedByTeacher(query,pageable, session.getId())).thenReturn(studentPage);

        // Assert
        assertThat(programManagerService.getAllStudentsWithEmployerNotYetAssessedBySupervisor(pageable, query, session.getId()).getContent().size()).isEqualTo(1);
    }

    @Test
    void getAllStudentsNotYetAssessedBySupervisor_Empty() throws JobApplicationNotFoundException {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("wd@email.com,","123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(cv);

        JobInterview jobInterview = new JobInterview();
        jobInterview.setId(1L);
        jobInterview.setInterviewDate(LocalDateTime.now());
        jobInterview.setConfirmedByStudent(true);
        jobInterview.setJobOfferApplication(jobOfferApplication);

        List<JobInterview> jbInterview = new ArrayList<>(List.of(jobInterview));
        Pageable pageable = PageRequest.of(0, 5);
        String query = "";
        Session session = new Session();
        session.setId(1L);
        session.setSeason("Hiver");
        session.setYear("2024");

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setInternshipEvaluation(new InternshipEvaluation());
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(30L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);


        Employeur employeur = new Employeur();
        employeur.setId(5L);
        employeur.setCredentials("dadqa","dasda");

        EvaluationIntern evaluationIntern = new EvaluationIntern();
        evaluationIntern.setId(1L);
        evaluationIntern.setEmployeur(employeur);

        EvaluationEmployer evaluationEmployer = new EvaluationEmployer();
        evaluationEmployer.setId(1L);
        evaluationEmployer.setEmployeur(employeur);


        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setInternshipOffer(internshipOffer);
        internshipEvaluation.setEvaluationIntern(evaluationIntern);
        internshipEvaluation.setEvaluationEmployer(null);

        List<InternshipEvaluation> internshipEvaluations = new ArrayList<>(List.of(internshipEvaluation));

        Page<InternshipEvaluation> studentPage = new PageImpl<>(internshipEvaluations, pageable, 1);
        // Act
        when(internshipEvaluationRepository.findAllStudentsEmployeurNotYetEvaluatedByTeacher(query,pageable, session.getId())).thenReturn(Page.empty());

        // Assert
        assertThat(programManagerService.getAllStudentsWithEmployerNotYetAssessedBySupervisor(pageable, query, session.getId()).getContent().size()).isEqualTo(0);
    }


    @Test
    void update_ProgramManager() throws InvalidPasswordException {
        // Arrange
        ProgramManager programManager = new ProgramManager();
        programManager.setId(1L);
        programManager.setNom("Nom");
        programManager.setPrenom("Prenom");
        programManager.setAdresse("addresse");
        programManager.setTelephone("5555555555");
        programManager.setCredentials("va@mail.com", "123456");

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(programManagerRepository.findById(any())).thenReturn(Optional.of(programManager));
        when(programManagerRepository.save(programManager)).thenReturn(programManager);

        ProgramManagerDTO updateDTO = new ProgramManagerDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null");

        // Act
        ProgramManagerDTO result = programManagerService.updateProgramManager(1L, updateDTO);
        assertThat(result.nom()).isEqualTo(updateDTO.nom());
    }

    @Test
    void updateTeacher_Password() throws InvalidPasswordException {
        // Arrange
        ProgramManager programManager = new ProgramManager();
        programManager.setId(1L);
        programManager.setNom("Nom");
        programManager.setPrenom("Prenom");
        programManager.setAdresse("addresse");
        programManager.setTelephone("5555555555");
        programManager.setCredentials("va@mail.com", "123456");

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(programManagerRepository.findById(any())).thenReturn(Optional.of(programManager));
        when(programManagerRepository.save(programManager)).thenReturn(programManager);

        TeacherDTO updateDTO = new TeacherDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", discipline);

        UpdatePasswordDTO updatePasswordDTO;
        updatePasswordDTO = new UpdatePasswordDTO();
        updatePasswordDTO.setCurrentPassword(NoOpPasswordEncoder.getInstance().encode("123456"));
        updatePasswordDTO.setNewPassword(NoOpPasswordEncoder.getInstance().encode("1234567"));

        // Act
        ProgramManagerDTO result = programManagerService.updateProgramManagerPassword(1L,updatePasswordDTO);

        assertThat(result.nom().equalsIgnoreCase("Nom"));
    }

    @Test
    void updateTeacher_Password_Incorrect() throws InvalidPasswordException {
        // Arrange
        ProgramManager programManager = new ProgramManager();
        programManager.setId(1L);
        programManager.setNom("Nom");
        programManager.setPrenom("Prenom");
        programManager.setAdresse("addresse");
        programManager.setTelephone("5555555555");
        programManager.setCredentials("va@mail.com", "1234567");

        DisciplineTranslationDTO discipline = new DisciplineTranslationDTO("informatique", "Computer Science", "Informatique");

        when(programManagerRepository.findById(any())).thenReturn(Optional.of(programManager));
        when(programManagerRepository.save(programManager)).thenReturn(programManager);

        TeacherDTO updateDTO = new TeacherDTO(1L, "UpdatedFirstName", "UpdatedLastName",
                "as@mail.com", "null", "null", discipline);

        UpdatePasswordDTO updatePasswordDTO;
        updatePasswordDTO = new UpdatePasswordDTO();
        updatePasswordDTO.setCurrentPassword(NoOpPasswordEncoder.getInstance().encode("123456"));
        updatePasswordDTO.setNewPassword(NoOpPasswordEncoder.getInstance().encode("12345"));

        try {
            programManagerService.updateProgramManagerPassword(1L,updatePasswordDTO);
        } catch (InvalidPasswordException e) {
            assertThat(e.getMessage()).isEqualTo("Current password is incorrect.");
        }
    }
}
