package com.prose.service;


import com.prose.entity.*;
import com.prose.entity.users.*;
import com.prose.repository.*;
import com.prose.service.Exceptions.*;
import com.prose.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
class DemoService {
    private static final Logger logger = LoggerFactory.getLogger(JobOfferService.class);
    private final CurriculumVitaeService curriculumVitaeService;
    private final CurriculumVitaeRepository curriculumVitaeRepository;

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final EmployeurService employeurService;
    private final EmployeurRepository employeurRepository;

    private final JobOfferService jobOfferService;
    private final JobOfferRepository jobOfferRepository;
    private final TeacherService teacherService;

    private final TeacherRepository teacherRepository;

    private final ProgramManagerService programManagerService;
    private final ProgramManagerRepository programManagerRepository;
    private final AcademicSessionRepository academicSessionRepository;

    private final ContractService contractService;

    private final ScheduledTasksService scheduledTasksService;

    private final PasswordEncoder passwordEncoder;
    private final LoginService loginService;


    public DemoService(CurriculumVitaeService curriculumVitaeService, CurriculumVitaeRepository curriculumVitaeRepository,
                       StudentService studentService, StudentRepository studentRepository,
                       EmployeurService employeurService, EmployeurRepository employeurRepository,
                       JobOfferService jobOfferService, JobOfferRepository jobOfferRepository,
                       TeacherService teacherService, TeacherRepository teacherRepository, ProgramManagerService programManagerService,
                       ProgramManagerRepository programManagerRepository, AcademicSessionRepository academicSessionRepository, ContractService contractService, ScheduledTasksService scheduledTasksService, PasswordEncoder passwordEncoder, LoginService loginService) throws Exception {
        this.curriculumVitaeService = curriculumVitaeService;
        this.curriculumVitaeRepository = curriculumVitaeRepository;
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.employeurService = employeurService;
        this.employeurRepository = employeurRepository;
        this.jobOfferService = jobOfferService;
        this.jobOfferRepository = jobOfferRepository;
        this.teacherService = teacherService;
        this.teacherRepository = teacherRepository;
        this.programManagerService = programManagerService;
        this.programManagerRepository = programManagerRepository;
        this.academicSessionRepository = academicSessionRepository;
        this.contractService = contractService;
        this.scheduledTasksService = scheduledTasksService;
        this.passwordEncoder = passwordEncoder;
        this.loginService = loginService;
        doDemoService();
    }

