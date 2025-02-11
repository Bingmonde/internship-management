import React, {useEffect} from "react";
import i18next from "i18next";
import {useForm} from "react-hook-form";
import i18n from "i18next";
import {useTranslation} from "react-i18next";


const ChangerLangue = () => {

    const { i18n } = useTranslation();

    const methods = useForm();
    const {reset} = methods;

    const changeLanguage = (language) => {
        i18n.changeLanguage(language);
    };

    const getLanguage = () => {
        return i18n.language.split('-')[0];
    }

    // const buttonClassName = "w-auto mb-0 ml-[5px] py-1 px-2 border-none text-white rounded ";
    const buttonClassName = "-me-3 py-1 px-3 text-white rounded-full border-solid border-2 border-black ";

    return (
        <div className="language-buttons flex justify-end p-2 mb-0 mr-1 me-8 fixed z-50 top-0 right-0">
            {/*<button type="button" className={buttonClassName + (getLanguage() === "en" ? "bg-selected z-10" : "bg-success hover:bg-success-hover z-0")} onClick={() => {*/}
            <button type="button" className={buttonClassName + (getLanguage() === "en" ? "bg-selected z-50" : "bg-unselected hover:bg-success-hover z-40")} onClick={() => {
                changeLanguage('en');
                localStorage.setItem('lang', 'en');
                reset()
            }}

            >EN
            </button>
            {/*<button type="button" className={buttonClassName + (getLanguage() === "fr" ? "bg-selected z-10" : "bg-success hover:bg-success-hover z-0")} onClick={() => {*/}
            <button type="button" className={buttonClassName + (getLanguage() === "fr" ? "bg-selected z-50" : "bg-unselected hover:bg-success-hover z-40")} onClick={() => {
                changeLanguage('fr')
                localStorage.setItem('lang', 'fr');
                reset()
            }}

            >FR
            </button>
        </div>
    )
}

export default ChangerLangue;