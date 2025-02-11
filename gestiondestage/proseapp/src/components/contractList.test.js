import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import ContractList from './contractList';
import { I18nextProvider } from 'react-i18next';
import i18n from '../utils/i18n';
import { useSession } from './CurrentSession';
import { useOutletContext } from 'react-router-dom';

jest.mock('./CurrentSession', () => ({
    useSession: jest.fn(),
}));

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useOutletContext: jest.fn(),
}));

jest.mock('../utils/userInfo', () => ({
    getUserInfo: () => ({ token: 'mockToken', userType: 'EMPLOYEUR' }),
}));

describe('ContractList', () => {
    const mockContracts = [
        {
            id: 1,
            contractSignatureDTO: { employer: null, student: null, manager: null },
            jobOfferApplicationDTO: {
                jobOffer: { titre: 'Test Job 1', employeurDTO: { nomCompagnie: 'Company 1' } },
                CV: { studentDTO: { prenom: 'John', nom: 'Doe' } },
            },
        },
        {
            id: 2,
            contractSignatureDTO: { employer: null, student: null, manager: null },
            jobOfferApplicationDTO: {
                jobOffer: { titre: 'Test Job 2', employeurDTO: { nomCompagnie: 'Company 2' } },
                CV: { studentDTO: { prenom: 'Jane', nom: 'Doe' } },
            },
        },
    ];

    beforeEach(() => {
        useSession.mockReturnValue({ currentSession: { id: 1, season: 'Spring', year: 2023 } });
        useOutletContext.mockReturnValue([jest.fn()]);
        global.fetch = jest.fn();
    });

    test('renders correctly', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({ value: mockContracts, totalPages: 1 }),
        });

        render(
            <I18nextProvider i18n={i18n}>
                <ContractList />
            </I18nextProvider>
        );

        await waitFor(() => {
            expect(screen.getByText('Test Job 1 - John Doe')).toBeInTheDocument();
            expect(screen.getByText('Test Job 2 - Jane Doe')).toBeInTheDocument();
        });
    });
    test('displays an error message when fetching contracts fails', async () => {
        global.fetch.mockRejectedValueOnce(new Error('Network error'));

        render(
            <I18nextProvider i18n={i18n}>
                <ContractList />
            </I18nextProvider>
        );

        await waitFor(() => {
            expect(screen.getByText('No contracts available')).toBeInTheDocument();
        });
    });

    test('displays the correct number of contracts', async () => {
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({ value: mockContracts, totalPages: 1 }),
        });

        render(
            <I18nextProvider i18n={i18n}>
                <ContractList />
            </I18nextProvider>
        );

        await waitFor(() => {
            expect(screen.getAllByText(/Test Job/i)).toHaveLength(2);
        });
    });

});