    @Transactional
    protected void doDemoService() throws IOException, DisciplineNotFoundException, JobNotFoundException, InvalidUserFormatException, AlreadyExistsException, TooManyApplicationsExceptions, MissingPermissionsExceptions, MaxInternsReachedException, InternshipNotFoundException, DateExpiredException, InternshipOfferAlreadyConfirmedException, JobApplicationNotFoundException, InvalidBase64Exception {

        // blocking daily operations at startup
        this.scheduledTasksService.setShouldRunDailyOperationsAtStartup(false);

        // create a program manager
        ProgramManager programManager = new ProgramManager();
        programManager.setNom("Tremblay");
        programManager.setPrenom("Didier");
        programManager.setCredentials("gs@email.com",
                passwordEncoder.encode("123456"));
        programManager.setAdresse("1000 ici there");
        programManager.setTelephone("1234567890");
        programManager.setState(UserState.DEFAULT);
        programManagerRepository.save(programManager);
        UserDTO programManagerDTO = UserDTO.toDTO(programManager);

        StudentDTO studentDTO1 = studentService.createStudent(new
                StudentRegisterDTO("James", "Noah",
                "stu2@email.com",
                "3941, rue Cmabira",
                "5552659999",
                "123456",
                Discipline.ACCOUNTING.toString()));

         CurriculumVitaeDTO curriculumVitaeDTO1= curriculumVitaeService.addCV(
                loadMockMultipartFile(
                        "noah_cv.pdf",
                        "/cvsPDF/CV_Noah.pdf"),
                LocalDateTime.now(),
                studentDTO1.courriel()
        );

         curriculumVitaeService.validateCV(curriculumVitaeDTO1.id(),
                 ApprovalStatus.VALIDATED.toString(),
                 programManagerDTO);

        StudentDTO studentDTO2 = studentService.createStudent(new
                StudentRegisterDTO("Marien", "Nathan",
                "stu3@email.com",
                "3941, rue Cmabira",
                "5552659999",
                "123456",
                Discipline.INFORMATIQUE.toString()));

        CurriculumVitaeDTO curriculumVitaeDTO2= curriculumVitaeService.addCV(
                loadMockMultipartFile(
                        "Nathan_cv.pdf",
                        "/cvsPDF/CV_nathan.pdf"),
                LocalDateTime.now(),
                studentDTO2.courriel()
        );

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        curriculumVitaeService.addCV(
                loadMockMultipartFile(
                        "Nathan_cv_old.pdf",
                        "/cvsPDF/CV_nathan.pdf"),
                LocalDateTime.now().minusDays(1),
                studentDTO2.courriel()
        );
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        curriculumVitaeService.addCV(
                loadMockMultipartFile(
                        "Nathan_cv_older.pdf",
                        "/cvsPDF/CV_nathan.pdf"),
                LocalDateTime.now().minusDays(1),
                studentDTO2.courriel()
        );
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        curriculumVitaeService.addCV(
                loadMockMultipartFile(
                        "Nathan_cv_oldest.pdf",
                        "/cvsPDF/CV_nathan.pdf"),
                LocalDateTime.now().minusDays(1),
                studentDTO2.courriel()
        );
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        curriculumVitaeService.addCV(
                loadMockMultipartFile(
                        "Nathan_cv_even_more_old.pdf",
                        "/cvsPDF/CV_nathan.pdf"),
                LocalDateTime.now().minusDays(1),
                studentDTO2.courriel()
        );
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        curriculumVitaeService.addCV(
                loadMockMultipartFile(
                        "Nathan_cv_moreOld.pdf",
                        "/cvsPDF/CV_nathan.pdf"),
                LocalDateTime.now().minusDays(1),
                studentDTO2.courriel()
        );
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        curriculumVitaeService.validateCV(curriculumVitaeDTO2.id(),
                ApprovalStatus.VALIDATED.toString(),
                programManagerDTO);

        StudentDTO studentDTO3 = studentService.createStudent(new
                StudentRegisterDTO("Bing", "Shi",
                "stu4@email.com",
                "3941, rue Cmabira",
                "5552659999",
                "123456",
                Discipline.INFORMATIQUE.toString()));

        CurriculumVitaeDTO curriculumVitaeDTO3= curriculumVitaeService.addCV(
                loadMockMultipartFile(
                        "Shi_cv.pdf",
                        "/cvsPDF/CV_Bing.pdf"),
                LocalDateTime.now(),
                studentDTO3.courriel()
        );

        TeacherRegisterDTO teacherRegisterDTO = new TeacherRegisterDTO("Lacoursière", "François",
                "prof1@email.com ", "Montréal","1234567890", "123456",
                "informatique");
        TeacherDTO teacherDTO = teacherService.createTeacher(teacherRegisterDTO);

        TeacherRegisterDTO teacherRegisterDTO2 = new TeacherRegisterDTO("Lacoursièrette", "Francine",
                "prof2@email.com ", "Montréal","1234567890", "123456",
                "informatique");
        TeacherDTO teacherDTO2 = teacherService.createTeacher(teacherRegisterDTO2);

        curriculumVitaeService.validateCV(curriculumVitaeDTO3.id(),
                ApprovalStatus.VALIDATED.toString(),
                programManagerDTO);

        EmployeurDTO empDTO2 = employeurService.createEmployeur(new EmployeurRegisterDTO(
                "Équipe Microfix",
                "emp2@email.com",
                "Amanda",
                "Lachine",
                "K6S9V4",
                "2228974111",
                "5554444444",
                "123456",
                "123456"));

        EmployeurDTO empDTO3 = employeurService.createEmployeur(new EmployeurRegisterDTO(
                "Stelpro",
                "emp3@email.com",
                "Gregory",
                "Montréal",
                "K6S9V4",
                "2228974111",
                "5554444444",
                "123456",
                "123456"));

        EmployeurDTO empDTO4 = employeurService.createEmployeur(new EmployeurRegisterDTO(
                "Broadway Subway Constructors General Partnership",
                "emp4@email.com",
                "Pomerleau",
                "Montréal",
                "K6S9V4",
                "2228974111",
                "5554444444",
                "123456",
                "123456"));

         jobOfferService.createJobOffer(new JobOfferRegisterDTO(
                "Administrateur de systèmes",
                "2025-01-01",
                "2025-05-25",
                "1111 Rue Lapierre, LaSalle, QC H8N 2J4",
                "hybrid",
                3,
                20.5,
                32.0,
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                "" ),
                loadMockMultipartFile(
                        "jobOfferOffre555.pdf",
                        "/jobsPDF/job_admin_system.pdf"),
                empDTO2.courriel()
         );

        jobOfferService.createJobOffer(new JobOfferRegisterDTO(
                        "Technicien en informatique",
                        "2025-01-10",
                        "2025-04-12",
                        "2222 Rue Normand, Laval, QC H8N 2J4",
                        "office",
                        3,
                        20.5,
                        32.0,
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0),
                        "" ),
                loadMockMultipartFile(
                        "jobOfferOffre333.pdf",
                        "/jobsPDF/job_Technicien en Informatique.pdf"),
                empDTO3.courriel()
        );

