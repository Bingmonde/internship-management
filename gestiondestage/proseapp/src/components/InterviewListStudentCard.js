import React from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFilePdf, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {formatDate, formatTime, formatWeekday} from "../api/utils";
import SuccessBox from "./successBox";
import {UserGeneralAPI} from "../api/userAPI";
import {useTranslation} from "react-i18next";
import Interview from "./interview/interview";
import InterviewBasicInfo from "./interview/interviewBasicInfo";

const InterviewListStudentCard = ({
                           interview,
                           handleConfirmInterview,
                           handlePreviewCompany,
                           setPdfModalHelper,
                           findCompany,
                       }) => {
    const { t } = useTranslation();

    return (
        <div key={interview.id} className="card-offer-list-item">
            {interview.jobOfferApplication?.jobOffer.pdfDocu ? (
                <div
                    className="flex flex-row justify-center items-center hover:scale-110 cursor-pointer"
                    onClick={async () => {
                        const filename = interview.jobOfferApplication?.jobOffer.pdfDocu.fileName;
                        const pdfUrl = await UserGeneralAPI.previewPDF(filename);
                        setPdfModalHelper(pdfUrl, filename);
                    }}
                >
                    <h3 className="profile-section-title">{interview.jobOfferApplication?.jobOffer?.titre}</h3>
                    <FontAwesomeIcon icon={faFilePdf} className="profile-section-title ms-2 self-center" />
                </div>
            ) : (
                <h3 className="profile-section-title">{interview.jobOfferApplication?.jobOffer?.titre}</h3>
            )}

            <div className="flex flex-row p-1 items-center">
                <p className="card-offer-list-item-lable">{t("profile.companyName")}:</p>
                <div className="flex flex-row justify-center items-center">
                    <p
                        className="text-darkpurple cursor-pointer hover:scale-110"
                        onClick={() => handlePreviewCompany(interview)}
                    >
                        {findCompany(interview)}
                    </p>
                    <FontAwesomeIcon icon={faInfoCircle} className="profile-section-title ms-2 self-center" />
                </div>
            </div>
            <InterviewBasicInfo interview={interview} />
            {/*</div>*/}
            <div className="flex justify-center items-center w-full mt-3">
                {!interview.cancelledDate && !interview.isConfirmedByStudent &&
                    <button
                        className="btn-confirm w-1/2"
                        onClick={() => {
                            handleConfirmInterview(interview.id).then();
                        }}
                    >
                        {t("jobInterview.confirm")}
                    </button>
                }
            </div>
        </div>
    );
};

export default InterviewListStudentCard;
