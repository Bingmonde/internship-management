import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import "../../../utils/i18n";
import { SessionContext } from "../../CurrentSession";
import * as userInfo from "../../../utils/userInfo";
import i18n from "i18next";
import Reports from "../reports";

const mockNoDataForReport = {
    exception: null,
    value: [],
    pageNumber: 0,
    pageSize: 0,
    totalPages: 0
};

const mockNonValidatedOffers = {
    exception: null,
    value: [
        {
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
            isApproved: false,
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
        }
    ],
    pageNumber: 0,
    pageSize: 20,
    totalPages: 1
};

const mockValidatedOffers = {
    exception: null,
    value: [
        {
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
        }
    ],
    pageNumber: 0,
    pageSize: 20,
    totalPages: 1
};

const mockStudents = {
    exception: null,
    value: [
        {
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
        },
        {
            id: 3,
            nom: "Shi",
            prenom: "Bing",
            courriel: "stu2@email.com",
            adresse: "test",
            telephone: "1234567890",
            discipline: {
                id: "informatique",
                en: "Computer Science",
                fr: "Informatique"
            }
        }
    ],
    pageNumber: 0,
    pageSize: 20,
    totalPages: 1
};

const currentSession = {
    endDate: "2025-05-31T00:00",
    id: 2,
    season: "Hiver",
    startDate: "2025-01-01T00:00",
    year: "2025"
};

const valueToMockResponseMap = {
    "nonValidatedOffers": mockNonValidatedOffers,
    "validatedOffers": mockValidatedOffers,
    "signedUpStudents": mockStudents,
    "studentsNoCV": mockStudents,
    "studentsCVNotValidated": mockStudents,
    "studentsNoInterview": mockStudents,
    "studentsAwaitingInterview": mockStudents,
    "studentsAwaitingInterviewResponse": mockStudents,
    "studentsWhoFoundInternship": mockStudents,
    "studentsNotEvaluatedBySupervisor": mockStudents,
    "studentsSupervisorHasntEvaluatedEnterprise": mockStudents
}

beforeAll(() => {
    i18n.changeLanguage("fr");
    userInfo.setUserInfo("PROJECT_MANAGER", "xdafdsfs8FSDFSDxx", "Hydro Quebec");
});

beforeEach(() => {
    require("react-router-dom").useOutletContext.mockReturnValue([jest.fn()]);

    jest.spyOn(global, 'fetch').mockImplementation((url) => {
        // remove http://localhost:8080/internshipManager/ and everything after the ? from the url
        url = url.replace("http://localhost:8080/internshipManager/", "").split("?")[0];
        const response = valueToMockResponseMap[url];
        console.log("fetch mock called with url: ", url);
        if (response) {
            return Promise.resolve({
                ok: true,
                json: () => Promise.resolve(response)
            });
        }
        console.log("url: ", url);
        return Promise.reject(new Error('URL not mocked'));
    });
});

afterEach(() => {
    jest.restoreAllMocks();
});

jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useOutletContext: jest.fn()
}));

describe('Test report no data returned', () => {
    it('should display that there is no data for the report', async () => {
        jest.spyOn(global, 'fetch').mockResolvedValueOnce({
            json: jest.fn().mockResolvedValue(mockNoDataForReport)
        });

        render(
            <MemoryRouter>
                <SessionContext.Provider value={{ currentSession }}>
                    <Reports />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByText("Aucune donnée disponible")).toBeInTheDocument();
        });
    });
});

