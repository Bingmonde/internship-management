import React from 'react';
import { render, fireEvent, screen, waitFor } from '@testing-library/react';
import ContractBox from './contractBox';
import { I18nextProvider } from 'react-i18next';
import i18n from '../utils/i18n';

// Mock the necessary dependencies
jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useOutletContext: () => [jest.fn()],
}));

jest.mock('../utils/userInfo', () => ({
    getUserInfo: () => ({ token: 'mockToken' }),
}));

const mockInternshipOffer = {
    id: 1,
    contractSignatureDTO: {
        employer: null,
        student: null,
        manager: null,
    },
    jobOfferApplicationDTO: {
        jobOffer: {
            titre: 'Test Job',
            dateDebut: '2023-05-01',
            dateFin: '2023-08-31',
            tauxHoraire: '20',
            typeTravail: 'HYBRID',
            lieu: 'Test City',
            pdfDocu: { fileName: 'job_description.pdf' },
        },
        CV: {
            studentDTO: {
                prenom: 'John',
                nom: 'Doe',
                courriel: 'john.doe@example.com',
                telephone: '1234567890',
            },
            pdfDocu: { fileName: 'cv.pdf' },
        },
    },
};

describe('ContractBox', () => {
    const renderComponent = (role) => {
        return render(
            <I18nextProvider i18n={i18n}>
                <ContractBox
                    role={role}
                    internshipOffer={mockInternshipOffer}
                    nameFunc={() => 'Test Contract'}
                    beginProcess={jest.fn()}
                    setPdfModal={jest.fn()}
                    setNewDate={jest.fn()}
                />
            </I18nextProvider>
        );
    };

    test('renders correctly for employer role', () => {
        renderComponent('EMPLOYEUR');
        expect(screen.getByText('Test Contract')).toBeInTheDocument();
    });

    test('toggles content when clicked', () => {
        renderComponent('EMPLOYEUR');
        fireEvent.click(screen.getByText('Test Contract'));
        expect(screen.getByText('Offre de stage')).toBeInTheDocument();
    });

    test('displays employer signature pad when not signed', async () => {
        renderComponent('EMPLOYEUR');
        fireEvent.click(screen.getByText('Test Contract'));

        await waitFor(() => {
            expect(screen.getByText(/Signature/i)).toBeInTheDocument();
            expect(screen.getByText(/The student has not signed yet/i)).toBeInTheDocument();
        });
    });



    test('displays success message when employer has signed', async () => {
        const signedOffer = {
            ...mockInternshipOffer,
            contractSignatureDTO: {
                ...mockInternshipOffer.contractSignatureDTO,
                employer: '2023-05-01T12:00:00',
            },
        };

        render(
            <I18nextProvider i18n={i18n}>
                <ContractBox
                    role="EMPLOYEUR"
                    internshipOffer={signedOffer}
                    nameFunc={() => 'Test Contract'}
                    beginProcess={jest.fn()}
                    setPdfModal={jest.fn()}
                    setNewDate={jest.fn()}
                />
            </I18nextProvider>
        );

        fireEvent.click(screen.getByText('Test Contract'));

        await waitFor(() => {
            expect(screen.getByText((content, node) => {
                const hasText = (node) => node.textContent === "Signed on : 2023-05-01 12:00";
                const nodeHasText = hasText(node);
                const childrenDontHaveText = Array.from(node.children).every(child => !hasText(child));

                return nodeHasText && childrenDontHaveText;
            })).toBeInTheDocument();
        });
    });



});