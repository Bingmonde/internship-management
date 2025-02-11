import React, {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import './studentInternshipList/InternshipList.css';
import PaginationComponent from "./pagination/paginationComponent";
import {getUserInfo} from "../utils/userInfo";
import {useLocation, useOutletContext} from "react-router-dom";
import {useSession} from "./CurrentSession";
import SearchComponent from "./search/searchComponent";
import Modal from "./modal";
import ProfilePreview from "./userProfile/profilePreview";
import {Permission} from "../constants";
import {formatDate, formatTime, formatWeekday, isNotWelcomePage} from "../api/utils";
import SuccessBoxNoTrans from "./successBoxNoTrans";
import {UserGeneralAPI} from "../api/userAPI";
import PendingInternshipsStudentCard from "./PendingInternshipsStudentCard";
import InfoBox from "./infoBox";

const PendingInternshipsStudent = ({refIds, notificationIds}) => {

    const [setPdfModalHelper] = useOutletContext()
    const { t } = useTranslation();

    const [pendingInternships, setPendingInternships] = useState([]);
    const [errorMessage, setErrorMessage] = useState('');
    const [filter, setFilter] = useState(''); // Filter state
    const [fetchShit, setFetchShit] = useState(true);
    const { currentSession, setCurrentSession } = useSession();

    const [currentPageIndex, setCurrentPageIndex] = useState(0);
    const [totalPagesFromBackend, setTotalPagesFromBackend] = useState(0);
    const [companyDetail, setCompanyDetail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => {setIsModalOpen(false); setCompanyDetail(null)};

    const location = useLocation();
    const currentRoute = location.pathname;

    useEffect(() => {
        if (!currentSession) return;
        if (!fetchShit) return;
        fetchInternshipOffersToConfirm();
    }, [fetchShit]);

    useEffect(() => {
        if (errorMessage) {
            const timer = setTimeout(() => {
                setErrorMessage('');
            }, 5000); // Clear error message after 5 seconds
            return () => clearTimeout(timer); // Clear timer on unmount
        }
    }, [errorMessage]);

    useEffect(() => {
        if (!currentSession) return;
        setFetchShit(true)
    },[currentSession, currentPageIndex, filter]);

    const fetchInternshipOffersToConfirm = async () => {
        const token = getUserInfo().token;
        try {
            // TODO: in future sprint, change endpoint path? this sounds a lot like /student/jobOffers but returns offers that are further in the application process. -Alexis
            const res = await fetch('http://localhost:8080/student/internshipOffers?' +
                'season='+currentSession.season+
                "&year="+currentSession.year+
                '&page='+currentPageIndex+
                '&size=5' + ((filter !== "") ? "&" + (new URLSearchParams({q:filter})).toString() : ""), {
                headers: { 'Authorization': token }
            });

            // console.log(res);

            const data = await res.json();

            if (res.ok) {
                console.log(data.value);
                // only show the data in the refIds list if it is provided
                if (refIds)
                    setPendingInternships(data.value.filter(internship => refIds.includes(internship.id)));
                else
                    setPendingInternships(data.value);

                // set total pages from backend (to inform pagination component of numbers to show)
                setTotalPagesFromBackend(parseInt(data.totalPages, 10));
                // console.log("total pages from backend: " + parseInt(data.totalPages, 10));

                // console.log('fetched internships to be confirmed by student:', data.value);
                setFetchShit(false);
            } else {
                if (res.status === 404) {
                    setPendingInternships([]);
                    setFetchShit(false);
                    return;
                }
                throw new Error(data.message);
            }
        } catch (error) {
            setErrorMessage(t('Unable to fetch internships to confirm: ') + error.message);
            setFetchShit(false);
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
                // setPendingInternships((prev) =>
                //     prev.map((offer) =>
                //         offer.id === internshipOfferDTO.id ? data.value : offer
                //     )
                // );
                // remove from notification
                if (refIds) {
                    const index = refIds.indexOf(id);
                    console.log('index:', index);
                    UserGeneralAPI.markReadNotification([notificationIds[index]]);
                }

                console.log('confirmed internship:', data.value);
                setFetchShit(true);

                //
            } else {
                throw new Error(data.exception);
            }
        } catch (error) {
            setErrorMessage(t('Unable to confirm internship: ') + error.message);
        }
    };

    const previewPdf = async (fileName) => {
        const token = getUserInfo().token;
        try {
            const res = await fetch(`http://localhost:8080/download/file/${fileName}`, {
                headers: { 'Authorization': token }
            });
            if (res.ok) {
                const data = await res.json();
                const byteCharacters = atob(data.value);
                const byteNumbers = new Array(byteCharacters.length);
                for (let i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                const byteArray = new Uint8Array(byteNumbers);
                const blob = new Blob([byteArray], { type: 'application/pdf' });
                const pdfUrl = window.URL.createObjectURL(blob);
                setPdfModalHelper(pdfUrl,fileName)
            } else {
                throw new Error(t('Unable to preview PDF'));
            }
        } catch (error) {
            setErrorMessage(t('pendingInternshipsStudent.cantConfirmInternship') + error.message);
        }
    };

    // const filteredInternships = pendingInternships.filter((pendingInternship) => {
    //     const internship = pendingInternship.jobOfferApplicationDTO.jobOffer;
    //     return (
    //         internship.titre.toLowerCase().includes(filter.toLowerCase()) ||
    //         internship.lieu.toLowerCase().includes(filter.toLowerCase()) ||
    //         internship.typeTravail.toLowerCase().includes(filter.toLowerCase())
    //     );
    // });

    function convertTimestamp(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleString('fr-CA', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        });
    }

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

    return (
        <div className="w-full">
            {errorMessage && (
                <div style={{
                    background: '#1E555C',
                    color: 'white',
                    textAlign: 'center',
                    padding: '10px',
                    borderRadius: '5px',
                    marginBottom: '20px'
                }}>
                    {errorMessage}
                </div>
            )}
                <h2>{t('pendingInternshipsStudent.title')}</h2>
                {/*<input*/}
                {/*    type="text"*/}
                {/*    placeholder={t('Filter by title, location, work type, or salary')}*/}
                {/*    value={filter}*/}
                {/*    onChange={(e) => setFilter(e.target.value)}*/}
                {/*    className="w-full p-2 mb-5 border border-gray-300 rounded"*/}
                {/*/>*/}
                { isNotWelcomePage(currentRoute) && <SearchComponent placeholderText={t('Filter by title, location, work type, or salary')} stoppedTyping={setFilter} />}

                {pendingInternships.length === 0 ? (
                    <InfoBox msg={t('pendingInternshipsStudent.noInternshipsToConfirm')} />
                ) : (
                    <>
                        <div className={pendingInternships.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"} >
                            {pendingInternships.map((pendingInternship) => (
                                <PendingInternshipsStudentCard
                                    pendingInternship={pendingInternship}
                                    setPdfModalHelper={setPdfModalHelper}
                                    handlePreviewCompany={handlePreviewCompany}
                                    confirmInternship={confirmInternship}
                                    calculateCountDownDays={calculateCountDownDays}
                                />
                            ))}

                        </div>


                        {/*pagination widget*/}
                <div className="flex justify-center mt-2">
                    <PaginationComponent totalPages={totalPagesFromBackend} paginate={(pageNumber) => {
                        setCurrentPageIndex(pageNumber - 1); // page index starts at zero in backend, not the same as page "number"
                            }}/>
                        </div>
                    </>
                )}
            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {companyDetail && <ProfilePreview permission={Permission.Full} profile={companyDetail} />}
                </Modal>
            }
        </div>
    );
};

export default PendingInternshipsStudent;