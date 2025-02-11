
import Interview from "../interview/interview";
import React from "react";
import {CancelledStatusIcon, ConfirmedStatusIcon, WaitingStatusIcon} from "./stausIcons";
import './candidates.css';
import {useTranslation} from "react-i18next";

export const CandidateInterviewInfo = ({interviews, internship, cancelInterview}) => {
    const isInterviewConfirmed = interviews.length > 0 && interviews[interviews.length - 1].isConfirmedByStudent && !interviews[interviews.length - 1].cancelledDate;
    const isWaitingForConfirmation = interviews.length > 0 && !interviews[interviews.length - 1].cancelledDate && !interviews[interviews.length - 1].isConfirmedByStudent;
    const isInterviewExpired = interviews.length > 0 && new Date(interviews[interviews.length - 1].interviewDate) < new Date();
    const isInterviewCancelled = interviews.length > 0 && interviews[interviews.length - 1].cancelledDate != null;
    const isInternshipOfferSend = internship != null

    const redIcon =   (isInterviewExpired && !isInternshipOfferSend) || isInterviewCancelled
    const yellowIcon = isWaitingForConfirmation && !isInterviewExpired
    const greenIcon = isInterviewConfirmed

    const barColor = greenIcon ? "border-confirmed" : yellowIcon ? "border-pending" : "border-cancelled";
    const statusText = greenIcon ? "listCandidates.status.interviewConfirmed" : yellowIcon ? "listCandidates.status.interviewPending" : "listCandidates.status.interviewCancelled";
    const { t } = useTranslation();
    return (
        <div
            className={"flex flex-col border-l-4 w-full p-2 " + barColor}>
            <div
                className="pl-2 flex flex-row w-full">

                    {greenIcon && <ConfirmedStatusIcon/>}
                    {yellowIcon && <WaitingStatusIcon/>}
                    {redIcon && <CancelledStatusIcon/>}
                    <div
                        className="ms-2 self-center">{t(statusText)}
                    </div>

            </div>
                <div className="w-full">
                    <Interview interview={interviews[interviews.length - 1]}
                               handleCancelInterview={() => cancelInterview(interviews[0].id)}
                               fullPreview={false}/>

                </div>


        </div>
    )
}