import {format, isAfter, isBefore} from "date-fns";
import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {EmployeurAPI} from "../../api/employerAPI";
import {useSession} from "../CurrentSession";
import PaginationComponent from "../pagination/paginationComponent";
import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission} from "../../constants";
import {formatDate, formatWeekday, formatTime} from "../../api/utils";
import {UserGeneralAPI} from "../../api/userAPI";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFilePdf, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {useOutletContext} from "react-router-dom";
import Interview from "./interview";

const InterviewList = ({ refIds, notificationIds}) => {
    const [interviews, setInterviews] = useState([]);
    const [fetchInterviewsBoolean, setFetchInterviewsBoolean] = useState(true);
    // const [filteredInterviews, setFilteredInterviews] = useState([]);
    const [errorMessage, setErrorMessage] = useState('');
    const { t } = useTranslation();
    const [filterStartDate, setFilterStartDate] = useState('');
    const [filterEndDate, setFilterEndDate] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const { currentSession, setCurrentSession } = useSession();

    const [currentPageIndex, setCurrentPageIndex] = useState(0);
    const [totalPagesFromBackend, setTotalPagesFromBackend] = useState(0);
    const [resetPage, setResetPage] = useState(false);
    // const [candidateDetail, setCandidateDetail] = useState(null);
    // const [isModalOpen, setIsModalOpen] = useState(false);
    // const [setPdfModalHelper] = useOutletContext()
    // const openModal = () =>  setIsModalOpen(true);
    // const closeModal = () => {setIsModalOpen(false); setCandidateDetail(null)};



    const fetchInterviews = async () => {
        const data = await EmployeurAPI.getInterviews(currentPageIndex, filterStartDate, filterEndDate, filterStatus, currentSession);
        if(data){
            if (data.value != null){

                // set total pages from backend (to inform pagination component of numbers to show)
                setTotalPagesFromBackend(parseInt(data.totalPages, 10));
                console.log("total pages from backend: " + parseInt(data.totalPages, 10));
                console.log("interviews from backend: ", data.value);

                // show only the interviews in the refIds list if it is provided
                if (refIds) {
                    setInterviews(data.value.filter(interview => refIds.includes(interview.id)));
                    // remove from notification
                    await UserGeneralAPI.markReadNotification(notificationIds)
                }

                else
                    setInterviews(data.value);
                setFetchInterviewsBoolean(false)

            }
            else {
                setErrorMessage(data["exception"])
                setFetchInterviewsBoolean(false)
            }

        }
        else {
            setErrorMessage(t('jobInterview.errors.fetchInterviews'))
            setFetchInterviewsBoolean(false)
        }

    }

    const changeSession = async () => {
        await fetchInterviews();
    }

    useEffect(() => {
        if (!currentSession.id) return;
        if (!fetchInterviewsBoolean) return;
        fetchInterviews();
    }, [fetchInterviewsBoolean, currentSession]);

    useEffect(() => {
        setFetchInterviewsBoolean(true)
    }, [currentPageIndex]);

    useEffect(() => {
        if (!currentSession.id) return;
        setResetPage(true)
        setCurrentPageIndex(0)
        setFetchInterviewsBoolean(true)
    }, [filterStartDate, filterEndDate, filterStatus, currentSession]);

    useEffect(() => {
        if (!resetPage) return
        setResetPage(false)
    }, [resetPage]);


    // const findInterviewee = (interview) => {
    //     const fileInfo =  interview.jobOfferApplication.CV.pdfDocu.fileName.split('_')
    //     return fileInfo[1] + ' ' + fileInfo[2]
    // }

    const convertToLocalDateTime = (date) => {
        console.log('convert date', date)
        return date +  'T23:59:59'
    }

    const handleCancelInterview = async (interviewId) => {
        // console.log('interviewId', interviewId)
        const res = await EmployeurAPI.cancelInterveiw(interviewId);
        if (res.value != null){
            // update interviews
            setFetchInterviewsBoolean(true)
        }
        else
            setErrorMessage(res["exception"])
    }

    const clearCriterea = () => {
        setFilterStartDate('')
        setFilterEndDate('')
        setFilterStatus('')
        setResetPage(true)
        setCurrentPageIndex(0)
        // setFilteredInterviews(interviews)
    }

    // const handlePreviewCandidate = (interview) => {
    //     setCandidateDetail(interview.jobOfferApplication?.CV.studentDTO)
    //     openModal()
    // }

    return (
        <>
            <div className="w-full">
                <h2 className="text-2xl font-bold text-center py-5">{t('jobInterview.interviewList')}</h2>
                {errorMessage &&
                    <div className="flex flex-col">
                        <div
                            className="ms-2 border text-red text-center px-4 py-3 rounded relative">{errorMessage}
                        </div>
                        <button onClick={() => setErrorMessage('')}
                                className="bg-red text-white font-bold py-2 px-4 rounded hover:bg-red-600 mt-2">{t('return')}</button>
                    </div>
                }
                {!errorMessage &&
                    <div>

                        {!refIds &&
                            <div className="flex flex-col justify-center lg:flex-row bg-gray-200">
                                <div className="flex my-2 lg:w-auto w-full lg:justify-start justify-center">
                                    <input className="ms-2 px-4" type="date" value={filterStartDate}
                                           data-testid="start-date-picker"
                                           onChange={(e) => {
                                               setFilterStartDate(e.target.value)
                                           }}/>
                                    <p className="px-4 self-center">-</p>
                                    <input className="px-4" type="date" value={filterEndDate}
                                           data-testid="end-date-picker"
                                           onChange={(e) => {
                                               setFilterEndDate(e.target.value)
                                           }}/>
                                </div>
                                <div className="flex me-3 mx-1 my-2 ">
                                    <p className="px-4 w-1/2 self-center">{t('jobInterview.filterByConfirmationStatus')}: </p>
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
                                    <button onClick={clearCriterea}
                                            className="bg-darkpurple w-40 text-white font-bold py-2 text-center rounded hover:bg-success-hover ml-2">{t('jobInterview.clearCriterea')}</button>
                                </div>
                            </div>
                        }


                        <div className={interviews.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"}>
                            {interviews.map(interview => (
                                <Interview key={interview.id}
                                           interview={interview} handleCancelInterview={handleCancelInterview}
                                           fullPreview={true}
                                           />
                            ))}
                        </div>


                        {interviews.length === 0 &&
                            <div className="text-center m-2 text-red">{t('jobInterview.noInterviews')}</div>}
                        {/*pagination widget*/}
                        <div className="flex justify-center mt-2">
                            <PaginationComponent resetCurrentPage={resetPage} totalPages={totalPagesFromBackend}
                                                 paginate={(pageNumber) => {
                                                     setCurrentPageIndex(pageNumber - 1); // page index starts at zero in backend, not the same as page "number"
                            }}/>
                        </div>
                    </div>}
            </div>


        </>
    )
}
export default InterviewList



