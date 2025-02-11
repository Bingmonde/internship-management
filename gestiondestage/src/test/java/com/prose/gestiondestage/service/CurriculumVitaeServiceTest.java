package com.prose.gestiondestage.service;

import com.prose.entity.*;
import com.prose.entity.users.Student;
import com.prose.entity.users.auth.Role;
import com.prose.repository.*;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.CurriculumVitaeService;
import com.prose.service.EvaluationEmployerService;
import com.prose.service.Exceptions.CVNotFoundException;
import com.prose.service.Exceptions.DisciplineNotFoundException;
import com.prose.service.Exceptions.FileNotFoundException;
import com.prose.service.NotificationService;
import com.prose.service.StudentService;
import com.prose.service.dto.CurriculumVitaeDTO;
import com.prose.service.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CurriculumVitaeServiceTest {
    private CurriculumVitaeRepository curriculumVitaeRepository;

    private StudentRepository studentRepository;

    private PDFDocuRepository pdfDocuRepository;

    private CurriculumVitaeService curriculumVitaeService;

    private StudentService studentService;

    private NotificationService notificationService;

    private EvaluationEmployerService evaluationEmployerService;
    @BeforeEach
    void setup() throws IOException {
        curriculumVitaeRepository = mock(CurriculumVitaeRepository.class);
        studentRepository = mock(StudentRepository.class);
        pdfDocuRepository = mock(PDFDocuRepository.class);
        studentService = mock(StudentService.class);
        notificationService = mock(NotificationService.class);

        curriculumVitaeService = new CurriculumVitaeService(curriculumVitaeRepository, studentRepository, studentService, pdfDocuRepository, NoOpPasswordEncoder.getInstance(),notificationService);
    }

    @Test
    void addCVShouldReturnCurriculumVitaeDTOWhenSuccess() throws IOException {

        String formattedDate = "2024-10-02T18:17:24Z";
        try {
//            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(formattedDate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setCredentials("test@test.com", "a");
        student.setDiscipline(Discipline.INFORMATIQUE);
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.WAITING);
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        when(studentRepository.findUserAppByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(curriculumVitaeRepository.save(any(CurriculumVitae.class))).thenReturn(cv);

        // Act
        CurriculumVitaeDTO result = curriculumVitaeService.addCV(file, LocalDateTime.now(), student.getEmail());

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void addCV_Empty() throws IOException {

        String formattedDate = "2024-10-02T18:17:24Z";
        try {
//            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(formattedDate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setCredentials("test@test.com", "a");
        student.setDiscipline(Discipline.INFORMATIQUE);
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.WAITING);
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        when(studentRepository.findUserAppByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(curriculumVitaeRepository.save(any(CurriculumVitae.class))).thenReturn(cv);

        try {
            curriculumVitaeService.addCV(null, LocalDateTime.now(), student.getEmail());
        } catch (IOException e) {
            assertThat(e.getMessage()).isEqualTo("Empty file or no file uploaded");
        }
    }

    @Test
    void addCV_MAXSize() throws IOException {

        String formattedDate = "2024-10-02T18:17:24Z";
        try {
//            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(formattedDate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setCredentials("test@test.com", "a");
        student.setDiscipline(Discipline.INFORMATIQUE);
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.WAITING);
        byte[] i = new byte[999999999];
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", i);

        when(studentRepository.findUserAppByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(curriculumVitaeRepository.save(any(CurriculumVitae.class))).thenReturn(cv);

        // Act
        try {
            curriculumVitaeService.addCV(file, LocalDateTime.now(), student.getEmail());
        } catch (IOException e) {
            assertThat(e.getMessage()).isEqualTo("File is too large, maximum allowed size is " + ((10 * 1024 * 1024) / (1024 * 1024)) + "MB");
        }
    }

    @Test
    void addCV_BadContentTyp() throws IOException {

        String formattedDate = "2024-10-02T18:17:24Z";
        try {
//            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(formattedDate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setCredentials("test@test.com", "a");
        student.setDiscipline(Discipline.INFORMATIQUE);
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.WAITING);
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "skibidi", "PDF content".getBytes());

        when(studentRepository.findUserAppByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(curriculumVitaeRepository.save(any(CurriculumVitae.class))).thenReturn(cv);

        // Act
        try {
            curriculumVitaeService.addCV(file, LocalDateTime.now(), student.getEmail());
        } catch (IOException e) {
            assertThat(e.getMessage()).isEqualTo("Invalid file type, only PDF files are allowed");
        }

    }

    @Test
    void addCVShouldThrowIllegalArgumentExceptionIfDateHeureAjoutIsNull() {
        String formattedDate = "2024-10-02T18:17:24Z";
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(formattedDate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setCredentials("test@test.com", "a");
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        when(studentRepository.findUserAppByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(curriculumVitaeRepository.save(any(CurriculumVitae.class))).thenReturn(cv);

        // Act & Assert
        assertThatThrownBy(() -> curriculumVitaeService.addCV(file, null, student.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CV publication datetime cannot be null");
    }

    @Test
    void addCVShouldThrowUserNotFoundExceptionIfStudentNotFound() {

        String formattedDate = "2024-10-02T18:17:24Z";
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(formattedDate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Arrange
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());

        when(studentRepository.findUserAppByEmail("a")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> curriculumVitaeService.addCV(file, LocalDateTime.now(), "a"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Student with email a not found");
    }
    @Test
    void validateCVShouldThrowCVNotFoundExceptionIfCVNotFound() {
        // Arrange
        Long cvId = 1L;
        String approvalResult = "VALIDATED";

        when(curriculumVitaeRepository.findById(cvId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> curriculumVitaeService.validateCV(cvId, approvalResult,new UserDTO(6L,null,null,Role.PROGRAM_MANAGER)))
                .isInstanceOf(CVNotFoundException.class)
                .hasMessageContaining("CV not found");
    }
    @Test
    void getCVsWaitingForApprovalShouldReturnCVList() {
        // Arrange
        Student student = new Student();
        student.setId(3L);
        student.setCredentials("ljajd","dlijalkd");
        student.setDiscipline(Discipline.INFORMATIQUE);

        Date date = new Date();
        CurriculumVitae cv1 = new CurriculumVitae();
        CurriculumVitae cv2 = new CurriculumVitae();
        cv1.setStatus(ApprovalStatus.WAITING);
        cv2.setStatus(ApprovalStatus.WAITING);
        cv1.setDateHeureAjout(LocalDateTime.now());
        cv2.setDateHeureAjout(LocalDateTime.now());
        cv1.setStudent(student);
        cv2.setStudent(student);

        List<CurriculumVitae> cvList = List.of(cv1, cv2);
        Page<CurriculumVitae> page = new PageImpl<>(cvList, PageRequest.of(0, 2), 2);

        when(curriculumVitaeRepository.findByStatus(ApprovalStatus.WAITING)).thenReturn(cvList);

        // Act
        List<CurriculumVitaeDTO> result = curriculumVitaeService.getCVsWaitingForApproval();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getCVsWaitingForApprovalShouldThrowExceptionIfNoCVsFound() {
        // Arrange
        when(curriculumVitaeRepository.findByStatus(ApprovalStatus.WAITING)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThatThrownBy(() -> curriculumVitaeService.getCVsWaitingForApproval())
                .isInstanceOf(CVNotFoundException.class)
                .hasMessageContaining("No CVs waiting for approval");
    }

    @Test
    void getAllCVsFromStudent() {
        // Arrange
        Student student = new Student();
        student.setId(3L);
        student.setCredentials("ljajd","dlijalkd");
        student.setDiscipline(Discipline.INFORMATIQUE);

        List<CurriculumVitae> list = new ArrayList<>();
        CurriculumVitae curriculumVitae1 = new CurriculumVitae();
        curriculumVitae1.setId(1L);
        curriculumVitae1.setStatus(ApprovalStatus.WAITING);
        curriculumVitae1.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae1.setStudent(student);
        list.add(curriculumVitae1);

        CurriculumVitae curriculumVitae2 = new CurriculumVitae();
        curriculumVitae2.setId(2L);
        curriculumVitae2.setStatus(ApprovalStatus.WAITING);
        curriculumVitae2.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae2.setStudent(student);
        list.add(curriculumVitae2);

        CurriculumVitae curriculumVitae3 = new CurriculumVitae();
        curriculumVitae3.setId(3L);
        curriculumVitae3.setStatus(ApprovalStatus.WAITING);
        curriculumVitae3.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae3.setStudent(student);
        list.add(curriculumVitae3);

        when(curriculumVitaeRepository.findByStudentIdOrderByDateHeureAjoutDesc(eq(1L),any())).thenReturn(new PageImpl<>(list, PageRequest.of(5,5),5));

        List<CurriculumVitaeDTO> result = curriculumVitaeService.getCVsByStudentId(1L,5,5).getContent();

        assertThat(result.size()).isEqualTo(list.size());
        assertThat(result.getFirst().id()).isEqualTo(curriculumVitae1.getId());
    }

    @Test
    void getAllCVsFromStudentEmpty() {
        // Arrange
        when(curriculumVitaeRepository.findByStudentIdOrderByDateHeureAjoutDesc(eq(1L),any())).thenReturn(new PageImpl<>(List.of(),PageRequest.of(5,5),5));

        List<CurriculumVitaeDTO> result = curriculumVitaeService.getCVsByStudentId(1L,5,5).getContent();

        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void getAllValidatedCVsFromStudent() {
        // Arrange
        Student student = new Student();
        student.setId(3L);
        student.setCredentials("ljajd","dlijalkd");
        student.setDiscipline(Discipline.INFORMATIQUE);

        List<CurriculumVitae> list = new ArrayList<>();
        CurriculumVitae curriculumVitae1 = new CurriculumVitae();
        curriculumVitae1.setId(1L);
        curriculumVitae1.setStatus(ApprovalStatus.VALIDATED);
        curriculumVitae1.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae1.setStudent(student);
        list.add(curriculumVitae1);

        CurriculumVitae curriculumVitae2 = new CurriculumVitae();
        curriculumVitae2.setId(2L);
        curriculumVitae2.setStatus(ApprovalStatus.VALIDATED);
        curriculumVitae2.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae2.setStudent(student);
        list.add(curriculumVitae2);

        CurriculumVitae curriculumVitae3 = new CurriculumVitae();
        curriculumVitae3.setId(3L);
        curriculumVitae3.setStatus(ApprovalStatus.VALIDATED);
        curriculumVitae3.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae3.setStudent(student);
        list.add(curriculumVitae3);

        when(curriculumVitaeRepository.findByStudentIdAndStatus(1L,ApprovalStatus.VALIDATED)).thenReturn(list);

        List<CurriculumVitaeDTO> result = curriculumVitaeService.getValidatedCVsByStudentId(1L);

        assertThat(result.size()).isEqualTo(list.size());
        assertThat(result.getFirst().id()).isEqualTo(curriculumVitae1.getId());
    }

    @Test
    void getAllValidatedCVsFromStudentEmpty() {
        // Arrange
        when(curriculumVitaeRepository.findByStudentIdAndStatus(1L,ApprovalStatus.VALIDATED)).thenReturn(List.of());

        List<CurriculumVitaeDTO> result = curriculumVitaeService.getValidatedCVsByStudentId(1L);

        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void getCVsByDisciplineValid() {
        // Arrange
        Student student = new Student();
        student.setId(3L);
        student.setCredentials("ljajd","dlijalkd");
        student.setDiscipline(Discipline.INFORMATIQUE);

        List<CurriculumVitae> list = new ArrayList<>();
        CurriculumVitae curriculumVitae1 = new CurriculumVitae();
        curriculumVitae1.setId(1L);
        curriculumVitae1.setStatus(ApprovalStatus.WAITING);
        curriculumVitae1.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae1.setStudent(student);
        list.add(curriculumVitae1);

        CurriculumVitae curriculumVitae2 = new CurriculumVitae();
        curriculumVitae2.setId(2L);
        curriculumVitae2.setStatus(ApprovalStatus.WAITING);
        curriculumVitae2.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae2.setStudent(student);
        list.add(curriculumVitae2);

        CurriculumVitae curriculumVitae3 = new CurriculumVitae();
        curriculumVitae3.setId(3L);
        curriculumVitae3.setStatus(ApprovalStatus.WAITING);
        curriculumVitae3.setDateHeureAjout(LocalDateTime.now());
        curriculumVitae3.setStudent(student);
        list.add(curriculumVitae3);

        when(studentRepository.findByDiscipline(Discipline.INFORMATIQUE)).thenReturn(List.of(student));
        when(curriculumVitaeRepository.findByStudentId(student.getId())).thenReturn(list);

        try {
            List<CurriculumVitaeDTO> result = curriculumVitaeService.getCVsByDiscipline("INFORMATIQUE");

            assertThat(result.size()).isEqualTo(list.size());
            assertThat(result.getFirst().id()).isEqualTo(curriculumVitae1.getId());
        } catch (DisciplineNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // no cv found
    @Test
    void getCVsByDisciplineEmpty() {
        // Arrange
        when(studentRepository.findByDiscipline(Discipline.INFORMATIQUE)).thenReturn(List.of());
        when(curriculumVitaeRepository.findByStudentId(1L)).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> curriculumVitaeService.getCVsByDiscipline("INFORMATIQUE"))
                .isInstanceOf(CVNotFoundException.class)
                .hasMessageContaining("No CVs found for discipline: INFORMATIQUE");
    }

    @Test
    void validateCV_BadApprovalStatus(){
        // Arrange
        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStatus(ApprovalStatus.WAITING);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(new Student());
        cv.getStudent().setId(1L);

        when(curriculumVitaeRepository.findById(1L)).thenReturn(Optional.of(cv));

        // Act & Assert
        assertThatThrownBy(() -> curriculumVitaeService.validateCV(1L, "INVALID",new UserDTO(6L,null,null,Role.PROGRAM_MANAGER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid approval status");
    }

    @Test
    void getCVByID(){
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setCredentials("test@test.com", "a");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStatus(ApprovalStatus.WAITING);
        cv.setDateHeureAjout(LocalDateTime.now());
        cv.setStudent(student);
        cv.getStudent().setId(1L);

        when(curriculumVitaeRepository.findById(1L)).thenReturn(Optional.of(cv));

        // Act
        CurriculumVitaeDTO result = curriculumVitaeService.getCVById(1L);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void getPDF_Exception() throws FileNotFoundException {
        // Arrange
        when(pdfDocuRepository.findByFileName("test.pdf")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> curriculumVitaeService.getPDF("test.pdf"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("File not found");
    }


}
