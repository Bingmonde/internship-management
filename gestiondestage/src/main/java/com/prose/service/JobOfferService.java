package com.prose.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prose.entity.*;
import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Employeur;
import com.prose.entity.users.Student;
import com.prose.entity.users.auth.Role;
import com.prose.repository.*;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.*;
import com.prose.service.Exceptions.CVNotFoundException;
import com.prose.service.Exceptions.JobNotFoundException;
import com.prose.service.Exceptions.MissingPermissionsExceptions;
import com.prose.service.Exceptions.TooManyApplicationsExceptions;
import com.prose.service.dto.JobOfferApplicationDTO;
import com.prose.service.dto.JobOfferDTO;
import com.prose.service.dto.JobOfferRegisterDTO;
import com.prose.service.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JobOfferService {

    private static final Logger logger = LoggerFactory.getLogger(JobOfferService.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final JobOfferRepository jobOfferRepository;
    private final EmployeurRepository employeurRepository;

    private final StudentRepository studentRepository;
    private final PDFDocuRepository pdfDocuRepository;

    private final CurriculumVitaeRepository curriculumVitaeRepository;

    private final JobPermissionRepository jobPermissionRepository;
    private final JobOfferApplicationRepository jobOfferApplicationRepository;
    private final JobInterviewRepository jobInterviewRepository;

    private final InternshipOfferRepository internshipOfferRepository;

    private final AcademicSessionRepository academicSessionRepository;

    private final NotificationService notificationService;


    public JobOfferService(JobOfferRepository jobOfferRepository, EmployeurRepository employeurRepository, PDFDocuRepository pdfDocuRepository, StudentRepository studentRepository, CurriculumVitaeRepository curriculumVitaeRepository, JobPermissionRepository jobPermissionRepository, JobOfferApplicationRepository jobOfferApplicationRepository, InternshipOfferRepository internshipOfferRepository, NotificationService notificationService, JobInterviewRepository jobInterviewRepository, AcademicSessionRepository academicSessionRepository) {
        this.jobOfferRepository = jobOfferRepository;
        this.employeurRepository = employeurRepository;
        this.pdfDocuRepository = pdfDocuRepository;
        this.studentRepository = studentRepository;
        this.curriculumVitaeRepository = curriculumVitaeRepository;
        this.jobPermissionRepository = jobPermissionRepository;
        this.jobOfferApplicationRepository = jobOfferApplicationRepository;
        this.jobInterviewRepository = jobInterviewRepository;
        this.internshipOfferRepository = internshipOfferRepository;
        this.notificationService = notificationService;
        this.academicSessionRepository = academicSessionRepository;
    }

    @Transactional
    public JobOfferDTO createJobOffer(JobOfferRegisterDTO jobOfferRegisterDTO, MultipartFile uploadFile, String employeurEmail) throws IOException {
        logger.info("Creating job offer for employer with email: {}", employeurEmail);

        Employeur employeur = employeurRepository.findUserAppByEmail(employeurEmail)
                .orElseThrow(() -> {
                    logger.error("Employeur with email {} not found", employeurEmail);
                    return new UserNotFoundException("Employeur with email " + employeurEmail + " not found");
                });
        JobOffer jobOffer = new JobOffer();
        jobOffer.setTitre(jobOfferRegisterDTO.titre());
        jobOffer.setDateDebut(jobOfferRegisterDTO.dateDebut());
        jobOffer.setDateFin(jobOfferRegisterDTO.dateFin());
        jobOffer.setLieu(jobOfferRegisterDTO.lieu());
        jobOffer.setTypeTravail(jobOfferRegisterDTO.typeTravail());
        jobOffer.setNombreStagiaire(jobOfferRegisterDTO.nombreStagiaire());
        jobOffer.setTauxHoraire(jobOfferRegisterDTO.tauxHoraire());
        jobOffer.setDescription(jobOfferRegisterDTO.description());
        jobOffer.setWeeklyHours(jobOfferRegisterDTO.weeklyHours());
        jobOffer.setDayScheduleFrom(jobOfferRegisterDTO.dailyScheduleFrom());
        jobOffer.setDayScheduleTo(jobOfferRegisterDTO.dailyScheduleTo());
        jobOffer.setEmployeur(employeur);

        Session session = checkIfHaveToCreateNewSession(jobOffer);
        jobOffer.setSession(session);
        academicSessionRepository.save(session);

        // 添加空文件检查
        if (uploadFile == null || uploadFile.isEmpty()) {
            logger.error("Empty file or no file uploaded for job offer: {}", jobOffer.getTitre());
            throw new IOException("Empty file or no file uploaded");
        }

        // 文件上传处理
        logger.info("Processing file upload for job offer: {}", jobOffer.getTitre());

        if (!uploadFile.getContentType().equals("application/pdf")) {
            logger.error("Invalid file type for job offer: {}, expected PDF", jobOffer.getTitre());
            throw new IOException("Invalid file type, only PDF files are allowed");
        }

        if (uploadFile.getSize() > MAX_FILE_SIZE) {
            logger.error("File too large for job offer: {}", jobOffer.getTitre());
            throw new IOException("File is too large, maximum allowed size is " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
        }

        String fileName = employeur.getNomCompagnie() + "_" + jobOffer.getTitre().replace(' ', '_') + "_" + LocalDateTime.now() + ".pdf";
        PDFDocu pdfDocu = savePDF(uploadFile, fileName);
        jobOffer.setPdfDocu(pdfDocu);

        JobOffer savedJobOffer = jobOfferRepository.save(jobOffer);

        logger.info("Job offer created successfully with ID: {}", savedJobOffer.getId());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.OFFER_VALIDATION_REQUIRED)
                .filter(Role.PROGRAM_MANAGER)
                .referenceId(savedJobOffer.getId())
                .build());

        return JobOfferDTO.toDTO(savedJobOffer);
    }
    @Transactional
    public List<JobOfferDTO> getJobOffersByEmployeur(Long employeurId, Long sessionId) {
        List<JobOffer> jobOffers = jobOfferRepository.findByEmployeurId(employeurId, sessionId);
        return jobOffers.stream().map(JobOfferDTO::toDTO).collect(Collectors.toList());
    }
    @Transactional
    public JobOfferDTO updateJobOffer(Long jobOfferId, JobOfferRegisterDTO jobOfferRegisterDTO, String employeurEmail) throws MissingPermissionsExceptions {
        Employeur employeur = employeurRepository.findUserAppByEmail(employeurEmail)
                .orElseThrow(() -> new UserNotFoundException("Employeur with email " + employeurEmail + " not found"));

        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new UserNotFoundException("Job Offer with ID " + jobOfferId + " not found"));

        if (!jobOffer.getEmployeur().equals(employeur)) {
            throw new MissingPermissionsExceptions();
        }

        jobOffer.setTitre(jobOfferRegisterDTO.titre());
        jobOffer.setDateDebut(jobOfferRegisterDTO.dateDebut());
        jobOffer.setDateFin(jobOfferRegisterDTO.dateFin());
        jobOffer.setLieu(jobOfferRegisterDTO.lieu());
        jobOffer.setTypeTravail(jobOfferRegisterDTO.typeTravail());
        jobOffer.setNombreStagiaire(jobOfferRegisterDTO.nombreStagiaire());
        jobOffer.setTauxHoraire(jobOfferRegisterDTO.tauxHoraire());
        jobOffer.setDescription(jobOfferRegisterDTO.description());
//        jobOffer.setPdfPath(jobOfferRegisterDTO.pdfPath());

        JobOffer updatedJobOffer = jobOfferRepository.save(jobOffer);
        return JobOfferDTO.toDTO(updatedJobOffer);
    }
    @Transactional
    public Page<JobOfferDTO> getJobOffersForStudent(String studentEmail, Long sessionId, String query, int pageNumber, int pageSize) throws MissingPermissionsExceptions {
        Student student = studentRepository.findUserAppByEmail(studentEmail)
                .orElseThrow(() -> new UserNotFoundException("Student with email " + studentEmail + " not found"));

        // trouve le dernier CV validé de l'étudiant, s'il en a pas: erreur
        // tant qu'un cv de l'étudiant a été validé (même si c'est un ancien) la liste des offres de stage sera accessible
        List<CurriculumVitae> curriculumVitaes = curriculumVitaeRepository.findByStudentId(student.getId());
        boolean hasValidCV = curriculumVitaes.stream().anyMatch(cv -> cv.getStatus() == ApprovalStatus.VALIDATED);
        if (curriculumVitaes.isEmpty() || !hasValidCV) {
            throw new MissingPermissionsExceptions("Student with email " + studentEmail + " does not have a validated CV");
        }


        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<JobPermission> jobPermissions = jobPermissionRepository.findBySessionIdAndSearch(sessionId,student.getDiscipline().toString(),student,query.trim().toLowerCase(),pageable);

        /*// on enlève les offres qui ne sont pas dans le domaine de l'étudiant
        jobPermissions = jobPermissions.stream().filter(jobPermission -> jobPermission.getDisciplines().contains(student.getDiscipline().toString())).collect(Collectors.toList());

        // on enlève les offres de stage qui sont spécifiques à des étudiants et l'étudiant n'est pas dans la liste
        jobPermissions = jobPermissions.stream().filter(jobPermission -> jobPermission.getStudents().isEmpty() || jobPermission.getStudents().stream().anyMatch(s -> s.getId().equals(student.getId()))).collect(Collectors.toList());

        // filtrer les offres de stage qui sont approuvées
        jobPermissions = jobPermissions.stream().filter(jobPermission -> jobPermission.getJobOffer().isApproved()).collect(Collectors.toList());*/

        //filtrer les internship qui sont remplie
//        jobPermissions = jobPermissions.stream().filter(jobPermission -> jobPermission.getJobOffer().getInternshipOffer() == null || jobPermission.getJobOffer().getInternshipOffer().getStudentsLength() < jobPermission.getJobOffer().getNombreStagiaire()).collect(Collectors.toList());

        return jobPermissions.map(JobPermission::getJobOffer).map(JobOfferDTO::toDTO);
    }
    @Transactional
    public JobOffer getJobOfferById(Long jobOfferId) throws JobNotFoundException {
        Optional<JobOffer> jobOffer = jobOfferRepository.findById(jobOfferId);
        if (jobOffer.isEmpty()) {
            throw new JobNotFoundException("Job offer with ID " + jobOfferId + " not found");
        }
        return jobOffer.get();
    }
    @Transactional
    public JobOfferDTO getJobOfferDTOById(Long jobOfferId) throws JobNotFoundException {
        Optional<JobOffer> jobOffer = jobOfferRepository.findById(jobOfferId);
        if (jobOffer.isEmpty()) {
            throw new JobNotFoundException("Job offer with ID " + jobOfferId + " not found");
        }
        return JobOfferDTO.toDTO(jobOffer.get());
    }

