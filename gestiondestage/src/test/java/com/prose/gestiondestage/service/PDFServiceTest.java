package com.prose.gestiondestage.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.*;
import com.prose.entity.*;
import com.prose.entity.embedded.InterpersonalRelationshipsEvaluation;
import com.prose.entity.embedded.PersonalSkillsEvaluation;
import com.prose.entity.embedded.ProductivityEvaluation;
import com.prose.entity.embedded.QualityOfWorkEvaluation;
import com.prose.entity.users.Employeur;
import com.prose.entity.users.Student;
import com.prose.entity.users.Teacher;
import com.prose.entity.users.auth.Role;
import com.prose.repository.*;
import com.prose.service.CurriculumVitaeService;
import com.prose.service.Exceptions.EvaluationNotFoundException;
import com.prose.service.Exceptions.InternshipNotFoundException;
import com.prose.service.Exceptions.MissingPermissionsExceptions;
import com.prose.service.NotificationService;
import com.prose.service.PDFService;
import com.prose.service.StudentService;
import com.prose.service.dto.UserDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PDFServiceTest {
    private InternshipOfferRepository internshipOfferRepository;

    private InternshipEvaluationRepository internshipEvaluationRepository;

    private PDFService pdfService;

    @BeforeEach
    void setup() throws IOException {
        internshipOfferRepository = mock(InternshipOfferRepository.class);
        internshipEvaluationRepository = mock(InternshipEvaluationRepository.class);
        pdfService = new PDFService(internshipOfferRepository, internshipEvaluationRepository);
    }

    @Test
    void printContract_success() throws InternshipNotFoundException, IOException, MissingPermissionsExceptions {
        // arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);
        jobOffer.setWeeklyHours(40);
        jobOffer.setTauxHoraire(15.0);
        jobOffer.setLieu("Montreal");
        jobOffer.setDateDebut("2025-09-01");
        jobOffer.setDateFin("2025-12-31");
        jobOffer.setDayScheduleFrom(LocalTime.now());
        jobOffer.setDayScheduleTo(LocalTime.now());


        PDFDocu pdfDocu = new PDFDocu();
        pdfDocu.setId(1L);
        pdfDocu.setFileName("test.pdf");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        document.add(new Paragraph("This is a test PDF document."));
        document.add(new Paragraph("Generated for unit testing purposes."));
        document.close();
        byte[] pdfData = outputStream.toByteArray();
        pdfDocu.setPdfData(pdfData);
        jobOffer.setPdfDocu(pdfDocu);


        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        jobOffer.setEmployeur(employeur);
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);


        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));


        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        Contract contract = new Contract();
        contract.setId(1L);
        contract.setEmployerSign(LocalDateTime.now());

        byte[] signature = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAqMAAACHCAYAAAAiPVBhAAAAAXNSR0IArs4c6QAAIABJREFUeF7tnQnYVeMWx98Imd3okumWImNFxpQy19UkpDKH22QoktSlMpQh");

        contract.setEmployerSignImage(signature);
        contract.setStudentSign(LocalDateTime.now());
        contract.setStudentSignImage(signature);
        contract.setManagerSign(LocalDateTime.now());
        contract.setManagerSignImage(signature);

        ProgramManager manager = new ProgramManager();
        manager.setId(3L);
        manager.setNom("John");
        manager.setPrenom("Doe");
        contract.setManager(manager);

        internshipOffer.setContract(contract);

        UserDTO userDTO = UserDTO.toDTO(student);

        // act
        when (internshipOfferRepository.findById(1L)).thenReturn(Optional.of(internshipOffer));
        byte[] pdf = pdfService.printContract(1L, userDTO);

        // assert
        assertThat(pdf).isNotNull();

    }


    @Test
    void printContract_missingPermission() {
        // arrange
        // arrange
        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);
        jobOffer.setWeeklyHours(40);
        jobOffer.setTauxHoraire(15.0);
        jobOffer.setLieu("Montreal");
        jobOffer.setDateDebut("2025-09-01");
        jobOffer.setDateFin("2025-12-31");
        jobOffer.setDayScheduleFrom(LocalTime.now());
        jobOffer.setDayScheduleTo(LocalTime.now());


        PDFDocu pdfDocu = new PDFDocu();
        pdfDocu.setId(1L);
        pdfDocu.setFileName("test.pdf");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        document.add(new Paragraph("This is a test PDF document."));
        document.add(new Paragraph("Generated for unit testing purposes."));
        document.close();
        byte[] pdfData = outputStream.toByteArray();
        pdfDocu.setPdfData(pdfData);
        jobOffer.setPdfDocu(pdfDocu);


        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setCredentials("me@mail.com", "123456");
        jobOffer.setEmployeur(employeur);

        jobOffer.setEmployeur(employeur);
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("stu@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);


        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);
        cv.setDateHeureAjout(LocalDateTime.now());

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setActive(true);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setApplicationDate(LocalDateTime.now());
        jobOfferApplication.setCurriculumVitae(cv);

        jobOffer.setJobOfferApplications(List.of(jobOfferApplication));


        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);

        Contract contract = new Contract();
        contract.setId(1L);
        contract.setEmployerSign(LocalDateTime.now());

        byte[] signature = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAqMAAACHCAYAAAAiPVBhAAAAAXNSR0IArs4c6QAAIABJREFUeF7tnQnYVeMWx98Imd3okumWImNFxpQy19UkpDKH22QoktSlMpQh");

        contract.setEmployerSignImage(signature);
        contract.setStudentSign(LocalDateTime.now());
        contract.setStudentSignImage(signature);
        contract.setManagerSign(LocalDateTime.now());
        contract.setManagerSignImage(signature);

        ProgramManager manager = new ProgramManager();
        manager.setId(3L);
        manager.setNom("John");
        manager.setPrenom("Doe");
        contract.setManager(manager);
        internshipOffer.setContract(contract);


        Student student2 = new Student();
        student2.setId(2L);
        student2.setPrenom("Alice");
        student2.setNom("Smith");
        student2.setCredentials("email", "123456");
        student2.setDiscipline(Discipline.INFORMATIQUE);

        UserDTO userDTO2 = UserDTO.toDTO(student2);




        // act
        when (internshipOfferRepository.findById(1L)).thenReturn(Optional.of(internshipOffer));

        // assert
        assertThatThrownBy(() -> pdfService.printContract(1L, userDTO2))
                .isInstanceOf(MissingPermissionsExceptions.class);
    }

    @Test
    void printContract_InternshipNotFoundException() {
        // arrange
        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("test@email.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);
        UserDTO userDTO = UserDTO.toDTO(student);
        // act
        when (internshipOfferRepository.findById(anyLong())).thenReturn(Optional.empty());

        // assert
        assertThatThrownBy(() -> pdfService.printContract(1L, userDTO))
                .isInstanceOf(InternshipNotFoundException.class);
    }


    // TODO
    @Test
    void printInternshipOffer_success() {
    }

    private static byte[] createSignatureImage() throws IOException {
        int width = 200;
        int height = 100;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Draw a black rectangle as the signature
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    @Test
    void printInternEvaluation_success() throws IOException, EvaluationNotFoundException, MissingPermissionsExceptions {
        // arrange
        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setConfirmationStatus(ApprovalStatus.ACCEPTED);
        internshipOffer.setExpirationDate(LocalDateTime.now().plusDays(7L));

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitre("Junior Developer");
        jobOffer.setNombreStagiaire(1);
        jobOffer.setApproved(true);
        jobOffer.setActivated(true);
        jobOffer.setWeeklyHours(40);
        jobOffer.setTauxHoraire(15.0);
        jobOffer.setLieu("Montreal");
        jobOffer.setDateDebut("2025-09-01");
        jobOffer.setDateFin("2025-12-31");
        jobOffer.setDayScheduleFrom(LocalTime.now());
        jobOffer.setDayScheduleTo(LocalTime.now());

        Employeur employeur = new Employeur();
        employeur.setId(1L);
        jobOffer.setEmployeur(employeur);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);
        jobOfferApplication.setCurriculumVitae(new CurriculumVitae());
        jobOfferApplication.setJobOffer(jobOffer);
        internshipOffer.setJobOfferApplication(jobOfferApplication);

        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setId(1L);
        internshipEvaluation.setInternshipOffer(internshipOffer);

        EvaluationIntern evaluationIntern = new EvaluationIntern();
        evaluationIntern.setId(1L);
        evaluationIntern.setProgram("Informatique");
        evaluationIntern.setProductivityComments("Good job");
        evaluationIntern.setQualityOfWorkComments("Good job");
        evaluationIntern.setInterpersonalRelationshipsComments("Good job");
        evaluationIntern.setPersonalSkillsComments("Good job");
        evaluationIntern.setName("John Doe");
        evaluationIntern.setCompanyName("TestCompany");
        evaluationIntern.setSupervisorName("John Doe");
        evaluationIntern.setFunction("Junior Developer");
        evaluationIntern.setTelephone("514-123-4567");

        ProductivityEvaluation productivityEvaluation = new ProductivityEvaluation();
        productivityEvaluation.setProduction_a(EvaluationOption.TOTAL_AGREEMENT);
        productivityEvaluation.setProduction_b(EvaluationOption.TOTAL_AGREEMENT);
        productivityEvaluation.setProduction_c(EvaluationOption.TOTAL_AGREEMENT);
        productivityEvaluation.setProduction_d(EvaluationOption.TOTAL_AGREEMENT);
        productivityEvaluation.setProduction_e(EvaluationOption.TOTAL_AGREEMENT);
        evaluationIntern.setProductivityEvaluation(productivityEvaluation);

        QualityOfWorkEvaluation qualityOfWorkEvaluation = new QualityOfWorkEvaluation();
        qualityOfWorkEvaluation.setQuality_a(EvaluationOption.TOTAL_AGREEMENT);
        qualityOfWorkEvaluation.setQuality_b(EvaluationOption.TOTAL_AGREEMENT);
        qualityOfWorkEvaluation.setQuality_c(EvaluationOption.TOTAL_AGREEMENT);
        qualityOfWorkEvaluation.setQuality_d(EvaluationOption.TOTAL_AGREEMENT);
        qualityOfWorkEvaluation.setQuality_e(EvaluationOption.TOTAL_AGREEMENT);
        evaluationIntern.setQualityOfWorkEvaluation(qualityOfWorkEvaluation);

        InterpersonalRelationshipsEvaluation interpersonalRelationshipsEvaluation = new InterpersonalRelationshipsEvaluation();
        interpersonalRelationshipsEvaluation.setInterPersonal_a(EvaluationOption.TOTAL_AGREEMENT);
        interpersonalRelationshipsEvaluation.setInterPersonal_b(EvaluationOption.TOTAL_AGREEMENT);
        interpersonalRelationshipsEvaluation.setInterPersonal_c(EvaluationOption.TOTAL_AGREEMENT);
        interpersonalRelationshipsEvaluation.setInterPersonal_d(EvaluationOption.TOTAL_AGREEMENT);
        interpersonalRelationshipsEvaluation.setInterPersonal_e(EvaluationOption.TOTAL_AGREEMENT);
        interpersonalRelationshipsEvaluation.setInterPersonal_f(EvaluationOption.TOTAL_AGREEMENT);
        evaluationIntern.setInterpersonalRelationshipsEvaluation(interpersonalRelationshipsEvaluation);

        PersonalSkillsEvaluation personalSkillsEvaluation = new PersonalSkillsEvaluation();
        personalSkillsEvaluation.setPersonalStills_a(EvaluationOption.TOTAL_AGREEMENT);
        personalSkillsEvaluation.setPersonalStills_b(EvaluationOption.TOTAL_AGREEMENT);
        personalSkillsEvaluation.setPersonalStills_c(EvaluationOption.TOTAL_AGREEMENT);
        personalSkillsEvaluation.setPersonalStills_d(EvaluationOption.TOTAL_AGREEMENT);
        personalSkillsEvaluation.setPersonalStills_e(EvaluationOption.TOTAL_AGREEMENT);
        personalSkillsEvaluation.setPersonalStills_f(EvaluationOption.TOTAL_AGREEMENT);
        evaluationIntern.setPersonalSkillsEvaluation(personalSkillsEvaluation);

        evaluationIntern.setOverallAppreciation(OverallAppreciation.TOTAL_AGREEMENT);
        evaluationIntern.setOverallComments("Good job");
        evaluationIntern.setEvaluationDiscussedWithIntern(true);
        evaluationIntern.setSupervisionHoursPerWeek(40.0);
        evaluationIntern.setWillingnessToRehire(WillingnessToRehire.YES);
        evaluationIntern.setTechnicalTrainingComments("Yes");
        evaluationIntern.setEmployerSignature(createSignatureImage()); // Ensure this is a valid image byte array
        evaluationIntern.setDate(LocalDate.now());

        internshipEvaluation.setEvaluationIntern(evaluationIntern);

        when(internshipEvaluationRepository.findByInternshipOfferId(1L)).thenReturn(Optional.of(internshipEvaluation));
        UserDTO userDTO = new UserDTO(
                1L, "username", "email@example.com", Role.EMPLOYEUR
        );

        // act
        byte[] pdf = pdfService.printInternEvaluation(1L, 1L);

        // assert
        assertThat(pdf).isNotNull();
    }




//    public byte[] printEmployerEvaluation(long evaluationId, UserDTO user) throws IOException, EvaluationNotFoundException, MissingPermissionsExceptions {
//        InternshipEvaluation internshipEvaluation = internshipEvaluationRepository.findById(evaluationId).orElseThrow(() -> new EvaluationNotFoundException("Internship offer not found"));
//
//        if ( user.role() == Role.TEACHER && internshipEvaluation.getTeacher().getId() != user.id()) {
//            throw new MissingPermissionsExceptions("You are not authorized to access this evaluation");
//        }
//
//        // evaluation info
//        EvaluationEmployer evaluation = internshipEvaluation.getEvaluationEmployer();
//        Employeur employeur = internshipEvaluation.getInternshipOffer().getJobOfferApplication().getJobOffer().getEmployeur();
//        Student student = internshipEvaluation.getInternshipOffer().getJobOfferApplication().getCurriculumVitae().getStudent();
//        JobOffer jobOffer = internshipEvaluation.getInternshipOffer().getJobOfferApplication().getJobOffer();
//
//
//        int fontSizeTitle = 20;
//        int fontSizeBig = 16;
//        int fontSizeNormal = 10;
//        int fontSizeTiny = 8;
//
//        // basic info about internship
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
//        PdfDocument pdf = new PdfDocument(writer);
//        Document document = new Document(pdf);
//
//        float topMargin = 72;
//        float rightMargin = 36;
//        float bottomMargin = 36;
//        float leftMargin = 36;
//
//        document.setMargins(topMargin, rightMargin, bottomMargin, leftMargin);
//
//        pdf.addNewPage(PageSize.A4);
//        float pageWidth = pdf.getPage(1).getPageSize().getWidth();
//        float pageHeight = pdf.getPage(1).getPageSize().getHeight();
//
//        String logoLocation = "src/main/resources/img/cegep_logo.jpg";
//        Image logo = new Image(ImageDataFactory.create(logoLocation));
//        logo.setWidth(UnitValue.createPercentValue(30));
//        logo.setHorizontalAlignment(HorizontalAlignment.LEFT);
//        document.add(logo);
//
//        float imgHeight = logo.getImageHeight();
//        float yPosition = pageHeight - imgHeight * 0.3f - topMargin + 10;
//        Paragraph ate = new Paragraph("Alternance travail-études").setBold();
//        document.showTextAligned(ate, pageWidth - rightMargin, yPosition , TextAlignment.RIGHT);
//
//        Paragraph evaluationTitle = new Paragraph("ÉVALUATION DU MILIEU DE STAGE")
//                .setBold()
//                .setFontSize(fontSizeTitle)
//                .setTextAlignment(TextAlignment.CENTER)
//                .setMarginTop(10);
//        document.add(evaluationTitle);
//
//
//        // base info about employer
//        Div employerInfoBox = new Div()
//                .setKeepTogether(true)
//                .setMarginTop(5)
//                .setMarginBottom(5);
//        employerInfoBox.add(new Paragraph("IDENTIFICATION DE L’ENTREPRISE").setBold().setMarginTop(10));
//
//        String[] employerBasicInfoLabel1 = {"Nom de l’entreprise : ", "Personne contact : ", "Adresse : "};
//        String[] employerBasicInfoLabel2 = {"Ville : ", "Code postal : ", "Téléphone : ", "Télécopieur : "};
//
//        Table employerInfoTable = new Table(UnitValue.createPercentArray(2));
//        employerInfoTable.setWidth(UnitValue.createPercentValue(100));
//        employerInfoTable.setBorder(new SolidBorder(0.5f));
//
//        employerInfoTable.addCell(new Cell(1,2).add(new Paragraph(employerBasicInfoLabel1[0]).add(new Text(employeur.getNomCompagnie()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
//        employerInfoTable.addCell(new Cell(1,2).add(new Paragraph(employerBasicInfoLabel1[1]).
//                add(new Text(employeur.getContactPerson()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
//        employerInfoTable.addCell(new Cell(1,2).add(new Paragraph(employerBasicInfoLabel1[2]).add(new Text(employeur.getAdresse()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
//
//        employerInfoTable.addCell(new Cell().add(new Paragraph(employerBasicInfoLabel2[0]).add(new Text(employeur.getCity()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
//        employerInfoTable.addCell(new Cell().add(new Paragraph(employerBasicInfoLabel2[1]).add(new Text(employeur.getPostalCode()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
//        employerInfoTable.addCell(new Cell().add(new Paragraph(employerBasicInfoLabel2[2]).add(new Text(employeur.getTelephone().replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3")).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
//        employerInfoTable.addCell(new Cell().add(new Paragraph(employerBasicInfoLabel2[3]).add(new Text(employeur.getFax().replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3")).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
//
//        employerInfoBox.add(employerInfoTable);
//        document.add(employerInfoBox);
//
//        // about intern
//        Div internInfoBox = new Div()
//                .setKeepTogether(true)
//                .setMarginTop(5)
//                .setMarginBottom(5);
//        internInfoBox.add(new Paragraph("IDENTIFICATION DU STAGIAIRE").setBold().setMarginTop(10));
//
//        String[] internBasicInfoLabel = {"Nom du stagiaire : ", "Date du stage : ", "Stage (encercler): "};
//        Table internInfoTable = new Table(UnitValue.createPercentArray(new float[]{2, 1, 4}));
//        internInfoTable.setWidth(UnitValue.createPercentValue(100));
//        internInfoTable.setBorder(new SolidBorder(0.5f));
//
//
//        String dateStage = jobOffer.getDateDebut() + " à " + jobOffer.getDateFin();
//        internInfoTable.addCell(new Cell(1,3).add(new Paragraph(internBasicInfoLabel[0]).add(new Text(student.getPrenom() + " " + student.getNom()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
//        internInfoTable.addCell(new Cell(1,3).add(new Paragraph(internBasicInfoLabel[1]).add(new Text(dateStage).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
//        Div chosenInternNum = new Div()
//                .setWidth(25)
//                .setHeight(25)
//                .setBorder(new SolidBorder(1))
//                .setBorderRadius(new BorderRadius(50));
//        // TODO from database
//        internInfoTable.addCell(new Cell().add(new Paragraph(internBasicInfoLabel[2])).setBorder(Border.NO_BORDER));
//        if (evaluation.getStageNumber() == StageNumber.STAGE_1) {
//            chosenInternNum.add(new Paragraph("1").setMarginLeft(10));
//            internInfoTable.addCell(new Cell().add(chosenInternNum).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//            internInfoTable.addCell(new Cell().add(new Paragraph("2")).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//        }
//        else {
//            chosenInternNum.add(new Paragraph("2").setMarginLeft(10));
//            internInfoTable.addCell(new Cell().add(new Paragraph("1")).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//            internInfoTable.addCell(new Cell().add(chosenInternNum).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//        }
//        internInfoBox.add(internInfoTable);
//        document.add(internInfoBox);
//
//        // evalution
//        Div evaluationBox1 = new Div()
//                .setKeepTogether(true)
//                .setMarginTop(5)
//                .setMarginBottom(5);
//
//        evaluationBox1.add(new Paragraph("ÉVALUATION").setBold().setMarginTop(10));
//        String[] headers = {"", "Totalement en accord", "Plutôt en accord", "Plutôt désaccord*", "Totalement désaccord*", "Impossible de se prononcer"};
//        String[] evalutionsPart1 = {
//                "Les tâches confiées au stagiaire sont conformes aux tâches annoncées dans l’entente de stage.",
//                "Des mesures d’accueil facilitent l’intégration du nouveau stagiaire.",
//                "Le temps réel consacré à l’encadrement du stagiaire est suffisant."
//        };
//        String[] evalutionsPart2 = {
//                "L’environnement de travail respecte les normes d’hygiène et de sécurité au travail.",
//                "Le climat de travail est agréable.",
//                "Le milieu de stage est accessible par transport en commun.",
//                "Le salaire offert est intéressant pour le stagiaire.",
//        };
//        String[] evalutionsPart3 = {
//                "La communication avec le superviseur de stage facilite le déroulement du stage.",
//                "L’équipement fourni est adéquat pour réaliser les tâches confiées.",
//                "Le volume de travail est acceptable."
//        };
//
//
//        int[] evaluationResultsPart1 = {evaluation.getTasksMetExpectations().ordinal(), evaluation.getIntegrationSupport().ordinal(), evaluation.getSupervisionSufficient().ordinal()};
//
//        int[] evaluationResultsPart2 = {
//                evaluation.getWorkEnvironment().ordinal(),
//                evaluation.getWorkClimate().ordinal(),
//                evaluation.getAccessibleTransport().ordinal(),
//                evaluation.getSalaryInteresting().ordinal()
//        };
//
//        int[] evaluationResultsPart3 = {evaluation.getCommunicationWithSupervisor().ordinal(), evaluation.getEquipmentAdequate().ordinal(), evaluation.getWorkloadAcceptable().ordinal()};
//
//        Table evaluationTablePage1 = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1, 1}));
//        evaluationTablePage1.setWidth(UnitValue.createPercentValue(100));
//        evaluationTablePage1.setBorder(new SolidBorder(0.5f));
//
//        addHeaderCell(evaluationTablePage1, headers);
//
//        IntStream.range(0, evalutionsPart1.length).forEach(index -> {
//            addRowCell(evaluationTablePage1, evalutionsPart1[index], evaluationResultsPart1[index]);
//        });
//        evaluationTablePage1.addCell(new Cell()
//                .add(new Paragraph("Préciser le nombre d’heures/semaine :").setFontSize(8))
//                .add(new Paragraph("Premier mois : ").setFontSize(8).add(new Text(evaluation.getFirstMonthHours()).setUnderline(0.5f, -2))
//                        .setMarginLeft(10))
//                .add(new Paragraph("Deuxième mois : ").setFontSize(8).add(new Text(evaluation.getSecondMonthHours()).setUnderline(0.5f, -2))
//                        .setMarginLeft(10))
//                .add(new Paragraph("Troisième mois : ").setFontSize(8).add(new Text(evaluation.getThirdMonthHours()).setUnderline(0.5f, -2))
//                        .setMarginLeft(10))
//                .setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
//        evaluationTablePage1.addCell(new Cell(1,5).setBorder(Border.NO_BORDER));
//        evaluationBox1.add(evaluationTablePage1);
//        document.add(evaluationBox1);
//
//
//        Div evaluationBox2 = new Div()
//                .setKeepTogether(true)
//                .setMarginTop(5)
//                .setMarginBottom(5);
//
//        Table evaluationTablePage2 = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1, 1}));
//        evaluationTablePage2.setWidth(UnitValue.createPercentValue(100));
//        evaluationTablePage2.setBorder(new SolidBorder(0.5f));
//
//        addHeaderCell(evaluationTablePage2, headers);
//
//        IntStream.range(0, evalutionsPart2.length).forEach(index -> {
//            addRowCell(evaluationTablePage2, evalutionsPart2[index], evaluationResultsPart2[index]);
//        });
//        double salary = jobOffer.getTauxHoraire();
//        evaluationTablePage2.addCell(new Cell().add(new Paragraph("Préciser : ").setFontSize(8).add(new Text(salary+"").setUnderline(0.5f, -2)).add(new Text("  /l'heure"))).setBorder(Border.NO_BORDER));
//        evaluationTablePage2.addCell(new Cell(1,5).setBorder(Border.NO_BORDER));
//
//        IntStream.range(0, evalutionsPart3.length).forEach(index -> {
//            addRowCell(evaluationTablePage2, evalutionsPart3[index], evaluationResultsPart3[index]);
//        });
//        evaluationBox2.add(evaluationTablePage2);
//        evaluationBox2.add(new Paragraph("* Expliquer dans la section commentaires").setBold());
//        document.add(evaluationBox2);
//
//        // comments
//        document.add(new Paragraph("Commentaires :").setBold());
//        document.add(new Paragraph(evaluation.getComments()).setUnderline());
//
//
//        // observations
//        document.add(new Paragraph("OBSERVATIONS GÉNÉRALES").setBold().setMarginTop(10));
//        Table observationsTable = new Table(UnitValue.createPercentArray(new float[]{3,2,1}));
//        observationsTable.setWidth(UnitValue.createPercentValue(100));
//        observationsTable.setBorder(new SolidBorder(1));
//
//        observationsTable.addCell(new Cell(2, 1).add(new Paragraph("Ce milieu est à privilégier pour le:").setFontSize(fontSizeNormal)).setBorder(Border.NO_BORDER));
//        String[] stages = {"premier stage", "deuxième stage"};
//        int stage = evaluation.getPreferredStage().ordinal();
//
//        IntStream.range(0, stages.length).forEach(index -> {
//            observationsTable.addCell(new Cell().add(new Paragraph(stages[index]).setFontSize(fontSizeNormal)).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER));
//            Div checkbox = new Div()
//                    .setWidth(5)
//                    .setHeight(5)
//                    .setMarginLeft(10);
//            checkbox.setBorder(new SolidBorder(1));
//            if (index == stage) {
//                checkbox.setBackgroundColor(ColorConstants.BLACK);
//            }
//            observationsTable.addCell(new Cell().add(checkbox).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
//        });
//
//        observationsTable.addCell(new Cell(1, 3).setBorder(new SolidBorder(1)));
//        observationsTable.addCell(new Cell(4, 1).add(new Paragraph("Ce milieu est ouvert à accueillir :").setFontSize(fontSizeNormal)).setBorder(Border.NO_BORDER));
//        String[] numInterns = {"un stagiaire", "deux stagiaires", "trois stagiaires", "plus de trois"};
//        int numOfInterns = evaluation.getNumberOfInterns().ordinal();
//
//
//        IntStream.range(0, numInterns.length).forEach(index -> {
//            observationsTable.addCell(new Cell().add(new Paragraph(numInterns[index]).setFontSize(fontSizeNormal)).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER));
//            Div checkbox = new Div()
//                    .setWidth(5)
//                    .setHeight(5)
//                    .setMarginLeft(10);
//            checkbox.setBorder(new SolidBorder(1));
//            if (index == numOfInterns) {
//                checkbox.setBackgroundColor(ColorConstants.BLACK);
//            }
//            observationsTable.addCell(new Cell().add(checkbox).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
//        });
//
//        observationsTable.addCell(new Cell(1, 3).setBorder(new SolidBorder(1)));
//        observationsTable.addCell(new Cell(2, 1).add(new Paragraph("Ce milieu désire accueillir le même stagiaire pour un prochain stage").setFontSize(fontSizeNormal)).setBorder(Border.NO_BORDER));
//        String[] yesNo = {"oui", "non"};
//        int willTakeSameIntern = evaluation.getWillingToRehire().ordinal();
//
//        IntStream.range(0, yesNo.length).forEach(index -> {
//            observationsTable.addCell(new Cell().add(new Paragraph(yesNo[index]).setFontSize(fontSizeNormal)).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER));
//            Div checkbox = new Div()
//                    .setWidth(5)
//                    .setHeight(5)
//                    .setMarginLeft(10);
//            checkbox.setBorder(new SolidBorder(1));
//            if (index == willTakeSameIntern) {
//                checkbox.setBackgroundColor(ColorConstants.BLACK);
//            }
//            observationsTable.addCell(new Cell().add(checkbox).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
//        });
//
//
//        observationsTable.addCell(new Cell(1, 3).setBorder(new SolidBorder(1)));
//        observationsTable.addCell(new Cell(2, 1).add(new Paragraph("Ce milieu offre des quarts de travail variables : ").setFontSize(fontSizeNormal)).setBorder(Border.NO_BORDER));
//
//        int variableShifts = !evaluation.getSchedule1Start().isEmpty() && !evaluation.getSchedule2Start().isEmpty() ? 1 : 0;
//
//        IntStream.range(0, yesNo.length).forEach(index -> {
//            observationsTable.addCell(new Cell().add(new Paragraph(yesNo[index]).setFontSize(fontSizeNormal)).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER));
//            Div checkbox = new Div()
//                    .setWidth(5)
//                    .setHeight(5)
//                    .setMarginLeft(10);
//            checkbox.setBorder(new SolidBorder(1));
//            if (index != variableShifts) {
//                checkbox.setBackgroundColor(ColorConstants.BLACK);
//            }
//            observationsTable.addCell(new Cell().add(checkbox).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
//        });
//
//        Cell variableShiftsCell = new Cell(2, 3);
//        if (!evaluation.getSchedule1Start().isEmpty())
//            variableShiftsCell.add(new Paragraph("De ").add(new Text(evaluation.getSchedule1Start() + " ").setUnderline(0.5f, -2)).add(new Paragraph("  à  ").add(new Text(evaluation.getSchedule1End()).setUnderline(0.5f, -2))).setFontSize(fontSizeNormal));
//        if (!evaluation.getSchedule2Start().isEmpty())
//            variableShiftsCell.add(new Paragraph("De ").add(new Text(evaluation.getSchedule2Start() + " ").setUnderline(0.5f, -2)).add(new Paragraph("  à  ").add(new Text(evaluation.getSchedule2End()).setUnderline(0.5f, -2))).setFontSize(fontSizeNormal));
//        if (!evaluation.getSchedule3Start().isEmpty())
//            variableShiftsCell.add(new Paragraph("De ").add(new Text(evaluation.getSchedule3Start() + " ").setUnderline(0.5f, -2)).add(new Paragraph("  à  ").add(new Text(evaluation.getSchedule3End()).setUnderline(0.5f, -2))).setFontSize(fontSizeNormal));
//        observationsTable.addCell(variableShiftsCell.setBorder(Border.NO_BORDER).setMarginLeft(10));
//
//        document.add(observationsTable);
//
//        // signature
//        Div signatureBox = new Div()
//                .setKeepTogether(true)
//                .setMargin(5);
//
//        Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{2,1}));
//        signatureTable.setWidth(UnitValue.createPercentValue(100));
//
//        Image signature = new Image(ImageDataFactory.create(evaluation.getTeacherSignImage()));
//        signature.setWidth(20);
//        signature.setAutoScale(true);
//        String dateSignature = LocalDateTime.parse(evaluation.getDate()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//
//        signatureTable.addCell(new Cell().add(signature).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
//        signatureTable.addCell(new Cell().add(new Paragraph(dateSignature).setUnderline()).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
//        signatureTable.addCell(new Cell().add(new Paragraph("Signature de l’enseignant responsable du stagiaire")).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
//        signatureTable.addCell(new Cell().add(new Paragraph("Date").setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
//
//        signatureBox.add(signatureTable);
//        document.add(signatureBox);
//
//        document.close();
//        System.out.println("PDF about employer evaluation is created successfully!");
//        return byteArrayOutputStream.toByteArray();
//    }

    @Test
    void printEmployerEvaluation_success() throws IOException, EvaluationNotFoundException, MissingPermissionsExceptions {
        // arrange
        Teacher teacher = new Teacher();
        teacher.setId(1L);

        Student student = new Student();
        student.setId(1L);
        student.setPrenom("Bob");
        student.setNom("Smith");
        student.setCredentials("etu@etu.com", "123456");
        student.setDiscipline(Discipline.INFORMATIQUE);

        CurriculumVitae cv = new CurriculumVitae();
        cv.setId(1L);
        cv.setStudent(student);
        cv.setStatus(ApprovalStatus.VALIDATED);

        JobOfferApplication jobOfferApplication = new JobOfferApplication();
        jobOfferApplication.setId(1L);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(1L);
        Employeur employeur = new Employeur();
        employeur.setId(1L);
        employeur.setNomCompagnie("TestCompany");
        employeur.setContactPerson("John Doe");
        employeur.setAdresse("1234 rue de la rue");
        employeur.setCity("Montreal");
        employeur.setPostalCode("H1H 1H1");
        employeur.setTelephone("5141234567");
        employeur.setFax("5147654321");
        jobOffer.setEmployeur(employeur);
        jobOfferApplication.setJobOffer(jobOffer);
        jobOfferApplication.setCurriculumVitae(cv);

        InternshipOffer internshipOffer = new InternshipOffer();
        internshipOffer.setId(1L);
        internshipOffer.setJobOfferApplication(jobOfferApplication);

        InternshipEvaluation internshipEvaluation = new InternshipEvaluation();
        internshipEvaluation.setId(1L);
        internshipEvaluation.setInternshipOffer(internshipOffer);
        internshipEvaluation.setTeacher(teacher);

        EvaluationEmployer evaluationEmployer = new EvaluationEmployer();
        evaluationEmployer.setId(1L);
        evaluationEmployer.setTasksMetExpectations(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setIntegrationSupport(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setSupervisionSufficient(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setWorkEnvironment(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setWorkClimate(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setAccessibleTransport(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setSalaryInteresting(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setCommunicationWithSupervisor(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setEquipmentAdequate(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setWorkloadAcceptable(EvaluationOption.TOTAL_AGREEMENT);
        evaluationEmployer.setFirstMonthHours("40");
        evaluationEmployer.setSecondMonthHours("40");
        evaluationEmployer.setThirdMonthHours("40");
        evaluationEmployer.setComments("Good job");
        evaluationEmployer.setPreferredStage(StagePreference.PREMIER_STAGE);
        evaluationEmployer.setNumberOfInterns(NumberOfInterns.UN_ETUDIANT);
        evaluationEmployer.setWillingToRehire(WillingnessToRehire.YES);
        evaluationEmployer.setSchedule1Start("08:00");
        evaluationEmployer.setSchedule1End("16:00");
        evaluationEmployer.setSchedule2Start("08:00");
        evaluationEmployer.setSchedule2End("16:00");
        evaluationEmployer.setSchedule3Start("08:00");
        evaluationEmployer.setSchedule3End("16:00");
        evaluationEmployer.setTeacherSignImage(createSignatureImage());
        evaluationEmployer.setDate(LocalDateTime.now().toString());

        internshipEvaluation.setEvaluationEmployer(evaluationEmployer);

        when(internshipEvaluationRepository.findById(1L)).thenReturn(Optional.of(internshipEvaluation));
        UserDTO userDTO = new UserDTO(
                1L, "username", "pass", Role.TEACHER
        );

        // act
        byte[] pdf = pdfService.printEmployerEvaluation(1L, userDTO);

        // assert
        assertThat(pdf).isNotNull();

    }











}
