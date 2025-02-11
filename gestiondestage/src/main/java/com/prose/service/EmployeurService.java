package com.prose.service;

import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Employeur;
import com.prose.entity.*;
import com.prose.entity.users.Teacher;
import com.prose.entity.users.UserState;
import com.prose.repository.*;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.*;
import com.prose.service.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.prose.service.dto.EmployeurDTO.toDTO;

@Service
public class EmployeurService {

    private final EmployeurRepository employeurRepository;
    private final PasswordEncoder passwordEncoder;

    private final JobOfferApplicationRepository jobOfferApplicationRepository;
    private final JobInterviewRepository jobInterviewRepository;

    private final NotificationService notificationService;
    private final InternshipOfferRepository internshipOfferRepository;
    private final InternshipEvaluationRepository internshipEvaluationRepository;
    private final EvaluationInternRepository evaluationInternRepository;
    private final AcademicSessionRepository academicSessionRepository;


    public EmployeurService(EmployeurRepository employeurRepository, PasswordEncoder passwordEncoder, JobOfferApplicationRepository jobOfferApplicationRepository, JobInterviewRepository jobInterviewRepository, NotificationService notificationService, InternshipOfferRepository internshipOfferRepository, InternshipEvaluationRepository internshipEvaluationRepository, EvaluationInternRepository evaluationInternRepository, AcademicSessionRepository academicSessionRepository) {
        this.employeurRepository = employeurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jobOfferApplicationRepository = jobOfferApplicationRepository;
        this.jobInterviewRepository = jobInterviewRepository;
        this.notificationService = notificationService;
        this.internshipEvaluationRepository = internshipEvaluationRepository;
        this.evaluationInternRepository = evaluationInternRepository;
        this.academicSessionRepository = academicSessionRepository;
        System.out.println("Creating employeur");
//        Employeur employeur = new Employeur();
//        employeur.setNomCompagnie("Compagnie");
//        employeur.setAdresse("123 rue de la compagnie");
//        employeur.setTelephone("1234567890");
//        employeur.setCredentials("emp@email.com",passwordEncoder.encode("123456"));
//        employeurRepository.save(employeur);
        this.internshipOfferRepository = internshipOfferRepository;
    }

    public EmployeurDTO createEmployeur(EmployeurRegisterDTO employeurRegisterDTO) throws InvalidUserFormatException, AlreadyExistsException {
        if (!verifyInfo(employeurRegisterDTO)) {
            throw new InvalidUserFormatException("Some informations are missing or invalid. Please check your informations.");
        }

        Employeur employeur = new Employeur();
        employeur.setNomCompagnie(employeurRegisterDTO.nomCompagnie().trim());
        employeur.setCity(employeurRegisterDTO.city().trim());
        employeur.setPostalCode(employeurRegisterDTO.postalCode().trim());
        employeur.setFax(employeurRegisterDTO.fax().trim());
        employeur.setAdresse(employeurRegisterDTO.adresse().trim());
        employeur.setTelephone(employeurRegisterDTO.telephone().trim());
        employeur.setContactPerson(employeurRegisterDTO.contactPerson().trim());
        employeur.setCredentials(employeurRegisterDTO.courriel().trim().toLowerCase(Locale.ROOT),passwordEncoder.encode(employeurRegisterDTO.mdp().trim()));
        employeur.setState(UserState.DEFAULT);
        employeur.setNotificationIdCutoff(notificationService.getLatestId());

        try {
            EmployeurDTO employeurDTO = toDTO(employeurRepository.save(employeur));
            notificationService.addNotification(Notification.builder()
                    .code(NotificationCode.USER_CREATED)
                    .userId(employeurDTO.id())
                    .build());
            return employeurDTO;
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsException();
        }
    }

