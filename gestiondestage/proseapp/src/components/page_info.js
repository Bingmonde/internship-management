import './page_info.css'
import {useTranslation} from "react-i18next";
import {useLocation, useNavigate} from "react-router-dom";
import {useEffect} from "react";


export const PageInfo = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const info = location.state?.info || '';

    useEffect(() => {
        const timer = setTimeout(() => {
            navigate(-1)
        }, 5000)
        return () => clearTimeout(timer);
    }, []);

    return(
        <div className={"container-center"}>
            <div className={"fullpage-form"}>
            <h2>
                {t(info)}
            </h2>
            <p>
                {t('returnToPrevious5s')}
            </p>
            <button className="pageInfo" onClick={ () => navigate(-1)}>
                {t('return')}
            </button>
            </div>
        </div>
    )
}