        jobOfferService.createJobOffer(new JobOfferRegisterDTO(
                        "Systeme Analyst",
                        "2025-01-12",
                        "2025-05-02",
                        "3652 Rue Bourgeois, Laval, QC H8N 2J4",
                        "remote",
                        3,
                        20.5,
                        32.0,
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0),
                        "" ),
                loadMockMultipartFile(
                        "jobOfferOffre897.pdf",
                        "/jobsPDF/job_system analyst.pdf"),
                empDTO3.courriel()
        );

        jobOfferService.createJobOffer(new JobOfferRegisterDTO(
                        "Systeme Installation Coordinator",
                        "2025-01-21",
                        "2025-04-26",
                        "9032 Rue Sangevoix, Longueil, QC H8N 2J4",
                        "remote",
                        2,
                        31,
                        35.0,
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0),
                        "" ),
                loadMockMultipartFile(
                        "jobOfferOffre246.pdf",
                        "/jobsPDF/job_System Installation Coordinator.pdf"),
                empDTO4.courriel()
        );

        jobOfferService.createJobOffer(new JobOfferRegisterDTO(
                        "Java developper",
                        "2025-01-05",
                        "2025-03-19",
                        "6302 Rue Sangevoix, Longueil, QC H8N 2J4",
                        "remote",
                        2,
                        31,
                        35.0,
                        LocalTime.of(7, 0),
                        LocalTime.of(14, 0),
                        "" ),
                loadMockMultipartFile(
                        "jobOfferOffre246.pdf",
                        "/jobsPDF/job_Java developper.pdf"),
                empDTO2.courriel()
        );

         JobOfferDTO jobPython = jobOfferService.createJobOffer(new JobOfferRegisterDTO(
                        "Python Developper",
                        "2024-09-02",
                        "2024-12-02",
                        "1231 Rue Bordeau, Longueil, QC H8N 2J4",
                        "remote",
                        2,
                        31,
                        35.0,
                        LocalTime.of(7, 0),
                        LocalTime.of(14, 0),
                        "" ),
                loadMockMultipartFile(
                        "jobOfferOffre246.pdf",
                        "/jobsPDF/job_Python developper.pdf"),
                empDTO2.courriel()
        );

         JobPermissionDTO jobPermissionDTO = programManagerService.createJobPermission(jobPython.id(), new JobPermissionRegisterDTO(
                 new ArrayList<>(List.of("informatique")),
                 new ArrayList<>(),
                 LocalDate.of(2024, 12, 31),
                 true
         ), programManagerDTO);

         JobOfferApplicationDTO jobOfferAppDTO = jobOfferService.applyToJobOffer(jobPython.id(), curriculumVitaeDTO3.id(),
                 loginService.getUserByEmail(studentDTO3.courriel())
         );

        InternshipOfferDTO interDTO = jobOfferService.offerInternshipToStudent(jobOfferAppDTO.id(), loginService.getUserByEmail(empDTO2.courriel()), 30L);
        jobOfferService.confirmInternshipOffer(interDTO.id(), "accepted", studentDTO3.id());

        String signStudent = "iVBORw0KGgoAAAANSUhEUgAAAqMAAACHCAYAAAAiPVBhAAAAAXNSR0IArs4c6QAAIABJREFUeF7tnQnYVeMWx98Imd3okumWImNFxpQy19UkpDKH22QoktSlMpQhRChkKKVQ5jIlZVaJXFI3oTIkM3WR6T6/9z773H3Od853zj7f3ufs4b+e53uo9n73+/72/s5Ze71r/Ve1P//8808jEwEREIEEEHj++efNEUcckVrpgAEDzPDhwxOwci1RBERABMJLoJqc0fDeHM1MBETAXwJDhw41Q4YMSQ3atGlTM2PGDLPhhhv6eyGNJgIiIAIiUDABOaMFo9KBIiACUSbw22+/mbp165pPPvkkbRk4p4MHD47y0jR3ERABEYg0ATmjkb59mrwIiEChBB566CHTqVMne/jOO+9sli1bZtauXWvq169vlixZUugwOk4EREAERMBnAnJGfQaq4URABMJHgKhoo0aNzMKFC80WW2xhI6EDBw40P/30k53s8uXLzQ477BC+iWtGIiACIpAAAnJGE3CTtUQRSDqB0aNHm169elkMHTp0MP369TPNmjVLYZk6darp2LFj0jFp/SIgAiJQFgJyRsuCXRcVAREoFQGiorVr1zZfffWVveTEiRPNLrvsYvbbb7/UFEaMGGEuvPDCUk1J1xEBERABEXARkDOqx0EERCDWBMaPH29OO+00u8a2bduaxx57zLz77rumYcOGqXWfe+655uabb441By1OBERABMJKQM5oWO+M5iUCIuALgTZt2php06aZatWqmfnz55vGjRvbgiWio45169bN3HXXXb5cT4OIgAiIgAh4IyBn1BsvHS0CIhAhAqtWrTLbbrut+f333029evXMokWLTPXq1c37779vdt9999RKWrVqZZ566qkIrUxTFQEREIH4EJAzGp97qZWIgAhkELj77rvNmWeeaf+2b9++5oYbbrD/P2/evLSc0SZNmti/k4mACIiACJSegJzR0jPXFUVABEpE4JRTTjETJkwwW221lXnhhRfMnnvuaa/89ttvm7333js1i5122sksXbq0RLPSZURABERABNwE5IzqeRABEYglgV9//dXUqlXLfP/992bLLbc0X375pc0bxebOnWv233//1Lo333xz891338WSgxYlAiIgAmEnIGc07HdI8zMrVqwwjz76qHUs2rdvrz7ieiYKIjBz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";
        String signGS = "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";
        String signEmp = "iVBORw0KGgoAAAANSUhEUgAAAqMAAACHCAYAAAAiPVBhAAAAAXNSR0IArs4c6QAAIABJREFUeF7tnQnYVWP3/+9QNKMkot5UGjSgUFEZE0mDVAqVpEGmknrTK5S3TEWhRCE0UxJJI5IGQ6lEGihEkoQm1O/63O//nP8+5znPc6a9z/hd19UVPXvf970/+zz7rL3utb4r3+HDhw8bmQiIgAiIgAiIgAiIgAgkgUA+OaNJoK4pRUAEREAEREAEREAELAE5o/ogiIAIiIAIiIAIiIAIJI2AnNGkodfEIiACIiACIiACIiACckb1GRABERABERABERABEUgaATmjSUOviUVABERABERABERABOSM6jMgAiIgAiIgAiIgAiKQNAJyRpOGXhOLgAiIgAiIgAiIgAjIGdVnQAREQAREQAREQAREIGkE5IwmDb0mFgEREAEREAEREAERkDOqz4AIiIAIiIAIiIAIZDmBzz//3Bw4cMB8/PHHZt++feaoo44yW7duNd988405dOiQWbNmjdmzZ4/p1q2befDBB12lJWfUVZwaTAREQAREQAREQARSj8CyZcusc7lhwwbz3Xffmc2bN5uffvrJ4ISGszp16pjChQvbw+644w7TsmXLcKdE9XM5o1Hh0sEiIAIiIAIiIAIikJoEfv75Z/Pll1+aJUuW2Cjn0qVLzfr1663zGWw1atQwxx9/vClfvrwpV66c/TH/VqJECVOyZElTvXr1hF2knNGEodZEIiACIiACIiACIuAOgb1799ot9dWrV5vZs2ebXbt2mbVr15r9+/fbCYhkEtGsXLmyOfnkk03dunVN8eLF7d+pZnJGU+2OaD0iIAIiIAIiIAIiEESAPE6cz/nz55vFixeb999/338EUcxLL73UlC1b1px11lnm7LPPNsWKFUsbhnJG0+ZWaaEiIAIiIAIiIALZQmDbtm1mzpw55scff7SOJzmff/75p6lUqZLdWm/UqJG55JJLzHnnnZf2SOSMpv0t1AWIgAiIgAjEQoAq4fbt25tVq1bZPLkePXqYAQMGxDKUzhEBVwg888wzdtt97Nix/vHYaq9Xr545//zzTcOGDc1JJ53kylypNIic0VS6G1qLCIiACIiApwQOHjxonnrqKdOnT5+Q80ybNs20bt3a0zVocBGAADJJ48ePt1vuK1asMNu3b7dg6tevb2rXrm3O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";

        programManagerService.beginContract(interDTO.id(), programManagerDTO);

        contractService.sign(loginService.getUserByEmail(studentDTO3.courriel()), interDTO.id(), new SignatureBase64DTO(signStudent, "123456"));
        contractService.sign(loginService.getUserByEmail(empDTO2.courriel()), interDTO.id(), new SignatureBase64DTO(signEmp, "123456"));
        contractService.sign(programManagerDTO, interDTO.id(), new SignatureBase64DTO(signGS, "123456"));

        programManagerService.assignToProf(teacherDTO.id(), interDTO.id(), programManagerDTO);

    }

    private MultipartFile loadMockMultipartFile(String fileName, String filePath) throws IOException {
        return new MockMultipartFile(
                "file",
                fileName,
                "application/pdf",
                Objects.requireNonNull(DemoService.class.getResourceAsStream(filePath)).readAllBytes()
        );
    }

}
