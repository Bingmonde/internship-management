import {UserGeneralAPI} from "../api/userAPI";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFilePdf, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {formatDate, formatTime, formatWeekday} from "../api/utils";
import SuccessBox from "./successBox";
import React, {useEffect, useState} from "react";
import {getUserInfo} from "../utils/userInfo";
import {useTranslation} from "react-i18next";
import Modal from "./modal";
import ProfilePreview from "./userProfile/profilePreview";
import {Permission} from "../constants";
import {StudentAPI} from "../api/studentAPI";
import InterviewListStudentCard from "./InterviewListStudentCard";
import {useSession} from "./CurrentSession";

const InterviewListStudentSingleNotification = ({refIds, notificationIds, setPdfModalHelper }) => {

     const [interview, setInterview] = useState(null);
     const [errorMessage, setErrorMessage] = useState("");
     const { t } = useTranslation();
     const [companyDetail, setCompanyDetail] = useState(null);
     const [isModalOpen, setIsModalOpen] = useState(false);
     const openModal = () =>  setIsModalOpen(true);
     const closeModal = () => {setIsModalOpen(false); setCompanyDetail(null)};

     console.log("refIds : "+ refIds)
     console.log("refIds is  : "+ typeof refIds)

    const fetchInterView = async () => {
        const token = getUserInfo().token;
        try {
            const res = await fetch('http://localhost:8080/jobInterview/' + refIds, {
                headers: { 'Authorization': token },
            });
            const data = await res.json();
            if (res.ok) {
                setInterview(data.value);
            } else {
                console.error("Fetch failed:", data);
            }
        } catch (error) {
            setErrorMessage(t('Unable to fetch internships: ') + error.message);
        }
    };


    useEffect(() => {
        fetchInterView()
     }, [])

    const handleConfirmInterview = async (interviewId) => {
        const res = await StudentAPI.confirmInterview(interviewId);
        if (res.value != null){
            fetchInterView()
        }
        else{
            setErrorMessage(res["exception"])
        }

    }
    const findCompany = (interview) => {
        return interview.jobOfferApplication.jobOffer.employeurDTO.nomCompagnie
    }

    const handlePreviewCompany = (interview) => {
        setCompanyDetail(interview.jobOfferApplication?.jobOffer.employeurDTO)
        openModal()
    }

    if(interview === null) return <span>Loading...</span>


    return (
        <>
            <InterviewListStudentCard
                interview={interview}
                handleConfirmInterview={handleConfirmInterview}
                handlePreviewCompany={handlePreviewCompany}
                setPdfModalHelper={setPdfModalHelper}
                findCompany={findCompany}
            />
            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {companyDetail && <ProfilePreview permission={Permission.Limited} profile={companyDetail} />}
                </Modal>
            }
        </>
    )
}

export default InterviewListStudentSingleNotification;