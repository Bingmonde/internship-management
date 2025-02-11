import { render, screen, waitFor,fireEvent } from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import "../../utils/i18n/index";
import {SessionContext} from "../CurrentSession";
import * as userInfo from "../../utils/userInfo";
import InterviewList from "./interviewList";
import i18n from "i18next";
import InterviewDetail from "./interviewDetail";


beforeAll(() => {
    i18n.changeLanguage("fr")
    userInfo.setUserInfo("EMPLOYEUR","mockToken", "Hydro Quebec")
})

const mockInterviews = [
    {
        id: 1,
        interviewDate: "2024-12-07T21:16:00",
        interviewType: "Online",
        interviewLocationOrLink: "https://meet.google.com/",
        jobOfferApplication: {
            id: 1,
            jobOffer: {
                id: 1,
                titre: "Python Developer",
                dateDebut: "2025-01-01",
                dateFin: "2025-05-31",
                lieu: "1111 Rue Lapierre, LaSalle, QC H8N 2J4",
                typeTravail: "remote",
                nombreStagiaire: 3,
                tauxHoraire: 20.5,
                weeklyHours: 32.0,
                dayScheduleFrom: "08:00:00",
                dayScheduleTo: "16:00:00",
                description: "Python Developer",
                pdfDocu: {
                    fileName: "Hydro-Québec_Python_Developer_2024-11-17.pdf"
                },
                isApproved: true,
                isActivated: true,
                employeurDTO: {
                    id: 4,
                    nomCompagnie: "Hydro-Québec",
                    contactPerson: "Alice",
                    adresse: "123 rue de la compagnie",
                    city: "Montréal",
                    postalCode: "H1H1H1",
                    telephone: "1234567890",
                    fax: "1234567890",
                    courriel: "emp@email.com"
                }
            },
            CV: {
                id: 1,
                pdfDocu: {
                    fileName: "CV_Nathan_Marien_2024-11-17_232211.pdf"
                },
                dateHeureAjout: "3924-11-15T00:00:00Z",
                status: "validated",
                studentDTO: {
                    id: 2,
                    nom: "Marien",
                    prenom: "Nathan",
                    courriel: "stu1@email.com",
                    adresse: "test",
                    telephone: "1234567890",
                    discipline: {
                        id: "informatique",
                        en: "Computer Science",
                        fr: "Informatique"
                    }
                }
            },
            active: true,
            applicationDate: "2024-11-17T23:22:11.647992",
            approvalStatus: "WAITING",
            jobOfferId: 1,
            studentId: 2
        },
        isConfirmedByStudent: false,
        confirmationDate: null,
        creationDate: "2024-11-17T23:22:11.689243",
        cancelledDate: null
    }
]


describe('Test interview detail',  () => {


    it ('should render interview detail', () => {
        render(<InterviewDetail interviews={mockInterviews}/>)
        const interviewState = screen.getByText(/Dans le futur/i)
        const interviewDate = screen.getByText(/2024-12-07 21:16/i)
        const interviewType = screen.getByText(/En ligne/i)
        const confirmationStatus = screen.getByText(/En attente de la confirmation par l'étudiant/i)
        const lien = screen.getByText(/https:\/\/meet.google.com\//i)

        expect(interviewState).toBeInTheDocument()
        expect(interviewDate).toBeInTheDocument()
        expect(interviewType).toBeInTheDocument()
        expect(confirmationStatus).toBeInTheDocument()
        expect(lien).toBeInTheDocument()
    });

    it ('should render interview detail', () => {
        render(<InterviewDetail interviews={mockInterviews}/>)
        const interviewState = screen.getByText(/Dans le futur/i)
        const interviewDate = screen.getByText(/2024-12-07 21:16/i)
        const interviewType = screen.getByText(/En ligne/i)
        const confirmationStatus = screen.getByText(/En attente de la confirmation par l'étudiant/i)
        const lien = screen.getByText(/https:\/\/meet.google.com\//i)

        expect(interviewState).toBeInTheDocument()
        expect(interviewDate).toBeInTheDocument()
        expect(interviewType).toBeInTheDocument()
        expect(confirmationStatus).toBeInTheDocument()
        expect(lien).toBeInTheDocument()
    });


})