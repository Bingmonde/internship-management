package com.prose.service;

import com.prose.entity.*;
import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Student;
import com.prose.entity.users.Teacher;
import com.prose.entity.users.auth.Role;
import com.prose.repository.*;
import com.prose.repository.AcademicSessionRepository;
import com.prose.repository.InternshipOfferRepository;
import com.prose.repository.JobOfferRepository;
import com.prose.repository.JobPermissionRepository;
import com.prose.repository.ProgramManagerRepository;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.*;
import com.prose.service.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;

import static com.prose.service.dto.ProgramManagerDTO.toDTO;

@Service
public class ProgramManagerService {

    private final ProgramManagerRepository programManagerRepository;
    private final PasswordEncoder passwordEncoder;

    private final JobOfferService jobOfferService;

    private final StudentService studentService;

    private final JobPermissionRepository jobPermissionRepository;
    private final JobOfferRepository jobOfferRepository;
    private final InternshipOfferRepository internshipOfferRepository;

    private final TeacherRepository teacherRepository;

    private final InternshipEvaluationRepository internshipEvaluationRepository;

    private final AcademicSessionRepository academicSessionRepository;

    private final NotificationService notificationService;

    private final CurriculumVitaeRepository curriculumVitaeRepository;

    private final StudentRepository studentRepository;
    private final JobOfferApplicationRepository jobOfferApplicationRepository;
    private final JobInterviewRepository jobInterviewRepository;

    public ProgramManagerService(ProgramManagerRepository programManagerRepository, PasswordEncoder passwordEncoder, JobOfferService jobOfferService, StudentService studentService, JobPermissionRepository jobPermissionRepository, JobOfferRepository jobOfferRepository, InternshipOfferRepository internshipOfferRepository, TeacherRepository teacherRepository, InternshipEvaluationRepository internshipEvaluationRepository, AcademicSessionRepository academicSessionRepository, NotificationService notificationService, CurriculumVitaeRepository curriculumVitaeRepository, StudentRepository studentRepository, JobOfferApplicationRepository jobOfferApplicationRepository, JobInterviewRepository jobInterviewRepository) {
        this.programManagerRepository = programManagerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jobOfferService = jobOfferService;
        this.studentService = studentService;
        this.jobPermissionRepository = jobPermissionRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.internshipOfferRepository = internshipOfferRepository;
        this.teacherRepository = teacherRepository;
        this.internshipEvaluationRepository = internshipEvaluationRepository;
        this.academicSessionRepository = academicSessionRepository;
//        List<ProgramManager> list = getAllProgramManager();
//        if (list.isEmpty()) {
//            System.out.println("Creating a new program manager");
//            ProgramManager programManager = new ProgramManager();
//            programManager.setNom("test");
//            programManager.setPrenom("gs");
//            programManager.setCredentials("gs@email.com",
//                    passwordEncoder.encode("123456"));
//            programManager.setAdresse("1000 ici there");
//            programManager.setTelephone("1234567890");
//            this.programManagerRepository.save(programManager);
//        }
        this.notificationService = notificationService;
        this.curriculumVitaeRepository = curriculumVitaeRepository;
        this.studentRepository = studentRepository;
        this.jobOfferApplicationRepository = jobOfferApplicationRepository;
        this.jobInterviewRepository = jobInterviewRepository;
    }

    public ProgramManagerDTO createProgramManager(ProgramManagerRegisterDTO programManagerRegisterDTO) throws InvalidUserFormatException, AlreadyExistsException {
        if (!verifyInfo(programManagerRegisterDTO)) {
            throw new InvalidUserFormatException("Some informations are missing or invalid. Please check your informations.");
        }
        //TODO : HASH le mot de passe
        ProgramManager programManager = new ProgramManager();
        programManager.setNom(programManagerRegisterDTO.nom().trim());
        programManager.setPrenom(programManagerRegisterDTO.prenom().trim());

        programManager.setAdresse(programManagerRegisterDTO.adresse().trim());
        programManager.setTelephone(programManagerRegisterDTO.telephone().trim());
        programManager.setCredentials(programManagerRegisterDTO.courriel().trim().toLowerCase(Locale.ROOT), passwordEncoder.encode(programManagerRegisterDTO.mdp().trim()));
        programManager.setNotificationIdCutoff(notificationService.getLatestId());

        try {
            return toDTO(programManagerRepository.save(programManager));
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsException();
        }
    }

