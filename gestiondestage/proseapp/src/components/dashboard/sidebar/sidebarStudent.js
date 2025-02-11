import {NavLink} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {
    faBriefcase,
    faClipboardCheck,
    faFileSignature,
    faFileUpload,
    faUserGraduate
} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {useTranslation} from "react-i18next";

const SidebarStudent = () => {

    const { t } = useTranslation();

    return (
        <>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/internships" end={true}>
                <FontAwesomeIcon icon={faBriefcase} className="mr-2"/>
                {t('dashboardMenu.internships')}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/uploadCV" end={true}>
                <FontAwesomeIcon icon={faFileUpload} className="mr-2"/>
                {t('dashboardMenu.studentUploadCV')}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button mt-4 flex items-center"
                     to="/dashboard/interviewListStudent"
                     end={true}>
                <FontAwesomeIcon icon={faClipboardCheck} className="mr-2"/>
                {t('dashboardMenu.myInterviews')}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/pendingInternships"
                     end={true}>
                <FontAwesomeIcon icon={faUserGraduate} className="mr-2"/>
                {t('dashboardMenu.pendingInternships')}
            </NavLink>
            <NavLink data-testid={"navlink"} className="taskbar-button flex items-center"
                     to="/dashboard/contracts" end={true}>
                <FontAwesomeIcon icon={faFileSignature} className="mr-2"/>
                {t("dashboardMenu.contracts")}
            </NavLink>
        </>
    )

};

export default SidebarStudent;