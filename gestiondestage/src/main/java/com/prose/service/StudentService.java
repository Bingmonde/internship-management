package com.prose.service;

import com.prose.entity.Discipline;
import com.prose.entity.InternshipOffer;
import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Student;
import com.prose.entity.users.UserState;
import com.prose.repository.EmployeurRepository;
import com.prose.repository.InternshipOfferRepository;
import com.prose.repository.JobInterviewRepository;
import com.prose.repository.StudentRepository;
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


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

import java.util.Objects;

import static com.prose.service.dto.StudentDTO.toDTO;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final JobInterviewRepository jobInterviewRepository;
    private final InternshipOfferRepository internshipOfferRepository;
    private final EmployeurRepository employeurRepository;

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    public StudentService(StudentRepository studentsRepository, PasswordEncoder passwordEncoder, JobInterviewRepository jobInterviewRepository, NotificationService notificationService, InternshipOfferRepository internshipOfferRepository, EmployeurRepository employeurRepository) {
        this.studentRepository = studentsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jobInterviewRepository = jobInterviewRepository;

//        Student etudiant = new Student();
//        etudiant.setNom("test");
//        etudiant.setPrenom("stu1");
//        etudiant.setAdresse("test");
//        etudiant.setTelephone("1234567890");
//        etudiant.setDiscipline(Discipline.INFORMATIQUE);
//        etudiant.setCredentials("stu1@email.com", passwordEncoder.encode("123456"));
//
//        Student etudiant2 = new Student();
//        etudiant2.setNom("test");
//        etudiant2.setPrenom("stu2");
//        etudiant2.setAdresse("test");
//        etudiant2.setTelephone("1234567890");
//        etudiant2.setDiscipline(Discipline.INFORMATIQUE);
//        etudiant2.setCredentials("stu2@email.com", passwordEncoder.encode("123456"));
//
//        studentRepository.save(etudiant);
//        studentRepository.save(etudiant2);

        this.notificationService = notificationService;
        this.internshipOfferRepository = internshipOfferRepository;
        this.employeurRepository = employeurRepository;
    }

    public StudentDTO createStudent(StudentRegisterDTO studentDTO) throws InvalidUserFormatException, AlreadyExistsException{
        if (!verifyInfo(studentDTO)) {
            throw new InvalidUserFormatException("Some informations are missing or invalid. Please check your informations.");
        }
        //TODO : HASH le mot de passe
        Student etudiant = new Student();
        etudiant.setNom(studentDTO.nom().trim());
        etudiant.setPrenom(studentDTO.prenom().trim());
        etudiant.setAdresse(studentDTO.adresse().trim());
        etudiant.setTelephone(studentDTO.telephone().trim());
        etudiant.setState(UserState.MISSING_CV);
        etudiant.setNotificationIdCutoff(notificationService.getLatestId());
        etudiant.setLastConnection(LocalDateTime.now());
        try {
            etudiant.setDiscipline(Discipline.toEnum(studentDTO.discipline()));
        } catch (IllegalArgumentException e) {
            throw new InvalidUserFormatException("Enum not found");
        }
        etudiant.setCredentials(studentDTO.courriel().trim().toLowerCase(Locale.ROOT), passwordEncoder.encode(studentDTO.mdp().trim()));

        try {
            StudentDTO studentDTO1 = toDTO(studentRepository.save(etudiant));

            notificationService.addNotification(Notification.builder()
                    .code(NotificationCode.USER_CREATED)
                    .userId(studentDTO1.id())
                    .build());

            return studentDTO1;
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsException();
        }
    }

    public StudentDTO getStudent(long id) {
//        return toDTO(studentRepository.findById(id).orElseThrow(UserNotFoundException::new));
        return toDTO(getStudentEntity(id));
    }

    public Student getStudentEntity(long id) {
        return studentRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public StudentDTO getStudentByEmail(String email) {
        return toDTO(studentRepository.findUserAppByEmail(email).orElseThrow(UserNotFoundException::new));
    }

    public Page<StudentDTO> getStudentsByDiscipline(String discipline, String query, int pageNumber, int pageSize) throws DisciplineNotFoundException {
        Objects.requireNonNull(discipline);
        if (!Discipline.isValidDiscipline(discipline)) {
            throw new DisciplineNotFoundException("Discipline not found");
        }
        Pageable pageable = PageRequest.of(pageNumber,pageSize);

        String escapedQuery = query.trim().toLowerCase(); //TODO : Escapes?

        Page<Student> students = studentRepository.findByDiscipline(Discipline.toEnum(discipline),escapedQuery,pageable);
        if (students.isEmpty()) {
            throw new UserNotFoundException("No students found for this discipline");
        }
        return students.map(StudentDTO::toDTO);
    }

    @Transactional
    public Page<InternshipOfferDTO> getContracts(long userid, long sessionId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<InternshipOffer> internshipList = internshipOfferRepository.findInternshipOfferWithContractStudent(userid,sessionId,pageable);
        return internshipList.map(InternshipOfferDTO::toDTO);
    }

    private boolean verifyInfo(StudentRegisterDTO etudiantDTO) {
        return  !etudiantDTO.prenom().trim().isEmpty() &&
                !etudiantDTO.nom().trim().isEmpty() &&
                etudiantDTO.telephone().trim().matches("^\\d{10}$") &&
                etudiantDTO.courriel().trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public Page<JobInterviewDTO> getJobInterviews(long id, long sessionId, String startDateString, String endDateString, String statusString, int pageNumber, int pageSize) throws JobNotFoundException, InvalidUserFormatException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        Pageable pageable = PageRequest.of(pageNumber,pageSize, Sort.by("interviewDate"));

        try {
            LocalDateTime startDate;
            LocalDateTime endDate;
            if (startDateString.trim().isEmpty()) {
                startDate = LocalDate.EPOCH.atTime(LocalTime.MIDNIGHT);
            } else {
                startDate = LocalDate.parse(startDateString, formatter).atTime(LocalTime.MIDNIGHT);
            }

            if (endDateString.trim().isEmpty()) {
                endDate = LocalDateTime.of(9999,12,31,23,59);
            } else {
                endDate = LocalDate.parse(endDateString, formatter).atTime(LocalTime.MAX);
            }

            return switch (statusString) {
                case "" ->
                        jobInterviewRepository.findJobInterviewByStudentIdPaged(id, sessionId, startDate, endDate, pageable).map(JobInterviewDTO::toDTO);
                case "confirmed" ->
                        jobInterviewRepository.findJobInterviewByStudentIdConfirmedByStudent(id, sessionId, startDate, endDate, true, pageable).map(JobInterviewDTO::toDTO);
                case "nonConfirmed" ->
                        jobInterviewRepository.findJobInterviewByStudentIdConfirmedByStudent(id, sessionId, startDate, endDate, false, pageable).map(JobInterviewDTO::toDTO);
                case "valide" ->
                        jobInterviewRepository.findJobInterviewByStudentIdNotCancelled(id, sessionId, startDate, endDate, pageable).map(JobInterviewDTO::toDTO);
                case "cancelled" ->
                        jobInterviewRepository.findJobInterviewByStudentIdCancelled(id, sessionId, startDate, endDate, pageable).map(JobInterviewDTO::toDTO);
                case null, default -> throw new InvalidUserFormatException();
            };

        } catch (DateTimeParseException e) {
            throw new InvalidUserFormatException();
        }
    }

    public void setLastConnection(long id) {
        Student student = studentRepository.findById(id).orElseThrow(UserNotFoundException::new);
        student.setLastConnection(LocalDateTime.now());
        studentRepository.save(student);
    }
    @Transactional
    public StudentDTO updateStudent(Long userId, StudentDTO updateDTO) throws UserNotFoundException{
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Student not found"));

        // 更新字段
        if (updateDTO.prenom() != null) student.setPrenom(updateDTO.prenom());
        if (updateDTO.nom() != null) student.setNom(updateDTO.nom());
        if (updateDTO.discipline() != null) {
            String disciplineVal = updateDTO.discipline().id();

            Discipline discipline = Discipline.toEnum(disciplineVal);
            student.setDiscipline(discipline);
        }        if (updateDTO.courriel() != null) student.getCredentials().setEmail(updateDTO.courriel());
        if (updateDTO.telephone() != null) student.setTelephone(updateDTO.telephone());
        if (updateDTO.adresse() != null) student.setAdresse(updateDTO.adresse());


        Student updatedStudent = studentRepository.save(student);


        return StudentDTO.toDTO(updatedStudent);
    }
    @Transactional
    public StudentDTO updateStudentPassword(Long userId, UpdatePasswordDTO updatePasswordDTO) throws UserNotFoundException, InvalidPasswordException {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Student not found"));

        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), student.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect.");
        }
        student.getCredentials().setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        Student updatedStudent = studentRepository.save(student);
        return StudentDTO.toDTO(updatedStudent);
    }

}
