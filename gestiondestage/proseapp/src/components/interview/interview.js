import {UserGeneralAPI} from "../../api/userAPI";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFilePdf, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {formatDate, formatTime, formatWeekday} from "../../api/utils";
import React, {useState} from "react";
import {useTranslation} from "react-i18next";
import {useOutletContext} from "react-router-dom";
import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission} from "../../constants";
import InterviewBasicInfo from "./interviewBasicInfo";
import {getUserInfo} from "../../utils/userInfo";

const Interview = ({interview, handleCancelInterview,handleConfirmInterview, fullPreview = true}) => {
    const { t } = useTranslation();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [setPdfModalHelper] = useOutletContext()
    const [candidateDetail, setCandidateDetail] = useState(null);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => {setIsModalOpen(false); setCandidateDetail(null)};


    const usertype = getUserInfo().userType;

    const handlePreviewCandidate = (interview) => {
        setCandidateDetail(interview.jobOfferApplication?.CV.studentDTO)
        openModal()
    }

    const findInterviewee = (interview) => {
        const fileInfo =  interview.jobOfferApplication.CV.pdfDocu.fileName.split('_')
        return fileInfo[1] + ' ' + fileInfo[2]
    }

    const boxClassName = fullPreview ? "card-offer-list-item" : "card-offer-list-brief"

    return (
        <div className={boxClassName} style={{height: fullPreview && "480px"}}>
            {fullPreview && <div>
                {(interview.jobOfferApplication?.jobOffer.pdfDocu )?
                    (
                        <div className="flex flex-row justify-center items-center hover:scale-110 cursor-pointer"
                             onClick={async () => {
                                 const filename = interview.jobOfferApplication?.jobOffer.pdfDocu.fileName
                                 const pdfUrl = await UserGeneralAPI.previewPDF(filename)
                                 setPdfModalHelper(pdfUrl, filename)
                             }}
                        >
                            <h3 className="profile-section-title">{interview.jobOfferApplication?.jobOffer?.titre}</h3>
                            <FontAwesomeIcon icon={faFilePdf}
                                             className="profile-section-title ms-2 self-center"/>
                        </div>
                    )
                    :
                    <h3 className="profile-section-title">{interview.jobOfferApplication?.jobOffer?.titre}</h3>}
            </div>
            }

            {fullPreview &&
                <div className='flex flex-row items-center'>
                    <p className='card-offer-list-item-lable'>{t('jobInterview.interviewee')}:</p>
                    <div className="flex flex-row justify-center items-center hover:scale-110">
                        <p className="text-darkpurple  cursor-pointer "
                           onClick={() => handlePreviewCandidate(interview)}>{findInterviewee(interview)}</p>
                        <FontAwesomeIcon icon={faInfoCircle} className="profile-section-title ms-2 self-center"/>
                    </div>
                </div>
            }
            <InterviewBasicInfo interview={interview} />
            {usertype.toString() === 'EMPLOYEUR' &&

                <div className="flex justify-center items-center w-full">
                    {!interview.cancelledDate &&
                        <button
                            onClick={() => handleCancelInterview(interview.id)}
                            className="btn-cancel w-1/2"
                        >
                            {t('jobInterview.cancelInterview')}
                        </button>
                    }
                </div>
            }

            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {candidateDetail && <ProfilePreview permission={Permission.Limited} profile={candidateDetail}/>}
                </Modal>
            }
        </div>
    )

}

export default Interview;