    public EmployeurDTO getEmployeur(long id) {
        return toDTO(employeurRepository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    public EmployeurDTO getEmployeurByEmail(String email) {
        return toDTO(employeurRepository.findUserAppByEmail(email).orElseThrow(UserNotFoundException::new));
    }

    public List<EmployeurDTO> getAllEmployeurs() {
        List<Employeur> employeurs = employeurRepository.findAll();
        /*if (employeurs.isEmpty()) {
            throw new UserNotFoundException("No employers found");
        }*/
        return employeurs.stream().map(EmployeurDTO::toDTO).toList();
    }

    private boolean verifyInfo(EmployeurRegisterDTO employeurRegisterDTO) {
        return !employeurRegisterDTO.nomCompagnie().trim().isEmpty() && !employeurRegisterDTO.adresse().trim().isEmpty() && employeurRegisterDTO.courriel().trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") && employeurRegisterDTO.telephone().trim().matches("^\\d{10}$");
    }

    @Transactional
    public JobInterviewDTO createJobInterview(JobInterviewRegisterDTO jobInterviewRegisterDTO, UserDTO userDTO)
            throws InvalideInterviewTypeException, JobApplicationNotActiveException, MissingPermissionsExceptions, JobApplicationNotFoundException {
        Objects.requireNonNull(jobInterviewRegisterDTO);
        Objects.requireNonNull(userDTO);
        Objects.requireNonNull(jobInterviewRegisterDTO);

        Long employerId = userDTO.id();

        // date time formate is correct
        LocalDateTime interviewDate;
        try {
//            ZonedDateTime zonedDateTime = ZonedDateTime.parse(jobInterviewRegisterDTO.interviewDate().trim());
//            interviewDate = zonedDateTime.toLocalDateTime();
            interviewDate = LocalDateTime.parse(jobInterviewRegisterDTO.interviewDate().trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date time format");
        }
//        if (interviewDate.isBefore(LocalDateTime.now().plusDays(2))) {
//            throw new InvalideDateTimeException("Interview date must be at least 48 hours from now");
//        }

        // interview type is valid
        InterviewType interviewType;
        try {
            interviewType = InterviewType.toEnum(jobInterviewRegisterDTO.interviewType().trim());
        } catch (Exception e) {
            throw new InvalideInterviewTypeException("Invalid interview type");
        }

        // job application is already created and active
        Optional<JobOfferApplication> jobOfferApplication = jobOfferApplicationRepository.findById(jobInterviewRegisterDTO.jobOfferApplicationId());
        if (jobOfferApplication.isEmpty())
            throw new JobApplicationNotFoundException("Job application not found");

        if (!jobOfferApplication.get().isActive())
            throw new JobApplicationNotActiveException("Job application is not active");

        // job application doesn't belong to employer
        if (!jobOfferApplication.get().getJobOffer().getEmployeur().getId().equals(employerId)) {
            throw new MissingPermissionsExceptions("Job application does not belong to you");
        }

        // create job interview
        JobInterview jobInterview = new JobInterview();
        jobInterview.setInterviewDate(interviewDate);
        jobInterview.setInterviewType(interviewType);
        jobInterview.setInterviewLocationOrLink(jobInterviewRegisterDTO.interviewLocationOrLink());
        jobInterview.setJobOfferApplication(jobOfferApplication.get());
        jobOfferApplication.get().getJobInterviews().add(jobInterview);
        jobInterview.setConfirmedByStudent(false);
        jobInterview.setCreationDate(LocalDateTime.now());

        JobInterview savedJobInterview = jobInterviewRepository.save(jobInterview);
        jobOfferApplicationRepository.save(jobOfferApplication.get());

        notificationService.tryFindViewNotification(userDTO,NotificationCode.NEW_APPLICANT,jobOfferApplication.get().getId());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.NEW_INTERVIEW)
                .userId(savedJobInterview.getStudentId())
                .session(savedJobInterview.getJobOffer().getSession())
                .referenceId(savedJobInterview.getId())
                .build());

        return JobInterviewDTO.toDTO(savedJobInterview);

    }

    public Page<JobInterviewDTO> getJobInterview(long employeeId, long sessionId, String startDateString, String endDateString, String status, int pageNumber, int pageSize) throws InvalidUserFormatException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        Pageable pageable = PageRequest.of(pageNumber,pageSize, Sort.by("interviewDate"));

        try {
            LocalDateTime startDate;
            LocalDateTime endDate;
            if (startDateString.trim().isEmpty()) {
                startDate = LocalDate.EPOCH.atTime(0,0);
            } else {
                startDate = LocalDate.parse(startDateString, formatter).atTime(LocalTime.MIDNIGHT);
            }

            if (endDateString.trim().isEmpty()) {
                endDate = LocalDateTime.of(9999,12,31,23,59);
            } else {
                endDate = LocalDate.parse(endDateString, formatter).atTime(LocalTime.MAX);
            }

            return switch (status) {
                case "" ->
                    jobInterviewRepository.findJobInterviewByEmployerId(employeeId,sessionId,startDate,endDate,pageable).map(JobInterviewDTO::toDTO);
                case "confirmed" ->
                        jobInterviewRepository.findJobInterviewByEmployerIdConfirmedByStudent(employeeId,sessionId,startDate,endDate,true,pageable).map(JobInterviewDTO::toDTO);
                case "nonConfirmed" ->
                        jobInterviewRepository.findJobInterviewByEmployerIdConfirmedByStudent(employeeId,sessionId,startDate,endDate,false,pageable).map(JobInterviewDTO::toDTO);
                case "valide" ->
                        jobInterviewRepository.findJobInterviewByEmployerIdNotCancelled(employeeId,sessionId,startDate,endDate,pageable).map(JobInterviewDTO::toDTO);
                case "cancelled" ->
                        jobInterviewRepository.findJobInterviewByEmployerIdCancelled(employeeId,sessionId,startDate,endDate,pageable).map(JobInterviewDTO::toDTO);
                case null, default -> throw new InvalidUserFormatException();
            };
        } catch (DateTimeParseException e) {
            throw new InvalidUserFormatException();
        }
    }

