import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFilePdf, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import React, {useState} from "react";
import {useTranslation} from "react-i18next";
import {getUserInfo} from "../../utils/userInfo";
import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission} from "../../constants";
import JobOfferDetail from "../jobOfferDetail";

const StudentInternshipListCard = ({
                                       internship,
                                       applicationId,
                                       applied,
                                       setPdfModalHelper,
                                       fetchApplications,
                                       selectedCvId
                                   }) => {
    const {t} = useTranslation();
    const [errorMessage, setErrorMessage] = useState("");
    const [companyDetail, setCompanyDetail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () => setIsModalOpen(true);
    const closeModal = () => {
        setIsModalOpen(false);
        setCompanyDetail(null)
    };


    const handleConsultCompanyDetail = (intership) => {
        setCompanyDetail(intership.employeurDTO)
        openModal()
    }

    const applyToInternship = async (internshipId) => {
        if (!selectedCvId) {
            alert(t('Please select a CV first.'));
            return;
        }
        const token = getUserInfo().token;
        try {
            const res = await fetch('http://localhost:8080/student/jobOffers/apply', {
                method: 'POST',
                headers: {
                    'Authorization': token,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({jobOffer: internshipId, cv: selectedCvId})
            });
            if (res.ok) {
                setErrorMessage(t('Your CV has been successfully sent to the employer.'));
                await fetchApplications();
            } else if (res.status === 409) { // Conflict
                setErrorMessage(t('You have already sent your CV to this internship offer.'));
            } else {
                const errorText = await res.text();
                throw new Error(errorText);
            }
        } catch (error) {
            setErrorMessage(t('Failed to apply: ') + error.message);
        }
    };
    const previewPdf = async (fileName) => {
        const token = getUserInfo().token;
        try {
            const res = await fetch(`http://localhost:8080/download/file/${fileName}`, {
                headers: {'Authorization': token}
            });
            if (res.ok) {
                const data = await res.json();
                const byteCharacters = atob(data.value);
                const byteNumbers = new Array(byteCharacters.length);
                for (let i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                const byteArray = new Uint8Array(byteNumbers);
                const blob = new Blob([byteArray], {type: 'application/pdf'});
                const pdfUrl = window.URL.createObjectURL(blob);
                setPdfModalHelper(pdfUrl, fileName)
            } else {
                throw new Error(t('Unable to preview PDF'));
            }
        } catch (error) {
            setErrorMessage(t('Unable to preview PDF: ') + error.message);
        }
    };


    const cancelJobOfferApplication = async (applicationId) => {
        if (!applicationId) {
            setErrorMessage(t('Invalid application ID.'));
            return;
        }
        console.log('Cancelling application ID:', applicationId, 'Type:', typeof applicationId);

        const token = getUserInfo().token;
        try {
            const res = await fetch(`http://localhost:8080/student/applications/cancel/${applicationId}`, {
                method: 'POST',
                headers: {
                    'Authorization': token,
                    'Content-Type': 'application/json',
                },
            });
            if (res.ok) {
                setErrorMessage(t('Your application has been successfully canceled.'));
                await fetchApplications();
            } else {
                const data = await res.json();
                throw new Error(data.message || 'Unable to cancel application.');
            }
        } catch (error) {
            setErrorMessage(t('Failed to cancel application: ') + error.message);
        }
    };

    return (
        <div key={internship.id}>
            <div className="card-offer-list-item">
                {internship.pdfDocu ? (
                    <div
                        className="flex flex-row justify-center items-center cursor-pointer hover:scale-110"
                        onClick={() => previewPdf(internship.pdfDocu.fileName)}>
                        <h3 className="profile-section-title">{internship.titre}</h3>
                        <FontAwesomeIcon icon={faFilePdf}
                                         className="profile-section-title ms-2 self-center"/>
                    </div>
                ) : <h3 className="profile-section-title">{internship.titre}</h3>}

                <div className='flex flex-row p-1 items-center'>
                    <p className='card-offer-list-item-lable'>{t('profile.companyName')}:</p>
                    <div className="flex flex-row justify-center items-center hover:scale-110 ">
                        <p className="cursor-pointer text-darkpurple"
                           onClick={() => handleConsultCompanyDetail(internship)}>{internship.employeurDTO.nomCompagnie}</p>
                        <FontAwesomeIcon icon={faInfoCircle}
                                         className="profile-section-title ms-2 "/>
                    </div>

                </div>
                <JobOfferDetail jobOffer={internship} setErrorMessage={setErrorMessage}/>


                {/*<div className='flex flex-row p-1'>*/}
                {/*    <p className='card-offer-list-item-lable'>{t('Start Date')}:</p>*/}
                {/*    <p>{internship.dateDebut}</p>*/}
                {/*</div>*/}
                {/*<div className='flex flex-row p-1'>*/}
                {/*    <p className='card-offer-list-item-lable'>{t('End Date')}:</p>*/}
                {/*    <p>{internship.dateFin}</p>*/}
                {/*</div>*/}
                {/*<div className='flex flex-row p-1'>*/}
                {/*    <p className='card-offer-list-item-lable'>{t('Location')}:</p>*/}
                {/*    <p>{internship.lieu}</p>*/}
                {/*</div>*/}
                {/*<div className='flex flex-row p-1'>*/}
                {/*    <p className='card-offer-list-item-lable'>{t('Work Type')}:</p>*/}
                {/*    <p>{t(internship.typeTravail)}</p>*/}
                {/*</div>*/}
                {/*<div className='flex flex-row p-1'>*/}
                {/*    <p className='card-offer-list-item-lable'>{t('Preview Offer')}:</p>*/}
                {/*    {internship.pdfDocu ? (*/}
                {/*        <button*/}
                {/*            className="button-in-card-neutral"*/}
                {/*            onClick={() => previewPdf(internship.pdfDocu.fileName)}>*/}
                {/*            {t('PreviewDesc')}*/}
                {/*        </button>*/}
                {/*    ) : (*/}
                {/*        t('No PDF available')*/}
                {/*    )}*/}
                {/*</div>*/}
                <div className='flex flex-row p-1'>
                    <p className='card-offer-list-item-lable'>{t('Status')}:</p>
                    {applied ? t('Applied') : t('Not Applied')}
                </div>
                <div className='flex flex-row p-1'>
                    <p className='card-offer-list-item-lable'>{t('Actions')}:</p>
                    {applied ? (
                        <button
                            className="button-in-card"
                            onClick={() => cancelJobOfferApplication(applicationId)}
                        >
                            {t('Cancel Application')}
                        </button>
                    ) : (
                        <button
                            className="button-in-card-neutral"
                            onClick={() => applyToInternship(internship.id)}
                        >
                            {t('Submit CV to Employer')}
                        </button>
                    )}
                </div>

            </div>
            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {companyDetail && <ProfilePreview permission={Permission.Limited} profile={companyDetail}/>}
                </Modal>
            }
        </div>
    )
}
export default StudentInternshipListCard