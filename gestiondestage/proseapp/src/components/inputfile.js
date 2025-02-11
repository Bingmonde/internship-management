import { useFormContext } from "react-hook-form";
import React, {useState} from "react";
import { findInputError } from "../utils/forms/findInputError";
import { isFormInvalid } from "../utils/forms/isFormInvalid";
import { useTranslation } from 'react-i18next';

const InputFile = ({
                    cancelBanner,
                   name,
                   label,
                   id,
                   placeholder,
                      accept,
                   validation,
               }) => {
    const { register, formState: { errors }, trigger, resetField } = useFormContext();
    const error = errors[name];
    const isValid = !error;

    const { t } = useTranslation();
    const inputError = findInputError(errors, name);
    const isInvalid = isFormInvalid(inputError);
    const [touched, setTouched] = useState(false)

    return (
        <div className="text-left w-full mb-5">
            <label htmlFor={id} className={"block mr-[10px] my-0 ml-[5px] font-bold"}>
                {t(label)}
            </label>
            <input
                id={id}
                type="file"
                placeholder={placeholder}
                accept={accept}
                {...register(name, {
                    ...validation,
                    onBlur: () => {
                        if (cancelBanner) {
                            cancelBanner(false);
                        }
                        if (validation) {
                            setTouched(true)
                            trigger(name)
                        }
                    },
                    onChange: () => {
                        if (validation) {
                            setTouched(true)
                            trigger(name)
                        }
                    }
                })}
                className={"w-full p-2 mb-2.5 border-solid rounded ".concat((error ? "border-2 border-red" : touched ? "border-2 border-success" : "border border-neutral-300"))}
            />
            <p>{t("formLabels.fileSizeHint")}</p>
            <button type="button" className={"btn-pending w-full p-2"} onClick={(e) => {
                e.preventDefault()
                resetField(id)
            }}>
                {t("formLabels.unselectFile")}
            </button>

            {error && <p className="error-message">✖ {t(error.message)}</p>}
        </div>
    ); //TODO : Ajouter bouton retire fichier
};

export default InputFile;
