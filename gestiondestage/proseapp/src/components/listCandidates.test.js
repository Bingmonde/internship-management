// src/components/ListCandidates.test.js

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ListCandidates from './listCandidates';
import { MemoryRouter } from 'react-router-dom';
import { getUserInfo } from '../utils/userInfo';
import { useSession } from './CurrentSession';
import { EmployeurAPI } from '../api/employerAPI';

// Mocking react-i18next
jest.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (key) => key, // Mock translation function returns the key itself
    }),
}));

// Mocking utils/userInfo
jest.mock('../utils/userInfo', () => ({
    getUserInfo: jest.fn(),
}));

// Mocking CurrentSession
jest.mock('./CurrentSession', () => ({
    useSession: jest.fn(),
}));

// Mocking employerAPI
jest.mock('../api/employerAPI', () => ({
    EmployeurAPI: {
        getInterviews: jest.fn(),
        sortInterViewsByDate: jest.fn(),
        filterInterviewByApplicationId: jest.fn(),
        cancelInterveiw: jest.fn(),
    },
}));

// Mock child components
jest.mock('./interviewDetail', () => ({ interviews, closeDetail }) => (
    <div data-testid="interview-detail">Interview Detail Component</div>
));

jest.mock('./pagination/paginationComponent', () => ({ resetCurrentPage, totalPages, paginate }) => (
    <div data-testid="pagination-component">Pagination Component</div>
));

jest.mock('./search/searchComponent', () => ({ placeholderText, stoppedTyping }) => (
    <input
        data-testid="search-component"
        placeholder={placeholderText}
        onChange={(e) => stoppedTyping(e.target.value)}
    />
));

// Mock FontAwesomeIcon
jest.mock('@fortawesome/react-fontawesome', () => ({
    FontAwesomeIcon: () => <span data-testid="font-awesome-icon"></span>,
}));

// Mocking react-router-dom's useNavigate and useOutletContext
jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: jest.fn(),
    useOutletContext: jest.fn(),
}));

describe('ListCandidates Component', () => {
    const mockSetPdfModalHelper = jest.fn();
    const mockNavigate = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
        useSession.mockReturnValue({
            currentSession: {
                id: 1,
                season: 'Spring',
                year: 2024,
            },
            setCurrentSession: jest.fn(),
        });
        getUserInfo.mockReturnValue({
            token: 'fake-token',
        });
        EmployeurAPI.getInterviews.mockResolvedValue({
            value: [
                { id: 1, applicationId: 100, date: '2024-04-01', cancelledDate: null },
            ],
        });
        EmployeurAPI.sortInterViewsByDate.mockImplementation((interviews) => interviews);
        EmployeurAPI.filterInterviewByApplicationId.mockImplementation((applicationId, interviews) =>
            interviews.filter(interview => interview.applicationId === applicationId)
        );
        EmployeurAPI.cancelInterveiw.mockResolvedValue({
            ok: true,
            json: async () => ({}),
        });
        const { useNavigate, useOutletContext } = require('react-router-dom');
        useNavigate.mockReturnValue(mockNavigate);
        useOutletContext.mockReturnValue([mockSetPdfModalHelper]);
        global.fetch = jest.fn();
    });

    afterEach(() => {
        jest.resetAllMocks();
    });

    test('renders correctly when there are no offers', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({ value: [] }),
        });

        render(
            <MemoryRouter>
                <ListCandidates />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                'http://localhost:8080/employeur/jobOffers?season=Spring&year=2024',
                expect.objectContaining({
                    method: 'GET',
                    headers: {
                        'Authorization': 'fake-token',
                        'Content-Type': 'application/json',
                    },
                })
            );
        });

        expect(screen.getByText('listCandidates.noOffer')).toBeInTheDocument();
    });

    test('renders correctly with offers and candidates', async () => {
        const mockOffers = [
            { id: 1, titre: 'Software Engineer', currentInvites: 2, maxInvites: 5 },
            { id: 2, titre: 'Data Scientist', currentInvites: 5, maxInvites: 5 },
        ];

        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({ value: mockOffers }),
        });

        render(
            <MemoryRouter>
                <ListCandidates />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalled();
            expect(screen.getByText('Software Engineer')).toBeInTheDocument();
            expect(screen.getByText('Data Scientist')).toBeInTheDocument();
        });
    });
// Test for API Failure and Error Message Display
    test('displays an error message when fetching offers fails', async () => {
        global.fetch.mockRejectedValueOnce(new Error('Network error'));

        render(
            <MemoryRouter>
                <ListCandidates />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByText('error.network')).toBeInTheDocument();
        });
    });


});
