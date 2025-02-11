import React from 'react';
import { format } from 'date-fns';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendarAlt, faLink, faCheckCircle, faTimesCircle, faUser } from '@fortawesome/free-solid-svg-icons';
import './interviewDetail.css';
import {faLocationDot} from "@fortawesome/free-solid-svg-icons/faLocationDot";
import {faCircleExclamation} from "@fortawesome/free-solid-svg-icons/faCircleExclamation";

const InterviewDetail = ({ interviews = []}) => {
    console.log('interviews', interviews);
    const { t } = useTranslation();

    const formatDate = (date) => {
        try {
            return format(new Date(date), 'yyyy-MM-dd HH:mm');
        } catch (error) {
            console.error('Invalid date format:', date);
            return t('jobInterview.invalidDate');
        }
    };

    const getStatusTextAndColor = (status) => {
        const isAccepted = status.isConfirmedByStudent;
        return {
            text: isAccepted ? t('jobInterview.accepted') : t('jobInterview.nonAccepted'),
            color: isAccepted ? 'text-green-600' : 'text-red-600',
            icon: isAccepted ? faCheckCircle : faTimesCircle,
        };
    };

    if (!Array.isArray(interviews)) {
        console.error("InterviewDetail expects 'interviews' to be an array.");
        return (
            <div className="interview-detail-overlay">
                <div className="interview-detail-content">
                    <p>{t('jobInterview.invalidData')}</p>
                    <button
                        className="bg-red-500 hover:bg-red-600 text-white w-20 py-2 text-center font-bold rounded mt-2"
                        // onClick={closeDetail}
                    >
                        {t('jobInterview.hide')}
                    </button>
                </div>
            </div>
        );
    }

    const getInterviewState = (interview) => {
        if (interview.cancelledDate != null) return "cancelled"

        const interviewDate = new Date(interview.interviewDate)
        if (interviewDate > new Date()) {
            return "future"
        }
        return "past"
    }

    return (
        <div className="interview-detail-overlay">
            <div className="interview-detail-content">
                <h4 className="font-bold mb-2">{t('jobInterview.details')}</h4>
                {interviews.length === 0 ? (
                    <p>{t('jobInterview.noInterviews')}</p>
                ) : (
                    interviews.map((interview, index) => {
                        const studentStatus = getStatusTextAndColor(interview);

                        return (
                            <div key={index} className="interview-item mb-4">
                                <p>
                                    <FontAwesomeIcon icon={faCircleExclamation} className="mr-2 text-blue-500"/>
                                    <b>{t('jobInterview.interviewState.label')}</b>: {t("jobInterview.interviewState."+getInterviewState(interview))}
                                </p>
                                <p>
                                    <FontAwesomeIcon icon={faCalendarAlt} className="mr-2 text-blue-500"/>
                                    <b>{t('jobInterview.interviewDate')}</b>: {formatDate(interview.interviewDate)}
                                </p>
                                <p>
                                    <FontAwesomeIcon icon={faUser} className="mr-2 text-purple-500"/>
                                    <b>{t('jobInterview.interviewType')}</b>: {t("jobInterview.interviewTypeLabels." + interview.interviewType.toLowerCase())}
                                </p>
                                <p className={studentStatus.color}>
                                    <FontAwesomeIcon icon={studentStatus.icon} className="mr-2"/>
                                    <b>{t('jobInterview.confirmedByStudent')}</b>: {t(studentStatus.text)}
                                </p>
                                {(interview.interviewType.toLowerCase() === "online") && <p>
                                    <FontAwesomeIcon icon={faLink} className="mr-2 text-green-500"/>
                                    <b>{t('jobInterview.Link')}</b>: <a className={"text-cyan-600 underline"}
                                                                        href={interview.interviewLocationOrLink}>{interview.interviewLocationOrLink}</a>
                                </p>}
                                {(interview.interviewType.toLowerCase() === "inperson") && <p>
                                    <FontAwesomeIcon icon={faLocationDot} className="mr-2 text-green-500"/>
                                    <b>{t('jobInterview.Location')}</b>: {interview.interviewLocationOrLink}
                                </p>}
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
};

// InterviewDetail.propTypes = {
//     interviews: PropTypes.array,
//     closeDetail: PropTypes.func.isRequired,
// };

export default InterviewDetail;
