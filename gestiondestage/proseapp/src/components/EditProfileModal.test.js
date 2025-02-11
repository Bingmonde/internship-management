import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import EditProfileModal from './EditProfileModal';
import { MemoryRouter } from 'react-router-dom';
import { getUserInfo } from '../utils/userInfo';
import { FormProvider, useForm } from 'react-hook-form';

// Simulation des dépendances
jest.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (key) => key,
    }),
}));

jest.mock('../utils/userInfo', () => ({
    getUserInfo: jest.fn(),
}));

jest.mock('react-hook-form', () => {
    const originalModule = jest.requireActual('react-hook-form');
    return {
        ...originalModule,
        useForm: jest.fn(),
        FormProvider: ({ children }) => <div>{children}</div>,
    };
});

// Composant Input simulé
jest.mock('./input', () => ({ label, name, placeholder, ...rest }) => (
    <input data-testid={name} placeholder={placeholder} {...rest} />
));

// Composant Select simulé
jest.mock('./select', () => ({ label, name, options, ...rest }) => (
    <select data-testid={name} {...rest}>
        {Array.isArray(options) &&
            options.map((option) => (
                <option key={option.id} value={option.id}>
                    {option.name}
                </option>
            ))}
    </select>
));

describe('EditProfileModal Component', () => {
    const mockOnClose = jest.fn();
    const mockOnUpdateProfile = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
        useForm.mockReturnValue({
            register: jest.fn(),
            handleSubmit: (fn) => (e) => e && e.preventDefault(),
            formState: { errors: {} },
            watch: jest.fn(),
            setValue: jest.fn(),
        });
    });

    test('affiche correctement pour un étudiant', () => {
        getUserInfo.mockReturnValue({
            userType: 'student',
            token: 'fake-token',
        });

        const profile = {
            id: 1,
            prenom: 'John',
            nom: 'Doe',
            telephone: '123-456-7890',
            adresse: '123 Main St',
            discipline: 'CS',
        };

        render(
            <MemoryRouter>
                <EditProfileModal
                    profile={profile}
                    permission="student"
                    onClose={mockOnClose}
                    onUpdateProfile={mockOnUpdateProfile}
                />
            </MemoryRouter>
        );

        expect(screen.getByText('profile.editProfile')).toBeInTheDocument();
        expect(screen.getByTestId('prenom')).toBeInTheDocument();
        expect(screen.getByTestId('nom')).toBeInTheDocument();
        expect(screen.getByTestId('discipline')).toBeInTheDocument();
        expect(screen.getByTestId('telephone')).toBeInTheDocument();
        expect(screen.getByTestId('adresse')).toBeInTheDocument();
    });

    test('affiche correctement pour un employeur', () => {
        getUserInfo.mockReturnValue({
            userType: 'employeur',
            token: 'fake-token',
        });

        const profile = {
            id: 1,
            nomCompagnie: 'Acme Corp',
            contactPerson: 'Jane Smith',
            adresse: '456 Corporate Blvd',
            city: 'Metropolis',
            postalCode: '12345',
            telephone: '555-1234',
            fax: '555-5678',
        };

        render(
            <MemoryRouter>
                <EditProfileModal
                    profile={profile}
                    permission="employeur"
                    onClose={mockOnClose}
                    onUpdateProfile={mockOnUpdateProfile}
                />
            </MemoryRouter>
        );

        expect(screen.getByText('profile.editProfile')).toBeInTheDocument();
        expect(screen.getByTestId('nomCompagnie')).toBeInTheDocument();
        expect(screen.getByTestId('contactPerson')).toBeInTheDocument();
        expect(screen.getByTestId('city')).toBeInTheDocument();
        expect(screen.getByTestId('fax')).toBeInTheDocument();
        expect(screen.getByTestId('postalCode')).toBeInTheDocument();
    });


    test('gère le changement de mot de passe', async () => {
        getUserInfo.mockReturnValue({
            userType: 'student',
            token: 'fake-token',
        });

        const profile = {
            id: 1,
            prenom: 'John',
            nom: 'Doe',
            telephone: '123-456-7890',
            adresse: '123 Main St',
            discipline: 'CS',
        };

        useForm.mockReturnValue({
            register: jest.fn(),
            handleSubmit: (fn) => (e) => {
                e && e.preventDefault();
                return fn({
                    currentPassword: 'currentpass',
                    newPassword: 'newpass123',
                    confirmPassword: 'newpass123',
                });
            },
            formState: { errors: {} },
            watch: jest.fn().mockReturnValue('newpass123'),
            setValue: jest.fn(),
        });

        // Modification de la fonction fetch simulée
        global.fetch = jest.fn((url) => {
            if (url === 'http://localhost:8080/disciplines') {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve([
                        { id: 'CS', name: 'Computer Science' },
                        { id: 'EE', name: 'Electrical Engineering' },
                    ]),
                });
            } else if (url === `http://localhost:8080/userinfo/student/password/${profile.id}`) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({}),
                });
            } else {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({}),
                });
            }
        });

        render(
            <MemoryRouter>
                <EditProfileModal
                    profile={profile}
                    permission="student"
                    onClose={mockOnClose}
                    onUpdateProfile={mockOnUpdateProfile}
                />
            </MemoryRouter>
        );

        fireEvent.submit(screen.getByText('profile.updatePassword'));

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                `http://localhost:8080/userinfo/student/password/${profile.id}`,
                expect.objectContaining({
                    method: 'PUT',
                    headers: expect.objectContaining({
                        Authorization: 'fake-token',
                    }),
                    body: JSON.stringify({
                        currentPassword: 'currentpass',
                        newPassword: 'newpass123',
                    }),
                })
            );
        });

        expect(screen.getByText('profile.passwordUpdateSuccess')).toBeInTheDocument();
        expect(mockOnClose).toHaveBeenCalled();
    });


    test('affiche une erreur lorsque les mots de passe ne correspondent pas', async () => {
        getUserInfo.mockReturnValue({
            userType: 'student',
            token: 'fake-token',
        });

        const setValueMock = jest.fn();
        const watchMock = jest.fn((field) => {
            if (field === 'newPassword') return 'password123';
            return '';
        });

        useForm.mockReturnValue({
            register: jest.fn(),
            handleSubmit: (fn) => (e) => {
                e && e.preventDefault();
                return fn({
                    currentPassword: 'currentpass',
                    newPassword: 'password123',
                    confirmPassword: 'differentpassword',
                });
            },
            formState: { errors: {} },
            watch: watchMock,
            setValue: setValueMock,
        });

        const profile = {
            id: 1,
            prenom: 'John',
            nom: 'Doe',
            telephone: '123-456-7890',
            adresse: '123 Main St',
            discipline: 'CS',
        };

        global.fetch = jest.fn((url) => {
            if (url === 'http://localhost:8080/disciplines') {
                return Promise.resolve({
                    ok: true,
                    json: () =>
                        Promise.resolve([
                            { id: 'CS', name: 'Computer Science' },
                            { id: 'EE', name: 'Electrical Engineering' },
                        ]),
                });
            } else {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({}),
                });
            }
        });

        render(
            <MemoryRouter>
                <EditProfileModal
                    profile={profile}
                    permission="student"
                    onClose={mockOnClose}
                    onUpdateProfile={mockOnUpdateProfile}
                />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByTestId('newPassword'), {
            target: { value: 'password123' },
        });
        fireEvent.change(screen.getByTestId('confirmPassword'), {
            target: { value: 'differentpassword' },
        });

        fireEvent.submit(screen.getByText('profile.updatePassword'));

        await waitFor(() => {
            expect(screen.getByText('profile.passwordsDoNotMatch')).toBeInTheDocument();
        });
    });



    test('affiche une erreur lorsque le mot de passe actuel est manquant', async () => {
        getUserInfo.mockReturnValue({
            userType: 'student',
            token: 'fake-token',
        });

        useForm.mockReturnValue({
            register: jest.fn(),
            handleSubmit: (fn) => (e) => {
                e && e.preventDefault();
                return fn({
                    currentPassword: '', // Mot de passe actuel vide
                    newPassword: 'newpass123',
                    confirmPassword: 'newpass123',
                });
            },
            formState: { errors: { currentPassword: { message: 'profile.currentPasswordRequired' } } },
            watch: jest.fn().mockReturnValue('newpass123'),
            setValue: jest.fn(),
        });

        const profile = {
            id: 1,
            prenom: 'John',
            nom: 'Doe',
            telephone: '123-456-7890',
            adresse: '123 Main St',
            discipline: 'CS',
        };

        global.fetch = jest.fn((url) => {
            if (url === 'http://localhost:8080/disciplines') {
                return Promise.resolve({
                    ok: true,
                    json: () =>
                        Promise.resolve([
                            { id: 'CS', name: 'Computer Science' },
                            { id: 'EE', name: 'Electrical Engineering' },
                        ]),
                });
            } else {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({}),
                });
            }
        });

        render(
            <MemoryRouter>
                <EditProfileModal
                    profile={profile}
                    permission="student"
                    onClose={mockOnClose}
                    onUpdateProfile={mockOnUpdateProfile}
                />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByTestId('newPassword'), {
            target: { value: 'newpass123' },
        });
        fireEvent.change(screen.getByTestId('confirmPassword'), {
            target: { value: 'newpass123' },
        });

        fireEvent.submit(screen.getByText('profile.updatePassword'));

        // Trouver le titre du formulaire 'changePassword'
        const changePasswordHeading = screen.getByText('profile.changePassword');
        // Trouver le formulaire associé au titre
        const changePasswordForm = changePasswordHeading.closest('form');

        // Vérifier que le formulaire existe
        expect(changePasswordForm).toBeInTheDocument();

        // Rechercher le message d'erreur dans le formulaire 'changePassword'
        await waitFor(() => {
            expect(within(changePasswordForm).getByText('profile.currentPasswordRequired')).toBeInTheDocument();
        });
    });

});