describe('Test reports with 1 element for each report type', () => {
    it('each should display offer(s)', async () => {
        render(
            <MemoryRouter>
                <SessionContext.Provider value={{ currentSession }}>
                    <Reports />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        const valuesToCheck = [
            "nonValidatedOffers",
            "validatedOffers"
        ];

        for (const option of valuesToCheck) {
            console.log("testing option: ", option);

            const select = screen.getByRole("combobox");
            fireEvent.change(select, { target: { value: option } });

            await waitFor(() => {
                expect(screen.getByText("Python Developer")).toBeInTheDocument();
                expect(screen.getByText("Hydro-Québec")).toBeInTheDocument();
            });
        }
    });

    it('each should display student(s)', async () => {
        render(
            <MemoryRouter>
                <SessionContext.Provider value={{ currentSession }}>
                    <Reports />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        const valuesToCheck = [
            "signedUpStudents",
            "studentsNoCV",
            "studentsCVNotValidated",
            "studentsNoInterview",
            "studentsAwaitingInterview",
            "studentsAwaitingInterviewResponse",
            "studentsWhoFoundInternship",
            "studentsNotEvaluatedBySupervisor",
            "studentsSupervisorHasntEvaluatedEnterprise"
        ];

        for (const option of valuesToCheck) {
            console.log("testing option: ", option);

            const select = screen.getByRole("combobox");
            fireEvent.change(select, { target: { value: option } });

            await waitFor(() => {
                expect(screen.getByText((content, element) => {
                    return element.textContent.trim() === "Nathan Marien";
                })).toBeInTheDocument();
                expect(screen.getByText((content, element) => {
                    return element.textContent.trim() === "Bing Shi";
                })).toBeInTheDocument();
            });
        }
    });
});

describe('test clicking on card stuff', () => {
    it('should open student info modal', async () => {
        render(
            <MemoryRouter>
                <SessionContext.Provider value={{ currentSession }}>
                    <Reports />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        // select option "étudiants inscrits" in the dropdown
        const select = screen.getByRole("combobox");
        fireEvent.change(select, { target: { value: "signedUpStudents" } });

        await waitFor(() => {
            screen.getByText((content, element) => {
                return element.textContent.trim() === "Nathan Marien";
            });
        });

        // click on student button
        const button = screen.getByText((content, element) => {
            return element.textContent.trim() === "Nathan Marien";
        });
        fireEvent.click(button);

        // wait for the modal to appear and check if the student's phone number is displayed
        await waitFor(() => {
            expect(screen.getByText("(123) 456-7890")).toBeInTheDocument(); // only shown when modal is open
        });
    });

    it('should open enterprise info modal', async () => {
        render(
            <MemoryRouter>
                <SessionContext.Provider value={{ currentSession }}>
                    <Reports />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        // select option "offres non validées" in the dropdown
        const select = screen.getByRole("combobox");
        fireEvent.change(select, { target: { value: "nonValidatedOffers" } });

        await waitFor(() => {
            screen.getByText("Python Developer");
        });

        // click on enterprise button
        const button = screen.getByText("Hydro-Québec");
        fireEvent.click(button);

        // wait for the modal to appear and check if the enterprise's contactPerson is displayed
        await waitFor(() => {
            expect(screen.getByText("Alice")).toBeInTheDocument(); // only shown when modal is open
        });
    });
});

describe('test searching causing re-fetch with query as param', () => {

    it('should refetch with query param when typing in search bar (offer)', async () => {
        render(
            <MemoryRouter>
                <SessionContext.Provider value={{ currentSession }}>
                    <Reports />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        // select option "offres non validées" in the dropdown
        const select = screen.getByRole("combobox");
        fireEvent.change(select, { target: { value: "nonValidatedOffers" } });

        await waitFor(() => {
            screen.getByText("Python Developer");
        });

        // enter "py" in the search input
        const searchInput = screen.getByPlaceholderText("Rechercher");
        fireEvent.change(searchInput, { target: { value: "py" } });

        // make sure there is a call to the API with the search query
        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                "http://localhost:8080/internshipManager/nonValidatedOffers?page=0&size=20&q=py",
                { headers: { Authorization: "xdafdsfs8FSDFSDxx" } }
            );
        });
    });


    it('should refetch with query param when typing in search bar (student)', async () => {
        render(
            <MemoryRouter>
                <SessionContext.Provider value={{ currentSession }}>
                    <Reports />
                </SessionContext.Provider>
            </MemoryRouter>
        );

        // select option "étudiants inscrits" in the dropdown
        const select = screen.getByRole("combobox");
        fireEvent.change(select, { target: { value: "signedUpStudents" } });

        await waitFor(() => {
            screen.getByText((content, element) => {
                return element.textContent.trim() === "Nathan Marien";
            });
        });

        // reset mocks
        jest.restoreAllMocks();

        // mock fetch again for the search input test
        jest.spyOn(global, 'fetch').mockImplementation((url) => {
            if (response) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockNoDataForReport)
                });
            }
            return Promise.reject(new Error('URL not mocked'));
        });

        // enter "bing" in the search input
        const searchInput = screen.getByPlaceholderText("Rechercher");
        fireEvent.change(searchInput, { target: { value: "bing" } });

        // make sure there is a call to the API with the search query
        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                "http://localhost:8080/internshipManager/signedUpStudents?page=0&size=20&q=bing",
                { headers: { Authorization: "xdafdsfs8FSDFSDxx" } }
            );
        });
    });
});