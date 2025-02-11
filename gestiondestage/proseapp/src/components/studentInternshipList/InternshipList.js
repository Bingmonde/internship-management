import React, {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import './InternshipList.css';
import {getUserInfo} from "../../utils/userInfo";
import {useSession} from "../CurrentSession";
import {useOutletContext} from "react-router-dom";
import PaginationComponent from "../pagination/paginationComponent";
import SearchComponent from "../search/searchComponent";
import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission} from "../../constants";
import {UserGeneralAPI} from "../../api/userAPI";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFilePdf, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import StudentInternshipListCard from "./studentInternshipListCard";
import ValidatedCvDropdown from "./validatedCvDropdown";
import InfoBox from "../infoBox";
import ErrorBox from "../errorBox";

const InternshipList = ({refIds, notificationIds}) => {

    const [setPdfModalHelper] = useOutletContext()

    const {t} = useTranslation();


    const [selectedCvId, setSelectedCvId] = useState(''); // Selected CV ID
    const [internships, setInternships] = useState([]);
    const [applications, setApplications] = useState([]); // List of applications
    const [errorMessage, setErrorMessage] = useState('');
    const [filter, setFilter] = useState(''); // search filter
    const [loading, setLoading] = useState(false); // Loading state
    const {currentSession, setCurrentSession} = useSession();
    const [getPage, setGetPage] = useState(false)

    const [currentPageIndex, setCurrentPageIndex] = useState(0);
    const [totalPagesFromBackend, setTotalPagesFromBackend] = useState(0);
    const [noValidCV, setNoValidCV] = useState(false);
    const [resetPage, setResetPage] = useState(false);

    const workTypeMapping = {
        1: t("remote"),
        2: t("hybrid"),
        3: t("office")
    };

    useEffect(() => {
        if (errorMessage) {
            const timer = setTimeout(() => {
                setErrorMessage('');
            }, 5000); // Clear error message after 5 seconds
            return () => clearTimeout(timer); // Clear timer on unmount
        }
    }, [errorMessage]);

    useEffect(() => {
        if (!(getPage && currentSession.id)) return;
        fetchInternships()
        fetchApplications();
    }, [getPage, currentSession]);

    useEffect(() => {
        if (getPage) return
        setGetPage(true)
    }, [currentPageIndex]);

    useEffect(() => {
        if (!currentSession.id) return
        setCurrentPageIndex(0)
        setResetPage(true)
        setGetPage(true)
    }, [currentSession, filter]);

    useEffect(() => {
        if (!resetPage) return
        setResetPage(false)
    }, [resetPage]);

    const fetchInternships = async () => {
        const token = getUserInfo().token;
        try {
            const res = await fetch('http://localhost:8080/student/jobOffers?' +
                'season=' + currentSession.season +
                "&year=" + currentSession.year +
                '&page=' + currentPageIndex +
                '&size=5' +
                '&q=' + filter
                , {
                    headers: {'Authorization': token}
                });
            const data = await res.json();
            if (res.ok) {

                // set total pages from backend (to inform pagination component of numbers to show)
                setTotalPagesFromBackend(parseInt(data.totalPages, 10));
                console.log(data.value);
                console.log("total pages from backend: " + parseInt(data.totalPages, 10));

                setInternships(data.value);
            } else {
                throw new Error(data.message);
            }
        } catch (error) {
            setErrorMessage(t('Unable to fetch internships: ') + error.message);
        }
        setGetPage(false)
    };

    const fetchApplications = async () => {
        const token = getUserInfo().token;
        try {
            setLoading(true); // Start loading
            const response = await fetch('http://localhost:8080/student/applications?' +
                'season=' + currentSession.season +
                "&year=" + currentSession.year, {
                headers: {'Authorization': token}
            });
            const data = await response.json();
            if (response.ok) {
                // console.log('Applications data:', data.value); // Add log
                setApplications(data.value);
            } else {
                throw new Error(data.message);
            }
        } catch (error) {
            setErrorMessage(t('Unable to fetch applications: ') + error.message);
        } finally {
            setLoading(false); // End loading
        }
        setGetPage(false)
    };

    // Helper function to get application ID for a given internship
    const getApplicationId = (internshipId) => {
        const app = applications.find(app => app.jobOffer.id === internshipId && app.active);
        return app ? app.id : null;
    };

    const isApplied = (internshipId) => {
        return applications.some(app => app.jobOffer.id === internshipId && app.active);
    };


    return (
        <div className="w-full">
            {errorMessage && !noValidCV && (
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
            {!refIds &&
                <ValidatedCvDropdown selectedCvId={selectedCvId} setSelectedCvId={setSelectedCvId}
                                     setNoValidatedCv={setNoValidCV}></ValidatedCvDropdown>
            }

            <div>
                <h2>{t('Internship List')}</h2>
                {noValidCV && <div className={"px-8"}><ErrorBox msg={t("noCv")}></ErrorBox></div>}
                {!noValidCV &&
                    <>
                        {!refIds &&
                            <SearchComponent placeholderText={t('Filter by title, location, work type, or salary')}
                                             stoppedTyping={
                                                 (searchTerm) => {
                                                     setFilter(searchTerm)
                                                     console.log("search term: " + searchTerm);
                                                 }
                                             }/>
                        }
                        {internships.length === 0 ? (
                            <InfoBox msg={t('There are currently no available internships.')}  />
                            ) : (
                            <>
                                <div
                                    className={internships.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"}>
                                    {internships.map((internship) => {
                                        const applied = isApplied(internship.id);
                                        const applicationId = getApplicationId(internship.id);
                                        return (
                                            <StudentInternshipListCard key={internship.id} internship={internship}
                                                                       applicationId={applicationId} applied={applied}
                                                                       setPdfModalHelper={setPdfModalHelper}
                                                                       fetchApplications={fetchApplications}
                                                                       selectedCvId={selectedCvId}></StudentInternshipListCard>
                                        )
                                    })}

                                </div>

                                {/*pagination widget*/}
                                <div className="flex justify-center mt-2">
                                    <PaginationComponent resetCurrentPage={resetPage} totalPages={totalPagesFromBackend}
                                                         paginate={(pageNumber) => {
                                                             setCurrentPageIndex(pageNumber - 1); // page index starts at zero in backend, not the same as page "number"
                                                         }}/>
                                </div>
                            </>
                        )}
                    </>
                }
            </div>

            {loading && (
                <div style={{textAlign: 'center', marginTop: '20px'}}>
                    {t('Loading...')}
                </div>
            )}

        </div>
    );
};
export default InternshipList;
