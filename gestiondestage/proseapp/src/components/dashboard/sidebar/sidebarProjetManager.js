import {NavLink} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBriefcase, faChalkboardTeacher, faFilePdf, faFileSignature,faFileAlt} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {useTranslation} from "react-i18next";

const SidebarProjetManager  = () => {

    const { t } = useTranslation();

    return (
        <>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/validateOffers" end={true}>
                <FontAwesomeIcon icon={faBriefcase} className="mr-2"/>
                {t("validateOffers.dashboardTitle")}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/listCV" end={true}>
                <FontAwesomeIcon icon={faFilePdf} className="mr-2"/>
                {t('validationCV.validateCVs')}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/contracts" end={true}>
                <FontAwesomeIcon icon={faFileSignature} className="mr-2"/>
                {t("dashboardMenu.contracts")}
            </NavLink>
            <NavLink
                data-testid={"navlink"}
                className="taskbar-button flex items-center"
                to="/dashboard/assignToProf"
                end={true}>
                <FontAwesomeIcon icon={faChalkboardTeacher} className="mr-2"/>
                {t("dashboardMenu.assginToProf")}
            </NavLink>
            <NavLink className="taskbar-button flex items-center" to="/dashboard/reports" end={true}>
                <FontAwesomeIcon icon={faFileAlt} className="mr-2" />
                {t("dashboardMenu.reports")}
            </NavLink>
        </>
    )

}
export default SidebarProjetManager;