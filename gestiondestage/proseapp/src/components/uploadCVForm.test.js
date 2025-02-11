import React from 'react';
import { render, fireEvent, screen, waitFor } from '@testing-library/react';
import UploadCVForm from './uploadCVForm';
import { I18nextProvider } from 'react-i18next';
import i18n from '../utils/i18n';

// Mock the necessary dependencies
jest.mock('../utils/userInfo', () => ({
    getUserInfo: () => ({ token: 'mockToken' }),
}));

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useOutletContext: () => [jest.fn()],
}));

const mockCvList = [
    {
        pdfDocu: { fileName: 'cv1.pdf' },
        dateHeureAjout: '2023-05-01T12:00:00',
        status: 'validated',
    },
    {
        pdfDocu: { fileName: 'cv2.pdf' },
        dateHeureAjout: '2023-05-02T12:00:00',
        status: 'pending',
    },
];

describe('UploadCVForm', () => {
    beforeEach(() => {
        global.fetch = jest.fn().mockResolvedValueOnce({
            ok: true,
            json: async () => ({ value: mockCvList, totalPages: 1 }),
        });
    });

    test('renders and fetches CV list', async () => {
        render(
            <I18nextProvider i18n={i18n}>
                <UploadCVForm />
            </I18nextProvider>
        );

        await waitFor(() => {
            expect(screen.getByText('cv1.pdf')).toBeInTheDocument();
            expect(screen.getByText('cv2.pdf')).toBeInTheDocument();
        });
    });

    test('handles pagination', async () => {
        // Reset fetch mock for pagination
        global.fetch = jest.fn()
            .mockResolvedValueOnce({ // First page
                ok: true,
                json: async () => ({ value: mockCvList, totalPages: 2 }),
            })
            .mockResolvedValueOnce({ // Second page (empty for the test)
                ok: true,
                json: async () => ({ value: [], totalPages: 2 }),
            });

        render(
            <I18nextProvider i18n={i18n}>
                <UploadCVForm />
            </I18nextProvider>
        );

        await waitFor(() => {
            expect(screen.getByText('cv1.pdf')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByText('2'));

        await waitFor(() => {
            expect(screen.queryByText('cv1.pdf')).not.toBeInTheDocument();
            expect(screen.queryByText('cv2.pdf')).not.toBeInTheDocument();
        });
    });
});