import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import StudentsListTeacher from './studentsListTeacher';
import { MemoryRouter } from 'react-router-dom';
import * as userInfo from '../utils/userInfo';
import '@testing-library/jest-dom';
import { useSession } from './CurrentSession';
import { useOutletContext } from 'react-router-dom';

// Mock useTranslation
jest.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (key) => key,
    }),
}));

// Mock useSession
jest.mock('./CurrentSession', () => ({
    ...jest.requireActual('./CurrentSession'),
    useSession: jest.fn(),
}));

// Mock useOutletContext
jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useOutletContext: jest.fn(),
}));

// Mock ProfEvaluationForm
jest.mock('./profEvaluationForm', () => () => <div data-testid="prof-evaluation-form">ProfEvaluationForm Component</div>);

// Mock PdfPrintService
jest.mock('../api/pdfPrintService', () => ({
    PdfPrintService: {
        printEmployerEvaluation: jest.fn(),
    },
}));

beforeEach(() => {
    jest.spyOn(global, 'fetch').mockImplementation(() =>
        Promise.resolve({
            ok: true,
            json: () => Promise.resolve({ value: [] }),
        })
    );
});

afterEach(() => {
    jest.restoreAllMocks();
});

test('renders StudentsListTeacher component', async () => {
    const mockSession = { currentSession: { season: 'Winter', year: '2025' } };
    useSession.mockReturnValue(mockSession);

    useOutletContext.mockReturnValue([jest.fn()]);

    render(
        <MemoryRouter>
            <StudentsListTeacher />
        </MemoryRouter>
    );

    const headingElement = await screen.findByText(/studentList.studentsInMyCare/);
    expect(headingElement).toBeInTheDocument();
});

test('displays "No Students" when there is no data', async () => {
    const mockSession = { currentSession: { season: 'Winter', year: '2025' } };
    useSession.mockReturnValue(mockSession);

    useOutletContext.mockReturnValue([jest.fn()]);

    render(
        <MemoryRouter>
            <StudentsListTeacher />
        </MemoryRouter>
    );

    const noStudentsText = await screen.findByText(/studentList.noStudents/);
    expect(noStudentsText).toBeInTheDocument();
});




test('filters students based on input', async () => {
    const mockSession = { currentSession: { season: 'Winter', year: '2025' } };
    useSession.mockReturnValue(mockSession);

    useOutletContext.mockReturnValue([jest.fn()]);

    const mockData = {
        value: [
            {
                id: 1,
                internshipOffer: {
                    jobOfferApplicationDTO: {
                        CV: {
                            studentDTO: {
                                prenom: 'John',
                                nom: 'Doe',
                                courriel: 'john.doe@example.com',
                                adresse: '123 Main St',
                                telephone: '123-456-7890',
                                discipline: { id: 'CS' },
                            },
                        },
                        jobOffer: {
                            employeurDTO: {
                                name: 'Acme Corp',
                            },
                        },
                    },
                },
                evaluationEmployer: null,
            },
            {
                id: 2,
                internshipOffer: {
                    jobOfferApplicationDTO: {
                        CV: {
                            studentDTO: {
                                prenom: 'Jane',
                                nom: 'Smith',
                                courriel: 'jane.smith@example.com',
                                adresse: '456 Elm St',
                                telephone: '987-654-3210',
                                discipline: { id: 'EE' },
                            },
                        },
                        jobOffer: {
                            employeurDTO: {
                                name: 'Beta Inc',
                            },
                        },
                    },
                },
                evaluationEmployer: null,
            },
        ],
    };

    global.fetch.mockImplementationOnce(() =>
        Promise.resolve({
            ok: true,
            json: () => Promise.resolve(mockData),
        })
    );

    render(
        <MemoryRouter>
            <StudentsListTeacher />
        </MemoryRouter>
    );

    // Wait for the student names to appear
    const johnName = await screen.findByText('John');
    const janeName = await screen.findByText('Jane');
    expect(johnName).toBeInTheDocument();
    expect(janeName).toBeInTheDocument();

    const filterInput = screen.getByPlaceholderText('studentList.search');
    fireEvent.change(filterInput, { target: { value: 'Jane' } });

    // Now John should not be in the document
    expect(screen.queryByText('John')).not.toBeInTheDocument();
    expect(screen.getByText('Jane')).toBeInTheDocument();

    // Clear the filter
    fireEvent.change(filterInput, { target: { value: '' } });

    // Both students should be visible again
    expect(screen.getByText('John')).toBeInTheDocument();
    expect(screen.getByText('Jane')).toBeInTheDocument();
});

