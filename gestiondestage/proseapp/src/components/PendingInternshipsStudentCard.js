import React from 'react';
import SuccessBoxNoTrans from "./successBoxNoTrans";
import {formatDate, formatTime, formatWeekday} from "../api/utils";
import {useTranslation} from "react-i18next";
import {UserGeneralAPI} from "../api/userAPI";
import {faFilePdf, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const PendingInternshipsStudentCard = ({
                                           pendingInternship,
                                           setPdfModalHelper,
                                           handlePreviewCompany,
                                           confirmInternship,
                                           calculateCountDownDays,
                                       }) => {
    const { t } = useTranslation();

    return (
        <div key={pendingInternship.id} className="card-offer-list-item">
            <div className="flex flex-row justify-center items-center hover:scale-110 cursor-pointer">
                <h3 className="profile-section-title cursor-pointer hover:scale-110"
                    onClick={async () => {
                        const fileName = pendingInternship.jobOfferApplicationDTO.jobOffer.pdfDocu.fileName;
                        const pdfUrl = await UserGeneralAPI.previewPDF(fileName);
                        setPdfModalHelper(pdfUrl, fileName);
                    }}
                >
                    {pendingInternship.jobOfferApplicationDTO.jobOffer.titre}
                </h3>
                <FontAwesomeIcon icon={faFilePdf}
                                 className="profile-section-title ms-2 self-center"/>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable self-center'>{t('profile.companyName')}:</p>
                <div className="flex flex-row justify-center items-center hover:scale-110">
                    <p className="text-darkpurple cursor-pointer hover:scale-110 self-center"
                       onClick={() => handlePreviewCompany(pendingInternship.jobOfferApplicationDTO?.jobOffer.employeurDTO)}>
                        {pendingInternship.jobOfferApplicationDTO.jobOffer.employeurDTO.nomCompagnie}
                    </p>
                    <FontAwesomeIcon icon={faInfoCircle} className="profile-section-title ms-2 self-center"/>
                </div>

            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('Start Date')}:</p>
                <p className="self-center">
                    {pendingInternship.jobOfferApplicationDTO.jobOffer.dateDebut} {t('weekdays.' + formatWeekday(pendingInternship.jobOfferApplicationDTO.jobOffer.dateDebut))}
                </p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('pendingInternshipsStudent.endDate')}:</p>
                <p className="self-center">
                    {pendingInternship.jobOfferApplicationDTO.jobOffer.dateFin} {t('weekdays.' + formatWeekday(pendingInternship.jobOfferApplicationDTO.jobOffer.dateFin))}
                </p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('Location')}:</p>
                <p className="self-center">{pendingInternship.jobOfferApplicationDTO.jobOffer.lieu}</p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('Work Type')}:</p>
                <p className="self-center">{t(pendingInternship.jobOfferApplicationDTO.jobOffer.typeTravail)}</p>
            </div>
            {!pendingInternship.confirmationDate &&
                <>
                    <div className='flex flex-row p-1'>
                        <p className='card-offer-list-item-lable'>{t('pendingInternshipsStudent.expirationDate')}:</p>
                        <p className="self-center">
                            {formatDate(pendingInternship.expirationDate)} {t('weekdays.' + formatWeekday(pendingInternship.expirationDate))} {formatTime(pendingInternship.expirationDate)}
                        </p>
                    </div>
                    <p className="text-red text-center">
                        {t('pendingInternshipsStudent.internshipOfferCountDownDays', {days: calculateCountDownDays(pendingInternship.expirationDate)})}
                    </p>
                    <div className="flex justify-center items-center w-full mt-3">
                        <button
                            className="btn-confirm w-1/2"
                            onClick={() => confirmInternship(pendingInternship.id)}>
                            {t('pendingInternshipsStudent.confirm')}
                        </button>
                    </div>
                </>
            }
            {pendingInternship?.confirmationDate &&
                <SuccessBoxNoTrans
                    msg={t("pendingInternshipsStudent.confirmedOn") + " " + formatDate(pendingInternship?.confirmationDate)}
                />
            }
        </div>
    );
};

export default PendingInternshipsStudentCard;
