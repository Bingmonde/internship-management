import { render, screen, waitFor,fireEvent } from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import "../../utils/i18n/index";
import {SessionContext} from "../CurrentSession";
import * as userInfo from "../../utils/userInfo";
import InterviewList from "./interviewList";
import i18n from "i18next";
import InterviewListStudent from "./interviewsListStudent";


const mockResonseInterview = {
    exception: null,
    value: [
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
                        fileName: "Hydro-Québec_Python_Developer_2024-11-15.pdf"
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
                        fileName: "CV_Nathan_Marien_2024-11-15_213823.pdf"
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
                applicationDate: "2024-11-15T21:38:23.996544",
                approvalStatus: "WAITING",
                studentId: 2,
                jobOfferId: 1
            },
            isConfirmedByStudent: false,
            confirmationDate: null,
            creationDate: "2024-11-15T21:38:24.038633",
            cancelledDate: null
        }
    ],
    pageNumber: 0,
    pageSize: 10,
    totalPages: 1
};


const mockResonseInterviewAfterConfirmation = {
    exception: null,
    value: [
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
                        fileName: "Hydro-Québec_Python_Developer_2024-11-15.pdf"
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
                        fileName: "CV_Nathan_Marien_2024-11-15_213823.pdf"
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
                applicationDate: "2024-11-15T21:38:23.996544",
                approvalStatus: "WAITING",
                studentId: 2,
                jobOfferId: 1
            },
            isConfirmedByStudent: true,
            confirmationDate: "2024-11-17T23:48:07.836104",
            creationDate: "2024-11-15T21:38:24.038633",
            cancelledDate: null
        }
    ],
    pageNumber: 0,
    pageSize: 10,
    totalPages: 1
};
const mockResponseInterviewSorted = {
    "exception": null,
    "value": [],
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 0
}
const mockEmployer = {
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

const mockSetCurrentSession = jest.fn();
const currentSession = {
    endDate: "2025-05-31T00:00",
    id: 2,
    season: "Hiver",
    startDate: "2025-01-01T00:0",
    year: "2025"
}

beforeAll(() => {
    i18n.changeLanguage("fr")
    userInfo.setUserInfo("EMPLOYEUR","mockToken", "Hydro Quebec")
})


afterEach(() => {
    jest.restoreAllMocks();
});

describe('Test interview list for student',  () => {
    it('should display fetched interview info', async () => {


        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)  // Simulate response for getInterviews
            })

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewListStudent />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const jobTitle = screen.getByText('Python Developer')
        const companyName = screen.getByText('Hydro-Québec')
        const interviewDate = screen.getByText(/2024-12-07 Samedi/i)
        const interviewTime = screen.getByText(/21:16/i)
        const interviewType = screen.getByText(/En ligne/i)
        const confirmationButton = screen.getByRole('button', {name: /Confirmer/i})


        expect(jobTitle).toBeInTheDocument()
        expect(companyName).toBeInTheDocument()
        expect(interviewDate).toBeInTheDocument()
        expect(interviewTime).toBeInTheDocument()
        expect(interviewType).toBeInTheDocument()
        expect(confirmationButton).toBeInTheDocument()
    });

    it('test confirm interview', async () => {


        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue({exception: null, value: {status: 'success'}})
            });

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewListStudent />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const confirmationButton = screen.getByRole('button', {name: /Confirmer/i})
        fireEvent.click(confirmationButton)
        expect(fetch).toHaveBeenCalledTimes(2);
        const [url, options] = fetch.mock.calls[1];
        expect(url).toBe('http://localhost:8080/student/jobInterview/confirmation/1');
        expect(options).toEqual(expect.objectContaining({
            method: 'POST',
            headers: {
                'Authorization': 'mockToken',
                'Content-Type': 'application/json'
            }
        }));
    });

    it('display updated interview after cancelling', async () => {


        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue({exception: null, value: {status: 'success'}})
            })
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterviewAfterConfirmation)
            })

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewListStudent />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const confirmationButton = screen.getByRole('button', {name: /Confirmer/i})
        fireEvent.click(confirmationButton)
        await waitFor(() => expect(fetch).toHaveBeenCalledTimes(3));

        const confirmationInfo = screen.getAllByText(/Confirmé/)
        expect(confirmationInfo).toHaveLength(2)

    });

    it('test display selected date after applying sort criteria', async () => {

        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewListStudent />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const selectStatus = screen.getByRole('option', {name: /Annulé/i})
        fireEvent.change(selectStatus, { target: { value: 'cancelled' } });
        expect(selectStatus.value).toBe('cancelled');

    });

    it('test display interview after applying sort criteria', async () => {

        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResponseInterviewSorted)
            });


        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewListStudent />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const selectStatus = screen.getByRole('combobox')
        fireEvent.change(selectStatus, { target: { value: 'cancelled' } });
        await waitFor(() => screen.getByText(/Aucune entrevue/i));
        await waitFor(() => expect(fetch).toHaveBeenCalledTimes(2));
        await waitFor(() => screen.getByText(/Aucune entrevue/i));

        const noInterview = screen.getByText(/Aucune entrevue/i)
        expect(noInterview).toBeInTheDocument();

    });


    it('test display interview after clear sort criteria', async () => {

        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResponseInterviewSorted)
            })
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
        ;

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewListStudent />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const selectStatus = screen.getByRole('combobox')
        fireEvent.change(selectStatus, { target: { value: 'cancelled' } });
        await waitFor(() => screen.getByText(/Aucune entrevue/i));

        const clearButton = screen.getByRole('button', {name: /Effacer les critères/i})
        fireEvent.click(clearButton)
        await waitFor(() => expect(fetch).toHaveBeenCalledTimes(3));

        await waitFor(() => screen.getByText('Python Developer'));

        const jobTitle = screen.getByText('Python Developer')
        const companyName = screen.getByText('Hydro-Québec')
        const interviewDate = screen.getByText(/2024-12-07 Samedi/i)
        const interviewTime = screen.getByText(/21:16/i)
        const interviewType = screen.getByText(/En ligne/i)
        const confirmationButton = screen.getByRole('button', {name: /Confirmer/i})


        expect(jobTitle).toBeInTheDocument()
        expect(companyName).toBeInTheDocument()
        expect(interviewDate).toBeInTheDocument()
        expect(interviewTime).toBeInTheDocument()
        expect(interviewType).toBeInTheDocument()
        expect(confirmationButton).toBeInTheDocument()
    });

    it('should call handlePreviewCompany and render company name on click', async () => {

        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewListStudent />
                </SessionContext.Provider>
            </MemoryRouter>
        );
        await waitFor(() => screen.getByText('Python Developer'));
        const companyElement = screen.getByText(/Hydro-Québec/i);

        fireEvent.click(companyElement);

        await waitFor(() => screen.getAllByText(/Hydro-Québec/i));
        const companyName = screen.getAllByText(/Hydro-Québec/i);
        const companyAddress = screen.getByText(/123 rue de la compagnie/i);
        const companyCity = screen.getByText(/Montréal/i);
        const companyPostalCode = screen.getByText(/H1H1H1/i);
        const companyPhone = screen.getAllByText(/\(123\) 456-7890/);


        expect(companyName).toHaveLength(2);
        expect(companyAddress).toBeInTheDocument();
        expect(companyCity).toBeInTheDocument();
        expect(companyPostalCode).toBeInTheDocument();
        expect(companyPhone).toHaveLength(2);


    });




})