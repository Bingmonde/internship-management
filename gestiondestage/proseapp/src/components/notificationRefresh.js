import React, {useEffect} from 'react';
import {getUserInfo} from "../utils/userInfo";
import {useNavigate} from "react-router-dom";
import {faBars, faRightFromBracket, faSpinner} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import WelcomePage from "./welcomePage/welcomePage";

const NotificationRefresh = ({notifications, setNotifications, setError}) => {
    const navigate = useNavigate();

    const defaultPage = {
        "" : "/dashboard/welcome",
        "uploadCV" : "/dashboard/uploadCV",
        "applyJobOffer" : "/dashboard/internships",
        "respondInterview" : "/dashboard/interviewListStudent",
        "respondInternship" : "/dashboard/pendingInternships",
        "uploadJobOffer" : "/dashboard/uploadOffers",
        "listCandidates" : "/dashboard/candidatesList",
        "listInterview" : "/dashboard/interviewList",
        "validateCV" : "/dashboard/listCV",
        "validateJobOffer" : "/dashboard/validateOffers",
        "signContract" : "/dashboard/contracts"
    }

    const fetchNotification = async () => {
        let pageToMode = ""
        try {
            const res = await fetch('http://localhost:8080/notifications', {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                },
            });
            if (res.ok) {
                try {
                    const result = await res.json();
                    setNotifications(result.value.notifications);
                    pageToMode = result.value.page;
                } catch (e) {
                    setError("error.notification")
                }
            } else {
                setError("error.notification")
            }
        } catch (error) {
            console.error(error);
            setError('error.network');
        }
        navigate(defaultPage[pageToMode])
    }

    // fetchNotification()

    return (
        // <div className={"fullpage-form"}>
        //     <FontAwesomeIcon icon={faSpinner}
        //                      className="px-4 self-center hover:scale-125 transition-transform duration-300 pb-0 "/>
        // </div>
        <WelcomePage/>
    );
}

export default NotificationRefresh;