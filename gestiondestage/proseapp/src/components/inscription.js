import React, { useEffect, useState } from 'react';
import Input from "./input";
import { FormProvider, useForm } from "react-hook-form";
import {
    firstName_validation,
    address_validation,
    confirm_password_validation,
    email_validation,
    lastName_validation,
    password_validation,
    telephone_validation,
    discipline_validation,
    terms_read_validation,
    company_validation, fax_validation, postalCode_validation, city_validation, contactPerson_validation
} from "../utils/forms/formValidation";
import Select from "./select";
import {Trans, useTranslation} from 'react-i18next';
import i18next from "i18next";
import "./inscription.css"
import "./page_info.css"
import {Link, useNavigate} from "react-router-dom";

const Inscription = ({setLogin, }) => {

    useEffect(() => {
        localStorage.removeItem("userType")
        localStorage.removeItem("token")
        localStorage.removeItem("username")
    }, []);

    const { t } = useTranslation();
    const methods = useForm();
    const {  register, handleSubmit, formState: { errors }, getValues, setFocus, reset } = methods;
    const [disciplines, setDisciplines] = useState([]);
    const navigate = useNavigate();
    const [role, setRole] = useState('student');
    const [passwordVisible, setPasswordVisible] = useState(false);

    useEffect(() => {
        setFocus('firstName') || setFocus('company') ;
    }, [setFocus]);

    useEffect(() => {
        const getListDisciplines = async () => {
            const res = await fetch('http://localhost:8080/disciplines');
            const data = await res.json();
            console.log(data);
            setDisciplines(data);
        };
        if (role === 'student' || role === 'teacher')
            getListDisciplines();
    }, []);

    const togglePasswordVisibility = () => {
        setPasswordVisible(!passwordVisible);
    };

    const onSubmit = async (data) => {
        console.log(data);


        var { confirmPassword, telephone, termsAgree, ...user } = data
        var telephone_format = telephone.split('-').join('')
        user = {...user, telephone: telephone_format}
        console.log('user', user)


        // Pour etudiant
        if (role === 'student') {
            try {
                const res = await fetch('http://localhost:8080/inscription/students',{
                    method: 'POST',
                    headers: {
                        'Content-type': 'application/json',
                    },
                    body: JSON.stringify(user)
                })
                if (res.ok){
                    setLogin(user.courriel,user.mdp)
                }
                else {
                    var err = await res.json()
                    console.log(err)
                    console.log(err["exception"])
                    switch (err["exception"]) {
                        case 'AlreadyExists':
                            navigate('/error', { state: { info: t('errorStudentExist'), goto: "-1"} })
                            break
                        case 'InvalidUserFormat':
                            navigate('/error', { state: { info: t('errorStudentCreation'), goto: "-1"} })
                            break
                        default:
                            navigate('/error', { state: { info: t('errorStudentCreation'), goto: "-1"} })
                    }
                }
            }
            catch (error){
                console.error('Error creating account:', error.message);
                navigate('/error', { state: { info: t('errorStudentCreation'), goto: "-1"} })
            }
        }
        if (role === 'employer') {
            var {discipline, nom, prenom, ...employer} = user
            console.log('employer:', employer)
            try {
                const res = await fetch('http://localhost:8080/inscription/employeurs',{
                    method: 'POST',
                    headers: {
                        'Content-type': 'application/json',
                    },
                    body: JSON.stringify(employer)
                })
                if (res.ok){
                    setLogin(user.courriel,user.mdp)
                }
                else {
                    var err = await res.json()
                    console.log(err)
                    console.log(err["exception"])
                    switch (err["exception"]) {
                        case 'AlreadyExists':
                            navigate('/error', { state: { info: t('errorEmployerExist'), goto: "-1"} })
                            break
                        case 'InvalidUserFormat':
                            navigate('/error', { state: { info: t('errorEmployerCreation'), goto: "-1"} })
                            break
                        default:
                            navigate('/error', { state: { info: t('errorEmployerCreation'), goto: "-1"} })
                    }
                }
            }
            catch (error){
                console.error('Error creating account:', error.message);
                navigate('/error', { state: { info: t('errorEmployerCreation'), goto: "-1"} })
            }
        }

        // Pour professeur
        if (role === 'teacher') {
            const teacher = {
                nom: user.nom,
                prenom: user.prenom,
                courriel: user.courriel,
                adresse: user.adresse,
                telephone: user.telephone,
                mdp: user.mdp,
                discipline: user.discipline
            };
            console.log(teacher);
            console.log(JSON.stringify(teacher));
            try {
                const res = await fetch('http://localhost:8080/inscription/teachers',{
                    method: 'POST',
                    headers: {
                        'Content-type': 'application/json',
                    },
                    body: JSON.stringify(teacher)
                })
                if (res.ok){
                    setLogin(user.courriel,user.mdp)
                }
                else {
                    var err = await res.json()
                    console.log(err)
                    console.log(err["exception"])
                    switch (err["exception"]) {
                        case 'AlreadyExists':
                            navigate('/error', { state: { info: t('errorTeacherExist'), goto: "-1"} })
                            break
                        case 'InvalidUserFormat':
                            navigate('/error', { state: { info: t('errorTeacherCreation'), goto: "-1"} })
                            break
                        default:
                            navigate('/error', { state: { info: t('errorTeacherCreation'), goto: "-1"} })
                    }
                }
            }
            catch (error){
                console.error('Error creating account:', error.message);
                navigate('/error', { state: { info: t('errorTeacherCreation'), goto: "-1"} })
            }
        }

    };

    return (
        <>
            <div className="fullpage-column justify-between mx-auto flex mt-3">
                <button
                    onClick={(e) => {
                        setRole(e.target.value);
                        reset();
                    }}
                    value='student'
                    className={role === 'student' ? "btn-inscription chosen z-20" : "btn-inscription bg-orange-500 z-10"}
                >
                    {t("userTypes.student")}
                </button>
                <button
                    onClick={(e) => {
                        setRole(e.target.value);
                        reset();
                    }}
                    value='employer'
                    className={role === 'employer' ? "btn-inscription chosen z-20 -mx-4" : "btn-inscription bg-green-200 -mx-4 z-10"}
                >
                    {t("userTypes.employer")}
                </button>
                <button
                    onClick={(e) => {
                        setRole(e.target.value);
                        reset();
                    }}
                    value='teacher'
                    className={role === 'teacher' ? "btn-inscription chosen z-20" : "btn-inscription bg-lightpurple"}
                >
                    {t("userTypes.teacher")}
                </button>
            </div>
            <FormProvider {...methods}>
                <div className="container-center">
                    <form onSubmit={handleSubmit(onSubmit)} className="form-inscription mt-0">
                        <h2>{t('createAccount')}</h2>

                        {/* Role-specific Fields */}
                        {role === 'student' && (
                            <>
                                <Input {...firstName_validation} />
                                <Input {...lastName_validation} />
                                <Input {...email_validation} />
                                <Select {...discipline_validation} options={disciplines} />
                            </>
                        )}

                        {role === 'employer' && (
                            <>
                                <Input {...company_validation} />
                                <Input {...contactPerson_validation } />
                                <Input {...email_validation} />
                                <Input {...city_validation} />
                                <Input {...postalCode_validation} />
                            </>
                        )}

                        {role === 'teacher' && (
                            <>
                                <Input {...firstName_validation} />
                                <Input {...lastName_validation} />
                                <Input {...email_validation} />
                                <Select {...discipline_validation} options={disciplines} />
                            </>
                        )}

                        {/* Common Fields */}
                        <Input {...address_validation} />

                        <Input
                            {...telephone_validation}
                            onChange={(e) => {
                                let input = e.target.value.replace(/\D/g, "");
                                if (input.length > 10) input = input.slice(0, 10);
                                const formatted = input.replace(
                                    /(\d{3})(\d{0,3})(\d{0,4})/,
                                    (match, p1, p2, p3) => {
                                        if (p3) return `${p1}-${p2}-${p3}`;
                                        if (p2) return `${p1}-${p2}`;
                                        return p1;
                                    }
                                );
                                e.target.value = formatted;
                            }}
                        />

                        {/* Fax Field Only for Employers */}
                        {role === 'employer' && (
                            <Input
                                {...fax_validation}
                                onChange={(e) => {
                                    let input = e.target.value.replace(/\D/g, "");
                                    if (input.length > 10) input = input.slice(0, 10);
                                    const formatted = input.replace(
                                        /(\d{3})(\d{0,3})(\d{0,4})/,
                                        (match, p1, p2, p3) => {
                                            if (p3) return `${p1}-${p2}-${p3}`;
                                            if (p2) return `${p1}-${p2}`;
                                            return p1;
                                        }
                                    );
                                    e.target.value = formatted;
                                }}
                            />
                        )}

                        {/* Password Fields */}
                        <Input
                            {...password_validation}
                            type={passwordVisible ? "text" : "password"}
                            togglePasswordVisibility={togglePasswordVisibility}
                        />

                        <Input
                            {...confirm_password_validation}
                            validation={{
                                ...confirm_password_validation.validation,
                                validate: (value) => {
                                    const password = getValues('mdp');
                                    return value === password || t('formErrors.passwordsDoNotMatch');
                                }
                            }}
                            type={passwordVisible ? "text" : "password"}
                            togglePasswordVisibility={togglePasswordVisibility}
                        />

                        {/* Terms Agreement */}
                        <div className="checkbox-label">
                            <input
                                type="checkbox"
                                id="terms"
                                {...register('termsAgree', { ...terms_read_validation })}
                            />
                            <label htmlFor="terms">
                                {t('agreeTerms')}
                            </label>
                        </div>

                        {/* Submit Button */}
                        <button
                            onClick={() => {
                                localStorage.setItem("userType", role.toLowerCase());
                            }}
                            type="submit"
                            className="form-button"
                        >
                            {t('signUp')}
                        </button>

                        {/* Error Messages */}
                        {Object.keys(errors).length > 0 && (
                            <div className="text-red text-left mt-5">
                                <h3>{t('errors')}</h3>
                                <ul className="pl-5 list-disc">
                                    {Object.entries(errors).map(([name, error]) => (
                                        <li key={name}>
                                            {t(error.message)}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        {/* Link to Connection */}
                        <Trans i18nKey="goConnect">
                            Vous avez déjà un compte? <Link className="underline font-bold text-cyan-600" to="/connection">Connectez-vous</Link>
                        </Trans>
                    </form>
                </div>
            </FormProvider>
        </>
    );
};

export default Inscription;
