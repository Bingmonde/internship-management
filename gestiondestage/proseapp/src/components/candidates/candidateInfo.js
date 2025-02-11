import {CandidateJobApplication} from "./candidateJobApplication";
import {CandidateInterviewInfo} from "./candidateInterviewInfo";
import {CandidateInternshipOfferInfo} from "./candidateInternshipOfferInfo";
import React, {useState} from "react";
import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission} from "../../constants";
import JobInterviewForm from "../jobInterviewForm";
import {useTranslation} from "react-i18next";
import {UserGeneralAPI} from "../../api/userAPI";
import {useOutletContext} from "react-router-dom";

const CandidateInfo = ({ candidate,setErrorMessage,cancelInterview,handleInviteClick,createInterview }) => {
    console.log('render candidate info')
    const [setPdfModalHelper] = useOutletContext()
    const interviews = candidate.interviewOffer
    const internshipOffer = candidate.internshipOffer;
    const jobApplication = candidate.jobOfferApplication;

    const { t } = useTranslation();
    const [candidateDetail, setCandidateDetail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => {setIsModalOpen(false); setCandidateDetail(null)};


    const [selectedApplication, setSelectedApplication] = useState(null);
    const [isModalInterviewOpen, setIsModalInterviewOpen] = useState(false);
    const openModalInterview = (application) =>  {
        setIsModalInterviewOpen(true);
        setSelectedApplication(application)
    }
    const closeModalInterview = () => {setIsModalInterviewOpen(false); setSelectedApplication(null)};

    const handlePreviewCandidate = (candidate) => {
        setCandidateDetail(candidate.jobOfferApplication.CV.studentDTO);
        openModal()
    }


    // Function to handle CV preview
    const downloadCV = async (filename, downloadName) => {
        try {
            const pdfUrl = await UserGeneralAPI.previewPDF(filename);
            if (pdfUrl) {
                setPdfModalHelper(pdfUrl, downloadName);
            } else {
                setErrorMessage(t('error.fetchFailed'));
            }
        }
        catch (error) {
            console.error(error);
            setErrorMessage(t(error));
        }
    }


        return (
            <div
                 className="flex flex-col bg-prose-neutral rounded-2xl mt-5 py-2">
                <div
                    className="flex flex-col justify-between px-4 items-center">

                    {/*info candidate*/}
                    <p className="text-xl font-bold text-darkpurple cursor-pointer hover:scale-110 "
                       onClick={() => handlePreviewCandidate(candidate)}>{jobApplication.CV.studentDTO.prenom} {jobApplication.CV.studentDTO.nom}</p>

                    {/*job application*/}
                    <CandidateJobApplication downloadCV={downloadCV}
                                             jobApplication={jobApplication}
                                             t={t}
                                             collapse={true}/>
                    <br/>

                    {/*interview action and status*/}
                    {interviews.length > 0 &&
                        <CandidateInterviewInfo interviews={interviews}
                                                internship={internshipOffer}
                                                cancelInterview={cancelInterview}
                                                collapse={true}/>
                    }
                    { (interviews.length === 0 || interviews[interviews.length - 1].cancelledDate) && !internshipOffer &&
                        <button
                            className="btn-neutral w-1/2"
                            onClick={() => {
                                openModalInterview(jobApplication)
                            }}>
                            {t('jobInterview.arrangeInterview')}
                        </button>
                    }
                    <br/>
                    {!internshipOffer &&
                        <button
                            className="btn-neutral w-1/2"
                            onClick={() => handleInviteClick(jobApplication.id)}>
                            {t('Invite to Internship')}
                        </button>
                    }
                    {/*internship offer action and status*/}
                    {internshipOffer && <CandidateInternshipOfferInfo
                        internshipOffer={internshipOffer} t={t}
                        collapse={true}/>}

                </div>

                {isModalOpen &&
                    <Modal onClose={closeModal}>
                        {candidateDetail && <ProfilePreview permission={Permission.Limited} profile={candidateDetail} />}
                    </Modal>
                }
                {isModalInterviewOpen &&
                    <Modal onClose={closeModalInterview}>
                        {selectedApplication &&  <JobInterviewForm jobApplication={selectedApplication} createInterview={createInterview} onClose={closeModalInterview} />}
                    </Modal>
                }

            </div>
        )
}

export default CandidateInfo;