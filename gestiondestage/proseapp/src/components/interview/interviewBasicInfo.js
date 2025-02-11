import {formatDate, formatTime, formatWeekday} from "../../api/utils";
import React from "react";
import {useTranslation} from "react-i18next";
import {getUserInfo} from "../../utils/userInfo";


const InterviewBasicInfo = ({ interview }) => {
    const { t } = useTranslation();

    const checkInterviewStatus = (interview) => {
        const isCancelled = interview.cancelledDate != null;
        const isConfirmed = interview.isConfirmedByStudent;

        if (isCancelled) return 'jobInterview.cancelled';
        else if (isConfirmed) return 'jobInterview.confirmed';
        else return 'jobInterview.nonConfirmed';
    }


    return (
        <>
            <div className='flex flex-row'>
                <p className='card-offer-list-item-lable'>{t('jobInterview.interviewDate')}:</p>
                <p className="self-center">{formatDate(interview.interviewDate)} {t('weekdays.' + formatWeekday(interview.interviewDate))}</p>
            </div>
            <div className='flex flex-row'>
                <p className='card-offer-list-item-lable'>{t('jobInterview.interviewTime')}:</p>
                <p className="self-center">{formatTime(interview.interviewDate)}</p>
            </div>
            <div className='flex flex-row'>
                <p className='card-offer-list-item-lable'>{t('jobInterview.status')}:</p>
                <p className={interview.cancelledDate == null ? "text-black" : "text-red"}>
                    {t(checkInterviewStatus(interview))}</p>
            </div>
            <div className='flex flex-row'>
                <p className='card-offer-list-item-lable'>{t('jobInterview.interviewType')}:</p>
                <p>{t('jobInterview.interviewType' + interview.interviewType) + ""}</p>
            </div>
            {interview.interviewType.toLowerCase() === "online" &&
                <div className='flex flex-row'>
                    <p className='card-offer-list-item-lable'>{t('jobInterview.link')}:</p>
                    <a href={interview.interviewLocationOrLink}
                       target="_blank"
                       rel="noreferrer"
                       className="text-blue-600 hover:underline">{interview.interviewLocationOrLink}</a>
                </div>
            }
            {interview.interviewType.toLowerCase() === "inperson" &&
                <div className='flex flex-row'>
                    <p className='card-offer-list-item-lable'>{t('jobInterview.location')}:</p>
                    <p>{interview.interviewLocationOrLink}</p>

                </div>
            }
        </>
    )
}

export default InterviewBasicInfo;