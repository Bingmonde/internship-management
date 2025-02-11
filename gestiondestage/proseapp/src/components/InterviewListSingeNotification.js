import Interview from "./interview/interview";
import React, {useEffect, useState} from "react";
import {EmployeurAPI} from "../api/employerAPI";
import {getUserInfo} from "../utils/userInfo";
import {useTranslation} from "react-i18next";

const InterviewListSingeNotification = ({ refIds, notificationIds, setPdfModalHelper }) => {

    const [errorMessage, setErrorMessage] = useState('');
    const [interview, setInterview] = useState(null);
    const {t} = useTranslation();

    useEffect(() => {
        fetchInterview()
    }, []);

    const fetchInterview = async () => {
        const token = getUserInfo().token;
        try {
            const res = await fetch('http://localhost:8080/jobInterview/' + refIds, {
                headers: { 'Authorization': token },
            });
            console.log("Response status:", res.status);

            const data = await res.json();
            if (res.ok) {
                console.log("Fetched data:", data);
                setInterview(data.value);
            } else {
                console.error("Fetch failed:", data);
            }
        } catch (error) {
            console.error("Fetch error:", error.message);
            setErrorMessage(t('Unable to fetch internships: ') + error.message);
        }
    };

    const handleCancelInterview = async (interviewId) => {
        const res = await EmployeurAPI.cancelInterveiw(interviewId);
        if (res.value != null){
            fetchInterview()
        }
        else
            setErrorMessage(res["exception"])
    }

    if(interview === null) return <div>Loading...</div>

    return (
        <Interview key={interview.id}
                   interview={interview}
                   handleCancelInterview={handleCancelInterview}
                   fullPreview={true}
        />
    )

}

export default InterviewListSingeNotification;