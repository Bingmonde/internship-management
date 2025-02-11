import React from "react";
import {useTranslation} from "react-i18next";


const JobOfferDetail = ({ jobOffer}) => {
    const {t} = useTranslation();

    const convertTimeToFormat = (time) => {
        return time.split(':').slice(0, 2).join(':');
    }

    return (

        <>
            {/*<div className='flex flex-row p-1'>*/}
            {/*    <p className='card-offer-list-item-lable'>Description:</p>*/}
            {/*    <p>{jobOffer.description}</p>*/}
            {/*</div>*/}
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('formLabels.offerNumberPeople')}:</p>
                <p>{jobOffer.nombreStagiaire}</p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('Start Date')}:</p>
                <p>{jobOffer.dateDebut}</p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('End Date')}:</p>
                <p>{jobOffer.dateFin}</p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('formLabels.offerSalary')}:</p>
                <p>{jobOffer.tauxHoraire} $</p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('jobDetails.weeklyHours')}:</p>
                <p>{jobOffer.weeklyHours} hrs</p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('jobDetails.dailySchedule')}:</p>
                <p> {convertTimeToFormat(jobOffer.dayScheduleFrom)} - {convertTimeToFormat(jobOffer.dayScheduleTo)}</p>
            </div>


            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('Location')}:</p>
                <p>{jobOffer.lieu}</p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('Work Type')}:</p>
                <p>{t(jobOffer.typeTravail)}</p>
            </div>

        </>
    )
}

export default JobOfferDetail;