    public JobInterviewDTO cancelInterview(long interviewId,long employeeId) throws InterviewNotFoundException, MissingPermissionsExceptions, AlreadyExistsException {
        JobInterview interview = jobInterviewRepository.findById(interviewId).orElseThrow(InterviewNotFoundException::new);

        if (!interview.getJobOfferApplication().getJobOffer().getEmployeur().getId().equals(employeeId)) {
            throw new MissingPermissionsExceptions();
        }

        if (interview.getCancelledDate() != null) {
            throw new AlreadyExistsException();
        }

        interview.setCancelledDate(LocalDateTime.now());
        return JobInterviewDTO.toDTO(jobInterviewRepository.save(interview));
    }

    @Transactional
    public Page<InternshipOfferDTO> getContracts(long userid, long sessionId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<InternshipOffer> internshipList = internshipOfferRepository.findInternshipOfferWithContractEmployer(userid,sessionId, pageable);
        return internshipList.map(InternshipOfferDTO::toDTO);
    }

//    public List<StudentDTO> getStudentsByEmployerId(long id) { TODO: has no usage -Abutolib
//        employeurRepository.findById(id).orElseThrow(UserNotFoundException::new);
//
//        System.out.println("Fetching evaluations for employer ID: " + id);
//
//        List<InternshipEvaluation> evaluations = internshipEvaluationRepository.findCurrentEvaluationsByEmployerId(id);
//
//        System.out.println("Evaluations found: " + evaluations.size());
//
//        return evaluations.stream()
//                .map(evaluation -> {
//                    StudentDTO studentDTO = StudentDTO.toDTO(
//                            evaluation.getInternshipOffer()
//                                    .getJobOfferApplication()
//                                    .getCurriculumVitae()
//                                    .getStudent());
//                    System.out.println("Student found: " + studentDTO);
//                    return studentDTO;
//                })
//                .collect(Collectors.toList());
//    }
    public List<InternshipOfferDTO> getStudentsWithOffersByEmployerId(long employerId, long sessionId) {
        Session currentSession = academicSessionRepository.getReferenceById(sessionId);
        
        List<InternshipOffer> internshipOffers = internshipOfferRepository.findByEmployerId(employerId);
        List<InternshipOffer> internshipToSend = new ArrayList<>();
        
        for (InternshipOffer offer : internshipOffers) {
            Session offerSession = offer.getSession();
            if(offerSession.isPriorTo(currentSession) && offer.contractIsSigned()){
                internshipToSend.add(offer);
            }
        }
        if(internshipToSend.isEmpty()){
            return new ArrayList<>();
        }
        return internshipToSend.stream()
                .map(InternshipOfferDTO::toDTO) 
                .collect(Collectors.toList());
    }

