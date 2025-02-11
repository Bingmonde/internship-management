import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCheck, faHourglassHalf} from "@fortawesome/free-solid-svg-icons";
import {formatDate, formatTime, formatWeekday} from "../../api/utils";
import React from "react";
import {ConfirmedStatusIcon, WaitingStatusIcon} from "./stausIcons";
import './candidates.css';

export const CandidateInternshipOfferInfo = ({internshipOffer, t}) => {
    const barColor = internshipOffer && internshipOffer.confirmationDate ? "border-confirmed" : "border-pending";
    const statusText = internshipOffer && internshipOffer.confirmationDate ? "listCandidates.status.offerAccepted" : "listCandidates.status.offerPending";

    return (
        <div
            className={"flex flex-col border-l-4 w-full p-2 " + barColor}>
            {internshipOffer &&
            internshipOffer.confirmationDate
                ? (
                    <>
                        <div
                            className="pl-2 flex flex-row w-full">
                            <ConfirmedStatusIcon />
                            <p className="text-green-600 p-2">{t(statusText)}</p>
                        </div>
                            <div
                                className='flex flex-row p-1'>
                                <p className='card-offer-list-item-lable'>{t('listCandidates.confirmationDate')}:</p>
                                <p className="self-center">{formatDate(internshipOffer.confirmationDate)} {t('weekdays.' + formatWeekday(internshipOffer.confirmationDate))} {formatTime(internshipOffer.confirmationDate)}</p>

                            </div>

                    </>)
                : (
                    <>
                        <div
                            className="pl-2 flex flex-row w-full">
                            <WaitingStatusIcon/>
                            <div
                                className="ms-2 self-center">{t(statusText)}
                            </div>
                        </div>

                            <div
                                className='flex flex-row p-1'>
                                <p className='card-offer-list-item-lable'>{t('pendingInternshipsStudent.expirationDate')}:</p>
                                <p className="self-center">{formatDate(internshipOffer.expirationDate)} {t('weekdays.' + formatWeekday(internshipOffer.expirationDate))} {formatTime(internshipOffer.expirationDate)}</p>

                            </div>
                    </>)

            }
        </div>
    )
}