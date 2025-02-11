import {NavLink} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChartLine, faClipboardCheck, faFileAlt, faFileSignature, faUsers} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {useTranslation} from "react-i18next";

const SidebarEmployeur = () => {

    const { t } = useTranslation();

    return (
        <>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/uploadOffers" end={true}>
                <FontAwesomeIcon icon={faFileAlt} className="mr-2"/>
                {t("dashboardMenu.employerUploadOffer")}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/candidatesList" end={true}>
                <FontAwesomeIcon icon={faUsers} className="mr-2"/>
                {t("dashboardMenu.employerCandidatesList")}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/interviewList" end={true}>
                <FontAwesomeIcon icon={faClipboardCheck} className="mr-2"/>
                {t("dashboardMenu.consultInterview")}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/contracts" end={true}>
                <FontAwesomeIcon icon={faFileSignature} className="mr-2"/>
                {t("dashboardMenu.contracts")}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/evaluateStudent"
                     end={true}>
                <FontAwesomeIcon icon={faChartLine} className="mr-2"/>
                {t("dashboardMenu.evaluateStudent")}
            </NavLink>
        </>
    )

}

export default SidebarEmployeur;