    public ProgramManagerDTO getProgramManager(long id) {
        return toDTO(programManagerRepository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    public ProgramManagerDTO getProgramManagerByEmail(String email) {
        return toDTO(programManagerRepository.findUserAppByEmail(email).orElseThrow(UserNotFoundException::new));
    }

    @Transactional
    public JobPermissionDTO createJobPermission(long id, JobPermissionRegisterDTO jobPermissionRegisterDTO, UserDTO userDTO) throws JobNotFoundException, DisciplineNotFoundException {
        // validation
        Objects.requireNonNull(jobPermissionRegisterDTO);
        // TODO who cancel the joboffer validation?
        JobOffer jobOffer = jobOfferService.getJobOfferById(id);

        Session session = checkIfHaveToCreateNewSession(jobOffer);
        jobOffer.setSession(session);
        academicSessionRepository.save(session);
        System.out.println("Session created by GS when APPROVED: " + session.getYear() + " " + session.getSeason());

        for (String discipline : jobPermissionRegisterDTO.disciplines()) {
            if (!Discipline.isValidDiscipline(discipline))
                throw new DisciplineNotFoundException("Discipline not found");
        }
        String disciplines = jobPermissionRegisterDTO.disciplines().stream().reduce((a, b) -> a + ";" + b).orElse("");
        List<Student> students = new ArrayList<>();
        for ( long studentId : jobPermissionRegisterDTO.studentIds()) {
            students.add(studentService.getStudentEntity(studentId));
        }
        // create jobpermission
        // check create new jobpermission or update by joboffer id
        Optional<JobPermission> jobPermissionbd = jobPermissionRepository.findByJobOfferId(id);
        JobPermission jobPermission;
        jobPermission = jobPermissionbd.orElseGet(JobPermission::new);

        jobPermission.setDisciplines(disciplines);
        jobPermission.setExpirationDate(jobPermissionRegisterDTO.expirationDate());
        jobPermission.setStudents(students);
        jobOffer.setApproved(jobPermissionRegisterDTO.isApproved());
        jobOffer.setActivated(true);
        jobPermission.setJobOffer(jobOffer);

        // save jobpermission
        JobPermission jobPermissionDB = jobPermissionRepository.save(jobPermission);

        notificationService.tryFindViewNotification(userDTO,NotificationCode.OFFER_VALIDATION_REQUIRED,jobPermissionDB.getJobOffer().getId());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.JOB_OFFER_VALIDATED)
                .userId(jobPermissionDB.getEmployeur().getId())
                .session(session)
                .referenceId(jobPermissionDB.getJobOffer().getId())
                .build());

        createUserNotification(jobPermissionDB,session);

        // return jobpermissionDTO
        return JobPermissionDTO.toDTO(jobPermissionDB);
    }

    private void createUserNotification(JobPermission jobPermission, Session session) {
        if (!jobPermission.getStudents().isEmpty()) {
            for (Student student : jobPermission.getStudents()) {
                notificationService.addNotification(Notification.builder()
                                .code(NotificationCode.NEW_JOB_OFFER)
                                .userId(student.getId())
                                .session(session)
                                .referenceId(jobPermission.getJobOffer().getId())
                        .build());
            }
        } else {
            notificationService.addNotification(Notification.builder()
                    .code(NotificationCode.NEW_JOB_OFFER)
                    .filter(Role.STUDENT)
                    .discipline(Discipline.toEnum(jobPermission.getDisciplines()))
                    .session(session)
                    .referenceId(jobPermission.getJobOffer().getId())
                    .build());
        }
    }


    public List<JobOfferDTO> getJobOffersWaitingForApproval(Long sessionId) throws JobNotFoundException {
        List<JobOffer> jobOffers = jobOfferRepository.findByIsApprovedFalse(sessionId);
        if (jobOffers.isEmpty())
            throw new JobNotFoundException("No job offers waiting for approval");
        return jobOffers.stream().map(JobOfferDTO::toDTO).toList();
    }

    @Transactional
    public Page<InternshipOfferDTO> getContracts(long sessionId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<InternshipOffer> internshipList = internshipOfferRepository.findInternshipOfferWithOrAwaitingContract(sessionId,pageable);
        return internshipList.map(InternshipOfferDTO::toDTO);
    }

