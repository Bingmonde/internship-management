import React, {useEffect, useState} from "react";
import {getUserInfo} from "../utils/userInfo";
import {useTranslation} from "react-i18next";
import Modal from "./modal";
import ProfilePreview from "./userProfile/profilePreview";
import {Permission} from "../constants";
import PendingInternshipsStudentCard from "./PendingInternshipsStudentCard";

const PendingInternshipsStudentSingleNotification = ({ refIds, notificationIds, setPdfModalHelper }) => {

    const [pendingInternship, setPendingInternship] = useState(null);
    const [errorMessage, setErrorMessage] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const [companyDetail, setCompanyDetail] = useState(null);
    const closeModal = () => {setIsModalOpen(false); setCompanyDetail(null)};
    const { t } = useTranslation();

    useEffect(() => {
        fetchInternship()
    }, []);

    const fetchInternship = async () => {
        const token = getUserInfo().token;
        try {
            const res = await fetch('http://localhost:8080/internship/' + refIds, {
                headers: { 'Authorization': token },
            });
            console.log("Response status:", res.status);

            const data = await res.json();
            if (res.ok) {
                console.log("Fetched data:", data);
                setPendingInternship(data.value);
            } else {
                console.error("Fetch failed:", data);
            }
        } catch (error) {
            console.error("Fetch error:", error.message);
            setErrorMessage(t('Unable to fetch internships: ') + error.message);
        }
    };

    const confirmInternship = async (id) => {
        const token = getUserInfo().token;
        try {
            const res = await fetch(`http://localhost:8080/student/internshipOffers/${id}/confirmation?status=accepted`, {
                method: 'PUT',
                headers: {
                    'Authorization': token,
                    'Content-Type': 'application/json',
                }
            });

            const data = await res.json();
            if (res.ok) {
                console.log('confirmed internship:', data.value);
                fetchInternship()
            } else {
                console.log(data.exception)
            }
        } catch (error) {
            setErrorMessage(t('Unable to confirm internship: ') + error.message);
        }
    };

    const handlePreviewCompany = (company) => {
        setCompanyDetail(company);
        openModal()
    }

    const calculateCountDownDays = (expirationDate) => {
        const currentDate = new Date();
        const expirationDateObj = new Date(expirationDate);
        const diffTime = expirationDateObj - currentDate;
        return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    }

    if(pendingInternship === null) return <span>Loading...</span>


    return (
        <div>
            <PendingInternshipsStudentCard
                pendingInternship={pendingInternship}
                setPdfModalHelper={setPdfModalHelper}
                handlePreviewCompany={handlePreviewCompany}
                confirmInternship={confirmInternship}
                calculateCountDownDays={calculateCountDownDays}
            />
            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {companyDetail && <ProfilePreview permission={Permission.Full} profile={companyDetail} />}
                </Modal>
            }
        </div>
    )

}

export default PendingInternshipsStudentSingleNotification;