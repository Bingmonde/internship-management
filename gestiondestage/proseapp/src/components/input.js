import { useFormContext } from "react-hook-form";
import React, { useState } from "react";
import { findInputError } from "../utils/forms/findInputError";
import { isFormInvalid } from "../utils/forms/isFormInvalid";
import { useTranslation } from 'react-i18next';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faEye, faEyeSlash} from "@fortawesome/free-solid-svg-icons";

const Input = ({
                   cancelBanner,
                   onChange,
                   name,
                   label,
                   type,
                   id,
                   placeholder,
                   validation,
                   onFocus,
                   timeOptions,
                   togglePasswordVisibility
               }) => {
    const { register, formState: { errors }, trigger } = useFormContext();
    const error = errors[name];
    const isValid = !error;

    const { t } = useTranslation();
    const inputError = findInputError(errors, name);
    const isInvalid = isFormInvalid(inputError);
    const [touched, setTouched] = useState(false);

    return (
        <div className="text-left w-full mb-5">
            <label htmlFor={id} className={"block mr-[10px] my-0 ml-[5px] font-bold"}>
                {t(label)}
            </label>
            <div className={"flex flex-row items-center w-full border-solid rounded z-10 ".concat((error ? "border-2 border-red" : touched ? "border-2 border-success" : "border-2 border-neutral-300"))}>
                <input
                    id={id}
                    type={type}
                    placeholder={placeholder}
                    list={timeOptions ? `${id}-list` : undefined}
                    {...register(name, {
                        ...validation,
                        validate: validation.validate,
                        onBlur: () => {
                            if (cancelBanner) {
                                cancelBanner(false);
                            }
                            if (validation) {
                                setTouched(true);
                                trigger(name);
                            }
                        },
                        onChange: (event) => {
                            if (cancelBanner) {
                                cancelBanner(false);
                            }
                            if (onChange) {
                                onChange(event);
                            }
                        }

                    })}
                    className={"w-full p-2 border-0"}
                    onFocus={onFocus}
                />
                {togglePasswordVisibility &&
                    <div className="border-solid rounded-r border-r border-neutral-300 bg-white p-2">
                        <button
                            type="button"
                            onClick={togglePasswordVisibility}
                            className="text-gray-500 hover:text-gray-700"
                        >
                            <FontAwesomeIcon icon={type === 'text' ? faEye : faEyeSlash}/>
                        </button>
                    </div>

                }

            </div>

            {timeOptions && (
                <datalist id={`${id}-list`}>
                    {timeOptions.map((time) => (
                        <option key={time} value={time}/>
                    ))}
                </datalist>
            )}
            {error && <p className="error-message">✖ {t(error.message)}</p>}
        </div>
    );
};

export default Input;
