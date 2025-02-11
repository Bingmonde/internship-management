import {useEffect, useState} from "react";
import {useSession} from "../CurrentSession";
import {getUserInfo} from "../../utils/userInfo";
import UploadCVForm from "../studentCVs/uploadCVForm";
import PendingInternshipsStudent from "../pendingInternshipsStudent";
import InterviewListStudent from "../interview/interviewsListStudent";
import ListCandidates from "../candidates/listCandidates";
import InterviewList from "../interview/interviewList";
import ContractList from "../contracts/contractList";
import {UserGeneralAPI} from "../../api/userAPI";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBell} from "@fortawesome/free-solid-svg-icons";
import ValidateOffer from "../validateOffer";
import ListCV from "../listCV";
import AssignToProf from "../assignToProf";
import InternshipList from "../studentInternshipList/InternshipList";
import StudentsListTeacher from "../studentsListTeacher";
import StudentsListEmployer from "../studentsListEmployer";
import StudentCvNotification from "../studentCVs/studentCvNotification";
import {useOutletContext} from "react-router-dom";
import InternshipListNotifications from "../studentInternshipList/internshipListNotifications";
import InterviewListStudentNotifications from "../InterviewListStudentNotifications";
import ContractNotificationMap from "../contracts/contractNotificationMap";
import PendingInternshipsStudentNotifications from "../PendingInternshipsStudentNotifications";
import InterviewListNotifications from "../InterviewListNotifications";
import WelcomePageStudent from "./welcomePageStudent";
import WelcomePageEmployeur from "./welcomePageEmployeur";
import WelcomePageTeacher from "./welcomePageTeacher";
import WelcomePageProjetManager from "./welcomePageProjetManager";
import {useTranslation} from "react-i18next";

const WelcomePage = () => {
    const [setPdfModalHelper] = useOutletContext()
    const { currentSession, setCurrentSession } = useSession();
    const [error, setError] = useState("");
    const [originalNotifications, setOriginalNotifications] = useState([]);
    const [notifications, setNotifications] = useState([]);
    const [markAllAsRead, setMarkAllAsRead] = useState(false);

    const {t} = useTranslation();

    // fetch notifications
    const fetchNotifications = async () => {
        try {
            const res = await fetch("http://localhost:8080/notifications/unread?" +
                "season="+currentSession.season+"&" +
                "year="+currentSession.year
                , {
                    method: 'GET',
                    headers: {
                        'Authorization': getUserInfo().token,
                    },
                });
            if (res.ok) {
                const result = await res.json();
                console.log('usertype', getUserInfo().userType);
                console.log( 'notifications unread',result);
                // if (result.value.notifications.length > 0) {
                    setOriginalNotifications(result.value.notifications);
                    // notification classified by type
                    const notificationNames = new Set();

                    result.value.notifications.forEach(notification => {
                        if (notification.type.split(".")[1] === "user_created") UserGeneralAPI.markReadNotification([notification.id])
                        notificationNames.add(notification.type.split(".")[1]);
                    })

                    console.log('notificationNames:', notificationNames);
                    // convert set to list
                    setNotifications([...notificationNames].map((notificationName) => {
                        return {
                            key: notificationName,
                            value: {
                                refIds: getRefIds(result.value.notifications.filter(notification => notification.type.split(".")[1] === notificationName)),
                                ids: getNotificationIds(result.value.notifications.filter(notification => notification.type.split(".")[1] === notificationName))
                            }
                        }
                    }))
                // }
            }
            else {
                setError("error.notification")
            }
        } catch (error) {
            console.error(error);
            setError('error.network');
        }
    }

    useEffect(() => {
        if(!currentSession.id) return
        setMarkAllAsRead(false)
        fetchNotifications()
    }, [currentSession]);

    const markAllNotificationsAsRead = async () => {
        await UserGeneralAPI.markReadNotification(originalNotifications.map(notification => notification.id));
        setMarkAllAsRead(true)
    }

    const getRefIds = (notifications) => notifications.map((notification) => notification.referenceId);
    const getNotificationIds = (notifications) => notifications.map((notification) => notification.id);

    return (
        <div className="w-full">
            <h2>{t("welcomePage.welcome")}</h2>
            {(markAllAsRead || notifications.length === 0) &&
                <h3 className="text-2xl text-center">{t("welcomePage.noNewNotifications")}</h3>}
            {!markAllAsRead && (
                <>
                    {notifications.length > 0 && !markAllAsRead &&
                        <div className="flex flex-col">
                            <h3 className="text-2xl text-center">{t("welcomePage.newNotifications")} {originalNotifications.length} notification(s)</h3>
                            <div className="flex flex-row cursor-pointer hover:text-red ml-auto xl:w-1/3 w-full p-3 text-darkpurple" >
                                <FontAwesomeIcon icon={faBell} className="text-2xl self-center"/>
                                <div className="  mx-5 self-end me-10 text-xl"
                                     onClick={markAllNotificationsAsRead}>{t("welcomePage.markAllAsRead")}</div>
                            </div>

                        </div>}

                    {notifications.length > 0 && getUserInfo().userType.toString().toLowerCase() === "student" &&
                        notifications.map((notification) => {
                            return <WelcomePageStudent notification={notification} setPdfModalHelper={setPdfModalHelper}/>
                        })
                    }

                    {notifications.length > 0 && getUserInfo().userType.toString().toLowerCase() === "employeur" &&
                        notifications.map((notification) => {
                            return <WelcomePageEmployeur notification={notification} setPdfModalHelper={setPdfModalHelper}/>
                        })
                    }

                    {notifications.length > 0 && getUserInfo().userType.toString().toLowerCase() === "teacher" &&
                        notifications.map((notification) => {
                            return <WelcomePageTeacher notification={notification} setPdfModalHelper={setPdfModalHelper}/>
                        })
                    }

                    {notifications.length > 0 && getUserInfo().userType.toString().toLowerCase() === "projet_manager" &&
                        notifications.map((notification) => {
                            return <WelcomePageProjetManager notification={notification} setPdfModalHelper={setPdfModalHelper}/>
                        })
                    }
                </>
            )
            }

        </div>
    );
}

export default WelcomePage;