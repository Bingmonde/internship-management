// ListCandidates.js

import React, { useEffect, useState } from "react";
import { getUserInfo } from "../../utils/userInfo";
import { format, isValid } from 'date-fns';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faCheck, faChevronDown, faChevronRight,faHourglassHalf, faXmark} from "@fortawesome/free-solid-svg-icons";
import {useLocation, useNavigate, useOutletContext} from "react-router-dom";
import { useTranslation } from "react-i18next";
import { EmployeurAPI } from "../../api/employerAPI";
import {useSession} from "../CurrentSession";
import PaginationComponent from "../pagination/paginationComponent";
import SearchComponent from "../search/searchComponent";
import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission, welcomeRoute} from "../../constants";
import {formatDate, formatTime, formatWeekday, isNotWelcomePage} from "../../api/utils";
import {UserGeneralAPI} from "../../api/userAPI";
import Interview from "../interview/interview";
import JobInterviewForm from "../jobInterviewForm";
import {CandidateInterviewInfo} from "./candidateInterviewInfo";
import {CandidateInternshipOfferInfo} from "./candidateInternshipOfferInfo";
import {CandidateJobApplication} from "./candidateJobApplication";
import Switch from "./switch";
import ErrorBox from "../errorBox";
import JobOfferDetail from "../jobOfferDetail";
import OfferInfo from "./OfferInfo";

