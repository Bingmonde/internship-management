import React from "react";
import {useTranslation} from "react-i18next";

const StudentCVCard = ({cv,handlePreview}) => {
    const { t, i18n } = useTranslation();

    return (<div key={cv.id}>
        <div className="card-offer-list-item">
            <h3 className="profile-section-title">{cv.pdfDocu?.fileName || 'No file name available'}</h3>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('validationCV.uploadedOn')}:</p>
                <p className="self-center">
                    {cv.dateHeureAjout ? new Date(cv.dateHeureAjout).toLocaleString() : 'N/A'}
                </p>
            </div>
            <div className='flex flex-row p-1'>
                <p className='card-offer-list-item-lable'>{t('validationCV.status')}:</p>
                <p className="self-center">
                                 <span style={{color: cv.status === 'validated' ? 'green' : 'red'}}>
                                     {t(`elementState.${cv.status}`)}
                                 </span>
                </p>
            </div>
            <div className='flex flex-row p-1 justify-center'>
                <button
                    className="btn-neutral w-1/2"
                    onClick={() => handlePreview(cv.pdfDocu?.fileName)}>
                    {t('preview')}
                </button>
            </div>
        </div>
    </div>
    );
};

export default StudentCVCard;