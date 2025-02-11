package com.prose.service;

import com.prose.entity.Discipline;
import com.prose.entity.InternshipEvaluation;
import com.prose.entity.Session;
import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Teacher;
import com.prose.entity.users.UserState;
import com.prose.repository.*;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.AlreadyExistsException;
import com.prose.service.Exceptions.InternshipNotFoundException;
import com.prose.service.Exceptions.InvalidPasswordException;
import com.prose.service.Exceptions.InvalidUserFormatException;
import com.prose.service.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.prose.service.dto.TeacherDTO.toDTO;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final InternshipEvaluationRepository internshipEvaluationRepository;
    private InternshipOfferRepository internshipOfferRepository;
    private StudentRepository studentRepository;
    private final AcademicSessionRepository academicSessionRepository;


    public TeacherService(TeacherRepository teacherRepository, PasswordEncoder passwordEncoder, NotificationService notificationService, InternshipEvaluationRepository internshipEvaluationRepository, AcademicSessionRepository academicSessionRepository) {
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.internshipEvaluationRepository = internshipEvaluationRepository;
        this.academicSessionRepository = academicSessionRepository;
    }


    public TeacherDTO createTeacher(TeacherRegisterDTO teacherRegisterDTO) throws InvalidUserFormatException, AlreadyExistsException {
        if (!verifyInfo(teacherRegisterDTO)) {
            throw new InvalidUserFormatException("Some informations are missing or invalid. Please check your informations.");
        }
        //TODO : HASH le mot de passe
        Teacher teacher = new Teacher();
        teacher.setNom(teacherRegisterDTO.nom().trim());
        teacher.setPrenom(teacherRegisterDTO.prenom().trim());
        teacher.setAdresse(teacherRegisterDTO.adresse().trim());
        teacher.setTelephone(teacherRegisterDTO.telephone().trim());
        teacher.setCredentials(teacherRegisterDTO.courriel().trim().toLowerCase(Locale.ROOT),passwordEncoder.encode(teacherRegisterDTO.mdp().trim()));
        teacher.setNotificationIdCutoff(notificationService.getLatestId());
        teacher.setState(UserState.TEACHER_DEFAULT);
        try {
            teacher.setDiscipline(Discipline.toEnum(teacherRegisterDTO.discipline().trim()));
        } catch (IllegalArgumentException e) {
            throw new InvalidUserFormatException("Enum not found");
        }

        try {
            TeacherDTO teacherDTO = toDTO(teacherRepository.save(teacher));

            notificationService.addNotification(Notification.builder()
                    .code(NotificationCode.USER_CREATED)
                    .userId(teacherDTO.id())
                    .build());

            return teacherDTO;
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsException();
        }
    }


    private boolean verifyInfo(TeacherRegisterDTO teacherRegisterDTO) {
        return !teacherRegisterDTO.prenom().trim().isEmpty() &&
                !teacherRegisterDTO.nom().trim().isEmpty() &&
                !teacherRegisterDTO.adresse().trim().isEmpty() &&
                teacherRegisterDTO.telephone().trim().matches("^\\d{10}$") &&
                teacherRegisterDTO.courriel().trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public TeacherDTO getTeacher(long id) {
        return toDTO(teacherRepository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    public TeacherDTO getTeacherByEmail(String email) {
        return toDTO(teacherRepository.findUserAppByEmail(email).orElseThrow(UserNotFoundException::new));
    }

    public List<TeacherDTO> getAllProfs() {
        List<Teacher> teachers = teacherRepository.findAll();
        if (teachers.isEmpty()) {
            throw new UserNotFoundException("No teachers found");
        }
        return teachers.stream().map(TeacherDTO::toDTO).toList();
    }

    // eq6-27
    public List<StudentDTO> getStudentsByTeacherId(long id) {
        // assert that teacher exists
        teacherRepository.findById(id).orElseThrow(UserNotFoundException::new);

        List<InternshipEvaluation> evaluations = internshipEvaluationRepository.findCurrentEvaluationsByTeacherId(id);

        return evaluations.stream()
                .map(evaluation -> StudentDTO.toDTO(evaluation.getInternshipOffer().getJobOfferApplication().getCurriculumVitae().getStudent()))
                .toList();
    }

    @Transactional
    public List<InternshipEvaluationDTO> getEmployeurByTeacher(long teacherId, long sessionId) {
        teacherRepository.findById(teacherId).orElseThrow(() -> new UserNotFoundException("Teacher not found"));
        Session currentSession = academicSessionRepository.getReferenceById(sessionId);

        List<InternshipEvaluation> evaluations = internshipEvaluationRepository.findByTeacherId(teacherId);
        List<InternshipEvaluation> evaluationsToSend = new ArrayList<>();

        for (InternshipEvaluation evaluation : evaluations) {
            Session offerSession = evaluation.getSession();
            if(offerSession.isPriorTo(currentSession)){
                evaluationsToSend.add(evaluation);
            }
        }
        if (evaluationsToSend.isEmpty()) {
            return new ArrayList<>();
        }
        return evaluations.stream()
                .map(InternshipEvaluationDTO::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeacherDTO updateTeacher(Long userId, TeacherDTO updateDTO) throws UserNotFoundException, InvalidPasswordException {
        Teacher teacher = teacherRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Teacher not found"));

        if (updateDTO.prenom() != null) {
            teacher.setPrenom(updateDTO.prenom());
        }
        if (updateDTO.nom() != null) {
            teacher.setNom(updateDTO.nom());
        }
        if (updateDTO.discipline() != null) {
            String disciplineVal = updateDTO.discipline().id();
            Discipline discipline = Discipline.toEnum(disciplineVal);
            teacher.setDiscipline(discipline);
        }
        if (updateDTO.courriel() != null) {
            teacher.getCredentials().setEmail(updateDTO.courriel());
        }
        if (updateDTO.telephone() != null) {
            teacher.setTelephone(updateDTO.telephone());
        }
        if (updateDTO.adresse() != null) {
            teacher.setAdresse(updateDTO.adresse());
        }

        Teacher updatedTeacher = teacherRepository.save(teacher);
        return TeacherDTO.toDTO(updatedTeacher);
    }

    @Transactional
    public TeacherDTO updateTeacherPassword(Long userId, UpdatePasswordDTO updatePasswordDTO) throws UserNotFoundException, InvalidPasswordException {
        Teacher teacher = teacherRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Teacher not found"));

        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), teacher.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect.");
        }
        teacher.getCredentials().setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        Teacher updatedTeacher = teacherRepository.save(teacher);
        return TeacherDTO.toDTO(updatedTeacher);
    }

}
