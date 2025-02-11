package com.prose.service;

import com.prose.entity.InternshipEvaluation;
import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Student;
import com.prose.repository.InternshipEvaluationRepository;
import com.prose.service.dto.EmployeurDTO;
import com.prose.service.dto.TeacherDTO;
import com.prose.service.dto.UserDTO;
import lombok.Setter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class ScheduledTasksService {

    private final NotificationService notificationService;
    private final TeacherService teacherService;
    private final EmployeurService employerService;

    private final InternshipEvaluationRepository internshipEvaluationRepository;
    private final ProgramManagerService programManagerService;

    @Setter
    private boolean shouldRunDailyOperationsAtStartup = true;

    public ScheduledTasksService(TeacherService teacherService, InternshipEvaluationRepository internshipEvaluationRepository, NotificationService notificationService, EmployeurService employerService, ProgramManagerService programManagerService) {
        this.teacherService = teacherService;
        this.internshipEvaluationRepository = internshipEvaluationRepository;
        this.notificationService = notificationService;
        this.employerService = employerService;
        this.programManagerService = programManagerService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (shouldRunDailyOperationsAtStartup) {
            runDailyOperations();
        } else {
            System.out.println("Daily operations blocked from running at startup (probably by demoservice)");
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void runDailyOperations() {
        final Date date = new Date();
        System.out.println("Daily tasks executing at " + date);

        try {
            operationNotifyConcernedTeachersEvaluateInternshipEnvironment();
            operationNotifyConcernedEmployersEvaluateInterns();
        } catch (Exception e) {
            System.out.println("Error during daily tasks: " + e.getMessage());
        }
    }

    public void operationNotifyConcernedTeachersEvaluateInternshipEnvironment() {
        System.out.println("Notifying concerned teachers (if any) to evaluate internship environment (for internships starting today)");

        List<TeacherDTO> teachers = teacherService.getAllProfs();

        for (TeacherDTO teacher : teachers) {
            List<InternshipEvaluation> evaluations = internshipEvaluationRepository.findByTeacherId(teacher.id());

            for (InternshipEvaluation evaluation : evaluations) {
                // the pattern for startdate in joboffer is YYYY-MM-DD
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dateDebut = LocalDate.parse(evaluation.getInternshipOffer().getJobOfferApplication().getJobOffer().getDateDebut(), formatter);

                if (dateDebut.equals(LocalDate.now())) {
                    Student student = evaluation.getInternshipOffer().getJobOfferApplication().getCurriculumVitae().getStudent();

                    notificationService.addNotification(
                            Notification.builder().code(NotificationCode.INTERNSHIP_ENVIRONMENT_TO_REVIEW)
                                    .userId(teacher.id())
                                    .referenceId(evaluation.getId())
                                    .session(programManagerService.getCurrentAcademicSession())
                                    .build());
                    System.out.println("Notified teacher " + teacher.prenom() + " " + teacher.nom() + " to evaluate internship environment for student " + student.getPrenom() + " " + student.getNom());
                }
            }
        }

    }

    public void operationNotifyConcernedEmployersEvaluateInterns() {
        System.out.println("Notifying concerned employers (if any) to evaluate interns (for internships ending in 2 weeks exactly)");

        List<EmployeurDTO> employers = employerService.getAllEmployeurs();

        for (EmployeurDTO employer : employers) {
            List<InternshipEvaluation> evaluations = internshipEvaluationRepository.findCurrentEvaluationsByEmployerId(employer.id());

            for (InternshipEvaluation evaluation : evaluations) {
                // the pattern for startdate in joboffer is YYYY-MM-DD
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dateFin = LocalDate.parse(evaluation.getInternshipOffer().getJobOfferApplication().getJobOffer().getDateFin(), formatter);

                if (dateFin.equals(LocalDate.now().plusWeeks(2))) {
                    Student student = evaluation.getInternshipOffer().getJobOfferApplication().getCurriculumVitae().getStudent();

                    notificationService.addNotification(
                            Notification.builder().code(NotificationCode.INTERN_TO_REVIEW)
                                    .userId(employer.id())
                                    .referenceId(evaluation.getInternshipOffer().getId())
                                    .build());
                    System.out.println("Notified employer " + employer.nomCompagnie() + " to evaluate intern " + student.getPrenom() + " " + student.getNom());
                }
            }
        }
    }
}
