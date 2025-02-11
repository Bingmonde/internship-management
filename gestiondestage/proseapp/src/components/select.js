import { useFormContext, Controller } from "react-hook-form";
import { findInputError } from "../utils/forms/findInputError";
import { isFormInvalid } from "../utils/forms/isFormInvalid";
import { useTranslation } from 'react-i18next';
import './input.css';

const Select = ({ cancelBanner, name, label, id, validation, options, onChange, onFocus }) => {
    const { control, formState: { errors } } = useFormContext();
    const { t, i18n } = useTranslation();
    const inputError = findInputError(errors, name);
    const isInvalid = isFormInvalid(inputError);
    const lang = i18n.language.split('-')[0];

    return (
        <div className="text-left w-full mb-5">
            <label htmlFor={id} className="block mr-[10px] my-0 ml-[5px] font-bold">
                {t(label)}
            </label>
            <Controller
                name={name}
                control={control}
                rules={validation}
                defaultValue=""
                render={({ field, fieldState }) => (
                    <select
                        {...field}
                        id={id}
                        onBlur={(e) => {
                            field.onBlur(e);
                            if (cancelBanner) {
                                cancelBanner(false);
                            }
                        }}
                        onChange={(e) => {
                            field.onChange(e);
                            if (cancelBanner) {
                                cancelBanner(false);
                            }
                            if (onChange) onChange(e);
                        }}
                        className={
                            "w-full p-2 mb-2.5 border-solid rounded bg-white " +
                            (isInvalid
                                ? "border-2 border-red"
                                : fieldState.isDirty
                                    ? "border-2 border-success"
                                    : "border border-neutral-300")
                        }
                        onFocus={onFocus}
                    >
                        <option value="">
                            {t("emptySelect")}
                        </option>
                        {options &&
                            options.map((option, index) => (
                                <option key={option.id || index} value={option.id}>
                                    {option.customText || option[lang] || option['en']}
                                </option>
                            ))}
                    </select>
                )}
            />
            {isInvalid && (
                <p className="error-message">✖ {t(inputError.error.message)}</p>
            )}
        </div>
    );
};

export default Select;
