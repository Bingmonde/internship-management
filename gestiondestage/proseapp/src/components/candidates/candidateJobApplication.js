import {format} from "date-fns";
import React from "react";
import {ConfirmedStatusIcon} from "./stausIcons";
import './candidates.css';
import {formatDate, formatTime} from "../../api/utils";
export const CandidateJobApplication = ({jobApplication, downloadCV, t}) => {
    // function utilitaire
    const generateFileName = (offerTitle, student, applicationDate) => {
        return `${offerTitle.replaceAll(' ', '_')}` + '_'+ student.prenom + '_' + student.nom + '_' + format(applicationDate, 'yyyy-MM-dd') + '.pdf';
    }
    return (
        <div
            className="flex flex-col border-l-4 border-greenish w-full p-2">
            <div
                className="pl-2 flex flex-row w-full">
                <ConfirmedStatusIcon/>
                <div className="ms-2 self-center">
                    {t('listCandidates.status.jobApplied')}
                </div>
            </div>
             <>
                <div className='flex flex-row p-1'>
                    <p className='card-offer-list-item-lable'>{t('Application Date')}:</p>
                    <p className="self-center">{formatDate(jobApplication.applicationDate)}</p>
                </div>
                <div className="text-center">
                    <button
                        className="btn-neutral w-1/2"
                        onClick={() => downloadCV(jobApplication.CV?.pdfDocu?.fileName, generateFileName(jobApplication.jobOffer.titre, jobApplication.CV.studentDTO, jobApplication.applicationDate))}
                    >
                        {t('contract.studentInfo.cvButton')}
                    </button>
                </div>
            </>

        </div>
    )
}