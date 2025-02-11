import {useTranslation} from "react-i18next";
import React, {useEffect, useState} from "react";
import {getUserInfo} from "../../utils/userInfo";
import SearchComponent from "../search/searchComponent";
import PaginationComponent from "../pagination/paginationComponent";

import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission} from "../../constants";
import {
    faBriefcase, faClock,
    faFileUpload,
    faLocation,
    faLocationPin,
    faMapLocationDot, faTimes, faTimesCircle
} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faGraduationCap} from "@fortawesome/free-solid-svg-icons/faGraduationCap";

import {previewPdf} from "../../api/utils";
import {useOutletContext} from "react-router-dom";
import {useSession} from "../CurrentSession";
import InfoBox from "../infoBox";


const Reports = () => {
    const [setPdfModalHelper] = useOutletContext()

    const { currentSession, setCurrentSession } = useSession();

    const {t, i18n} = useTranslation();
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const [selectedReportType, setSelectedReportType] = useState('nonValidatedOffers');
    const [searchQuery, setSearchQuery] = useState("");

    const [fetchedReportData, setFetchedReportData] = useState([]);
    const [lastFetchedReportDataType, setLastFetchedReportDataType] = useState(null);

    const lang = getUserInfo().lang;

    const ReportDataTypes = {
        OFFER: 'offer',
        STUDENT: 'student'
    };

    const fetchedReportDataTypes = {
        nonValidatedOffers: ReportDataTypes.OFFER,
        validatedOffers: ReportDataTypes.OFFER,
        signedUpStudents: ReportDataTypes.STUDENT,
        studentsNoCV: ReportDataTypes.STUDENT,
        studentsCVNotValidated: ReportDataTypes.STUDENT,
        studentsNoInterview: ReportDataTypes.STUDENT,
        studentsAwaitingInterview: ReportDataTypes.STUDENT,
        studentsAwaitingInterviewResponse: ReportDataTypes.STUDENT,
        studentsWhoFoundInternship: ReportDataTypes.STUDENT,
        studentsNotEvaluatedBySupervisor: ReportDataTypes.STUDENT,
        studentsSupervisorHasntEvaluatedEnterprise: ReportDataTypes.STUDENT
    };

    const [currentPageIndex, setCurrentPageIndex] = useState(0);
    const [totalPagesFromBackend, setTotalPagesFromBackend] = useState(0);

    const [studentDetails, setStudentDetails] = useState(null);
    const [companyDetails, setCompanyDetails] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () => setIsModalOpen(true);
    const closeModal = () => {
        setIsModalOpen(false);
        setStudentDetails(null);
        setCompanyDetails(null);
    };

    useEffect(() => {
        console.log("selected: " + selectedReportType);
        fetchReportDataForSelectedType();
    }, [selectedReportType]);

    useEffect(() => {
        console.log("updated search query: " + searchQuery);
        fetchReportDataForSelectedType();
    }, [searchQuery]);

    useEffect(() => {
        console.log("changed current page index: " + currentPageIndex);
        fetchReportDataForSelectedType();
    }, [currentPageIndex]);

    useEffect(() => {
        console.log("current session: " + currentSession.id);
        fetchReportDataForSelectedType();
    }, [currentSession]);

    useEffect(() => {
        if (errorMessage) {
            const timer = setTimeout(() => {
                setErrorMessage('');
                setSuccessMessage('');
            }, 5000);
            return () => clearTimeout(timer);
        }
    }, [errorMessage, successMessage]);


    const fetchReportDataForSelectedType = async () => {
        if (!currentSession.id) return;
        const token = getUserInfo().token;
        try {
            const res = await fetch(`http://localhost:8080/internshipManager/${selectedReportType}` +
                '?season=' + currentSession.season +
                '&year=' + currentSession.year +
                `&page=` + currentPageIndex +
                `&size=20` +
                `&q=${searchQuery}`
                , {
                    headers: {'Authorization': token}
                });
            // console.log(res);
            if (res.ok) {
                const data = await res.json();
                // console.log(data.value);
                // console.log(data);

                // set total pages from backend (to inform pagination component of numbers to show)
                setTotalPagesFromBackend(parseInt(data.totalPages, 10));
                console.log("total pages from backend: " + parseInt(data.totalPages, 10));

                setFetchedReportData(data.value);
                console.log("fetched report");
                console.log(data.value);

                // set data type so ui can display accordingly
                setLastFetchedReportDataType(fetchedReportDataTypes[selectedReportType]);
                // console.log("last fetched report data type: " + fetchedReportDataTypes[selectedReportType]);
            } else {
                const err = await res.json();
                console.log(err);
                console.log(err["exception"]);
                setErrorMessage(t('reports.errors.' + err["exception"]) || err["exception"]);
                setFetchedReportData([]);
            }
        } catch (err) {
            console.log(err);

            setErrorMessage(t('error.network'));
            setFetchedReportData([]);
        }
    }

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
                setPdfModalHelper(pdfUrl,fileName);
            } else {
                throw new Error(t('Unable to preview PDF'));
            }
        } catch (error) {
            setErrorMessage(t('Unable to preview PDF: ') + error.message);
        }
    };

    const handlePreviewStudent = (student) => {
        // console.log('student details for ', student);
        setStudentDetails(student);
        openModal()
    }

    const handlePreviewCompanyDetails = (internship) => {
        setCompanyDetails(internship.employeurDTO)
        openModal()
    }

    return (
        <div className="p-5 w-full">
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
            <div>
                <h2 className="mb-1 text-xl font-bold">{t('reports.title')}</h2>
                <div className="flex justify-center">
                    <select
                        className="border border-gray-300 rounded p-2 mb-2 w-1/2"
                        name="reportType"
                        id="reportType"
                        value={selectedReportType}
                        onChange={(e) => setSelectedReportType(e.target.value)}
                    >
                        <option value="nonValidatedOffers">{t('reports.types.nonValidatedOffers')}</option>
                        <option value="validatedOffers">{t('reports.types.validatedOffers')}</option>
                        <option value="signedUpStudents">{t('reports.types.signedUpStudents')}</option>
                        <option value="studentsNoCV">{t('reports.types.studentsNoCV')}</option>
                        <option value="studentsCVNotValidated">{t('reports.types.studentsCVNotValidated')}</option>
                        <option value="studentsNoInterview">{t('reports.types.studentsNoInterview')}</option>
                        <option
                            value="studentsAwaitingInterview">{t('reports.types.studentsAwaitingInterview')}</option>
                        <option
                            value="studentsAwaitingInterviewResponse">{t('reports.types.studentsAwaitingInterviewResponse')}</option>
                        <option
                            value="studentsWhoFoundInternship">{t('reports.types.studentsWhoFoundInternship')}</option>
                        <option
                            value="studentsNotEvaluatedBySupervisor">{t('reports.types.studentsNotEvaluatedBySupervisor')}</option>
                        <option
                            value="studentsSupervisorHasntEvaluatedEnterprise">{t('reports.types.studentsSupervisorHasntEvaluatedEnterprise')}</option>
                    </select>
                </div>
                <div className="flex justify-center">
                    <div className="w-full">
                        <SearchComponent placeholderText={t("reports.searchPlaceholder")}
                                         stoppedTyping={setSearchQuery}/>
                    </div>
                </div>

                {/*no data*/}
                {fetchedReportData.length === 0 && (
                    <InfoBox msg={t('reports.noData')}  />

            )}
                {/*data*/}
                <div className="report-list">
                    {fetchedReportData.map((report, index) => (
                        <div key={index} className="w-full">
                            {/*student card*/}
                            {lastFetchedReportDataType === ReportDataTypes.STUDENT &&
                                <div
                                    className="report-card text-center cursor-pointer"
                                    name={report.prenom + " " + report.nom}
                                    onClick={() => handlePreviewStudent(report)}>
                                    <>
                                        <FontAwesomeIcon icon={faGraduationCap} className="mr-2"/>
                                        <p className="text-xl text-center py-5 text-center font-bold">{report.prenom} {report.nom}</p>

                                        <p >{report.courriel}</p>

                                        <p >{t(report.discipline[lang])}</p>
                                    </>
                                </div>
                            }
                            {/*offer card*/}
                            {lastFetchedReportDataType === ReportDataTypes.OFFER &&
                                <div
                                    className="report-card"
                                    >
                                    <div>
                                        <span className="text-xl text-center py-5 text-center font-bold">{report.titre} </span>
                                        <button
                                            className="text-blue-500 underline"
                                            onClick={() => previewPdf(report.pdfDocu.fileName)}>
                                            {t('view')}
                                        </button>
                                        <br/>
                                        <button className="text-blue-500 underline mb-2"
                                                onClick={() => handlePreviewCompanyDetails(report)}>
                                            {t(report.employeurDTO.nomCompagnie)}
                                        </button>
                                        <br/>
                                        <FontAwesomeIcon icon={faMapLocationDot}
                                                         className="mr-2"/><span>{report.lieu}</span><br/>
                                        <FontAwesomeIcon icon={faClock} className="mr-2"/>
                                        <span
                                        >{report.dateDebut} {t('reports.to')} {report.dateFin}</span>
                                        <br/>
                                        <span className="rounded-2xl mt-2 bg-success p-1 text-darkpurple">{t(report.typeTravail)}</span>
                                    </div>
                                </div>
                            }
                        </div>
                    ))}
                </div>
                {/*paging widget*/}
                <div className="flex justify-center mt-2">
                    <PaginationComponent totalPages={totalPagesFromBackend} paginate={(pageNumber) => {
                        setCurrentPageIndex(pageNumber - 1); // page index starts at zero in backend, not the same as page "number"
                    }}/>
                </div>
            </div>
            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {studentDetails && <ProfilePreview permission={Permission.Full} profile={studentDetails}/>}
                    {companyDetails && <ProfilePreview permission={Permission.Full} profile={companyDetails} />}
                </Modal>
            }
        </div>
    );
}

export default Reports;