const ListCandidates = ({refIds, notificationIds}) => {

    const [myOffers, setMyOffers] = useState([]);
    const { currentSession, setCurrentSession } = useSession();
    // const [page, setPage] = useState([])
    const [resetPage, setResetPage] = useState(false);

    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const { t } = useTranslation();

    // filter applications by candidates status (all, interviewees or job offered)
    const [filterForCandidatesByJobId, setFilterForCandidatesByJobId] = useState({});

    // Stats for each job offer
    const [jobOffersStats, setJobOffersStats] = useState({});

    // State for the invitation modal
    const [selectedDays, setSelectedDays] = useState(7);
    const [invitingApplicationId, setInvitingApplicationId] = useState(null);

    // pagination
    const [filter, setFilter] = useState("");


    // Effect to fetch job offers on component mount
    useEffect(() => {
        if (!currentSession.id) return
        if (myOffers.length > 0) return
        fetchMyOffers();
        fetchJobOffersStats();
    }, [currentSession]);

    useEffect(() => {
        if (!resetPage) return
        setResetPage(false)
    }, [resetPage]);

    // useEffect(() => {
    //     if (!currentSession.id) return
    //     fetchMyOffers();
    // }, [filterForCandidatesByJobId]);


    const fetchJobOffersStats = async () => {
        try {
            const res = await EmployeurAPI.getJobOffersStats(currentSession);
            if (res.ok) {
                const data = await res.json();
                setJobOffersStats(data.value);
            }
            else {
                var err = await res.json();
                console.log(err);
                console.log(err["exception"]);
                setErrorMessage(t('validationCV.' + err["exception"]));
            }
        } catch (err) {
            console.log(err)
        }
    }


    const fetchMyOffers = async () => {
        const data = await EmployeurAPI.getJobOffersByEmployer(currentSession);
        if (data.value != null) {
            const offers =  data.value.map((offer, index) => {
                if (index === 0) {
                    return {
                        ...offer,
                        expanded: true,
                        fetched: false,
                        fullInfoApplications: [],
                        page: {pageNumber:0,pageSize: 0, totalPage: 0}
                    }
                }
                return {
                    ...offer,
                    expanded: false,
                    fetched: false,
                    fullInfoApplications: [],
                    page: {pageNumber:0,pageSize: 0, totalPage: 0}
                }
            })
            setMyOffers(offers)
            // fetch 1st page of applications
            if (offers.length > 0)
                await fetchFullApplicationsByOfferId(data.value[0].id, 0, 0)
        }
        else {
            const err = data.exception
            console.log(err);
            setErrorMessage(t('validationCV.' + err) || err);
        }
    };

    const fetchFullApplicationsByOfferId = async (offerId, offerIndex, pageNo) => {
        console.log('enter fetchFullApplicationsByOfferId', offerId, offerIndex, pageNo)

        // const filterForCandidatesByJobIdSentValue = filterForCandidatesByJobId[offerId] !== undefined
        //     ? filterForCandidatesByJobId[offerId]
        //     : "all";
        // console.log('filterForCandidatesByJobIdSentValue', filterForCandidatesByJobIdSentValue)
        const data = await EmployeurAPI.getFullJobApplications(offerId, currentSession, filter, pageNo, 10);
        if (data.value != null) {
            setMyOffers((prevOffers) =>
                prevOffers.map((offer, index) => {
                    if (index === offerIndex) {
                        console.log('inside fetch full applications', myOffers?.fullInfoApplications)
                        return {
                            ...offer,
                            fullInfoApplications: [...offer.fullInfoApplications, ...data.value],
                            fetched: true,
                            page: {
                                pageNumber: pageNo + 1,
                                pageSize: data.pageSize,
                                totalPage: data.totalPage
                            }
                        };
                    }
                    return offer;
                })
            );
        }
        else {
            const err = data.exception
            console.log(err);
            setErrorMessage(t(err) || err);
        }
    }

    const cancelInterview = async (interviewId) => {
        const data = await EmployeurAPI.cancelInterveiw(interviewId);
        if (data.value != null) {
            const updateMyOffers = myOffers.map((offer) => {
                return {
                    ...offer,
                    fullInfoApplications: offer.fullInfoApplications.map((application) => {
                        return {
                            ...application,
                            interviewOffer: application.interviewOffer.map((interview) => {
                                if (interview.id === interviewId) {
                                    return data.value
                                }
                                return interview
                            })
                        }
                    })
                }
            })
            setMyOffers(updateMyOffers)

        }
        else {
            const err = data.exception
            console.log(err);
            setErrorMessage(t(err) || err);
        }
    }


    const manageExpandedAndFetched = (offerIndex, toExpand, toFetch) => {
        setMyOffers((prevOffers) =>
            prevOffers.map((offer, index) => {
                if (index === offerIndex) {
                    return {
                        ...offer,
                        expanded: toExpand,
                        fetched: toFetch
                    };
                }
                return offer;
            })
        );
    }

    // Function to toggle display of candidates for an offer (manual toggle)
    const toggleShowOfferCandidates = async (offerIndex, offerId) => {
        if (myOffers[offerIndex].fetched)
            manageExpandedAndFetched(offerIndex, !myOffers[offerIndex].expanded, myOffers[offerIndex].fetched)

        // fetch data if not fetched
        else {
            const pageNo = myOffers[offerIndex].page.pageNumber
            const pageSize = myOffers[offerIndex].page.pageSize
            await fetchFullApplicationsByOfferId(offerId, offerIndex, pageNo)
            manageExpandedAndFetched(offerIndex, true, true)
        }
    };


    // Function to invite a candidate
    const inviteToInternship = async (jobApplicationId, offerExpireInDays) => {
        if (!offerExpireInDays || isNaN(offerExpireInDays) || offerExpireInDays < 1) {
            console.error("Invalid expiration days value");
            setErrorMessage(t('error.invalidExpireInDays'));
            return;
        }

        const data = await EmployeurAPI.inviteToInternship(jobApplicationId, offerExpireInDays);
        if (data.value != null) {
            const updatedMyOffers = myOffers.map((offer) => {
                return {
                    ...offer,
                    fullInfoApplications: offer.fullInfoApplications.map((application) => {
                        if (application.jobOfferApplication.id === jobApplicationId) {
                            return {
                                ...application,
                                internshipOffer: data.value
                            }
                        }
                        return application
                    })
                }
            })
            setMyOffers(updatedMyOffers)
        }
        else {
            const err = data.exception
            console.log(err);
            setErrorMessage(t(err) || err);
        }
    };

    const createInterview = async (jobInterview) => {
        const data = await EmployeurAPI.createJobInterview(jobInterview);
        if (data.value != null) {
            const updatedMyOffers = myOffers.map((offer) => {
                return {
                    ...offer,
                    fullInfoApplications: offer.fullInfoApplications.map((application) => {
                        if (application.jobOfferApplication.id === jobInterview.jobOfferApplicationId) {
                            return {
                                ...application,
                                interviewOffer: [...application.interviewOffer, data.value]
                            }
                        }
                        return application
                    })
                }
            })
            setMyOffers(updatedMyOffers)
        }
        else {
            const err = data.exception
            console.log(err);
            setErrorMessage(t(err) || err);
        }
    }

    // Function to open the invitation modal
    const handleInviteClick = (jobApplicationId) => {
        setInvitingApplicationId(jobApplicationId);
        setSelectedDays(7); //
    };

    // Function to confirm the invitation
    const confirmInvite = () => {
        if (invitingApplicationId !== null && selectedDays >= 1) {
            inviteToInternship(invitingApplicationId, selectedDays);
            setInvitingApplicationId(null);
            setSelectedDays(1);
        } else {
            setErrorMessage(t('error.selectDays'));
        }
    };

    // Effect to clear success message after 3 seconds
    useEffect(() => {
        if (successMessage) {
            const timer = setTimeout(() => {
                setSuccessMessage('');
            }, 3000);

            return () => clearTimeout(timer);
        }
    }, [successMessage]);



        return (
            <div
                className= {"card-offer-list-single-child"}>
                {!refIds && (
                    <>
                        <h2 className="text-2xl font-bold text-center py-5">{t('listCandidates.title')}</h2>
                        {/* Error Message */}
                        {errorMessage &&
                            <div className="flex flex-col">
                                <div className="border text-red-600 text-center px-4 py-3 rounded relative">{errorMessage}</div>
                                <button onClick={() => setErrorMessage('')}
                                        className="bg-red-600 text-red font-bold py-2 px-4 rounded hover:bg-red-700 mt-2">
                                    {t('close')}
                                </button>
                            </div>
                        }
                        {/*<ErrorBox msg={errorMessage} />*/}
                        {/* Success Message */}
                        {successMessage && (
                            <div className="flex flex-col">
                                <div className="border text-white text-center px-4 py-3 rounded relative"
                                     style={{ backgroundColor: '#1E555C' }}>
                                    {successMessage}
                                </div>
                            </div>

                        )}
                    </>
                )}

            {/* Main Content */}
            {!errorMessage &&
                <div>
                    {/* Filter */}
                    <div className="mb-2 w-full">
                        {/*<SearchComponent placeholderText={t('listCandidates.filterByName')} stoppedTyping={setFilter} />*/}

                    {/* No Offers Message */}
                    {myOffers.length === 0 &&
                        <div className="flex flex-col">
                            <div className="border text-red-600 text-center px-4 py-3 rounded relative">{t('listCandidates.noOffer')}</div>
                        </div>}
                    {/* List of Offers */}
                    {myOffers.length > 0 &&
                        <div className="flex flex-col">
                    {myOffers.map((offer, offerIndex) => {

                        return (
                            <OfferInfo refIds={refIds} offer={offer} offerIndex={offerIndex} jobOffersStats={jobOffersStats} key={offer.id}
                                       setErrorMessage={setErrorMessage}
                                       toggleShowOfferCandidates={toggleShowOfferCandidates}
                                       fetchFullApplicationsByOfferId={fetchFullApplicationsByOfferId}
                                       resetPage={resetPage} handleInviteClick={handleInviteClick}
                                       createInterview={createInterview} cancelInterview={cancelInterview} />

                        )
                    }
                    )}
                    </div>
                    }
                </div>
            </div>}

                {/* Invitation Modal */}
                {invitingApplicationId !== null && (
                    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
                        <div className="bg-white p-6 rounded shadow-lg w-80">
                            <h3 className="text-lg font-bold mb-4">{t('inviteInternship.title')}</h3>
                            <div className="mb-4">
                                <label className="block mb-2">{t('Due date')}</label>
                                <input
                                    type="number"
                                    min="1"
                                    value={selectedDays}
                                    onChange={(e) => setSelectedDays(Number(e.target.value))}
                                    className="border rounded px-4 py-2 w-full"
                                    placeholder={t('inviteInternship.enterDays')}
                                />
                            </div>
                            <div className="flex justify-end space-x-2">
                                <button
                                    onClick={() => {
                                        setInvitingApplicationId(null);
                                        setSelectedDays(7);
                                    }}
                                    className="text-white px-4 py-2 rounded hover:bg-red-600"
                                    style={{backgroundColor: '#f03e1f'}}
                                >
                                    {t('cancel')}
                                </button>
                                <button
                                    onClick={confirmInvite}
                                    className="text-white px-4 py-2 rounded hover:bg-darkgreen-600"
                                    style={{ backgroundColor: '#1E555C' }}
                                >
                                    {t('confirm')}
                                </button>
                            </div>
                        </div>
                    </div>
                )}



        </div>
    )};
export default ListCandidates;


