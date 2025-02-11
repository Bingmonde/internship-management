import { render, screen, waitFor,fireEvent } from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import "../../utils/i18n/index";
import {SessionContext} from "../CurrentSession";
import * as userInfo from "../../utils/userInfo";
import InterviewList from "./interviewList";
import i18n from "i18next";
import InterviewListStudent from "./interviewsListStudent";
import {setUserInfo} from "../../utils/userInfo";


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

const mockResonseInterviewCancelled = {
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
            confirmationDate: "2024-11-16T21:38:24.038633",
            creationDate: "2024-11-15T21:38:24.038633",
            cancelledDate: "2024-11-15T21:38:24.038633",
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

const currentSession = {
    endDate: "2025-05-31T00:00",
    id: 2,
    season: "Hiver",
    startDate: "2025-01-01T00:0",
    year: "2025"
}

beforeAll(() => {
    i18n.changeLanguage("fr")
    localStorage.setItem("lang", "fr")
    userInfo.setUserInfo("EMPLOYEUR","mockToken", "Hydro Quebec")
})

afterEach(() => {
    jest.restoreAllMocks();
});

describe('Test interview list',  () => {

    it('should display fetched interview info', async () => {

        const mockSetCurrentSession = jest.fn();
        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)  // Simulate response for getInterviews
            })

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewList />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const jobTitle = screen.getByText('Python Developer')
        const candidateName = screen.getByText('Nathan Marien')
        const interviewDate = screen.getByText(/2024-12-07 Samedi/i)
        const interviewTime = screen.getByText(/21:16/i)
        const interviewType = screen.getByText(/En ligne/i)
        const status = screen.getAllByText(/Non confirmé/i)


        expect(jobTitle).toBeInTheDocument()
        expect(candidateName).toBeInTheDocument()
        expect(interviewDate).toBeInTheDocument()
        expect(interviewTime).toBeInTheDocument()
        expect(interviewType).toBeInTheDocument()
        expect(status[1]).toBeInTheDocument()
    });

    it('test trigger cancel interview', async () => {

        const mockSetCurrentSession = jest.fn();
        const mockCancelInterview = jest.fn()
        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
        .mockResolvedValueOnce({
            json: jest.fn().mockResolvedValue(mockResonseInterviewCancelled)
        });


        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewList />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const cancelButton = screen.getByRole('button', {name: /Annuler l'entrevue/i})

        fireEvent.click(cancelButton)

        expect(fetch).toHaveBeenCalledTimes(2);

        const [url, options] = fetch.mock.calls[1];

        expect(url).toBe('http://localhost:8080/employeur/jobInterview/1');
        expect(options).toEqual(expect.objectContaining({
            method: 'DELETE',
            headers: {
                'Authorization': 'mockToken',
                'Content-Type': 'application/json'
            }
        }));
    });

    it('test display updated interview after cancelling', async () => {

        const mockSetCurrentSession = jest.fn();
        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue({exception: null, value: {status: 'success'}})
            })
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterviewCancelled)
            })
        ;

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewList />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const cancelButton = screen.getByRole('button', {name: /Annuler l'entrevue/i})

        fireEvent.click(cancelButton)

        await waitFor(() => expect(fetch).toHaveBeenCalledTimes(3));

        const cancelledStatus = screen.getAllByText(/Entrevue annulée/i)

        expect(cancelledStatus).toHaveLength(1);

    });


    it('test display selected date after applying sort criteria', async () => {

        const mockSetCurrentSession = jest.fn();
        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewList />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const endDateInput = screen.getByTestId('end-date-picker');
        fireEvent.change(endDateInput, { target: { value: '2024-11-01' } });
        expect(endDateInput.value).toBe('2024-11-01');

    });

    it('test display interview after applying sort criteria', async () => {

        const mockSetCurrentSession = jest.fn();
        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResponseInterviewSorted)
            })
        ;

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewList />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const endDateInput = screen.getByTestId('end-date-picker');
        fireEvent.change(endDateInput, { target: { value: '2024-11-01' } });
        await waitFor(() => screen.getByText('Aucune entrevue'));

        const noInterview = screen.getByText('Aucune entrevue')
        expect(noInterview).toBeInTheDocument();

    });


    it('test display interview after clear sort criteria', async () => {

        const mockSetCurrentSession = jest.fn();
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
                    <InterviewList />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('Python Developer'));

        const endDateInput = screen.getByTestId('end-date-picker');
        fireEvent.change(endDateInput, { target: { value: '2024-11-01' } });
        await waitFor(() => screen.getByText('Aucune entrevue'));
        const clearButton = screen.getByRole('button', {name: /Effacer les critères/i})
        fireEvent.click(clearButton)
        await waitFor(() => expect(fetch).toHaveBeenCalledTimes(3));

        await waitFor(() => screen.getByText('Python Developer'));

        const jobTitle = screen.getByText('Python Developer')
        const candidateName = screen.getByText('Nathan Marien')
        const interviewDate = screen.getByText(/2024-12-07 Samedi/i)
        const interviewTime = screen.getByText(/21:16/i)
        const interviewType = screen.getByText(/En ligne/i)
        const status = screen.getAllByText(/Non confirmé/i)


        expect(jobTitle).toBeInTheDocument()
        expect(candidateName).toBeInTheDocument()
        expect(interviewDate).toBeInTheDocument()
        expect(interviewTime).toBeInTheDocument()
        expect(interviewType).toBeInTheDocument()
        expect(status[1]).toBeInTheDocument()
    });
    it('should call handlePreviewCandidate and render candidate name on click', async () => {
        const mockSetCurrentSession = jest.fn();
        jest.spyOn(global, 'fetch')
            .mockResolvedValueOnce({
                json: jest.fn().mockResolvedValue(mockResonseInterview)
            })

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{currentSession: currentSession, setCurrentSession: mockSetCurrentSession}}>
                    <InterviewList />
                </SessionContext.Provider>
            </MemoryRouter>
        );
        await waitFor(() => screen.getByText('Python Developer'));
        const candidateElement = screen.getByText(/Nathan Marien/i);

        fireEvent.click(candidateElement);

        await waitFor(() => screen.getAllByText(/Nathan Marien/i));
        const candidateName = screen.getAllByText(/Nathan Marien/i);
        const candidateAddr = screen.queryByText(/test/i);
        const candidateTel = screen.getByText(/\(123\) 456-7890/);
        const candidateEmail = screen.getByText(/stu1@email.com/i);
        const candidateDiscipline = screen.getByText(/Informatique/i);

        expect(candidateName).toHaveLength(2);
        expect(candidateAddr).toBeNull();
        expect(candidateTel).toBeInTheDocument()
        expect(candidateEmail).toBeInTheDocument();
        expect(candidateDiscipline).toBeInTheDocument();



    });




})




