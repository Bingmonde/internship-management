package com.prose.service;

import com.prose.entity.*;
import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.Student;
import com.prose.entity.users.auth.Role;
import com.prose.repository.CurriculumVitaeRepository;
import com.prose.repository.PDFDocuRepository;
import com.prose.repository.StudentRepository;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.CVNotFoundException;
import com.prose.service.Exceptions.DisciplineNotFoundException;
import com.prose.service.Exceptions.FileNotFoundException;
import com.prose.service.dto.CurriculumVitaeDTO;
import com.prose.service.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CurriculumVitaeService {

    private static final Logger logger = LoggerFactory.getLogger(JobOfferService.class);

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private final CurriculumVitaeRepository curriculumVitaeRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final PDFDocuRepository pdfDocuRepository;
    private final NotificationService notificationService;



    public CurriculumVitaeService(CurriculumVitaeRepository curriculumVitaeRepository, StudentRepository studentRepository, StudentService studentService, PDFDocuRepository pdfDocuRepository, PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.curriculumVitaeRepository = curriculumVitaeRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.pdfDocuRepository = pdfDocuRepository;
        this.notificationService = notificationService;
    }

    // pour ajouter un CV, on passe un fichier pdf, la date actuelle, puis l'email de l'étudiant (pas pris la peine de créer un cvRegisterDTO pour ça)
    public CurriculumVitaeDTO addCV(MultipartFile uploadFile, LocalDateTime dateHeureAjout, String studentEmail) throws IOException, IllegalArgumentException {
        logger.info("Creating CV for student with email: {}", studentEmail);

        Student student = studentRepository.findUserAppByEmail(studentEmail)
                .orElseThrow(() -> {
                    logger.error("Student with email {} not found", studentEmail);
                    return new UserNotFoundException("Student with email " + studentEmail + " not found");
                });

        String userName = student.getPrenom() + " " + student.getNom();

        if (dateHeureAjout == null) {
            logger.error("No publication datetime passed for {}'s CV", userName);
            throw new IllegalArgumentException("CV publication datetime cannot be null");
        }

        CurriculumVitae cv = new CurriculumVitae();

        cv.setDateHeureAjout(dateHeureAjout);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.WAITING);

        if (uploadFile == null || uploadFile.isEmpty()) {
            logger.error("Empty file or no file uploaded for {}'s CV", userName);
            throw new IOException("Empty file or no file uploaded");
        }

        logger.info("Processing file upload for {}'s CV", userName);

        if (!uploadFile.getContentType().equals("application/pdf")) {
            logger.error("Invalid file type for for {}'s CV, expected PDF", userName);
            throw new IOException("Invalid file type, only PDF files are allowed");
        }

        if (uploadFile.getSize() > MAX_FILE_SIZE) {
            logger.error("File too large for for {}'s CV", userName);
            throw new IOException("File is too large, maximum allowed size is " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
        }

        String fileName = "CV_" + student.getPrenom() + "_" + student.getNom() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyy-MM-dd_HHmmss")) + ".pdf";

        // only for simulated testing
        if (pdfDocuRepository.findByFileName(fileName).isPresent()) {
            fileName = "CV_" + student.getPrenom() + "_" + student.getNom() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyy-MM-dd_HHmmss")) + "1.pdf";
        }

        PDFDocu pdfDocu = savePDF(uploadFile, fileName);
        cv.setPdfDocu(pdfDocu);

        CurriculumVitae savedCV = curriculumVitaeRepository.save(cv);
        logger.info("Student CV created successfully with ID: {}", savedCV.getId());

        notificationService.addNotification(Notification.builder()
                        .code(NotificationCode.CV_VALIDATION_REQUIRED)
                        .filter(Role.PROGRAM_MANAGER)
                        .referenceId(savedCV.getId())
                .build());

        return CurriculumVitaeDTO.toDTO(savedCV);
    }

    public Page<CurriculumVitaeDTO> getCVsByStudentId(Long studentId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<CurriculumVitae> cvs = curriculumVitaeRepository.findByStudentIdOrderByDateHeureAjoutDesc(studentId, pageable);
        return cvs.map(CurriculumVitaeDTO::toDTO);
    }

    public List<CurriculumVitaeDTO> getValidatedCVsByStudentId(Long studentId) {
        List<CurriculumVitae> cvs = curriculumVitaeRepository.findByStudentIdAndStatus(studentId,ApprovalStatus.VALIDATED);
        return cvs.stream().map(CurriculumVitaeDTO::toDTO).collect(Collectors.toList());
    }


    private PDFDocu savePDF(MultipartFile uploadFile, String fileName) throws IOException {
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

    public CurriculumVitaeDTO validateCV(Long cvId, String approvalResult, UserDTO userDTO) throws CVNotFoundException {
        logger.info("Validating CV with ID: {}", cvId);
        CurriculumVitae cv = curriculumVitaeRepository.findById(cvId)
                .orElseThrow(() -> new CVNotFoundException("CV not found"));

        try {
            cv.setStatus(ApprovalStatus.toEnum(approvalResult));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid approval status: {}", approvalResult);
            throw new IllegalArgumentException("Invalid approval status");
        }

        CurriculumVitae updatedCV = curriculumVitaeRepository.save(cv);
        logger.info("CV with ID: {} validated successfully", cvId);

        notificationService.tryFindViewNotification(userDTO,NotificationCode.CV_VALIDATION_REQUIRED, updatedCV.getId());

        notificationService.addNotification(Notification.builder()
                .code(NotificationCode.CV_VALIDATED)
                .userId(updatedCV.getStudentIdLong())
                .referenceId(updatedCV.getId())
                .build());

        return CurriculumVitaeDTO.toDTO(updatedCV);
    }

    public List<CurriculumVitaeDTO> getCVsByDiscipline(String discipline) throws DisciplineNotFoundException {
        List<Student> students = studentRepository.findByDiscipline(Discipline.toEnum(discipline));
        List<CurriculumVitae> cvs = students.stream()
                .map(student -> curriculumVitaeRepository.findByStudentId(student.getId()))
                .flatMap(List::stream)
                .toList();
        if (cvs.isEmpty()) {
            throw new CVNotFoundException("No CVs found for discipline: " + discipline);
        }
        return cvs.stream().map(CurriculumVitaeDTO::toDTO).collect(Collectors.toList());
    }

    public CurriculumVitaeDTO getCVById(Long id) {
        return CurriculumVitaeDTO.toDTO(curriculumVitaeRepository.findById(id).orElseThrow(CVNotFoundException::new));
    }

    public List<CurriculumVitaeDTO> getCVsWaitingForApproval() throws CVNotFoundException {
        String query = "";
        List<CurriculumVitae> cvs = curriculumVitaeRepository.findByStatus(ApprovalStatus.WAITING);
        if (cvs.isEmpty()) {
            throw new CVNotFoundException("No CVs waiting for approval");
        }
        return cvs.stream().map(CurriculumVitaeDTO::toDTO).collect(Collectors.toList());
    }

    public byte[] getPDF(String filename) throws FileNotFoundException {
        Optional<PDFDocu> pdfDocu = pdfDocuRepository.findByFileName(filename);
        if (pdfDocu.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }
        return pdfDocu.get().getPdfData();
    }

}