    @Transactional
    public EvaluationInternDTO saveEvaluation(long internshipOfferId, EvaluationInternDTO evaluationDTO, UserDTO userDTO) throws InvalidBase64Exception, IOException, InternshipNotFoundException {
        if (evaluationDTO == null) {
            throw new IllegalArgumentException("Evaluation data cannot be null");
        }

        EvaluationIntern evaluation = new EvaluationIntern();

        Employeur employeur = employeurRepository.findById(userDTO.id()).orElseThrow(UserNotFoundException::new);
        evaluation.setEmployeur(employeur);

        InternshipEvaluation internshipEvaluation = internshipEvaluationRepository.findByInternshipOfferId(internshipOfferId)
                .orElseThrow(() -> new InternshipNotFoundException("Internship not found"));

        evaluation.setName(evaluationDTO.getName());
        evaluation.setDate(evaluationDTO.getDate());
        evaluation.setSupervisorName(evaluationDTO.getSupervisorName());
        evaluation.setCompanyName(evaluationDTO.getCompanyName());
        evaluation.setTelephone(evaluationDTO.getTelephone());

        evaluation.setProductivityEvaluation(evaluationDTO.getProductivityEvaluation());
        evaluation.setProductivityComments(evaluationDTO.getProductivityComments());

        evaluation.setQualityOfWorkEvaluation(evaluationDTO.getQualityOfWorkEvaluation());
        evaluation.setQualityOfWorkComments(evaluationDTO.getQualityOfWorkComments());

        evaluation.setInterpersonalRelationshipsEvaluation(evaluationDTO.getInterpersonalRelationshipsEvaluation());
        evaluation.setInterpersonalRelationshipsComments(evaluationDTO.getInterpersonalRelationshipsComments());

        evaluation.setPersonalSkillsEvaluation(evaluationDTO.getPersonalSkillsEvaluation());
        evaluation.setPersonalSkillsComments(evaluationDTO.getPersonalSkillsComments());

        evaluation.setOverallAppreciation(evaluationDTO.getOverallAppreciation());
        evaluation.setOverallComments(evaluationDTO.getOverallComments());
        evaluation.setProgram(evaluationDTO.getProgram());
        evaluation.setEvaluationDiscussedWithIntern(evaluationDTO.isEvaluationDiscussedWithIntern());
        evaluation.setSupervisionHoursPerWeek(evaluationDTO.getSupervisionHoursPerWeek());
        evaluation.setWillingnessToRehire(evaluationDTO.getWillingnessToRehire());
        evaluation.setTechnicalTrainingComments(evaluationDTO.getTechnicalTrainingComments());
        evaluation.setFunction(evaluationDTO.getFunction());
        evaluation.setReturnFormToEmail(evaluationDTO.getReturnFormToEmail());
        evaluation.setName(evaluationDTO.getName());
        evaluation.setReturnFormToName(evaluationDTO.getReturnFormToName());

        byte[] signaturePNG = Base64.getDecoder().decode(evaluationDTO.getEmployerSignature());

        if (ImageIO.read(new ByteArrayInputStream(signaturePNG)) == null) {
            throw new InvalidBase64Exception();
        }
        evaluation.setEmployerSignature(signaturePNG);

        // 保存 EvaluationIntern 实体以获取生成的 ID
        EvaluationIntern savedEvaluation = evaluationInternRepository.save(evaluation);
        internshipEvaluation.setEvaluationIntern(savedEvaluation);
        internshipEvaluationRepository.save(internshipEvaluation);

        notificationService.tryFindViewNotification(userDTO,NotificationCode.INTERN_TO_REVIEW,internshipOfferId);

        // 将保存的 EvaluationIntern 转换为 DTO 返回
        return EvaluationInternDTO.toDTO(savedEvaluation);
    }

//    private Session checkIfNextExist(Session session){ TODO: No usage -Abutolib
//        Session nextSession = new Session();
//        nextSession.setYear(String.valueOf(Integer.parseInt(session.getYear()) + 1));
//        nextSession.getNextSeason();
//        List<Session> sessions = academicSessionRepository.findAll();
//        for (Session s : sessions){
//            if (s.getYear().equals(nextSession.getYear()) && s.getSeason().equals(nextSession.getSeason())){
//                return s;
//            }
//        }
//        return academicSessionRepository.save(nextSession);
//    }
    @Transactional
    public EmployeurDTO updateEmployeur(Long userId, EmployeurDTO employeurUpdate) throws UserNotFoundException {
        Employeur employeur = employeurRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Employeur not found"));

        if (employeurUpdate.nomCompagnie() != null) {
            employeur.setNomCompagnie(employeurUpdate.nomCompagnie());
        }
        if (employeurUpdate.contactPerson() != null) {
            employeur.setContactPerson(employeurUpdate.contactPerson());
        }
        if (employeurUpdate.adresse() != null) {
            employeur.setAdresse(employeurUpdate.adresse());
        }
        if (employeurUpdate.city()!= null) {
            employeur.setCity(employeurUpdate.city());
        }
        if (employeurUpdate.postalCode()!= null) {
            employeur.setPostalCode(employeurUpdate.postalCode());
        }
        if (employeurUpdate.telephone()!= null) {
            employeur.setTelephone(employeurUpdate.telephone());
        }
        if (employeurUpdate.fax()!= null) {
            employeur.setFax(employeurUpdate.fax());
        }
        employeur = employeurRepository.save(employeur);
        return EmployeurDTO.toDTO(employeur);
    }
    @Transactional
    public EmployeurDTO updateEmployerPassword(Long userId, UpdatePasswordDTO updatePasswordDTO) throws UserNotFoundException, InvalidPasswordException {
        Employeur employer = employeurRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Employer not found"));

        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), employer.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect.");
        }
        employer.getCredentials().setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        Employeur updatedEmployer = employeurRepository.save(employer);
        return EmployeurDTO.toDTO(updatedEmployer);
    }

}