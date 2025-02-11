import {getUserInfo} from "../../utils/userInfo";
import StudentInternshipListCard from "./studentInternshipListCard";
import React, {useEffect, useState} from "react";
import ErrorBox from "../errorBox";
import {useTranslation} from "react-i18next";
import {useSession} from "../CurrentSession";

const InternshipListSingleNotification = ({selectedCvId,refIds, notificationIds, setPdfModalHelper}) => {
    const [internships, setInternships] = useState();
    const [errorMessage, setErrorMessage] = useState("");
    const [loading, setLoading] = useState(true)
    const { t } = useTranslation();
    const { currentSession, setCurrentSession } = useSession();
    const [applications, setApplications] = useState([]); // List of applications

    useEffect(() => {
        if (!currentSession.id) return
        fetchInternships()
        fetchApplications();
    }, [currentSession]);

    const fetchApplications = async () => {
        const token = getUserInfo().token;
        try {
            setLoading(true); // Start loading
            const response = await fetch('http://localhost:8080/student/applications?' +
                'season='+currentSession.season+
                "&year="+currentSession.year, {
                headers: { 'Authorization': token }
            });
            const data = await response.json();
            if (response.ok) {
                // console.log('Applications data:', data.value); // Add log
                setApplications(data.value);
            } else {
                throw new Error(data.message);
            }
        } catch (error) {
            setErrorMessage(t('Unable to fetch applications: ') + error.message);
        } finally {
            setLoading(false); // End loading
        }
    };

    const fetchInternships = async () => {
        const token = getUserInfo().token;
        try {
            const res = await fetch('http://localhost:8080/jobOffers/' + refIds
                , {
                    headers: { 'Authorization': token }
                });
            const data = await res.json();
            if (res.ok) {
                setInternships(data.value);
            } else {
                throw new Error(data.message);
            }
        } catch (error) {
            setErrorMessage(t('Unable to fetch internships: ') + error.message);
        }
    };
    // Helper function to get application ID for a given internship
    const getApplicationId = (internshipId) => {
        const app = applications.find(app => app.jobOffer.id === internshipId && app.active);
        return app ? app.id : null;
    };

    const isApplied = (internshipId) => {
        return applications.some(app => app.jobOffer.id === internshipId && app.active);
    };


    let applied;
    let applicationId;
    if (internships) {
        applied = isApplied(internships.id);
        applicationId = getApplicationId(internships.id);
    }
    return (
        <>
            {errorMessage.trim().length !== 0 && <ErrorBox msg={errorMessage} />}
            {(internships && !loading) &&
                    <StudentInternshipListCard selectedCvId={selectedCvId} setPdfModalHelper={setPdfModalHelper} fetchApplications={fetchApplications} internship={internships} applicationId={applicationId} applied={applied}></StudentInternshipListCard>}
        </>
    )
}

export default InternshipListSingleNotification;