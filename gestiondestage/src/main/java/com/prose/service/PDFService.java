package com.prose.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.*;
import com.prose.entity.*;
import com.prose.entity.embedded.InterpersonalRelationshipsEvaluation;
import com.prose.entity.embedded.PersonalSkillsEvaluation;
import com.prose.entity.embedded.ProductivityEvaluation;
import com.prose.entity.embedded.QualityOfWorkEvaluation;
import com.prose.entity.users.Employeur;
import com.prose.entity.users.ProgramManager;
import com.prose.entity.users.Student;
import com.prose.entity.users.auth.Role;
import com.prose.repository.InternshipEvaluationRepository;
import com.prose.repository.InternshipOfferRepository;
import com.prose.service.Exceptions.*;
import com.prose.service.dto.UserDTO;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.time.temporal.ChronoUnit;


@Service
public class PDFService {
    private static final Logger logger = LoggerFactory.getLogger(JobOfferService.class);
    private final InternshipOfferRepository internshipOfferRepository;

    private final InternshipEvaluationRepository internshipEvaluationRepository;

    public PDFService(InternshipOfferRepository internshipOfferRepository, InternshipEvaluationRepository internshipEvaluationRepository) {
        this.internshipOfferRepository = internshipOfferRepository;
        this.internshipEvaluationRepository = internshipEvaluationRepository;
    }

