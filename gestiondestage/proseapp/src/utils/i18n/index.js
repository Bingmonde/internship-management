import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import enLang from './locales/en/en.json'
import frLang from './locales/fr/fr.json'

const defaultLanguage = (localStorage.getItem("lang")) ? localStorage.getItem("lang") : navigator.language.split('-')[0];

const resources = {
    en: {
        translation: enLang
    },
    fr: {
        translation: frLang
    }
};

i18n
    .use(initReactI18next) // passes i18n down to react-i18next
    .init({
        resources,
        fallbackLng: 'en',
        lng: defaultLanguage,
        interpolation: {
            escapeValue: false // react already safes from xss
        }
    });


export default i18n;