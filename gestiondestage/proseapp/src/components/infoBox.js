import {faExclamation} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {useTranslation} from "react-i18next";

const InfoBox = ({msg}) => {
    const { t, i18n } = useTranslation();

    return (
        <div className="flex items-center justify-center min-h-60">
            <div className="w-1/2 flex flex-row bg-yellow-100 mx-auto p-5 border-l-4 border-success-hover">
                <FontAwesomeIcon icon={faExclamation}
                                 className="pl-2 pr-4 w-8 h-8 text-success-hover text-start"/>
                <p className={"text-darkpurple text-xl flex-grow text-center"}>{t(msg)}
                </p>
            </div>
        </div>

    );
}
export default InfoBox