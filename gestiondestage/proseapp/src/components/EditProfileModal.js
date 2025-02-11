import React, { useEffect, useState } from 'react';
import Input from './input';
import Select from './select';
import { FormProvider, useForm, Controller } from 'react-hook-form';
import { FaLock, FaUserEdit } from 'react-icons/fa';

import {
    firstName_validation,
    lastName_validation,
    address_validation,
    confirm_password_validation,
    password_validation,
    telephone_validation,
    discipline_validation,
    company_validation,
} from '../utils/forms/formValidation';
import { useTranslation } from 'react-i18next';
import { getUserInfo } from '../utils/userInfo';
import { useNavigate } from 'react-router-dom';
import ErrorBox from "./errorBox";

const EditProfileModal = ({ profile, permission, onClose, onUpdateProfile, updateUsername }) => {
    const { t } = useTranslation();
    const userInfo = getUserInfo();
    const lang = userInfo.lang || 'en';
    const userType = userInfo.userType.toLowerCase();
    const navigate = useNavigate();
    const [passwordVisible, setPasswordVisible] = useState(false);

    // Function to format telephone number as 111-111-1111
    const formatPhoneNumber = (number) => {
        if (!number) return '';
        const cleaned = number.replace(/\D/g, '');
        const match = cleaned.match(/^(\d{3})(\d{3})(\d{4})$/);
        return match ? `${match[1]}-${match[2]}-${match[3]}` : number;
    };
    // Function to format tax number as XXX-XXX-XXX
    const formatTaxNumber = (number) => {
        if (!number) return '';
        const cleaned = number.replace(/\D/g, ''); // Remove non-numeric characters
        const match = cleaned.match(/^(\d{3})(\d{3})(\d{3})$/);
        return match ? `${match[1]}-${match[2]}-${match[3]}` : number;
    };


    // Initialize useForm with formatted telephone number
    const profileMethods = useForm({
        defaultValues: {
            ...profile,
            telephone: formatPhoneNumber(profile.telephone),
            fax: formatTaxNumber(profile.fax), // Format tax on initialization
            discipline: profile.discipline ? profile.discipline.id : '',
        },
    });

    const passwordMethods = useForm();

    const {
        register: profileRegister,
        handleSubmit: handleProfileSubmit,
        formState: { errors: profileErrors },
        setValue: setProfileValue,
        control: profileControl,
    } = profileMethods;

    const {
        register: passwordRegister,
        handleSubmit: handlePasswordSubmit,
        formState: { errors: passwordErrors },
        watch: passwordWatch,
        setValue: passwordSetValue,
    } = passwordMethods;

    const [disciplines, setDisciplines] = useState([]);
    const [message, setMessage] = useState('');
    const [messageType, setMessageType] = useState(null);

    useEffect(() => {
        // Fetch disciplines if user is student or teacher
        const fetchDisciplines = async () => {
            try {
                const res = await fetch('http://localhost:8080/disciplines');
                if (res.ok) {
                    const data = await res.json();
                    setDisciplines(data);
                } else {
                    console.error('Failed to fetch disciplines');
                }
            } catch (error) {
                console.error('Error fetching disciplines:', error);
            }
        };
        if (userType === 'student' || userType === 'teacher') {
            fetchDisciplines();
        }
    }, [userType]);

    // Handle profile update
    const onSubmit = async (data) => {
        const token = userInfo.token;
        let updatePayload = {};

        if (userType === 'employeur') {
            updatePayload = {
                id: data.id,
                nomCompagnie: data.nomCompagnie,
                contactPerson: data.contactPerson,
                adresse: data.adresse,
                city: data.city,
                postalCode: data.postalCode,
                telephone: data.telephone.replace(/-/g, ''), // Remove hyphens before sending
                fax: data.fax,
            };
        } else if (userType === 'projet_manager') {
            updatePayload = {
                id: data.id,
                prenom: data.prenom,
                nom: data.nom,
                telephone: data.telephone.replace(/-/g, ''),
                adresse: data.adresse,
            };
        } else {
            // For student or teacher
            const selectedDiscipline = disciplines.find(
                (discipline) => discipline.id === data.discipline
            );

            updatePayload = {
                id: data.id,
                prenom: data.prenom,
                nom: data.nom,
                telephone: data.telephone.replace(/-/g, ''),
                adresse: data.adresse,
                discipline: selectedDiscipline, // Send full DisciplineTranslationDTO object
            };
        }

        try {
            const response = await fetch(`http://localhost:8080/userinfo/${userType}/${data.id}`, {
                method: 'PUT',
                headers: {
                    Authorization: token,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updatePayload),
            });

            if (response.ok) {
                const updatedData = await response.json();
                setMessage(t('profile.updateSuccess'));
                setMessageType('success');
                if (onUpdateProfile) {
                    onUpdateProfile(updatedData);
                }
                updateUsername(token, true);
                // Optionally close the modal
                onClose();
            } else {
                const errorData = await response.json();
                setMessage(`${t('profile.updateError')}: ${errorData.message || t('unknownError')}`);
                setMessageType('error');
            }
        } catch (error) {
            setMessage(`${t('profile.updateError')}: ${error.message}`);
            setMessageType('error');
        }
    };
    // Handle fax input change to format as user types
    const handleFaxChange = (e) => {
        const input = e.target.value;
        const cleaned = input.replace(/\D/g, ''); // Remove non-numeric characters
        let formatted = cleaned;

        if (cleaned.length > 6) {
            formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3, 6)}-${cleaned.slice(6, 9)}`;
        } else if (cleaned.length > 3) {
            formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
        }

        setProfileValue('fax', formatted);
    };


    // Handle password update
    const onPasswordChange = async (data) => {
        const id = profile.id;

        // Check if new password and confirm password match
        if (data.newPassword !== data.confirmPassword) {
            setMessage(t('profile.passwordsDoNotMatch'));
            setMessageType('error');
            return;
        }

        // Check if new password is different from current password
        if (data.newPassword === data.currentPassword) {
            setMessage(t('profile.newPasswordMustBeDifferent'));
            setMessageType('error');
            return;
        }

        if (!data.currentPassword) {
            setMessage(t('profile.currentPasswordRequired'));
            setMessageType('error');
            return;
        }

        const token = userInfo.token;
        const updatePasswordPayload = {
            currentPassword: data.currentPassword,
            newPassword: data.newPassword,
        };

        try {
            const response = await fetch(`http://localhost:8080/userinfo/${userType}/password/${id}`, {
                method: 'PUT',
                headers: {
                    Authorization: token,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updatePasswordPayload),
            });

            if (response.ok) {
                setMessage(t('profile.passwordUpdateSuccess'));
                setMessageType('success');

                passwordSetValue('currentPassword', '');
                passwordSetValue('newPassword', '');
                passwordSetValue('confirmPassword', '');

                onClose();
            } else {
                let errorMessage = t('profile.passwordUpdateError');

                try {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                } catch (parseError) {
                    console.error('Error parsing error response:', parseError);
                }

                setMessage(`${t('profile.passwordUpdateError')}`);
                setMessageType('error');
            }
        } catch (error) {
            setMessage(`${t('profile.passwordUpdateError')}: ${error.message}`);
            setMessageType('error');
            console.error('Network error while updating password:', error);
        }
    };

    // Handle telephone input change to format as user types
    const handleTelephoneChange = (e) => {
        const input = e.target.value;
        const cleaned = input.replace(/\D/g, '');
        let formatted = cleaned;

        if (cleaned.length > 6) {
            formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3, 6)}-${cleaned.slice(6, 10)}`;
        } else if (cleaned.length > 3) {
            formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
        }

        setProfileValue('telephone', formatted);
    };

    const togglePasswordVisibility = () => {
        setPasswordVisible(!passwordVisible);
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
            <div className="bg-white p-6 rounded-md shadow-md w-full max-w-7xl mx-auto overflow-auto max-h-screen">
                <h2 className="text-2xl font-bold mb-6 flex items-center">
                    <FaUserEdit className="mr-2" /> {t('profile.editProfile')}
                </h2>

                {/* Profile Update Form */}
                <FormProvider {...profileMethods}>
                    <form onSubmit={handleProfileSubmit(onSubmit)}>
                        <h3 className="text-xl font-semibold mb-4 text-gray-800">{t('profile.basicInfo')}</h3>

                        {userType === 'student' && (
                            <>
                                <Input {...firstName_validation} register={profileRegister} />
                                <Input {...lastName_validation} register={profileRegister} />
                            </>
                        )}

                        {userType === 'teacher' && (
                            <>
                                <Input {...firstName_validation} register={profileRegister} />
                                <Input {...lastName_validation} register={profileRegister} />
                            </>
                        )}

                        {userType === 'projet_manager' && (
                            <>
                                <Input {...firstName_validation} register={profileRegister} />
                                <Input {...lastName_validation} register={profileRegister} />
                            </>
                        )}

                        {userType === 'employeur' && (
                            <>
                                <Input {...company_validation} register={profileRegister} />
                                <Input
                                    label={t('profile.city')}
                                    name="city"
                                    type="text"
                                    placeholder={t('profile.cityPlaceholder')}
                                    validation={{ required: t('fieldRequired') }}
                                    register={profileRegister}
                                />
                                <Input
                                    label={t('profile.fax')}
                                    name="fax"
                                    type="text"
                                    placeholder={t('profile.faxPlaceholder')}
                                    onChange={handleFaxChange} // Call the formatting handler
                                    register={profileRegister}
                                    validation={{
                                        required: t('fieldRequired'), // Validation rule
                                        pattern: {
                                            value: /^\d{3}-\d{3}-\d{3}$/, // Fax number format validation
                                            message: t('profile.invalidFaxFormat'), // Error message
                                        },
                                    }}
                                />

                                <Input
                                    label={t('profile.postalCode')}
                                    name="postalCode"
                                    type="text"
                                    placeholder={t('profile.postalCodePlaceholder')}
                                    validation={{ required: t('fieldRequired') }}
                                    register={profileRegister}
                                />
                            </>
                        )}

                        <Input
                            {...telephone_validation}
                            register={profileRegister}
                            onChange={handleTelephoneChange}
                        />
                        <Input {...address_validation} register={profileRegister} />

                        {/* Display validation errors */}
                        {Object.keys(profileErrors).length > 0 && (
                            <div className="text-red text-left mt-[20px]">
                                <h3>{t('errors')}</h3>
                                <ul className="pl-[20px] list-disc">
                                    {Object.entries(profileErrors).map(([name, error]) => (
                                        <li key={name}>{t(error.message)}</li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        <div className="flex justify-end space-x-4 mt-6">

                            <button
                                type="submit"
                                className="px-4 py-2 bg-greenish text-white rounded-md hover:bg-gray focus:outline-none focus:ring-2 focus:ring-green-400"
                            >
                                {t('profile.saveChanges')}
                            </button>
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 bg-red text-white rounded-md hover:bg-gray focus:outline-none focus:ring-2 focus:ring-gray-400"
                            >
                                {t('profile.cancel')}
                            </button>
                        </div>
                    </form>
                </FormProvider>

                {/* Password Change Form */}
                {permission && (
                    <FormProvider {...passwordMethods}>
                        <form onSubmit={handlePasswordSubmit(onPasswordChange)} className="mt-6">
                            <h3 className="text-xl font-semibold mb-4 text-gray-800 flex items-center">
                                <FaLock className="mr-2" /> {t('profile.changePassword')}
                            </h3>
                            <Input
                                label={t('profile.currentPassword')}
                                name="currentPassword"
                                type="password"
                                placeholder={t('profile.currentPasswordPlaceholder')}
                                validation={password_validation.validation}
                                register={passwordRegister}
                            />
                            <Input
                                label={t('profile.newPassword')}
                                name="newPassword"
                                type="password"
                                placeholder={t('profile.newPasswordPlaceholder')}
                                validation={{
                                    ...password_validation.validation,
                                    validate: (value) =>
                                        value !== passwordWatch('currentPassword') || t('profile.newPasswordMustBeDifferent'),
                                }}
                                register={passwordRegister}
                                type={passwordVisible ? "text" : "password"}
                                togglePasswordVisibility={togglePasswordVisibility}
                            />
                            <Input
                                {...confirm_password_validation}
                                validation={{
                                    ...confirm_password_validation.validation,
                                    validate: (value) => {
                                        const password = passwordWatch('newPassword');
                                        return value === password || t('formErrors.passwordsDoNotMatch');
                                    },
                                }}
                                register={passwordRegister}
                                type={passwordVisible ? "text" : "password"}
                                togglePasswordVisibility={togglePasswordVisibility}
                            />

                            {/* Display validation errors */}
                            {Object.keys(passwordErrors).length > 0 && (
                                <div className="text-red text-left mt-[20px]">
                                    <h3>{t('errors')}</h3>
                                    <ul className="pl-[20px] list-disc">
                                        {Object.entries(passwordErrors).map(([name, error]) => (
                                            <li key={name}>{t(error.message)}</li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                            {message && (
                                <ErrorBox msg={message}></ErrorBox>
                            )}

                            <div className="flex justify-end space-x-4 mt-6">

                                <button
                                    type="submit"
                                    className="px-4 py-2 bg-greenish text-white rounded-md hover:bg-gray focus:outline-none focus:ring-2 focus:ring-blue-400"
                                >
                                    {t('profile.updatePassword')}
                                </button>
                                <button
                                    type="button"
                                    onClick={onClose}
                                    className="px-4 py-2 bg-red text-white rounded-md hover:bg-gray focus:outline-none focus:ring-2 focus:ring-gray-400"
                                >
                                    {t('profile.cancel')}
                                </button>
                            </div>
                        </form>
                    </FormProvider>
                )}
            </div>
        </div>
    );
};

export default EditProfileModal;
