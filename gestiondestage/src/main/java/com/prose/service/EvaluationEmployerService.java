package com.prose.service;

import com.prose.entity.EvaluationEmployer;
import com.prose.entity.InternshipEvaluation;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Employeur;
import com.prose.repository.EmployeurRepository;
import com.prose.repository.EvaluationEmployerRepository;
import com.prose.repository.InternshipEvaluationRepository;
import com.prose.service.Exceptions.InternshipNotFoundException;
import com.prose.service.Exceptions.InvalidBase64Exception;
import com.prose.service.dto.EvaluationEmployerDTO;
import com.prose.service.dto.SignatureBase64DTO;
import com.prose.service.dto.UserDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Optional;

import static io.jsonwebtoken.impl.security.EdwardsCurve.findById;

@Service
public class EvaluationEmployerService {

    @Autowired
    private EvaluationEmployerRepository evaluationEmployerRepository;

    @Autowired
    private EmployeurRepository employerRepository;

    @Autowired
    private InternshipEvaluationRepository internshipEvaluationRepository;
    @Autowired
    private NotificationService notificationService;


    @Transactional
    public EvaluationEmployer createEvaluation(long id, EvaluationEmployerDTO evaluationEmployerDTO, UserDTO userDTO) throws Exception {

        Long teacherId = userDTO.id();
        Optional<InternshipEvaluation> internshipEvaluation = internshipEvaluationRepository.findById(id);
        if (internshipEvaluation.isEmpty()) {
            throw new InternshipNotFoundException("Internship evaluation not found with ID: " + id);
        }

        Optional<Employeur> employerOpt = employerRepository.findById(evaluationEmployerDTO.employerId());
        if (employerOpt.isEmpty()) {
            throw new Exception("Employer not found with ID: " + evaluationEmployerDTO.employerId());
        }
        Employeur employeur = employerOpt.get();

        if (evaluationEmployerDTO.firstMonthHours() == null || evaluationEmployerDTO.firstMonthHours().trim().isEmpty()) {
            throw new Exception("Hours cannot be null or empty");
        }
        if (evaluationEmployerDTO.secondMonthHours() == null || evaluationEmployerDTO.secondMonthHours().trim().isEmpty()) {
            throw new Exception("Hours cannot be null or empty");
        }
        if (evaluationEmployerDTO.thirdMonthHours() == null || evaluationEmployerDTO.thirdMonthHours().trim().isEmpty()) {
            throw new Exception("Hours cannot be null or empty");
        }

        // Validate that hours are non-negative after confirming they are not null or empty
        if (Integer.parseInt(evaluationEmployerDTO.firstMonthHours()) < 0) {
            throw new Exception("Invalid hours for the first month");
        }
        if (Integer.parseInt(evaluationEmployerDTO.secondMonthHours()) < 0) {
            throw new Exception("Invalid hours for the second month");
        }
        if (Integer.parseInt(evaluationEmployerDTO.thirdMonthHours()) < 0) {
            throw new Exception("Invalid hours for the third month");
        }

        byte[] signaturePNG = Base64.getDecoder().decode(evaluationEmployerDTO.signatureDTO());

        if (ImageIO.read(new ByteArrayInputStream(signaturePNG)) == null) {
            throw new InvalidBase64Exception();
        }

        // Check date format
        try {
            LocalDate.parse(evaluationEmployerDTO.date());
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid date format");
        }

        // Check preferred stage
        if (evaluationEmployerDTO.preferredStage() == null) {
            throw new Exception("Preferred stage is required");
        }

        // Proceed with setting other fields and saving the entity
        EvaluationEmployer evaluationEmployer = new EvaluationEmployer();
        evaluationEmployer.setEmployeur(employeur);
        evaluationEmployer.setStageNumber(evaluationEmployerDTO.stageNumber());
        evaluationEmployer.setFirstMonthHours(evaluationEmployerDTO.firstMonthHours());
        evaluationEmployer.setSecondMonthHours(evaluationEmployerDTO.secondMonthHours());
        evaluationEmployer.setThirdMonthHours(evaluationEmployerDTO.thirdMonthHours());
        evaluationEmployer.setPreferredStage(evaluationEmployerDTO.preferredStage());
        evaluationEmployer.setNumberOfInterns(evaluationEmployerDTO.numberOfInterns());
        evaluationEmployer.setWillingToRehire(evaluationEmployerDTO.willingToRehire());
        evaluationEmployer.setSchedule1Start(evaluationEmployerDTO.schedule1Start());
        evaluationEmployer.setSchedule1End(evaluationEmployerDTO.schedule1End());
        evaluationEmployer.setSchedule2Start(evaluationEmployerDTO.schedule2Start());
        evaluationEmployer.setSchedule2End(evaluationEmployerDTO.schedule2End());
        evaluationEmployer.setSchedule3Start(evaluationEmployerDTO.schedule3Start());
        evaluationEmployer.setSchedule3End(evaluationEmployerDTO.schedule3End());
        evaluationEmployer.setComments(evaluationEmployerDTO.comments());
        evaluationEmployer.setObservations(evaluationEmployerDTO.observations());
//        evaluationEmployer.setTeacherSignature(evaluationEmployerDTO.teacherSignature());
        evaluationEmployer.setDate(evaluationEmployerDTO.date());

        // Set evaluation parts
        evaluationEmployer.setTasksMetExpectations(evaluationEmployerDTO.tasksMetExpectations());
        evaluationEmployer.setIntegrationSupport(evaluationEmployerDTO.integrationSupport());
        evaluationEmployer.setSupervisionSufficient(evaluationEmployerDTO.supervisionSufficient());
        evaluationEmployer.setWorkEnvironment(evaluationEmployerDTO.workEnvironment());
        evaluationEmployer.setWorkClimate(evaluationEmployerDTO.workClimate());
        evaluationEmployer.setAccessibleTransport(evaluationEmployerDTO.accessibleTransport());
        evaluationEmployer.setSalaryInteresting(evaluationEmployerDTO.salaryInteresting());
        evaluationEmployer.setCommunicationWithSupervisor(evaluationEmployerDTO.communicationWithSupervisor());
        evaluationEmployer.setEquipmentAdequate(evaluationEmployerDTO.equipmentAdequate());
        evaluationEmployer.setWorkloadAcceptable(evaluationEmployerDTO.workloadAcceptable());
        evaluationEmployer.setTeacherSignImage(signaturePNG);
        // set signature time
        LocalDateTime now = LocalDateTime.now();
        evaluationEmployer.setDate(now.toString());

        EvaluationEmployer savedEvaluationEmployer = evaluationEmployerRepository.save(evaluationEmployer);
        internshipEvaluation.get().setEvaluationEmployer(savedEvaluationEmployer);

        internshipEvaluationRepository.save(internshipEvaluation.get());
        notificationService.tryFindViewNotification(userDTO, NotificationCode.INTERNSHIP_ENVIRONMENT_TO_REVIEW,savedEvaluationEmployer.getId());

        // Save to the database
        return savedEvaluationEmployer;
    }

}