    public byte[] printContract(long internshipId, UserDTO user) throws InternshipNotFoundException, IOException, MissingPermissionsExceptions {
        InternshipOffer internshipOffer = internshipOfferRepository.findById(internshipId).orElseThrow(() -> new InternshipNotFoundException("Internship offer not found"));

        // if contract is not finished
        Student student = internshipOffer.getJobOfferApplication().getCurriculumVitae().getStudent();
        Employeur employer = internshipOffer.getJobOfferApplication().getJobOffer().getEmployeur();
        ProgramManager manager = internshipOffer.getContract() != null ? internshipOffer.getContract().getManager() : null;

        // validation: student
        if (user.role() == Role.STUDENT) {
            if (user.id() != student.getId()) {
                throw new MissingPermissionsExceptions("You are not authorized to access this contract");
            }
        }
        // validation: employer
        if (user.role() == Role.EMPLOYEUR) {
            if (user.id() != employer.getId()) {
                throw new MissingPermissionsExceptions("You are not authorized to access this contract");
            }
        }

        JobOffer jobOffer = internshipOffer.getJobOfferApplication().getJobOffer();
        String studentName = student.getPrenom() + " " + student.getNom();
        String employerName = employer.getNomCompagnie();
        String managerName = manager != null ? manager.getPrenom() + " " + manager.getNom() : "";

        Paragraph notSignedYet = new Paragraph("En attente de signature").setFontColor(ColorConstants.RED);
        Image studentSignature = null;
        Image employerSignature = null;
        Image managerSignature = null;

        String dateStudentSign = "";
        String dateEmployerSign = "";
        String dateManagerSign = "";

        if (internshipOffer.getContract()!= null){
            if (internshipOffer.getContract().getStudentSignImage() != null){
                studentSignature = new Image(ImageDataFactory.create(internshipOffer.getContract().getStudentSignImage()));
                studentSignature.setWidth(20);
                studentSignature.setAutoScale(true);
                dateStudentSign = internshipOffer.getContract().getStudentSign().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            if (internshipOffer.getContract().getEmployerSignImage() != null) {
                employerSignature = new Image(ImageDataFactory.create(internshipOffer.getContract().getEmployerSignImage()));
                employerSignature.setWidth(20);
                employerSignature.setAutoScale(true);
                dateEmployerSign = internshipOffer.getContract().getEmployerSign().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }


            if (internshipOffer.getContract().getManagerSignImage() != null) {
                managerSignature = new Image(ImageDataFactory.create(internshipOffer.getContract().getManagerSignImage()));
                managerSignature.setWidth(20);
                managerSignature.setAutoScale(true);
                dateManagerSign = internshipOffer.getContract().getManagerSign().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        float topMargin = 72;
        float rightMargin = 36;
        float bottomMargin = 36;
        float leftMargin = 36;

        document.setMargins(topMargin, rightMargin, bottomMargin, leftMargin);

        // cover page
        // width: 595.0 height: 842.0
        pdf.addNewPage(PageSize.A4);
        Paragraph title = new Paragraph("CONTRAT DE STAGE");
        title.setFontSize(24);
        title.setBold();

        float pageHeight = pdf.getPage(1).getPageSize().getHeight();


        Div coverPage = new Div()
                .setKeepTogether(true)
                .setWidth(UnitValue.createPercentValue(100))
                .setHeight(pageHeight - topMargin - bottomMargin)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        coverPage.add(title);
        document.add(coverPage);

        // Add contract detail
        Paragraph contractTitle = new Paragraph("ENTENTE DE STAGE INTERVENUE ENTRE LES PARTIES SUIVANTES").setBold().setFontSize(16).setHorizontalAlignment(HorizontalAlignment.LEFT).setMargin(5);
        document.add(contractTitle);
        Div infoTerms = new Div()
                .setKeepTogether(true)
                .setMargin(5)
                .setTextAlignment(TextAlignment.CENTER);

        Paragraph terms1 = new Paragraph("Dans le cadre de la formule ATE, les parties citées ci-dessous :");
        Paragraph terms2 = new Paragraph("Le gestionnaire de stage,").add(new Text(managerName).setUnderline().setBold());
        Paragraph terms3 = new Paragraph("L'employeur, ").add(new Text(employerName).setUnderline().setBold());
        Paragraph terms4 = new Paragraph("L'étudiant(e), ").add(new Text(studentName).setUnderline().setBold());
        Paragraph et = new Paragraph("et").setBold().setMargin(10);
        Paragraph terms5 = new Paragraph("Conviennent des conditions de stage suivantes :").setMargin(10);

        infoTerms.add(terms1);
        infoTerms.add(terms2);
        infoTerms.add(et);
        infoTerms.add(terms3);
        infoTerms.add(et);
        infoTerms.add(terms4);
        infoTerms.add(terms5);
        document.add(infoTerms);

        Div jobOfferBox = new Div()
                .setKeepTogether(true)
                .setMargin(5);

        Table table = new Table(1);
        float tableWidth = 500;
        table.setWidth(tableWidth);
        float tableHeight = 300;
        table.setHeight(tableHeight);


        String daySchedule = String.format("%02d:%02d", jobOffer.getDayScheduleFrom().getHour(), jobOffer.getDayScheduleFrom().getMinute()) + " - " +
                String.format("%02d:%02d", jobOffer.getDayScheduleTo().getHour(), jobOffer.getDayScheduleTo().getMinute());

        String[][] infoStage = {
                {"ENDROIT DU STAGE", jobOffer.getLieu()},
                {"DUREE DU STAGE", "Date de début: " + jobOffer.getDateDebut() + "\n" +  "Date de fin: " + jobOffer.getDateFin()
                        + "\nNombre total de semaines: " +
                        ChronoUnit.WEEKS.between(LocalDate.parse(jobOffer.getDateDebut()), LocalDate.parse(jobOffer.getDateFin()))},
//                        LocalDate.parse(jobOffer.getDateDebut()).until(LocalDate.parse(jobOffer.getDateFin())).getDays() / 7},
                {"HORAIRE DE TRAVAIL", "Horaire de travail: " + daySchedule
                        + "\nNombre total d'heures par semaine: " + jobOffer.getWeeklyHours() + "h" },
                {"SALAIRE", "Salaire horaire: " + jobOffer.getTauxHoraire()}
        };

        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

        for (int i = 0; i < infoStage.length; i++) {
            for (int j = 0; j < infoStage[i].length; j++) {
                Cell cell = new Cell();
                cell.add(new Paragraph(infoStage[i][j]));
                cell.setHorizontalAlignment(HorizontalAlignment.CENTER);

                if (j % 2 == 0) {
                    cell.setFont(boldFont);
                    cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                }
                table.addCell(cell);
            }
        }

        jobOfferBox.add(table);
        document.add(jobOfferBox);


        Div jobDesBox = new Div()
                .setKeepTogether(true)
                .setMargin(5);

        Paragraph taches = new Paragraph("TACHES ET RESPONSABILITES DU STAGIAIRE").setBold().setFontSize(16);
        jobDesBox.add(taches);

        Table offreDescription = new Table(1);
        offreDescription.setWidth(UnitValue.createPercentValue(100));

        offreDescription.addCell(new Cell().add(new Paragraph("Voir l'annexe dans la dernière page")).setMarginBottom(10));
        jobDesBox.add(offreDescription);

        Paragraph responsabilites = new Paragraph("RESPONSABILITES").setBold().setFontSize(12).setHorizontalAlignment(HorizontalAlignment.CENTER).setMarginTop(15);
        Paragraph college = new Paragraph("Le Collège s'engage à:").setBold();
        Paragraph collegeRules = new Paragraph("""
                - Offrir une aide à l'intégration à l'étudiant ou à l'entreprise si nécessaire
                - Rémunérer l'entreprise pour son aide
                - Envoyer un enseignant pour faire une évaluation de l'entreprisey
                - Envoyer un enseignant rencontrer l'étudiant chaque semaine le vendredi afin d'évaluer le bon déroulement du stage
                """);
        Paragraph entreprise = new Paragraph("L’entreprise s’engage à:").setBold();
        Paragraph entrepriseRules = new Paragraph("""
                - Créer un environnement qui favorise l'apprentissage
                - Respecter toutes les lois et politiques
                - Effectuer un suivi à la fin du stage en remplissant un formulaire d'évaluation du stagiaire
                - Permettre à un enseignant de faire une évaluation sur l'entreprise
                """);
        Paragraph etudiant = new Paragraph("L’étudiant s’engage à:").setBold();
        Paragraph etudiantRules = new Paragraph("""
                - Travailler pendant la journée au complète du lundi au jeudi pendant 7 semaines
                - Respecter tout les lois et politiques de l'entreprises
                - Suivre les instructions au mieux de leurs capacités
                - Rencontrer un enseignant chaque semaine le vendredi afin d'évaluer le bon déroulement du stage""");

        jobDesBox.add(responsabilites);
        jobDesBox.add(college);
        jobDesBox.add(collegeRules);
        jobDesBox.add(entreprise);
        jobDesBox.add(entrepriseRules);
        jobDesBox.add(etudiant);
        jobDesBox.add(etudiantRules);

        document.add(jobDesBox);


        Div signatureBox = new Div()
                .setKeepTogether(true)
                .setMargin(5);

        Table signatures = new Table(UnitValue.createPercentArray(2));
        signatures.setWidth(UnitValue.createPercentValue(100));


        Cell cellTitle = new Cell(1, 2);
        cellTitle.add(new Paragraph("SIGNATURES").setBold().setFontSize(16));
        cellTitle.setBackgroundColor(ColorConstants.LIGHT_GRAY);

        Cell cellDesc = new Cell(2,2);
        cellDesc.add(new Paragraph("Les parties s’engagent à respecter cette entente de stage").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(16));
        cellDesc.add(new Paragraph("En foi de quoi les parties ont signé,").setTextAlignment(TextAlignment.LEFT).setBold());

        signatures.addCell(cellTitle);
        signatures.addCell(cellDesc);

        Cell cellEtudiant = new Cell(3,2);
        cellEtudiant.add(new Paragraph("L’étudiant(e):").setBold());
        signatures.addCell(cellEtudiant);
        if (studentSignature == null) {
            signatures.addCell(new Cell().add(notSignedYet));
        } else {
            signatures.addCell(new Cell().add(studentSignature));
        }
        signatures.addCell(new Cell().add(new Paragraph(dateStudentSign)).setVerticalAlignment(VerticalAlignment.MIDDLE));
        signatures.addCell(new Cell().add(new Paragraph(studentName)).setBorderBottom(new SolidBorder(2)));
        signatures.addCell(new Cell().add(new Paragraph("Date")).setBorderBottom(new SolidBorder(2)));

        Cell cellEmployer = new Cell(6,2);
        cellEmployer.add(new Paragraph("L’employeur:").setBold());
        signatures.addCell(cellEmployer);

        if (employerSignature == null) {
            signatures.addCell(new Cell().add(notSignedYet));
        } else {
            signatures.addCell(new Cell().add(employerSignature));
        }

        signatures.addCell(new Cell().add(new Paragraph(dateEmployerSign)).setVerticalAlignment(VerticalAlignment.MIDDLE));
        signatures.addCell(new Cell().add(new Paragraph(employerName)).setBorderBottom(new SolidBorder(2)));
        signatures.addCell(new Cell().add(new Paragraph("Date")).setBorderBottom(new SolidBorder(2)));

        Cell cellGestionnaire = new Cell(9,2);
        cellGestionnaire.add(new Paragraph("Le gestionnaire de stage:").setBold());
        signatures.addCell(cellGestionnaire);

        if (managerSignature == null) {
            signatures.addCell(new Cell().add(notSignedYet));
        } else {
            signatures.addCell(new Cell().add(managerSignature));
        }

        signatures.addCell(new Cell().add(new Paragraph(dateManagerSign)).setVerticalAlignment(VerticalAlignment.MIDDLE));
        signatures.addCell(new Cell().add(new Paragraph(managerName)));
        signatures.addCell(new Cell().add(new Paragraph("Date")));

        signatureBox.add(signatures);
        document.add(signatureBox);

        // add job description as annex
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

        Div annexPage = new Div()
                .setKeepTogether(true)
                .setWidth(UnitValue.createPercentValue(100))
                .setHeight(pageHeight - topMargin - bottomMargin)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        Paragraph annexTitle = new Paragraph("Annexe - DESCRIPTION DE l'OFFRE DE STAGE").setBold().setFontSize(16).setHorizontalAlignment(HorizontalAlignment.CENTER).setMargin(20);
        annexPage.add(annexTitle);
        document.add(annexPage);


        byte[] jobDescription = jobOffer.getPdfDocu().getPdfData();
        if (jobDescription == null || jobDescription.length == 0) {
            throw new IllegalArgumentException("The job description PDF data is null or empty.");
        }
        String fileName = jobOffer.getPdfDocu().getFileName();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("The file name is null or empty.");
        }
        document.close();
        System.out.println("PDF contract created successfully!");

        return mergePdf(byteArrayOutputStream.toByteArray(), jobDescription);

    }

    @Transactional
    public byte[] printInternEvaluation(long internshipOfferId, long userId) throws IOException, EvaluationNotFoundException, MissingPermissionsExceptions {
        //TODO waiting fo merging with eq6-28
        // validation: evaluation belongs to employer, not dont yet

        InternshipEvaluation  internshipEvaluation = internshipEvaluationRepository.findByInternshipOfferId(internshipOfferId).orElseThrow(() -> new EvaluationNotFoundException("Internship evaluation not found"));

        // validation: user is employer
        if (internshipEvaluation.getInternshipOffer().getJobOfferApplication().getJobOffer().getEmployeur().getId() != userId) {
            throw new MissingPermissionsExceptions("You are not authorized to access this evaluation");
        }

        EvaluationIntern evaluationIntern = internshipEvaluation.getEvaluationIntern();
        if (evaluationIntern == null)
            throw new EvaluationNotFoundException("Evaluation not found");


        // info about internship
        Student student = internshipEvaluation.getInternshipOffer().getJobOfferApplication().getCurriculumVitae().getStudent();
        Employeur employer = internshipEvaluation.getInternshipOffer().getJobOfferApplication().getJobOffer().getEmployeur();


            int fontSizeTitle = 20;
            int fontSizeBig = 16;
            int fontSizeNormal = 10;
            int fontSizeTiny = 8;

            // basic info about internship
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(byteArrayOutputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            float topMargin = 72;
            float rightMargin = 36;
            float bottomMargin = 36;
            float leftMargin = 36;

            document.setMargins(topMargin, rightMargin, bottomMargin, leftMargin);

            pdf.addNewPage(PageSize.A4);
            float pageWidth = pdf.getPage(1).getPageSize().getWidth();
            float pageHeight = pdf.getPage(1).getPageSize().getHeight();

            String logoLocation = "src/main/resources/img/cegep_logo.jpg";
            Image logo = new Image(ImageDataFactory.create(logoLocation));
            logo.setWidth(UnitValue.createPercentValue(30));
            logo.setHorizontalAlignment(HorizontalAlignment.LEFT);
            document.add(logo);

            float imgHeight = logo.getImageHeight();
            float yPosition = pageHeight - imgHeight * 0.3f - topMargin + 10;
            Paragraph ate = new Paragraph("Alternance travail-études").setBold();
            document.showTextAligned(ate, pageWidth - rightMargin, yPosition, TextAlignment.RIGHT);

            Paragraph evaluationTitle = new Paragraph("FICHE D’ÉVALUATION DU STAGIAIRE")
                    .setBold()
                    .setFontSize(fontSizeTitle)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10);
            document.add(evaluationTitle);


            Div studentInfoBox = new Div()
                    .setKeepTogether(true)
                    .setBorder(new SolidBorder(1))
                    .setPaddingLeft(3)
                    .setMarginTop(10)
                    .setMarginBottom(10);

            String[] internBasicInfoLabel = {"Programme d’études: ", "Nom de l’entreprise : ", "Nom du superviseur :  ", "Fonction : ", "Téléphone : "};
//
//            for (String label : internBasicInfoLabel) {
//                studentInfoBox.add(new Paragraph(label).setFontSize(fontSizeNormal).add(new Text("info").setUnderline(0.5f, -2)));
//            }
            studentInfoBox.add(new Paragraph(internBasicInfoLabel[0]).setFontSize(fontSizeNormal).add(new Text(evaluationIntern.getProgram()).setUnderline(0.5f, -2)));
            studentInfoBox.add(new Paragraph(internBasicInfoLabel[1]).setFontSize(fontSizeNormal).add(new Text(evaluationIntern.getCompanyName()).setUnderline(0.5f, -2)));
            studentInfoBox.add(new Paragraph(internBasicInfoLabel[2]).setFontSize(fontSizeNormal).add(new Text(evaluationIntern.getSupervisorName()).setUnderline(0.5f, -2)));
            studentInfoBox.add(new Paragraph(internBasicInfoLabel[3]).setFontSize(fontSizeNormal).add(new Text(evaluationIntern.getFunction()).setUnderline(0.5f, -2)));
            studentInfoBox.add(new Paragraph(internBasicInfoLabel[4]).setFontSize(fontSizeNormal).add(new Text(evaluationIntern.getTelephone().replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3")).setUnderline(0.5f, -2)));

            document.add(studentInfoBox);

            Paragraph noticeCheckBox = new Paragraph("Veuillez cocher les comportements observés chez le stagiaire et formuler des commentaires s’il y a lieu.")
                    .setBold()
                    .setFontSize(fontSizeNormal)
                    .setMarginTop(10)
                    .setMarginBottom(10)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT);
            document.add(noticeCheckBox);


            String[] headers = {"Le stagiaire a été en mesure de :", "Totalement en accord", "Plutôt en accord", "Plutôt en désaccord", "Totalement en désaccord", "N/A*"};
            // productivity evaluation
            String productivityTitle = "1. PRODUCTIVITÉ";
            String productivitySubTitle = "Capacité d’optimiser son rendement au travail";
            String[] productivityEvaluations = {
                    "a) \tplanifier et organiser son travail de façon efficace",
                    "b) \tcomprendre rapidement les directives relatives à son travail",
                    "c) \tmaintenir un rythme de travail soutenu",
                    "d) \tétablir ses priorités",
                    "e) \trespecter ses échéanciers"
            };
            // TODO get evaluations from database
//            int[] results = {1, 0, 2, 3, 4};
        ProductivityEvaluation productivityEvaluation = evaluationIntern.getProductivityEvaluation();
            int[] results = {
                    productivityEvaluation.getProduction_a().ordinal(),
                    productivityEvaluation.getProduction_b().ordinal(),
                    productivityEvaluation.getProduction_c().ordinal(),
                    productivityEvaluation.getProduction_d().ordinal(),
                    productivityEvaluation.getProduction_e().ordinal()
            };

            String productivityComments = evaluationIntern.getProductivityComments();

            Div productivityEvaluationBox = generateEvaluationTable(productivityTitle, productivitySubTitle, headers, productivityEvaluations, results, productivityComments);
            document.add(productivityEvaluationBox);

            // legend box
            Div legendBox = new Div()
                    .setKeepTogether(true)
                    .setMargin(5);

            Paragraph lengendNA = new Paragraph("* N/A = non applicable")
                    .setBold()
                    .setFontSize(fontSizeTiny)
                    .setMarginTop(10)
                    .setMarginBottom(10)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT);
            legendBox.add(lengendNA);
            document.add(legendBox);


            // work quality evaluation
            String workQualityTitle = "2. QUALITÉ DU TRAVAIL";
            String workQualitySubTitle = "Capacité de s’acquitter des tâches sous sa responsabilité en s’imposant personnellement des normes de qualité";
            String[] workQualityEvaluations = {
                    "a) \trespecter les mandats qui lui ont été confiés",
                    "b) \tporter attention aux détails dans la réalisation de ses tâches",
                    "c) \tvérifier son travail, s’assurer que rien n’a été oublié",
                    "d) \trechercher des occasions de se perfectionner",
                    "e) \tfaire une bonne analyse des problèmes rencontrés"
            };
            // TODO get evaluations from database
//            int[] workQualityResults = {1, 0, 2, 3, 4};
        QualityOfWorkEvaluation qualityOfWorkEvaluation = evaluationIntern.getQualityOfWorkEvaluation();
        int[] workQualityResults = {
                qualityOfWorkEvaluation.getQuality_a().ordinal(),
                qualityOfWorkEvaluation.getQuality_b().ordinal(),
                qualityOfWorkEvaluation.getQuality_c().ordinal(),
                qualityOfWorkEvaluation.getQuality_d().ordinal(),
                qualityOfWorkEvaluation.getQuality_e().ordinal()
        };
            String workQualityComments = evaluationIntern.getQualityOfWorkComments();


            Div workQualityEvaluationBox = generateEvaluationTable(workQualityTitle, workQualitySubTitle, headers, workQualityEvaluations, workQualityResults, workQualityComments);
            document.add(workQualityEvaluationBox);

            // interpersonal skills evaluation
            String interpersonalSkillsTitle = "3. QUALITÉS DES RELATIONS INTERPERSONNELLES";
            String interpersonalSkillsSubTitle = "Capacité d’établir des interrelations harmonieuses dans son milieu de travail";
            String[] interpersonalSkillsEvaluations = {
                    "a) \tétablir facilement des contacts avec les gens",
                    "b) \tcontribuer activement au travail d’équipe",
                    "c) \ts’adapter facilement à la culture de l’entreprise",
                    "d) \taccepter les critiques constructives",
                    "f) \tfaire preuve d’écoute active en essayant de comprendre le point de vue de l’autreé"
            };
            // TODO get evaluations from database
//            int[] interpersonalSkillsResults = {1, 0, 2, 3, 4};
        InterpersonalRelationshipsEvaluation interpersonalRelationshipsEvaluation = evaluationIntern.getInterpersonalRelationshipsEvaluation();
            int[] interpersonalSkillsResults = {
                    interpersonalRelationshipsEvaluation.getInterPersonal_a().ordinal(),
                    interpersonalRelationshipsEvaluation.getInterPersonal_b().ordinal(),
                    interpersonalRelationshipsEvaluation.getInterPersonal_c().ordinal(),
                    interpersonalRelationshipsEvaluation.getInterPersonal_d().ordinal(),
                    interpersonalRelationshipsEvaluation.getInterPersonal_e().ordinal(),
                    interpersonalRelationshipsEvaluation.getInterPersonal_f().ordinal()
            };
            String interpersonalSkillsComments = evaluationIntern.getInterpersonalRelationshipsComments();

            Div interpersonalSkillsEvaluationBox = generateEvaluationTable(interpersonalSkillsTitle, interpersonalSkillsSubTitle, headers, interpersonalSkillsEvaluations, interpersonalSkillsResults, interpersonalSkillsComments);
            document.add(interpersonalSkillsEvaluationBox);


            // personal skills
            String personalSkillsTitle = "4. HABILETÉS PERSONNELLES";
            String personalSkillsSubTitle = "Capacité de faire preuve d’attitudes ou de comportements matures et responsables";
            String[] personalSkillsEvaluations = {
                    "a) \tdémontrer de l’intérêt et de la motivation au travail",
                    "b) \texprimer clairement ses idées",
                    "c) \tfaire preuve d’initiative",
                    "e) \tdémontrer un bon sens des responsabilités ne requérant qu’un minimum de supervision",
                    "f) \têtre ponctuel et assidu à son travail"
            };

            // TODO get evaluations from database
//            int[] personalSkillsResults = {1, 0, 2, 3, 4};
            PersonalSkillsEvaluation personalSkillsEvaluation = evaluationIntern.getPersonalSkillsEvaluation();
            int[] personalSkillsResults = {
                    personalSkillsEvaluation.getPersonalStills_a().ordinal(),
                    personalSkillsEvaluation.getPersonalStills_b().ordinal(),
                    personalSkillsEvaluation.getPersonalStills_c().ordinal(),
                    personalSkillsEvaluation.getPersonalStills_d().ordinal(),
                    personalSkillsEvaluation.getPersonalStills_e().ordinal(),
                    personalSkillsEvaluation.getPersonalStills_f().ordinal()
            };
            String personalSkillsComments = evaluationIntern.getPersonalSkillsComments(); ;

            Div personalSkillsEvaluationBox = generateEvaluationTable(personalSkillsTitle, personalSkillsSubTitle, headers, personalSkillsEvaluations, personalSkillsResults, personalSkillsComments);
            document.add(personalSkillsEvaluationBox);


            // appreciation evaluation
            Div appreciationEvaluationBox = new Div()
                    .setKeepTogether(true)
                    .setMargin(5);
            Table appreciationTable = new Table(1);
            appreciationTable.setWidth(UnitValue.createPercentValue(100));

            String appreciation = "APPRÉCIATION GLOBALE DU STAGIAIRE";
            appreciationTable.addCell(new Cell().add(new Paragraph(appreciation).setBold().setFontSize(fontSizeBig).setTextAlignment(TextAlignment.CENTER)));

            Table appreciationCheckBoxes = new Table(UnitValue.createPercentArray(new float[]{4, 1}));
            appreciationCheckBoxes.setWidth(UnitValue.createPercentValue(100));
            String[] appreciationEvaluations = {
                    "Les habiletés démontrées dépassent de beaucoup les attentes",
                    "Les habiletés démontrées dépassent les attentes",
                    "Les habiletés démontrées répondent pleinement aux attentes",
                    "Les habiletés démontrées répondent partiellement aux attentes",
                    "Les habiletés démontrées répondent peu aux attentes",
            };
            // ----------------------------------------------------------todo -----------------------------------------------
            int appreciationResults = evaluationIntern.getOverallAppreciation().ordinal();
            IntStream.range(0, appreciationEvaluations.length).forEach(index -> {
                String evaluation = appreciationEvaluations[index];
                appreciationCheckBoxes.addCell(new Cell()
                        .add(new Paragraph(evaluation).setFontSize(fontSizeNormal))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBorder(Border.NO_BORDER));

                Div checkbox = new Div()
                        .setWidth(5)
                        .setHeight(5);
                checkbox.setBorder(new SolidBorder(1));
                if (appreciationResults == index)
                    checkbox.setBackgroundColor(ColorConstants.BLACK);

                appreciationCheckBoxes.addCell(new Cell()
                        .add(checkbox)
                        .setPadding(5)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(Border.NO_BORDER));
            });
            appreciationTable.addCell(new Cell().add(appreciationCheckBoxes));

            Table appreciationCommentsTable = new Table(1);
            appreciationCommentsTable.addCell(new Cell().add(new Paragraph("Précisez votre appréciation: ")).setBorder(Border.NO_BORDER));
            String appreciationComments = evaluationIntern.getOverallComments();
            appreciationCommentsTable.addCell(new Cell().add(new Paragraph(appreciationComments).setUnderline()).setBorder(Border.NO_BORDER));
            appreciationTable.addCell(new Cell().add(appreciationCommentsTable));

            Table confirmationWithIntern = new Table(UnitValue.createPercentArray(new float[]{4, 1, 1, 1, 1}));
            confirmationWithIntern.addCell(new Cell().add(new Paragraph("Cette évaluation a été discutée avec le stagiaire :").setBold()).setBorder(Border.NO_BORDER));
            String[] confirmationWithInternText = {"Oui", "Non"};
            boolean confirmationWithInternResult = evaluationIntern.isEvaluationDiscussedWithIntern();
            for (int i = 0; i < confirmationWithInternText.length; i++) {
                confirmationWithIntern.addCell(new Cell().add(new Paragraph(confirmationWithInternText[i])).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE));
                Div checkbox = new Div()
                        .setWidth(5)
                        .setHeight(5)
                        .setMarginRight(10);
                checkbox.setBorder(new SolidBorder(1));
                if (i == 0 && confirmationWithInternResult)
                    checkbox.setBackgroundColor(ColorConstants.BLACK);
                if (i == 1 && !confirmationWithInternResult)
                    checkbox.setBackgroundColor(ColorConstants.BLACK);
                confirmationWithIntern.addCell(new Cell().add(checkbox).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            }
            appreciationTable.addCell(new Cell().add(confirmationWithIntern));
            appreciationEvaluationBox.add(appreciationTable);
            document.add(appreciationEvaluationBox);

            // num of hours
            Div numOfHoursBox = new Div()
                    .setBorder(new SolidBorder(0.5f))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setKeepTogether(true)
                    .setMargin(5);
            Table numOfHoursTable = new Table(UnitValue.createPercentArray(new float[]{5, 1}));
            numOfHoursTable.setWidth(UnitValue.createPercentValue(100));

            double numberOfHours = evaluationIntern.getSupervisionHoursPerWeek();
            Paragraph numOfHoursText = new Paragraph("Veuillez indiquer le nombre d’heures réel par semaine d’encadrement accordé au stagiaire : ")
                    .setBold()
                    .setFontSize(fontSizeNormal)
                    .setMargin(5)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT);
            Paragraph numOfHoursText2 = new Paragraph(numberOfHours + "").setFontSize(fontSizeNormal).setUnderline();
            numOfHoursTable.addCell(new Cell().add(numOfHoursText).setBorder(Border.NO_BORDER));
            numOfHoursTable.addCell(new Cell().add(numOfHoursText2).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            numOfHoursBox.add(numOfHoursTable);
            document.add(numOfHoursBox);

            // signature
            Div signatureBox = new Div()
                    .setKeepTogether(true)
                    .setMargin(5);
            Table signatureTable = new Table(1);
            signatureTable.setWidth(UnitValue.createPercentValue(100));
            String[] welcomeNextIntern = {"Oui", "Non", "Peut-être"};
            Table welcomeNextInternTable = new Table(6);
            welcomeNextInternTable.setWidth(UnitValue.createPercentValue(100));
            welcomeNextInternTable.addCell(new Cell(1, 6).add(new Paragraph("L’entreprise aimerait accueillir cet élève pour son prochain stage :")
                            .setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER));
            int willingnessToRehireOrdinal = evaluationIntern.getWillingnessToRehire().ordinal();

            for (int i = 0; i < welcomeNextIntern.length; i++) {
                welcomeNextInternTable.addCell(new Cell().add(new Paragraph(welcomeNextIntern[i])).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
                Div checkbox = new Div()
                        .setWidth(5)
                        .setHeight(5)
                        .setMarginRight(10);
                checkbox.setBorder(new SolidBorder(1));
                if (i == willingnessToRehireOrdinal)
                    checkbox.setBackgroundColor(ColorConstants.BLACK);
                welcomeNextInternTable.addCell(new Cell().add(checkbox).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER));
            }
            signatureTable.addCell(new Cell().add(welcomeNextInternTable));

            Cell commentsEmployer = new Cell();
            Paragraph commentEmployer = new Paragraph("La formation technique du stagiaire était-elle suffisante pour accomplir le mandat de stage?");
            String commentEmployerContent = evaluationIntern.getTechnicalTrainingComments();
            commentsEmployer.add(commentEmployer).add(new Paragraph(commentEmployerContent).setUnderline());
            signatureTable.addCell(commentsEmployer);

            Table employerInfoTable = new Table(UnitValue.createPercentArray(2));
            employerInfoTable.setWidth(UnitValue.createPercentValue(100));
            // TODO
            String employerName = evaluationIntern.getName();
            String fonctionEmployer = evaluationIntern.getFunction();
            String signDate = evaluationIntern.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            Image signature = new Image(ImageDataFactory.create(evaluationIntern.getEmployerSignature())).setWidth(100).setMarginLeft(10);
            String signatureDate = "Date de signature";
            employerInfoTable.addCell(new Cell().add(new Paragraph("Nom : ").add(new Paragraph(employerName.toUpperCase()).setUnderline())).setBorder(Border.NO_BORDER));
            employerInfoTable.addCell(new Cell().add(new Paragraph("Fonction : ").add(new Paragraph(fonctionEmployer).setUnderline())).setBorder(Border.NO_BORDER));

            employerInfoTable.addCell(new Cell().add(new Paragraph("Signature  : __").add(signature).add("__")).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            employerInfoTable.addCell(new Cell().add(new Paragraph("Date : ").add(new Paragraph(signDate).setUnderline())).setBorder(Border.NO_BORDER));

            signatureTable.addCell(employerInfoTable);
            signatureBox.add(signatureTable);
            document.add(signatureBox);

            // info about prof
            Div profInfoBox = new Div()
                    .setKeepTogether(true)
                    .setPaddingLeft(3)
                    .setMarginTop(10)
                    .setMarginBottom(10);

            String profName = "François Lacoursière";
            String profEmail = "francois.lacoursiere@claurendeau.qc.ca";
            Table profInfoTable = new Table(2);
            profInfoTable.addCell(new Cell().add(new Paragraph("Veuillez retourner ce formulaire à :").setBold()).setBorder(Border.NO_BORDER));
            profInfoTable.addCell(new Cell().add(new Paragraph(profName).add(new Paragraph(profEmail))).setBorder(Border.NO_BORDER));

            profInfoBox.add(profInfoTable);
            document.add(profInfoBox);

            Div footer = new Div()
                    .setKeepTogether(true)
                    .setMarginTop(10)
                    .setMarginBottom(10);

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            Paragraph footerText = new Paragraph("Nous vous remercions de votre appui !").setBold();
            footer.add(footerText);
            Paragraph infoCegep = new Paragraph("Cégep André-Laurendeau\t\t\t\n" +
                    "ALTERNANCE TRAVAIL-ÉTUDES\n" +
                    today).setFontSize(fontSizeNormal);
            footer.add(infoCegep);
            document.add(footer);


            document.close();
            System.out.println("PDF about intern evaluation is created successfully!");

            return byteArrayOutputStream.toByteArray();

    }



    public byte[] printEmployerEvaluation(long evaluationId, UserDTO user) throws IOException, EvaluationNotFoundException, MissingPermissionsExceptions {
        InternshipEvaluation internshipEvaluation = internshipEvaluationRepository.findById(evaluationId).orElseThrow(() -> new EvaluationNotFoundException("Internship offer not found"));

        if ( user.role() == Role.TEACHER && internshipEvaluation.getTeacher().getId() != user.id()) {
            throw new MissingPermissionsExceptions("You are not authorized to access this evaluation");
        }

        // evaluation info
        EvaluationEmployer evaluation = internshipEvaluation.getEvaluationEmployer();
        Employeur employeur = internshipEvaluation.getInternshipOffer().getJobOfferApplication().getJobOffer().getEmployeur();
        Student student = internshipEvaluation.getInternshipOffer().getJobOfferApplication().getCurriculumVitae().getStudent();
        JobOffer jobOffer = internshipEvaluation.getInternshipOffer().getJobOfferApplication().getJobOffer();


            int fontSizeTitle = 20;
            int fontSizeBig = 16;
            int fontSizeNormal = 10;
            int fontSizeTiny = 8;

            // basic info about internship
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(byteArrayOutputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            float topMargin = 72;
            float rightMargin = 36;
            float bottomMargin = 36;
            float leftMargin = 36;

            document.setMargins(topMargin, rightMargin, bottomMargin, leftMargin);

            pdf.addNewPage(PageSize.A4);
            float pageWidth = pdf.getPage(1).getPageSize().getWidth();
            float pageHeight = pdf.getPage(1).getPageSize().getHeight();

            String logoLocation = "src/main/resources/img/cegep_logo.jpg";
            Image logo = new Image(ImageDataFactory.create(logoLocation));
            logo.setWidth(UnitValue.createPercentValue(30));
            logo.setHorizontalAlignment(HorizontalAlignment.LEFT);
            document.add(logo);

            float imgHeight = logo.getImageHeight();
            float yPosition = pageHeight - imgHeight * 0.3f - topMargin + 10;
            Paragraph ate = new Paragraph("Alternance travail-études").setBold();
            document.showTextAligned(ate, pageWidth - rightMargin, yPosition , TextAlignment.RIGHT);

            Paragraph evaluationTitle = new Paragraph("ÉVALUATION DU MILIEU DE STAGE")
                    .setBold()
                    .setFontSize(fontSizeTitle)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10);
            document.add(evaluationTitle);


            // base info about employer
            Div employerInfoBox = new Div()
                    .setKeepTogether(true)
                    .setMarginTop(5)
                    .setMarginBottom(5);
            employerInfoBox.add(new Paragraph("IDENTIFICATION DE L’ENTREPRISE").setBold().setMarginTop(10));

            String[] employerBasicInfoLabel1 = {"Nom de l’entreprise : ", "Personne contact : ", "Adresse : "};
            String[] employerBasicInfoLabel2 = {"Ville : ", "Code postal : ", "Téléphone : ", "Télécopieur : "};

            Table employerInfoTable = new Table(UnitValue.createPercentArray(2));
            employerInfoTable.setWidth(UnitValue.createPercentValue(100));
            employerInfoTable.setBorder(new SolidBorder(0.5f));

            employerInfoTable.addCell(new Cell(1,2).add(new Paragraph(employerBasicInfoLabel1[0]).add(new Text(employeur.getNomCompagnie()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
            employerInfoTable.addCell(new Cell(1,2).add(new Paragraph(employerBasicInfoLabel1[1]).
                    add(new Text(employeur.getContactPerson()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
            employerInfoTable.addCell(new Cell(1,2).add(new Paragraph(employerBasicInfoLabel1[2]).add(new Text(employeur.getAdresse()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));

            employerInfoTable.addCell(new Cell().add(new Paragraph(employerBasicInfoLabel2[0]).add(new Text(employeur.getCity()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
            employerInfoTable.addCell(new Cell().add(new Paragraph(employerBasicInfoLabel2[1]).add(new Text(employeur.getPostalCode()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
            employerInfoTable.addCell(new Cell().add(new Paragraph(employerBasicInfoLabel2[2]).add(new Text(employeur.getTelephone().replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3")).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
            employerInfoTable.addCell(new Cell().add(new Paragraph(employerBasicInfoLabel2[3]).add(new Text(employeur.getFax().replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3")).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));

            employerInfoBox.add(employerInfoTable);
            document.add(employerInfoBox);

            // about intern
            Div internInfoBox = new Div()
                    .setKeepTogether(true)
                    .setMarginTop(5)
                    .setMarginBottom(5);
            internInfoBox.add(new Paragraph("IDENTIFICATION DU STAGIAIRE").setBold().setMarginTop(10));

            String[] internBasicInfoLabel = {"Nom du stagiaire : ", "Date du stage : ", "Stage (encercler): "};
            Table internInfoTable = new Table(UnitValue.createPercentArray(new float[]{2, 1, 4}));
            internInfoTable.setWidth(UnitValue.createPercentValue(100));
            internInfoTable.setBorder(new SolidBorder(0.5f));


            String dateStage = jobOffer.getDateDebut() + " à " + jobOffer.getDateFin();
            internInfoTable.addCell(new Cell(1,3).add(new Paragraph(internBasicInfoLabel[0]).add(new Text(student.getPrenom() + " " + student.getNom()).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
            internInfoTable.addCell(new Cell(1,3).add(new Paragraph(internBasicInfoLabel[1]).add(new Text(dateStage).setUnderline(0.5f, -2))).setBorder(Border.NO_BORDER));
            Div chosenInternNum = new Div()
                    .setWidth(25)
                    .setHeight(25)
                    .setBorder(new SolidBorder(1))
                    .setBorderRadius(new BorderRadius(50));
            // TODO from database
            internInfoTable.addCell(new Cell().add(new Paragraph(internBasicInfoLabel[2])).setBorder(Border.NO_BORDER));
            if (evaluation.getStageNumber() == StageNumber.STAGE_1) {
                chosenInternNum.add(new Paragraph("1").setMarginLeft(10));
                internInfoTable.addCell(new Cell().add(chosenInternNum).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
                internInfoTable.addCell(new Cell().add(new Paragraph("2")).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            }
            else {
                chosenInternNum.add(new Paragraph("2").setMarginLeft(10));
                internInfoTable.addCell(new Cell().add(new Paragraph("1")).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
                internInfoTable.addCell(new Cell().add(chosenInternNum).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            }
            internInfoBox.add(internInfoTable);
            document.add(internInfoBox);

            // evalution
            Div evaluationBox1 = new Div()
                    .setKeepTogether(true)
                    .setMarginTop(5)
                    .setMarginBottom(5);

            evaluationBox1.add(new Paragraph("ÉVALUATION").setBold().setMarginTop(10));
            String[] headers = {"", "Totalement en accord", "Plutôt en accord", "Plutôt désaccord*", "Totalement désaccord*", "Impossible de se prononcer"};
            String[] evalutionsPart1 = {
                    "Les tâches confiées au stagiaire sont conformes aux tâches annoncées dans l’entente de stage.",
                    "Des mesures d’accueil facilitent l’intégration du nouveau stagiaire.",
                    "Le temps réel consacré à l’encadrement du stagiaire est suffisant."
            };
            String[] evalutionsPart2 = {
                    "L’environnement de travail respecte les normes d’hygiène et de sécurité au travail.",
                    "Le climat de travail est agréable.",
                    "Le milieu de stage est accessible par transport en commun.",
                    "Le salaire offert est intéressant pour le stagiaire.",
            };
            String[] evalutionsPart3 = {
                    "La communication avec le superviseur de stage facilite le déroulement du stage.",
                    "L’équipement fourni est adéquat pour réaliser les tâches confiées.",
                    "Le volume de travail est acceptable."
            };


            int[] evaluationResultsPart1 = {evaluation.getTasksMetExpectations().ordinal(), evaluation.getIntegrationSupport().ordinal(), evaluation.getSupervisionSufficient().ordinal()};

            int[] evaluationResultsPart2 = {
                    evaluation.getWorkEnvironment().ordinal(),
                    evaluation.getWorkClimate().ordinal(),
                    evaluation.getAccessibleTransport().ordinal(),
                    evaluation.getSalaryInteresting().ordinal()
            };

            int[] evaluationResultsPart3 = {evaluation.getCommunicationWithSupervisor().ordinal(), evaluation.getEquipmentAdequate().ordinal(), evaluation.getWorkloadAcceptable().ordinal()};

            Table evaluationTablePage1 = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1, 1}));
            evaluationTablePage1.setWidth(UnitValue.createPercentValue(100));
            evaluationTablePage1.setBorder(new SolidBorder(0.5f));

            addHeaderCell(evaluationTablePage1, headers);

            IntStream.range(0, evalutionsPart1.length).forEach(index -> {
                addRowCell(evaluationTablePage1, evalutionsPart1[index], evaluationResultsPart1[index]);
            });
            evaluationTablePage1.addCell(new Cell()
                    .add(new Paragraph("Préciser le nombre d’heures/semaine :").setFontSize(8))
                    .add(new Paragraph("Premier mois : ").setFontSize(8).add(new Text(evaluation.getFirstMonthHours()).setUnderline(0.5f, -2))
                            .setMarginLeft(10))
                    .add(new Paragraph("Deuxième mois : ").setFontSize(8).add(new Text(evaluation.getSecondMonthHours()).setUnderline(0.5f, -2))
                            .setMarginLeft(10))
                    .add(new Paragraph("Troisième mois : ").setFontSize(8).add(new Text(evaluation.getThirdMonthHours()).setUnderline(0.5f, -2))
                            .setMarginLeft(10))
                    .setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            evaluationTablePage1.addCell(new Cell(1,5).setBorder(Border.NO_BORDER));
            evaluationBox1.add(evaluationTablePage1);
            document.add(evaluationBox1);


            Div evaluationBox2 = new Div()
                    .setKeepTogether(true)
                    .setMarginTop(5)
                    .setMarginBottom(5);

            Table evaluationTablePage2 = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1, 1}));
            evaluationTablePage2.setWidth(UnitValue.createPercentValue(100));
            evaluationTablePage2.setBorder(new SolidBorder(0.5f));

            addHeaderCell(evaluationTablePage2, headers);

            IntStream.range(0, evalutionsPart2.length).forEach(index -> {
                addRowCell(evaluationTablePage2, evalutionsPart2[index], evaluationResultsPart2[index]);
            });
            double salary = jobOffer.getTauxHoraire();
            evaluationTablePage2.addCell(new Cell().add(new Paragraph("Préciser : ").setFontSize(8).add(new Text(salary+"").setUnderline(0.5f, -2)).add(new Text("  /l'heure"))).setBorder(Border.NO_BORDER));
            evaluationTablePage2.addCell(new Cell(1,5).setBorder(Border.NO_BORDER));

            IntStream.range(0, evalutionsPart3.length).forEach(index -> {
                addRowCell(evaluationTablePage2, evalutionsPart3[index], evaluationResultsPart3[index]);
            });
            evaluationBox2.add(evaluationTablePage2);
            evaluationBox2.add(new Paragraph("* Expliquer dans la section commentaires").setBold());
            document.add(evaluationBox2);

            // comments
            document.add(new Paragraph("Commentaires :").setBold());
            document.add(new Paragraph(evaluation.getComments()).setUnderline());


            // observations
            document.add(new Paragraph("OBSERVATIONS GÉNÉRALES").setBold().setMarginTop(10));
            Table observationsTable = new Table(UnitValue.createPercentArray(new float[]{3,2,1}));
            observationsTable.setWidth(UnitValue.createPercentValue(100));
            observationsTable.setBorder(new SolidBorder(1));

            observationsTable.addCell(new Cell(2, 1).add(new Paragraph("Ce milieu est à privilégier pour le:").setFontSize(fontSizeNormal)).setBorder(Border.NO_BORDER));
            String[] stages = {"premier stage", "deuxième stage"};
            int stage = evaluation.getPreferredStage().ordinal();

            IntStream.range(0, stages.length).forEach(index -> {
                observationsTable.addCell(new Cell().add(new Paragraph(stages[index]).setFontSize(fontSizeNormal)).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER));
                Div checkbox = new Div()
                        .setWidth(5)
                        .setHeight(5)
                        .setMarginLeft(10);
                checkbox.setBorder(new SolidBorder(1));
                if (index == stage) {
                    checkbox.setBackgroundColor(ColorConstants.BLACK);
                }
                observationsTable.addCell(new Cell().add(checkbox).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
            });

            observationsTable.addCell(new Cell(1, 3).setBorder(new SolidBorder(1)));
            observationsTable.addCell(new Cell(4, 1).add(new Paragraph("Ce milieu est ouvert à accueillir :").setFontSize(fontSizeNormal)).setBorder(Border.NO_BORDER));
            String[] numInterns = {"un stagiaire", "deux stagiaires", "trois stagiaires", "plus de trois"};
            int numOfInterns = evaluation.getNumberOfInterns().ordinal();


            IntStream.range(0, numInterns.length).forEach(index -> {
                observationsTable.addCell(new Cell().add(new Paragraph(numInterns[index]).setFontSize(fontSizeNormal)).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER));
                Div checkbox = new Div()
                        .setWidth(5)
                        .setHeight(5)
                        .setMarginLeft(10);
                checkbox.setBorder(new SolidBorder(1));
                if (index == numOfInterns) {
                    checkbox.setBackgroundColor(ColorConstants.BLACK);
                }
                observationsTable.addCell(new Cell().add(checkbox).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
            });

            observationsTable.addCell(new Cell(1, 3).setBorder(new SolidBorder(1)));
            observationsTable.addCell(new Cell(2, 1).add(new Paragraph("Ce milieu désire accueillir le même stagiaire pour un prochain stage").setFontSize(fontSizeNormal)).setBorder(Border.NO_BORDER));
            String[] yesNo = {"oui", "non"};
            int willTakeSameIntern = evaluation.getWillingToRehire().ordinal();

            IntStream.range(0, yesNo.length).forEach(index -> {
                observationsTable.addCell(new Cell().add(new Paragraph(yesNo[index]).setFontSize(fontSizeNormal)).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER));
                Div checkbox = new Div()
                        .setWidth(5)
                        .setHeight(5)
                        .setMarginLeft(10);
                checkbox.setBorder(new SolidBorder(1));
                if (index == willTakeSameIntern) {
                    checkbox.setBackgroundColor(ColorConstants.BLACK);
                }
                observationsTable.addCell(new Cell().add(checkbox).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
            });


            observationsTable.addCell(new Cell(1, 3).setBorder(new SolidBorder(1)));
            observationsTable.addCell(new Cell(2, 1).add(new Paragraph("Ce milieu offre des quarts de travail variables : ").setFontSize(fontSizeNormal)).setBorder(Border.NO_BORDER));

            int variableShifts = !evaluation.getSchedule1Start().isEmpty() && !evaluation.getSchedule2Start().isEmpty() ? 1 : 0;

            IntStream.range(0, yesNo.length).forEach(index -> {
                observationsTable.addCell(new Cell().add(new Paragraph(yesNo[index]).setFontSize(fontSizeNormal)).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER));
                Div checkbox = new Div()
                        .setWidth(5)
                        .setHeight(5)
                        .setMarginLeft(10);
                checkbox.setBorder(new SolidBorder(1));
                if (index != variableShifts) {
                    checkbox.setBackgroundColor(ColorConstants.BLACK);
                }
                observationsTable.addCell(new Cell().add(checkbox).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
            });

            Cell variableShiftsCell = new Cell(2, 3);
            if (!evaluation.getSchedule1Start().isEmpty())
                variableShiftsCell.add(new Paragraph("De ").add(new Text(evaluation.getSchedule1Start() + " ").setUnderline(0.5f, -2)).add(new Paragraph("  à  ").add(new Text(evaluation.getSchedule1End()).setUnderline(0.5f, -2))).setFontSize(fontSizeNormal));
            if (!evaluation.getSchedule2Start().isEmpty())
                variableShiftsCell.add(new Paragraph("De ").add(new Text(evaluation.getSchedule2Start() + " ").setUnderline(0.5f, -2)).add(new Paragraph("  à  ").add(new Text(evaluation.getSchedule2End()).setUnderline(0.5f, -2))).setFontSize(fontSizeNormal));
            if (!evaluation.getSchedule3Start().isEmpty())
                variableShiftsCell.add(new Paragraph("De ").add(new Text(evaluation.getSchedule3Start() + " ").setUnderline(0.5f, -2)).add(new Paragraph("  à  ").add(new Text(evaluation.getSchedule3End()).setUnderline(0.5f, -2))).setFontSize(fontSizeNormal));
            observationsTable.addCell(variableShiftsCell.setBorder(Border.NO_BORDER).setMarginLeft(10));

            document.add(observationsTable);

            // signature
            Div signatureBox = new Div()
                    .setKeepTogether(true)
                    .setMargin(5);

            Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{2,1}));
            signatureTable.setWidth(UnitValue.createPercentValue(100));

            Image signature = new Image(ImageDataFactory.create(evaluation.getTeacherSignImage()));
            signature.setWidth(20);
            signature.setAutoScale(true);
           String dateSignature = LocalDateTime.parse(evaluation.getDate()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            signatureTable.addCell(new Cell().add(signature).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
            signatureTable.addCell(new Cell().add(new Paragraph(dateSignature).setUnderline()).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
            signatureTable.addCell(new Cell().add(new Paragraph("Signature de l’enseignant responsable du stagiaire")).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
            signatureTable.addCell(new Cell().add(new Paragraph("Date").setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));

            signatureBox.add(signatureTable);
            document.add(signatureBox);

            document.close();
            System.out.println("PDF about employer evaluation is created successfully!");
            return byteArrayOutputStream.toByteArray();
    }


    private static Div generateEvaluationTable(String title, String subTitle, String[] headers, String[] evalutions, int[] evaluationResults, String comments) {
        Div box = new Div()
                .setKeepTogether(true)
                .setMargin(5);

        Table table = new Table(1);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);

        if (title != null && subTitle != null) {
            Paragraph productivityTitle = new Paragraph(title)
                    .setBold()
                    .setFontSize(16)
                    .setMarginTop(10);

            Paragraph productivitytitle2 = new Paragraph(subTitle);

            table.addCell(new Cell().
                    add(productivityTitle)
                    .add(productivitytitle2)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        Table tableCheckBoxes = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1, 1}));

        addHeaderCell(tableCheckBoxes, headers);

        IntStream.range(0, evalutions.length).forEach(index -> {
            addRowCell(tableCheckBoxes, evalutions[index], evaluationResults[index]);
        });

        table.addCell(new Cell().add(tableCheckBoxes));

        // comments
        Cell cellComments = new Cell();
        Paragraph comment = new Paragraph("Commentaires:").setBold().setFontSize(10);
        Paragraph commentContent = new Paragraph(comments).setFontSize(10).setUnderline();
        cellComments.add(comment).add(commentContent);
        table.addCell(cellComments);

        box.add(table);

        return box;
    }

    private static void addHeaderCell(Table table, String[] headers) {
        Arrays.stream(headers).sequential().forEach(header -> {
            table.addCell(new Cell().add(new Paragraph(header).setBold().setFontSize(8)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
        });
    }

    private static void addRowCell(Table table, String evaluation, int result){
        table.addCell(new Cell().add(new Paragraph(evaluation).setFontSize(8).setMarginTop(10)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
        for (int i = 0; i < 5; i++) {
            Div checkbox = new Div()
                    .setWidth(5)
                    .setHeight(5)
                    .setMarginLeft(10);
            checkbox.setBorder(new SolidBorder(1));

            if (result == i)
                checkbox.setBackgroundColor(ColorConstants.BLACK);

            table.addCell(new Cell()
                    .add(checkbox)
                    .setPadding(5)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(Border.NO_BORDER));

        }
    }

    private byte[] mergePdf(byte[] contract, byte[] jobDesc){
        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(byteArrayOutputStream);
            PdfDocument pdf = new PdfDocument(writer);
            PdfMerger merger = new PdfMerger(pdf);

            PdfDocument firstSourcePdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(contract)));
            merger.merge(firstSourcePdf, 1, firstSourcePdf.getNumberOfPages());

            PdfDocument secondSourcePdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(jobDesc)));
            merger.merge(secondSourcePdf, 1, secondSourcePdf.getNumberOfPages());

            firstSourcePdf.close();
            secondSourcePdf.close();
            pdf.close();
            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }



}