    public void beginContract(long internshipOfferId, UserDTO userDTO) throws InternshipNotFoundException, JobApplicationNotFoundException {
        InternshipOffer internshipOffer = internshipOfferRepository.findById(internshipOfferId).orElseThrow(InternshipNotFoundException::new);
        internshipOffer.setContract(new Contract());

        internshipOfferRepository.save(internshipOffer);

        notificationService.tryFindViewNotification(userDTO,NotificationCode.CONTRACT_TO_START,internshipOffer.getId());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.CONTRACT_TO_SIGN)
                .userId(internshipOffer.getEmployeurId())
                .session(internshipOffer.getSession())
                .referenceId(internshipOffer.getId())
                .build());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.CONTRACT_TO_SIGN)
                .userId(internshipOffer.getStudentId())
                .session(internshipOffer.getSession())
                .referenceId(internshipOffer.getId())
                .build());
    }

    /*public List<InternshipOfferDTO> getContracts() {
        List<InternshipOffer> internshipList = internshipOfferRepository.findInternshipOfferWithOrAwaitingContract();
        return internshipList.stream().map(InternshipOfferDTO::toDTO).toList();
    }*/


    public List<InternshipOfferDTO> getInternsWaitingForAssignmentToProf() throws InternshipNotFoundException {
        // contract is ok, not has internship evalutation
        List<InternshipOffer> internshipList = internshipOfferRepository.findInternshipOfferWithContractAllSigned();
        if (internshipList.isEmpty())
            throw new InternshipNotFoundException("No internship offers waiting for assignment to professor");
        return internshipList.stream().map(InternshipOfferDTO::toDTO).toList();
    }

    public List<InternshipEvaluationDTO> getProfCurrentInterns(Long profId) {
        List<InternshipEvaluation> internshipEvaluations = internshipEvaluationRepository.findCurrentEvaluationsByTeacherId(profId);
        return internshipEvaluations.stream().map(InternshipEvaluationDTO::toDTO).toList();
    }

    private boolean verifyInfo(ProgramManagerRegisterDTO programManagerRegisterDTO) {
        return  !programManagerRegisterDTO.prenom().trim().isEmpty() &&
                !programManagerRegisterDTO.nom().trim().isEmpty() &&
                !programManagerRegisterDTO.adresse().trim().isEmpty() &&
                programManagerRegisterDTO.telephone().trim().matches("^\\d{10}$") &&
                programManagerRegisterDTO.courriel().trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    private Session checkIfHaveToCreateNewSession(JobOffer jobOffer){
        String[] dateSplit = jobOffer.getDateDebut().split("-");

        String year = dateSplit[0];
        String month = dateSplit[1];

        Session session = new Session();
        session.setYear(year);
        session = getCurrentSeason(session, month);
        session.setJobOffers(new ArrayList<>());
        session.addJobOffer(jobOffer);

        checkIfHaveToCreateNextSession(session);
        List<Session> sessions = academicSessionRepository.findAll();
        for (Session s : sessions){
            if (s.getYear().equals(session.getYear()) && s.getSeason().equals(session.getSeason())){
                s.addJobOffer(jobOffer);
                return s;
            }
        }
        return session;
    }

    public Session getCurrentAcademicSession(){
        Session currentSession = new Session();
        currentSession.setYear(String.valueOf(LocalDateTime.now().getYear()));
        currentSession = getCurrentSeason(currentSession, String.valueOf(LocalDateTime.now().getMonthValue()));

        return checkIfHaveToCreateNextSession(currentSession);
    }

    public SessionDTO getCurrentAcademicSessionDTO(){
        return SessionDTO.toDTO(getCurrentAcademicSession());
    }

    private Session checkIfHaveToCreateNextSession(Session currentSession) {
        List<Session> sessions = academicSessionRepository.findAll();
        Session nextSession = getNextSession(currentSession);
        for(Session s : sessions){
            if(s.getYear().equals(nextSession.getYear()) && s.getSeason().equals(nextSession.getSeason())){
                System.out.println("Next session already exists");
                return s;
            }
        }
        return academicSessionRepository.save(nextSession);
    }

    private static Session getCurrentSeason(Session session, String month) {
        switch (month){
            case "01":
            case "02":
            case "03":
            case "04":
            case "05":
                session.setSeason("Hiver");
                session.setStartDate(LocalDateTime.of(Integer.parseInt(session.getYear()), 1, 1, 0, 0));
                session.setEndDate(LocalDateTime.of(Integer.parseInt(session.getYear()), 5, 31, 0, 0));
                return session;
            case "06":
            case "07":
            case "08":
                session.setSeason("Été");
                session.setStartDate(LocalDateTime.of(Integer.parseInt(session.getYear()), 6, 1, 0, 0));
                session.setEndDate(LocalDateTime.of(Integer.parseInt(session.getYear()), 8, 31, 0, 0));
                return session;
            case "09":
            case "10":
            case "11":
            case "12":
                session.setSeason("Automne");
                session.setStartDate(LocalDateTime.of(Integer.parseInt(session.getYear()), 9, 1, 0, 0));
                session.setEndDate(LocalDateTime.of(Integer.parseInt(session.getYear()), 12, 31, 0, 0));
                return session;
        }
        return new Session();
    }

    private Session getNextSession(Session currentSession){
        Session nextSession = new Session();
        switch (currentSession.getSeason()){
            case "Hiver":
                nextSession.setSeason("Été");
                nextSession.setYear(currentSession.getYear());
                nextSession.setStartDate(LocalDateTime.of(Integer.parseInt(nextSession.getYear()), 6, 1, 0, 0));
                nextSession.setEndDate(LocalDateTime.of(Integer.parseInt(nextSession.getYear()), 8, 31, 0, 0));
                break;
            case "Été":
                nextSession.setSeason("Automne");
                nextSession.setYear(currentSession.getYear());
                nextSession.setStartDate(LocalDateTime.of(Integer.parseInt(nextSession.getYear()), 9, 1, 0, 0));
                nextSession.setEndDate(LocalDateTime.of(Integer.parseInt(nextSession.getYear()), 12, 31, 0, 0));
                break;
            case "Automne":
                nextSession.setSeason("Hiver");
                nextSession.setYear(String.valueOf(Integer.parseInt(currentSession.getYear())+1));
                nextSession.setStartDate(LocalDateTime.of(Integer.parseInt(nextSession.getYear()), 1, 1, 0, 0));
                nextSession.setEndDate(LocalDateTime.of(Integer.parseInt(nextSession.getYear()), 5, 31, 0, 0));
                break;
        }
        return nextSession;
    }

    @Transactional
    public InternshipEvaluationDTO assignToProf( long teacherId, long internshipOfferId, UserDTO userDTO) throws InternshipNotFoundException, UserNotFoundException {
        InternshipOffer internshipOffer = internshipOfferRepository.findById(internshipOfferId).orElseThrow(() -> new InternshipNotFoundException("Internship offer not found"));
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new UserNotFoundException("Teacher not found"));

        // create internship evaluation
        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setTeacher(teacher);
        internshipEvaluation.setInternshipOffer(internshipOffer);
        InternshipEvaluation internshipEvaluationDB = internshipEvaluationRepository.save(internshipEvaluation);
        internshipOffer.setInternshipEvaluation(internshipEvaluationDB);
        internshipOfferRepository.save(internshipOffer);

        notificationService.tryFindViewNotification(userDTO,NotificationCode.INTERN_REQUIRE_ASSIGNEMENT,internshipOffer.getStudentId());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.INTERN_ASSIGNMENT)
                .userId(teacherId)
                .referenceId(internshipEvaluationDB.getId())
                .build());

        return InternshipEvaluationDTO.toDTO(internshipEvaluationDB);
    }
    @Transactional
    public ProgramManagerDTO updateProgramManager(Long userId, ProgramManagerDTO pmUpdate) throws UserNotFoundException {
        ProgramManager pm = programManagerRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Program Manager not found"));

        if (pmUpdate.nom() != null) {
            pm.setNom(pmUpdate.nom());
        }
        if (pmUpdate.prenom() != null) {
            pm.setPrenom(pmUpdate.prenom());
        }
        if (pmUpdate.adresse() != null) {
            pm.setAdresse(pmUpdate.adresse());
        }
        if (pmUpdate.telephone() != null) {
            pm.setTelephone(pmUpdate.telephone());
        }
        if (pmUpdate.courriel() != null) {
            pm.setCourriel(pmUpdate.courriel());
        }
        if(pmUpdate.adresse()!=null){
            pm.setAdresse(pmUpdate.adresse());
        }

        pm = programManagerRepository.save(pm);
        return ProgramManagerDTO.toDTO(pm);
    }
    public ProgramManagerDTO updateProgramManagerPassword(Long userId, UpdatePasswordDTO updatePasswordDTO) throws UserNotFoundException, InvalidPasswordException {
        ProgramManager programManager = programManagerRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Program Manager not found"));

        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), programManager.getCredentials().getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect.");
        }

        programManager.getCredentials().setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        ProgramManager updatedProgramManager = programManagerRepository.save(programManager);

        return ProgramManagerDTO.toDTO(updatedProgramManager);
    }
    public Page<JobOfferDTO> getAllNonApprovedJobOffers(Pageable pageable, String query, Long sessionID) {
        Page<JobOffer> jobOffers = jobOfferRepository.findByIsApprovedFalsePage(pageable, query, sessionID);
        if(jobOffers.isEmpty()){
            return Page.empty();
        }
        return jobOffers.map(JobOfferDTO::toDTO);
    }

    public Page<JobOfferDTO> getAllApprovedJobOffers(Pageable pageable, String query, Long sessionId) {
        Page<JobOffer> jobOffers = jobOfferRepository.findByIsApprovedTrue(pageable,query, sessionId);
        if(jobOffers.isEmpty()){
            return Page.empty();
        }
        return jobOffers.map(JobOfferDTO::toDTO);
    }


    public Page<StudentDTO> getAllStudents(Pageable pageable, String query, Long sessionId) {
        Page<Student> studentDTOS = studentRepository.findAll(pageable,query, sessionId);
        if(studentDTOS.isEmpty()){
            return Page.empty();
        }
        return studentDTOS.map(StudentDTO::toDTO);
    }

    public Page<StudentDTO> getAllStudentsWithNoCV(Pageable pageable, String query, Long sessionId) {
        Page<StudentDTO> studentDTOSPage = curriculumVitaeRepository.getAllStudentsWithNoCV(pageable, query).map(StudentDTO::toDTO);
        if(studentDTOSPage.isEmpty()){
            return Page.empty();
        }
        return studentDTOSPage;
    }

    public Page<StudentDTO> getAllStudentsWithCVNotAccepted(Pageable pageable, String query, Long sessionId) {
        Page<CurriculumVitae> curriculumVitaeListWaiting = curriculumVitaeRepository.findByNotStatusPage(ApprovalStatus.VALIDATED, pageable, query);
        if(curriculumVitaeListWaiting.isEmpty()){
            return Page.empty();
        }
        return curriculumVitaeListWaiting.map(curriculumVitae -> StudentDTO.toDTO(curriculumVitae.getStudent()));
    }



    public Page<StudentDTO> getAllStudentsWithNoInterview(Pageable pageable, String query, Long sessionId){
        Page<Student> jobInterviews = jobInterviewRepository.findAllStudentWithNoInterview(pageable, query, sessionId);
        if(jobInterviews.isEmpty()){
            return Page.empty();
        }
        return jobInterviews.map(StudentDTO::toDTO);
    }

    public Page<StudentDTO> getAllStudentsWaitingForInterview(Pageable pageable, String query, Long sessionId){
        Page<JobInterview> jobInterviews = jobInterviewRepository.findAllStudentWaitingForInterview(pageable,query, sessionId);
        if(jobInterviews.isEmpty()){
            return Page.empty();
        }
        return jobInterviews.map(jobInterview -> StudentDTO.toDTO(jobInterview.getStudent()));
    }

    public Page<StudentDTO> getAllStudentsWaitingForInterviewAnswer(Pageable pageable, String query, Long sessionId) throws JobApplicationNotFoundException {
        Page<JobInterview> jobInterviews = jobInterviewRepository.findAllStudentWaitingForInterviewAnswer(pageable, query, sessionId);
        if(jobInterviews.isEmpty()){
            return Page.empty();
        }
        return jobInterviews.map(jobInterview -> StudentDTO.toDTO(jobInterview.getStudent()));
    }

    public Page<StudentDTO> getAllStudentsWithInternship(Pageable pageable, String query, Long sessionId) throws JobApplicationNotFoundException {
        Page<InternshipOffer> internshipOffers = internshipOfferRepository.findInternshipOfferByStatus(ApprovalStatus.ACCEPTED, pageable, query, sessionId);
        if(internshipOffers.isEmpty()){
            return Page.empty();
        }
        return internshipOffers.map(internshipOffer -> StudentDTO.toDTO(internshipOffer.getStudent()));
    }

    public Page<StudentDTO> getAllStudentsNotYetAssessedByTeacher(Pageable pageable, String query, Long sessionId) throws JobApplicationNotFoundException {
        Page<InternshipEvaluation> internshipEvaluations = internshipEvaluationRepository.findAllStudentsNotYetEvaluatedByTeacher(query, pageable, sessionId);
        if(internshipEvaluations.isEmpty()){
            return Page.empty();
        }
        return internshipEvaluations.map(internshipEvaluation -> StudentDTO.toDTO(internshipEvaluation.getInternshipOffer().getStudent()));
    }

    public Page<StudentDTO> getAllStudentsWithEmployerNotYetAssessedBySupervisor(Pageable pageable, String query, Long sessionId) throws JobApplicationNotFoundException {
        Page<InternshipEvaluation> internshipEvaluations = internshipEvaluationRepository.findAllStudentsEmployeurNotYetEvaluatedByTeacher(query, pageable, sessionId);
        if(internshipEvaluations.isEmpty()){
            return Page.empty();
        }
        return internshipEvaluations.map(internshipEvaluation -> StudentDTO.toDTO(internshipEvaluation.getInternshipOffer().getStudent()));
    }
}
