import {useFormContext, useFormState} from "react-hook-form";
import {findInputError} from "../utils/forms/findInputError";
import {isFormInvalid} from "../utils/forms/isFormInvalid";
import { useTranslation } from 'react-i18next';
import './input.css'
import {useState} from "react";
import {faCheck, faRightFromBracket} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const SuccessBox = ({msg}) => {
    const { t, i18n } = useTranslation();

    return (
        <div className="w-full border-l-greenish border-l-4 border-l-solid p-2 my-2 flex flex-row bg-greenish/[0.15]">
            <FontAwesomeIcon icon={faCheck}
                             className="pl-2 pr-4 w-8 h-8  self-center pb-0 text-greenish"/>
            <p class={"font-bold"}>{t(msg)}
            </p>
        </div>
    );
};

export default SuccessBox;