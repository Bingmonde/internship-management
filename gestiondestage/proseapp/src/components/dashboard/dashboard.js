import { Trans, useTranslation } from "react-i18next";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
    faBars,
    faRightFromBracket,
    faHome,
    faInfoCircle,
    faUserGraduate,
    faFileUpload,
    faClipboardCheck,
    faBriefcase,
    faFileAlt,
    faUsers,
    faFilePdf, faUser
} from "@fortawesome/free-solid-svg-icons";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { clearUserInfo, getUserInfo } from "../../utils/userInfo";
import NotificationBox from "../notificationBox";
import PdfPreview from "../pdfPreview";
import { EmployeurAPI } from "../../api/employerAPI";
import { useSession } from "../CurrentSession";
import DashboardSidebar from "./sidebar/sidebar";
import DashboardTopbar from "./topbar/topbar";

const Dashboard = ({setNotifications, notifications, notificationError }) => {
    const navigate = useNavigate();
    const { t, i18n } = useTranslation();
    const [error, setError] = useState("");
    const { currentSession, setCurrentSession } = useSession();

    const [pdfModal, setPdfModal] = useState({ pdfUrl: "about:blank", filename: "file.pdf", showModal: false });

    const setPdfModalHelper = (pdfUrl, filename) => {
        setPdfModal({ pdfUrl: pdfUrl, filename: filename, showModal: true });
    };

    const username = getUserInfo().username;
    const handleLogout = () => {
        clearUserInfo();
        navigate('/connection');
    };

    const handleHam = () => {
        setHam(!hamburger);
    };

    const handleType = (askedtype) => {
        return getUserInfo().userType.toLowerCase() === askedtype.toLowerCase();
    };

    const removeNotification = (id) => {
        setNotifications(notifications.filter(a => a.lastId !== id));
    };

    const [hamburger, setHam] = useState(false);

    return (
        <>
            {pdfModal.showModal && <PdfPreview pdfModal={pdfModal} setPdfModal={setPdfModal}></PdfPreview>}
            <div className="flex flex-wrap h-screen max-h-screen bg-white">
                <DashboardSidebar hamburger={hamburger} handleHam={handleHam}></DashboardSidebar>

                <div className="flex w-screen lg:w-4/5 shrink content-center">
                    <DashboardTopbar handleLogout={handleLogout} handleHam={handleHam}></DashboardTopbar>

                    <div
                        className={((hamburger) ? "hidden " : "block ") + "lg:block w-screen h-screen max-h-screen overflow-y-auto"}>
                        <div
                            className="flex flex-col w-screen lg:w-full justify-center items-center text-start pt-20 overflow-y-auto min-h-0 grow">
                            {notifications.map(value => (
                                <NotificationBox key={value.lastId} notif={value}
                                                 remove={() => removeNotification(value.lastId)}/>
                            ))}
                            <Outlet context={[setPdfModalHelper]}></Outlet>
                        </div>
                    </div>

                </div>
            </div>
        </>
    );
};

export default Dashboard;
