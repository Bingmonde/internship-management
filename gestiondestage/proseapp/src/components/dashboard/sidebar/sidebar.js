import {useTranslation} from "react-i18next";
import {useForm} from "react-hook-form";
import React from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {
    faBars,
    faBriefcase,
    faClipboardCheck, faFileAlt, faFilePdf,
    faFileUpload,
    faHome,
    faFileSignature,
    faChartLine,
    faInfoCircle, faUserGraduate, faUsers, faUser,faChalkboardTeacher
} from "@fortawesome/free-solid-svg-icons";
import {NavLink} from "react-router-dom";
import {getUserInfo} from "../../../utils/userInfo";
import logo from "../../../assets/image/prose-white.png";
import SidebarStudent from "./sidebarStudent";
import SidebarEmployeur from "./sidebarEmployeur";
import SidebarProjetManager from "./sidebarProjetManager";

const DashboardSidebar = ({hamburger, handleHam}) => {

    const { t } = useTranslation();

    const handleType = (askedtype) => {
        return getUserInfo().userType.toLowerCase() === askedtype.toLowerCase();
    };

    return (
        <div
            data-testid={"dashboard-sidebar"}
            className={((hamburger) ? "h-screen w-screen " : "hidden ") + "lg:block z-20 lg:w-1/5 text-white bg-gradient-to-br from-darkpurple from-10% via-selected via-30% to-lightpurple to-90% " +
                "border-r-2 border-darkpurple h-screen overflow-y-auto"}>
            <div className="flex flex-col z-100">
                <h2 className="p-4 font-bold text-start m-0 mb-2 flex items-center">
                    <FontAwesomeIcon
                        data-testid={"sidebar-ham"}
                        icon={faBars}
                        className="inline-block lg:hidden mr-2 px-4 self-center hover:scale-125 transition-transform duration-300 pb-0 cursor-pointer"
                        onClick={handleHam}
                    />
                    Prose <img src={logo} alt="Image de plume" className="h-12 inline mr-2"/>
                </h2>
                <NavLink data-testid={"navlink"} className="taskbar-button flex items-center" to="/dashboard"
                         end={true}>
                    <FontAwesomeIcon icon={faHome} className="mr-2"/>
                    {t("dashboardMenu.home")}
                </NavLink>

                {handleType("student") && (
                    <SidebarStudent/>
                )}

                {handleType("Employeur") && (
                    <SidebarEmployeur/>
                )}

                {handleType("projet_manager") && (
                    <SidebarProjetManager/>
                )}

                {handleType("TEACHER") && (
                    <NavLink
                        data-testid={"navlink"}
                        className="taskbar-button flex items-center"
                        to="/dashboard/myStudents"
                        end={true}
                    >
                        <FontAwesomeIcon icon={faUsers} className="mr-2"/>
                        {t("studentList.myStudents")}
                    </NavLink>
                )}

                <NavLink className="taskbar-button flex items-center mt-4" to="/dashboard/myProfile" end={true}>
                    <FontAwesomeIcon icon={faUser} className="mr-2"/>
                    {t("dashboardMenu.monProfile")}
                </NavLink>
            </div>
        </div>
    )
}

export default DashboardSidebar;