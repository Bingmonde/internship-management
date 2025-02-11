import {format, isAfter, isBefore} from "date-fns";
import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {StudentAPI} from "../../api/studentAPI";
import {useSession} from "../CurrentSession";
import PaginationComponent from "../pagination/paginationComponent";
import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission, welcomeRoute} from "../../constants";
import SuccessBox from "../successBox";
import {formatDate, formatWeekday, formatTime, isNotWelcomePage} from "../../api/utils";
import {useLocation, useOutletContext} from "react-router-dom";
import {UserGeneralAPI} from "../../api/userAPI";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFilePdf, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {previewPdf} from "../studentInternshipList/InternshipList";
import InterviewListStudentCard from "../InterviewListStudentCard";
import ErrorBox from "../errorBox";
import InfoBox from "../infoBox";


const InterviewListStudent = ({ refIds, notificationIds}) => {
    const [interviews, setInterviews] = useState([]);
    const [fetchInterviewsBool, setFetchInterviewsBool] = useState(true);
    // const [filteredInterviews, setFilteredInterviews] = useState([]);
    const [errorMessage, setErrorMessage] = useState('');
    const { t } = useTranslation();
    const [filterStartDate, setFilterStartDate] = useState('');
    const [filterEndDate, setFilterEndDate] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const {currentSession, setCurrentSession} = useSession();

    const [currentPageIndex, setCurrentPageIndex] = useState(0);
    const [totalPagesFromBackend, setTotalPagesFromBackend] = useState(0);
    const [resetPage, setResetPage] = useState(false);
    const [companyDetail, setCompanyDetail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => {setIsModalOpen(false); setCompanyDetail(null)};

    const location = useLocation();
    const currentRoute = location.pathname;
    const [setPdfModalHelper] = useOutletContext()
    const fetchInterviews = async () => {
        const data = await StudentAPI.getInterviews(currentPageIndex, filterStartDate, filterEndDate, filterStatus, currentSession);
        if(data){
            if (data.value != null){

                // set total pages from backend (to inform pagination component of numbers to show)
                setTotalPagesFromBackend(parseInt(data.totalPages, 10));
                console.log("total pages from backend: " + parseInt(data.totalPages, 10));
                // only show the data in the refIds list if it is provided
                if (refIds)
                    setInterviews(data.value.filter(interview => refIds.includes(interview.id)));
                else
                    setInterviews(data.value);
                // const sortedInterviews = StudentAPI.sortInterViewsByDate(res.value);
                // setInterviews(sortedInterviews)
                // if (filteredInterviews.length === 0)
                //     setFilteredInterviews(sortedInterviews)
                // setFetchInterviewsBool(false)

            }
            else {
                if (data["exception"].startsWith('No job interviews found')) {
                    setInterviews([]);
                    setFetchInterviewsBool(false)
                    // setFilteredInterviews([]);
                    return;
                }
                setInterviews([]);
                setErrorMessage(data["exception"])
            }
            setFetchInterviewsBool(false)
        }
        else{
            setErrorMessage(t('jobInterview.erros.fetchInterviews'))
        }
        setFetchInterviewsBool(false)
    }

    useEffect(() => {
        if (currentSession.id == null) return;
        if (!fetchInterviewsBool) return;

        fetchInterviews();
    }, [fetchInterviewsBool, currentSession]);

    useEffect(() => {
        setFetchInterviewsBool(true)
    }, [currentPageIndex]);

    useEffect(() => {
        if (!currentSession.id) return;
        setResetPage(true)
        setCurrentPageIndex(0)
        setFetchInterviewsBool(true)
    }, [filterStartDate, filterEndDate, filterStatus, currentSession]);

    useEffect(() => {
        if (!resetPage) return
        setResetPage(false)
    }, [resetPage]);

    // useEffect(() => {
    //     handleFilterAll();
    // }, [filterStartDate, filterEndDate, filterStatus, currentSession]);

    const convertToLocalDateTime = (date) => {
        return date +  'T23:59:59'
    }

    const clearCriteria = () => {
        setFilterStartDate('')
        setFilterEndDate('')
        setFilterStatus('')
        // setFilteredInterviews(interviews)
    }

    const handleConfirmInterview = async (interviewId) => {

        const res = await StudentAPI.confirmInterview(interviewId);
        if (res.value != null){
            // const filteredConfirmedInterview = filteredInterviews.filter(interview => interview.id !== interviewId)
            // setFilteredInterviews([...filteredConfirmedInterview, res.value])

            // update interviews
            setFetchInterviewsBool(true)
        }
        else
            setErrorMessage(res["exception"])

        // remove from notification
        // if (refIds){
        //     const index = refIds.indexOf(interviewId);
        //     UserGeneralAPI.markReadNotification([notificationIds[index]]);
        // }

    }
    const findCompany = (interview) => {
        return interview.jobOfferApplication.jobOffer.employeurDTO.nomCompagnie

    }

    const handlePreviewCompany = (interview) => {
        setCompanyDetail(interview.jobOfferApplication?.jobOffer.employeurDTO)
        openModal()
    }

    return (
        <>
            <div className="w-full">
                <h2 className="text-2xl font-bold text-center py-5">{t('jobInterview.interviewList')}</h2>
                {errorMessage &&
                    <div className="flex flex-col">
                        <div
                            className="ms-2 border text-red text-center px-4 py-3 rounded relative">
                        </div>
                        <button onClick={() => setErrorMessage('')}
                                className="bg-red text-white font-bold py-2 px-4 rounded hover:bg-red-600 mt-2">{t('return')}</button>
                    </div>
                }
                {!errorMessage &&
                    <div>
                        { isNotWelcomePage(currentRoute) &&
                            <div className="flex flex-col justify-center lg:flex-row bg-gray-200">
                                <div className="flex my-2 lg:w-auto w-full lg:justify-start justify-center">
                                    <input className="ms-2 px-4" type="date" value={filterStartDate}
                                           onChange={(e) => {
                                               setFilterStartDate(e.target.value)
                                           }}/>
                                    <p className="px-4">-</p>
                                    <input className="px-4" type="date" value={filterEndDate}
                                           onChange={(e) => {
                                               setFilterEndDate(e.target.value)
                                           }}/>
                                </div>
                                <div className="flex me-3 mx-1 my-2 ">
                                    <p className="px-4 w-1/2 ">{t('jobInterview.filterByConfirmationStatus')}: </p>
                                    <select onChange={(e) => {
                                        setFilterStatus(e.target.value)
                                    }}
                                            value={filterStatus}
                                    >
                                        <option value="" disabled={filterStatus !== ""}>---</option>
                                        <option value="valide">{t('jobInterview.valid')}</option>
                                        <option value="confirmed">{t('jobInterview.confirmed')}</option>
                                        <option value="nonConfirmed">{t('jobInterview.nonConfirmed')}</option>
                                        <option value="cancelled">{t('jobInterview.cancelled')}</option>
                                    </select>
                                </div>
                                <div className="flex mx-1 my-2 lg:justify-start justify-center">
                                    <button onClick={clearCriteria}
                                            className="bg-darkpurple w-40 text-white font-bold py-2 text-center rounded hover:bg-success-hover ml-2">{t('jobInterview.clearCriterea')}</button>
                                </div>
                            </div>
                        }

                        <div className={interviews.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"}>
                            {interviews.map(interview => (
                                <InterviewListStudentCard
                                    interview={interview}
                                    t={t}
                                    handleConfirmInterview={handleConfirmInterview}
                                    handlePreviewCompany={handlePreviewCompany}
                                    setPdfModalHelper={setPdfModalHelper}
                                    findCompany={findCompany}
                                />
                            ))}
                        </div>

                        {interviews.length === 0 &&
                            <InfoBox msg={'jobInterview.noInterviews'}/>}
                        {/*pagination widget*/}
                        <div className="flex justify-center mt-2">
                            <PaginationComponent resetCurrentPage={resetPage} totalPages={totalPagesFromBackend}
                                                 paginate={(pageNumber) => {
                                                     setCurrentPageIndex(pageNumber - 1); // page index starts at zero in backend, not the same as page "number"
                                                 }}/>
                        </div>
                    </div>}
            </div>

            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {companyDetail && <ProfilePreview permission={Permission.Limited} profile={companyDetail} />}
                </Modal>
            }
        </>
    )
}
export default InterviewListStudent;