//    public JobInterviewDTO getJobInterViewDTOById(Long jobInterviewId) throws JobNotFoundException {
//        Optional<JobInterview> jobInterview = jobInterviewRepository.findById(jobInterviewId);
//        if (jobInterview.isEmpty()) {
//            throw new JobNotFoundException("Interview with ID " + jobInterviewId + " not found");
//        }
//        return JobInterviewDTO.toDTO(jobInterview.get());
//    }

    // for test reasons, make it public
    @Transactional
    public PDFDocu savePDF(MultipartFile uploadFile, String fileName) throws IOException {
        try {
            byte[] pdfBytes = uploadFile.getBytes();
            PDFDocu pdfDocu = new PDFDocu();
            pdfDocu.setPdfData(pdfBytes);
            pdfDocu.setFileName(fileName);
            return pdfDocuRepository.save(pdfDocu);
        } catch (IOException e) {
            throw new IOException("Reading PDF file failed");
        }
    }

    @Transactional
    public JobOfferApplicationDTO applyToJobOffer(Long jobOfferId, long cvId, UserDTO userDTO) throws JobNotFoundException, CVNotFoundException, MissingPermissionsExceptions, TooManyApplicationsExceptions {
        long userId = userDTO.id();
        CurriculumVitae curriculumVitae = curriculumVitaeRepository.findById(cvId).orElseThrow(CVNotFoundException::new);
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId).orElseThrow(JobNotFoundException::new);
        JobPermission jobPermission = jobPermissionRepository.findByJobOfferId(jobOfferId).orElseThrow(JobNotFoundException::new);

        if (curriculumVitae.getStatus() != ApprovalStatus.VALIDATED || curriculumVitae.getStudent().getId() != userId) {
            throw new MissingPermissionsExceptions();
        }

        // TODO : Est-ce qu'il devrait lancer des exceptions différents? Est-ce qu'on a besoin de savoir quel verification n'a pas réussi?
        if (Discipline.toEnum(jobPermission.getDisciplines()) != curriculumVitae.getStudent().getDiscipline()) {
            throw new MissingPermissionsExceptions();
        }

        if (!jobPermission.getStudents().isEmpty()) {
            if (jobPermission.getStudents().stream().noneMatch(student -> Objects.equals(student.getId(), curriculumVitae.getStudent().getId()))) {
                throw new MissingPermissionsExceptions();
            }
        }

        List<JobOfferApplication> otherApplications = jobOfferApplicationRepository.findJobOfferApplicationByStudentAndOffer(userId, jobOfferId);

        if (!otherApplications.isEmpty()) {
            if (otherApplications.stream().anyMatch(JobOfferApplication::isActive)) {
                throw new TooManyApplicationsExceptions();
            }
        }

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setCurriculumVitae(curriculumVitae);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());

        JobOfferApplicationDTO jobOfferApplicationDTO = JobOfferApplicationDTO.toDTO(jobOfferApplicationRepository.save(jobOfferApplication));

        notificationService.tryFindViewNotification(userDTO,NotificationCode.NEW_JOB_OFFER,jobOfferApplicationDTO.getJobOfferId());
        notificationService.tryFindViewNotification(UserDTO.toDTO(jobOfferApplication.getEmployeur()),NotificationCode.JOB_OFFER_VALIDATED,jobOfferApplicationDTO.getJobOfferId());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.NEW_APPLICANT)
                .userId(jobOfferApplication.getEmployeur().getId())
                .session(jobOfferApplication.getJobOffer().getSession())
                .referenceId(jobOfferApplicationDTO.id())
                .build());

        return jobOfferApplicationDTO;
    }
    @Transactional
    public List<JobOfferApplicationDTO> getJobOffersApplicationsFromStudent(Long studentId, Long sessionId) {
        //TODO : On va surement devoir ajouter le ApprovalStatus ici aussi ici aussi.
        return jobOfferApplicationRepository.findJobOfferApplicationByStudent(studentId, sessionId).stream().map(JobOfferApplicationDTO::toDTO).collect(Collectors.toList());
    }
    @Transactional
    public void cancelJobOfferApplicationStudent(long jobOfferApplicationId, long studentId) throws JobNotFoundException, MissingPermissionsExceptions {
        JobOfferApplication jobOfferApplication = jobOfferApplicationRepository.findById(jobOfferApplicationId).orElseThrow(JobNotFoundException::new);

        if (jobOfferApplication.getCurriculumVitae().getStudent().getId() != studentId) {
            throw new MissingPermissionsExceptions();
        }

        jobOfferApplication.setActive(false);
        jobOfferApplicationRepository.save(jobOfferApplication);
    }

    @Transactional
    public Page<JobOfferApplicationDTO> getJobOfferApplicationsFromJobOfferId(Long offerId, Long sessionId, String query, CandidatesStatusFilterForJobApplicationsFull candidatesStatusFilter, int pageNumber, int pageSize) throws JobNotFoundException {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<JobOfferApplication> offerApplicationPage = switch (candidatesStatusFilter) {
            case CandidatesStatusFilterForJobApplicationsFull.INTERVIEWEE ->
                    jobOfferApplicationRepository.findJobOfferApplicationByJobOfferIdOnlyInterviewees(offerId, sessionId, query, ApprovalStatus.VALIDATED, pageable);
            case CandidatesStatusFilterForJobApplicationsFull.JOB_OFFERED ->
                    jobOfferApplicationRepository.findJobOfferApplicationByJobOfferIdOnlyWithInternshipOffered(offerId, sessionId, query, ApprovalStatus.VALIDATED, pageable);
            default ->
                    jobOfferApplicationRepository.findJobOfferApplicationByJobOfferId(offerId, sessionId, query, ApprovalStatus.VALIDATED, pageable);
        };

        return offerApplicationPage.map(JobOfferApplicationDTO::toDTO);
    }
    @Transactional
    public JobOfferApplicationDTO getJobOfferApplication(Long applicationId) throws JobApplicationNotFoundException {
        return JobOfferApplicationDTO.toDTO(jobOfferApplicationRepository.findById(applicationId).orElseThrow(JobApplicationNotFoundException::new));
    }
    @Transactional
    public List<JobInterviewDTO> getInterviewFromJobOfferApplication(JobOfferApplicationDTO jobOfferApplicationDTO) {
        return jobInterviewRepository.findJobInterviewByStudentIdAndJobOfferId(jobOfferApplicationDTO.getStudentId(),jobOfferApplicationDTO.getJobOfferId())
                .stream().map(JobInterviewDTO::toDTO).toList();
    }
    @Transactional
    public JobInterviewDTO getInterviewFromId(long id) throws InterviewNotFoundException {
        return JobInterviewDTO.toDTO(jobInterviewRepository.findById(id).orElseThrow(InterviewNotFoundException::new));
    }
    @Transactional
    public JobInterviewDTO approveJobInterview(long jobInterviewId,UserDTO userDTO) throws JobNotFoundException, MissingPermissionsExceptions {
        Optional<JobInterview> jobInterviewOpt = jobInterviewRepository.findById(jobInterviewId);
        if (jobInterviewOpt.isEmpty()) {
            throw new JobNotFoundException("JobNotFoundException");
        }
        JobInterview jobInterview = jobInterviewOpt.get();

        if (!jobInterview.getStudentId().equals(userDTO.id())) {
            throw new MissingPermissionsExceptions();
        }

        jobInterview.setConfirmedByStudent(true);
        jobInterview.setConfirmationDate(LocalDateTime.now());

        JobInterviewDTO jobInterviewDTO = JobInterviewDTO.toDTO(jobInterviewRepository.save(jobInterview));

        notificationService.tryFindViewNotification(userDTO,NotificationCode.NEW_JOB_OFFER,jobInterview.getJobOffer().getId());
        notificationService.tryFindViewNotification(userDTO,NotificationCode.NEW_INTERVIEW,jobInterview.getId());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.INTERVIEW_CONFIRMED)
                .userId(jobInterview.getEmployeur().getId())
                .session(jobInterview.getJobOffer().getSession())
                .referenceId(jobInterviewDTO.id())
                .build());

        return jobInterviewDTO;
    }

    @Transactional
    public InternshipOfferDTO offerInternshipToStudent(Long jobApplicationId, UserDTO userDTO, Long offerExpireIn)
            throws JobNotFoundException, UserNotFoundException, MissingPermissionsExceptions, MaxInternsReachedException, NotEnoughTimeAllocatedException, JsonProcessingException {
        Long employeurId = userDTO.id();

        if (offerExpireIn <= 0){
            throw new NotEnoughTimeAllocatedException("NotEnoughTimeAllocatedException");
        }

        Optional<JobOfferApplication> optJobApplication = jobOfferApplicationRepository.findById(jobApplicationId);
        Optional<Employeur> optEmployeur = employeurRepository.findById(employeurId);

        if(optEmployeur.isEmpty()){
            throw new UserNotFoundException("UserNotFoundException");
        }
        if(optJobApplication.isEmpty()){
            throw new JobNotFoundException("JobNotFoundException");
        }

        JobOfferApplication jobApplication = optJobApplication.get();
        Employeur employeur = optEmployeur.get();

        if(!Objects.equals(employeur.getId(), employeurId)){
            throw new MissingPermissionsExceptions("MissingPermissionsExceptions");
        }

        JobOffer jobOffer = jobApplication.getJobOffer();

        int acceptedInterns =
                internshipOfferRepository.countByJobOfferApplication_JobOfferAndConfirmationStatusIn(
                jobOffer, List.of(ApprovalStatus.WAITING, ApprovalStatus.ACCEPTED));

        if (acceptedInterns >= jobOffer.getNombreStagiaire()) {
            throw new MaxInternsReachedException("MaxInternsReachedException");
        }

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setJobOfferApplication(jobApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(offerExpireIn));
        internshipOffer.setConfirmationStatus(ApprovalStatus.WAITING);
        internshipOffer.setConfirmationDate(null);

        InternshipOffer internshipOfferSaved = internshipOfferRepository.save(internshipOffer);
        jobApplication.setInternshipOffer(internshipOfferSaved);
        jobOfferApplicationRepository.save(jobApplication);

        notificationService.tryFindViewNotification(userDTO,NotificationCode.NEW_APPLICANT,jobApplication.getId());

        UserDTO studentUserDTO = UserDTO.toDTO(jobApplication.getCurriculumVitae().getStudent());

        jobApplication.getJobInterviews().forEach(jobInterview -> {
            notificationService.tryFindViewNotification(userDTO,NotificationCode.INTERVIEW_CONFIRMED,jobInterview.getId());
            notificationService.tryFindViewNotification(studentUserDTO,NotificationCode.NEW_INTERVIEW,jobInterview.getId());
        });

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.INTERNSHIP_OFFER_RECEIVED)
                .userId(jobApplication.getStudentId())
                .session(jobApplication.getJobOffer().getSession())
                .referenceId(internshipOfferSaved.getId())
                .build());

        return InternshipOfferDTO.toDTO(internshipOfferSaved);
    }
    @Transactional
    public Page<InternshipOfferDTO> getMyInternshipOffers(Long id, Long sessionId, String query, int pageNumber, int pageSize) throws InternshipNotFoundException {
        Objects.requireNonNull(id);

        // verify student has been offered internship
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<InternshipOffer> intershipOffers = internshipOfferRepository.findInternshipOffersByStudentId(id, sessionId, query.toLowerCase(),pageable);
        if (intershipOffers.isEmpty()) {
            throw new InternshipNotFoundException("InternshipNotFound");
        }

        return intershipOffers.map(InternshipOfferDTO::toDTO);

    }
    @Transactional
    public InternshipOfferDTO getInternshipOffer(Long id) throws InternshipNotFoundException {
        return InternshipOfferDTO.toDTO(internshipOfferRepository.getInternshipOfferById(id).orElseThrow(InternshipNotFoundException::new));
    }
    @Transactional
    public InternshipOfferDTO getInternshipOfferFromJobOfferApplication(long applicationId) throws InternshipNotFoundException {
        return InternshipOfferDTO.toDTO(internshipOfferRepository.getInternshipOfferByJobOfferApplicationId(applicationId).orElseThrow(InternshipNotFoundException::new));
    }

    @Transactional
    public InternshipOfferDTO confirmInternshipOffer(Long id, String status, Long stuId) throws InternshipNotFoundException, MissingPermissionsExceptions, DateExpiredException, InternshipOfferAlreadyConfirmedException, JobNotFoundException {
        Objects.requireNonNull(status);

        // verify internship offer exist
        Optional<InternshipOffer> internshipOfferDB = internshipOfferRepository.findById(id);
        if (internshipOfferDB.isEmpty()) {
            throw new InternshipNotFoundException("Internship offer not found");
        }

        InternshipOffer internshipOffer = internshipOfferDB.get();

        // verify this intership offer belongs to student
        if (!internshipOffer.getJobOfferApplication().getCurriculumVitae().getStudent().getId().equals(stuId)) {
            throw new MissingPermissionsExceptions("This internship offer does not belong to this student");
        }

        // intership offer already has been confirmed
        if (internshipOffer.getConfirmationStatus() != ApprovalStatus.WAITING) {
            throw new InternshipOfferAlreadyConfirmedException("This internship offer has already been confirmed");
        }

        // verify this intership offer is still valid (confirmed offer before expiration date)
        if (internshipOffer.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new DateExpiredException("This internship offer has expired");
        }

        // confirm internship offer
        try {
            internshipOffer.setConfirmationStatus(ApprovalStatus.toEnum(status));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ApprovalStatus value: " + status);
        }
        internshipOffer.setConfirmationDate(LocalDateTime.now());

        // set confimationstatus
        internshipOffer.setConfirmationStatus(ApprovalStatus.toEnum(status));

        // if the number of confirmed interns reaches the maximum number of job offers, set the job offer invisible
        Optional<JobOffer> jobOffer = jobOfferRepository.findById(internshipOffer.getJobOfferApplication().getJobOffer().getId());
        if (jobOffer.isEmpty()) {
            throw new JobNotFoundException("Job offer not found");
        }

        int confirmedInterns = internshipOfferRepository.countByJobOfferApplication_JobOfferAndConfirmationStatusIn(
                jobOffer.get(), List.of(ApprovalStatus.ACCEPTED));
        if (confirmedInterns >= jobOffer.get().getNombreStagiaire())
            jobOffer.get().setActivated(false);
        JobOffer joDB = jobOfferRepository.save(jobOffer.get());
        internshipOffer.getJobOfferApplication().setJobOffer(joDB);

        InternshipOffer confirmedInternshipOffer = internshipOfferRepository.save(internshipOffer);
        Student student = confirmedInternshipOffer.getJobOfferApplication().getCurriculumVitae().getStudent();

        UserDTO studentUserDTO = UserDTO.toDTO(student);
        UserDTO employerUserDTO = UserDTO.toDTO(confirmedInternshipOffer.getJobOfferApplication().getJobOffer().getEmployeur());
        for (JobInterview jobInterview : internshipOffer.getJobOfferApplication().getJobInterviews()) {
            notificationService.tryFindViewNotification(studentUserDTO,NotificationCode.NEW_INTERVIEW,jobInterview.getId());
            notificationService.tryFindViewNotification(employerUserDTO,NotificationCode.INTERVIEW_CONFIRMED,jobInterview.getId());
        }

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.INTERNSHIP_OFFER_ACCEPTED)
                .userId(jobOffer.get().getEmployeur().getId())
                .session(confirmedInternshipOffer.getSession())
                .build());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.CONTRACT_TO_START)
                .filter(Role.PROGRAM_MANAGER)
                .session(confirmedInternshipOffer.getSession())
                .referenceId(confirmedInternshipOffer.getId())
                .build());
        return InternshipOfferDTO.toDTO(confirmedInternshipOffer);

    }

    public List<SessionDTO> getAcademicSessions() {
        List<Session> sessions = academicSessionRepository.findAll();
        return sessions.stream().map(SessionDTO::toDTO).toList();
    }

    public SessionDTO getSessionFromDB(String season, String year) throws SessionNotFoundException {
        List<Session> sessions = academicSessionRepository.findAll();
        for (Session s : sessions) {
            if (s.getSeason().equals(season) && s.getYear().equals(year)) {
                return SessionDTO.toDTO(s);
            }
        }
        throw new SessionNotFoundException("SessionNotFoundException");
    }

    @Transactional
    public Map<Long, JobOfferStatsDTO> getJobOffersStats(Long employerId, Long sessionId) {
        // get all job offers from employer
        List<JobOffer> jobOffers = jobOfferRepository.findByEmployeurId(employerId, sessionId);
        Map<Long, JobOfferStatsDTO> jobOffersStats = new HashMap<>();
        for (JobOffer jobOffer : jobOffers) {
            long totalNbApplications = jobOfferApplicationRepository.countByJobOfferId(jobOffer.getId(), sessionId);
            long nbInternsNeeded = jobOffer.getNombreStagiaire();
            List<InternshipOffer> internshipOffers = internshipOfferRepository.getInternshipOffersByJobOfferIdAndSessionId(jobOffer.getId(), sessionId);
            long nbInternshipOffersSent = internshipOffers.size();
            long nbInternshipOffersAccepted = internshipOffers.stream().filter(i -> i.getConfirmationStatus() == ApprovalStatus.ACCEPTED).count();

            JobOfferStatsDTO jobOfferStats = new JobOfferStatsDTO(totalNbApplications, nbInternsNeeded, nbInternshipOffersSent, nbInternshipOffersAccepted);
            jobOffersStats.put(jobOffer.getId(), jobOfferStats);
        }

        return jobOffersStats;
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
}


