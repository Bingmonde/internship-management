import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import UploadOfferForm from './uploadOfferForm';
import { MemoryRouter } from 'react-router-dom';
import "../utils/i18n/index.mock.js";

// Global mock fetch
global.fetch = jest.fn();

describe('UploadOfferForm Component Tests', () => {
    beforeEach(() => {
        jest.resetAllMocks();
        fetch.mockResolvedValue({
            ok: true,
            json: async () => ({}),
        });
    });

    test('Renders all form inputs and the submit button', () => {
        render(<UploadOfferForm />, { wrapper: MemoryRouter });

        expect(screen.getByLabelText('formLabels.offerName')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.address')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.offerType')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.offerNumberPeople')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.offerDateBegin')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.offerDateDuration')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.offerSalary')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.offerWeeklyHours')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.offerDailyScheduleFrom')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.offerDailyScheduleTo')).toBeInTheDocument();
        expect(screen.getByLabelText('formLabels.offerFile')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /offreEnvoyer/i })).toBeInTheDocument();
    });

    test('Shows validation errors when submitting an empty form', async () => {
        render(<UploadOfferForm />, { wrapper: MemoryRouter });

        userEvent.click(screen.getByRole('button', { name: /offreEnvoyer/i }));

        await waitFor(() => {
            expect(screen.getByText(/offerNameRequired/i)).toBeInTheDocument();
        });
    });


    test('Successfully submits the form and displays a success message', async () => {
        fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => ({ message: 'Offer sent successfully' }),
        });

        render(<UploadOfferForm />, { wrapper: MemoryRouter });

        // Fill out the form
        userEvent.type(screen.getByLabelText(/formLabels.offerName/i), 'Test Offer');
        userEvent.type(screen.getByLabelText(/formLabels.address/i), '123 Test St');
        userEvent.selectOptions(screen.getByLabelText(/formLabels.offerType/i), 'remote');
        userEvent.type(screen.getByLabelText(/formLabels.offerNumberPeople/i), '4');
        userEvent.type(screen.getByLabelText(/formLabels.offerDateBegin/i), '2024-01-01');
        userEvent.type(screen.getByLabelText(/formLabels.offerDateDuration/i), '2024-12-31');
        userEvent.type(screen.getByLabelText(/formLabels.offerSalary/i), '50000');
        userEvent.type(screen.getByLabelText(/formLabels.offerWeeklyHours/i), '40');
        userEvent.type(screen.getByLabelText(/formLabels.offerDailyScheduleFrom/i), '09:00');
        userEvent.type(screen.getByLabelText(/formLabels.offerDailyScheduleTo/i), '17:00');

        // Simulate file upload
        const file = new File(['dummy content'], 'offer.pdf', { type: 'application/pdf' });
        const inputFile = screen.getByLabelText(/formLabels.offerFile/i);
        expect(inputFile).toBeInTheDocument()
        await userEvent.upload(inputFile, file);

        userEvent.click(screen.getByRole('button', { name: /offreEnvoyer/i }));

        // Wait for the success message to appear
        await waitFor(() => {
            expect(screen.getByText('Offer successfully sent')).toBeInTheDocument();
        });
    });

    test('Handles API failure correctly', async () => {
        fetch.mockResolvedValueOnce({
            ok: false,
            json: async () => ({ exception: 'InvalidData' }),
        });

        render(<UploadOfferForm />, { wrapper: MemoryRouter });

        userEvent.type(screen.getByLabelText(/formLabels.offerName/i), 'Test Offer');
        userEvent.click(screen.getByRole('button', { name: /offreEnvoyer/i }));

        await waitFor(() => {
            expect(screen.queryByText(/offerSent/i)).not.toBeInTheDocument();
        });
    });

    test('Validates input fields correctly', async () => {
        render(<UploadOfferForm />, { wrapper: MemoryRouter });

        const offerNameInput = screen.getByLabelText(/formLabels.offerName/i);
        userEvent.clear(offerNameInput);
        userEvent.tab();

        await waitFor(() => {
            expect(screen.getByText(/offerNameRequired/i)).toBeInTheDocument();
        });
    });

    test('Displays error message when required fields are missing', async () => {
        render(<UploadOfferForm />, { wrapper: MemoryRouter });

        const offerNameInput = screen.getByLabelText(/formLabels.offerName/i);
        userEvent.clear(offerNameInput);
        userEvent.tab();

        await waitFor(() => {
            const errorMessage = screen.getByText((content, element) => {
                return element.tagName.toLowerCase() === 'p' && content.includes("offerNameRequired");
            });
            expect(errorMessage).toBeInTheDocument();
        });
    });
});