test('opens evaluation form modal when "Open Evaluation Form" is clicked', async () => {
    const mockSession = { currentSession: { season: 'Winter', year: '2025' } };
    useSession.mockReturnValue(mockSession);

    useOutletContext.mockReturnValue([jest.fn()]);

    const mockData = {
        value: [
            {
                id: 1,
                internshipOffer: {
                    jobOfferApplicationDTO: {
                        CV: {
                            studentDTO: {
                                prenom: 'John',
                                nom: 'Doe',
                                courriel: 'john.doe@example.com',
                                adresse: '123 Main St',
                                telephone: '123-456-7890',
                                discipline: { id: 'CS' },
                            },
                        },
                        jobOffer: {
                            employeurDTO: {
                                name: 'Acme Corp',
                            },
                        },
                    },
                },
                evaluationEmployer: null,
            },
        ],
    };

    global.fetch.mockImplementationOnce(() =>
        Promise.resolve({
            ok: true,
            json: () => Promise.resolve(mockData),
        })
    );

    render(
        <MemoryRouter>
            <StudentsListTeacher />
        </MemoryRouter>
    );

    // Wait for the student name to appear
    const studentName = await screen.findByText('John');
    expect(studentName).toBeInTheDocument();

    // Expand details
    const viewDetailsButton = screen.getByText('studentList.viewDetails');
    fireEvent.click(viewDetailsButton);

    // Click on 'Open Evaluation Form'
    const openEvaluationFormButton = screen.getByText('studentList.openEvaluationForm');
    fireEvent.click(openEvaluationFormButton);

    // Now the modal should be displayed
    const modalElement = screen.getByTestId('prof-evaluation-form');
    expect(modalElement).toBeInTheDocument();
});

test('shows "Print" button when evaluationEmployer is present', async () => {
    const mockSession = { currentSession: { season: 'Winter', year: '2025' } };
    useSession.mockReturnValue(mockSession);

    useOutletContext.mockReturnValue([jest.fn()]);

    const mockData = {
        value: [
            {
                id: 1,
                internshipOffer: {
                    jobOfferApplicationDTO: {
                        CV: {
                            studentDTO: {
                                prenom: 'John',
                                nom: 'Doe',
                                courriel: 'john.doe@example.com',
                                adresse: '123 Main St',
                                telephone: '123-456-7890',
                                discipline: { id: 'CS' },
                            },
                        },
                        jobOffer: {
                            employeurDTO: {
                                name: 'Acme Corp',
                            },
                        },
                    },
                },
                evaluationEmployer: {
                    someField: 'someValue',
                },
            },
        ],
    };

    global.fetch.mockImplementationOnce(() =>
        Promise.resolve({
            ok: true,
            json: () => Promise.resolve(mockData),
        })
    );

    render(
        <MemoryRouter>
            <StudentsListTeacher />
        </MemoryRouter>
    );

    // Wait for the student name to appear
    const studentName = await screen.findByText('John');
    expect(studentName).toBeInTheDocument();

    // Expand details
    const viewDetailsButton = screen.getByText('studentList.viewDetails');
    fireEvent.click(viewDetailsButton);

    // 'Print' button should be visible
    const printButton = screen.getByText('printpdf.print');
    expect(printButton).toBeInTheDocument();
});


test('displays error message when fetch fails', async () => {
    const mockSession = { currentSession: { season: 'Winter', year: '2025' } };
    useSession.mockReturnValue(mockSession);

    useOutletContext.mockReturnValue([jest.fn()]);

    global.fetch.mockImplementationOnce(() =>
        Promise.reject(new Error('Fetch failed'))
    );

    render(
        <MemoryRouter>
            <StudentsListTeacher />
        </MemoryRouter>
    );

    const errorMessage = await screen.findByText(/studentList.cantFetchData/);
    expect(errorMessage).toBeInTheDocument();
});
