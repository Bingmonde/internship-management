import React, { useEffect, useState } from 'react';
import Input from "./input";
import { FormProvider, useForm } from "react-hook-form";
import { password_validation, email_validation } from "../utils/forms/formValidation";
import { Trans, useTranslation } from 'react-i18next';
import { Link } from "react-router-dom";
import { clearUserInfo } from "../utils/userInfo";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faEye, faEyeSlash } from "@fortawesome/free-solid-svg-icons";

const Connection = ({ setLogin }) => {
    const { t } = useTranslation();
    const methods = useForm();
    const { handleSubmit } = methods;

    const [passwordVisible, setPasswordVisible] = useState(false);

    useEffect(() => {
        clearUserInfo();
    }, []);

    const togglePasswordVisibility = () => {
        setPasswordVisible(!passwordVisible);
    };

    const onSubmit = async (data) => {
        setLogin(data.courriel.toLowerCase(), data.mdp);
    };

    return (
        <div className="flex flex-col items-center">
            <div className="flex flex-row w-full justify-center">
                <p className="mt-20 text-darkpurple text-3xl font-bold text-center">{t('systemWelcome')}</p>
            </div>
            <FormProvider {...methods}>
                <div className={"container-center h-2/3 mt-10"}>
                    <form onSubmit={handleSubmit(onSubmit)} className={"fullpage-form"}>
                        <h2>{t(`connect`)}</h2>

                        {/* Email input */}
                        <Input {...email_validation} />

                        {/* Password input with visibility toggle */}
                        <div className="relative flex flex-col">
                            <div className="relative h-[44px]">
                                <Input
                                    {...password_validation}
                                    type={passwordVisible ? "text" : "password"}
                                    className="pr-10 h-full border-gray-300 rounded-md"
                                    togglePasswordVisibility={togglePasswordVisibility}
                                />

                            </div>
                            <p className="text-sm text-red-500 mt-1">
                                {methods.formState.errors?.mdp?.message}
                            </p>
                        </div>

                        <button
                            type="submit"
                            className="form-button mt-20"
                        >
                            {t(`connect`)}
                        </button>
                        <Trans i18nKey="goInscription">
                            Vous n'avez pas encore de compte? <Link className={"underline font-bold text-cyan-600"}
                                                                    to={"/inscription"}>Inscrivez-vous</Link>
                        </Trans>
                    </form>
                </div>
            </FormProvider>
        </div>
    );
};

export default Connection;
