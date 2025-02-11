import React, {useEffect, useState} from "react";
import { format } from 'date-fns';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faInfo, faInfoCircle, faLink, faSort, faSortDown, faSortUp} from "@fortawesome/free-solid-svg-icons";
import {useNavigate, useOutletContext} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {getUserInfo} from "../utils/userInfo";
import {getStudentName} from "../api/utils";
import Modal from "./modal";
import ProfilePreview from "./userProfile/profilePreview";
import {Permission} from "../constants";
import {UserGeneralAPI} from "../api/userAPI";
const ListCV = ({refIds, notificationIds}) => {
    const [setPdfModalHelper] = useOutletContext()

    const [cvs, setCvs] = useState([]);
    const [statusSort, setStatusSort] = useState(1);
    const [dateSort, setDateSort] = useState(1);
    const [currentSort, setCurrentSort] = useState('date');
    const [disciplines, setDisciplines] = useState([]);
    const [selectedDiscipline, setSelectedDiscipline] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [studentDetail, setStudentDetail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => {setIsModalOpen(false); setStudentDetail(null)};

    const navigate = useNavigate();
    const { t } = useTranslation();
    const lang = getUserInfo().lang;

    // get CVs from database
    useEffect(() => {
        console.log(getUserInfo().token)
        fetchCVsWaitingForApproval()
    }, []);

    useEffect(() => {
        const getListDisciplines = async () => {
            const res = await fetch('http://localhost:8080/disciplines');
            const data = await res.json();
            console.log(data);
            setDisciplines(data);
        };
        getListDisciplines();
    }, []);

    // get CVs waiting for approval
    const fetchCVsWaitingForApproval = async () => {
        try {
            const res = await fetch('http://localhost:8080/intershipmanager/validateCV/waitingforapproval', {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });
            if (res.ok) {
                const data = await res.json();
                console.log(data.value);
                // var date = new Date(data.value[0].dateHeureAjout);
                // console.log(format(date, 'yyyy-MM-dd'));
                const sortedCVs = sortCVsByDate(data.value);
            }
            else {
                var err = await res.json()
                console.log(err)
                console.log(err["exception"])
                // TODO create a modal to show the error
                setErrorMessage(t('validationCV.' + err["exception"]))
            }


        } catch (err) {
            console.log(err)
        }
    };

    // get CVs by discipline
    const fetchCVsByDiscipline = async (e) => {
        if (!e.target.value) {
            setCvs([]);
            setSelectedDiscipline(e.target.value)
            await fetchCVsWaitingForApproval()
            return;
        }
        const selectedDiscipline = disciplines.find(
            d => d[getUserInfo().lang] === e.target.value
        );
        console.log(selectedDiscipline);

        try {
            const res = await fetch('http://localhost:8080/intershipmanager/validateCV/discipline/' + selectedDiscipline.id, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });
            if (res.ok) {
                const data = await res.json();
                console.log(data);
                handleDefaultSort(data.value);
            }
            else {
                var err = await res.json()
                console.log(err)
                console.log(err["exception"])
                // TODO create a modal to show the error
                setErrorMessage(t('validationCV.' + err["exception"]))
                setSelectedDiscipline('')

            }
        } catch (err) {
            console.log(err)
        }
    };

    const handleValidation = async (cvId, status) => {
        try {
            const res = await fetch(`http://localhost:8080/intershipmanager/validateCV/${cvId}?approvalResult=${status ? 'validated' : 'rejected'}` , {
                method: 'PUT',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });
            if (res.ok) {
                const data = await res.json();
                console.log(data.value);
                console.log('CV validated');
                // remove from notification
                if (refIds) {
                    const notificationIndex = refIds.indexOf(data.value);
                    UserGeneralAPI.markReadNotification([notificationIds[notificationIndex]]);
                }

                cvs.filter(cv => cv.id === cvId)[0].status = data.value.status;
                setCvs([...cvs]);
            }
            else {
                var err = await res.json()
                console.log(err)
                console.log(err["exception"])
                // TODO create a modal to show the error
                setErrorMessage(t(err["exception"]))
            }
        } catch (err) {
            console.log(err)
        }
    }

    const sortCVsByStatus = () => {
        const sortedCVs = [...cvs].sort((a, b) => {
            if (a.status < b.status) {
                return -statusSort;
            }
            if (a.status > b.status) {
                return statusSort;
            }
            return 0;
        });
        setCvs(sortedCVs);
        console.log('sorted by status', cvs);
    }

    const sortCVsByDate = (cvsToSort) => {
        const sortedCVs = [...cvsToSort]?.sort((a, b) => {
            if (a.dateHeureAjout < b.dateHeureAjout) {
                return -dateSort;
            }
            if (a.dateHeureAjout > b.dateHeureAjout) {
                return dateSort;
            }
            return 0;
        });
        setCvs(sortedCVs);
        console.log('sorted by date', cvs);
    }

    const handleToggleSortbyStatus = () => {
        setCurrentSort('status');
        setStatusSort(-statusSort);
        sortCVsByStatus();
    }
    const handleToggleSortbyDate = () => {
        setCurrentSort('date');
        setDateSort(-dateSort);
        sortCVsByDate(cvs)
    }


    const handleDefaultSort = (listCV) => {
        // sort by date
        sortCVsByDate(listCV);
        setCurrentSort('date');
    }

    const downloadCV = async (filename) => {
        try {
            const res = await fetch(`http://localhost:8080/download/file/${filename}`, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });
            if (res.ok) {
                const jsonResponse = await res.json();
                const base64String = jsonResponse.value;

                // Decode the Base64 string
                const byteCharacters = atob(base64String);
                const byteNumbers = new Array(byteCharacters.length);
                for (let i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                const byteArray = new Uint8Array(byteNumbers);

                const blob = new Blob([byteArray], { type: 'application/pdf' });
                const url = window.URL.createObjectURL(blob);
                setPdfModalHelper(url,filename)
                // link.download = filename;
                // document.body.appendChild(link);
                // link.click();
                // link.remove()
            }
            else {
                var err = await res.json()
                console.log(err)
                console.log(err["exception"])
                // TODO create a modal to show the error
                setErrorMessage(t(err["exception"]))
            }

        } catch (err) {
            console.log(err)
        }
    }

    const handlePreviewStudent = (student) => {
        setStudentDetail(student)
        openModal()

    }

    // render CVs in the table
    return (
        <div className="w-full">
            <h2 className="text-2xl font-bold text-center py-5">{t('validationCV.cvList')}</h2>
            {!refIds && (
                <>

                    <div className="flex flex-col justify-center md:flex-row bg-gray-200">
                        <div className="flex m-2 md:w-1/2 w-full md:justify-start justify-center">
                            <p className="px-2 self-center">{t('validationCV.byDiscipline')}: </p>
                            <select id="disciplines" onChange={(e) => {
                                fetchCVsByDiscipline(e)
                                setErrorMessage('')
                            }}
                                    className="w-1/2 md:w-1/2 rounded p-2">
                                <option value={''}>
                                    ------
                                </option>
                                {disciplines?.map(discipline => (
                                    <option key={discipline.id} value={discipline[getUserInfo().lang]}>
                                        {discipline[getUserInfo().lang]}
                                    </option>

                                ))}
                            </select>
                        </div>
                        <div className="flex m-2 md:w-1/2 w-full md:justify-end justify-center">
                            <p className="self-center">{t('validationCV.sortBy')}: </p>
                            <button
                                className={`mx-2 px-2 my-1 py-2 md:w-1/5 w-1/3 rounded  ${currentSort == 'status' ? 'bg-success' : 'bg-lightpurple'}`}
                                onClick={handleToggleSortbyStatus}>{t('validationCV.status')}
                            </button>

                            <button
                                className={`mx-2 px-2 my-1 py-2 md:w-1/5 w-1/3 rounded ${currentSort == 'date' ? 'bg-success' : 'bg-lightpurple'}`}
                                onClick={handleToggleSortbyDate}>Date
                            </button>
                        </div>
                    </div>
                    {errorMessage &&
                        <div className="flex flex-col">
                            <div
                                className="border text-red text-center px-4 py-3 rounded relative">{errorMessage}
                            </div>
                            {/*<button onClick={() => setErrorMessage('')} className="bg-red text-white font-bold py-2 px-4 rounded hover:bg-red-600 mt-2 w-1/2 self-center">{t('return')}</button>*/}
                        </div>
                    }
                </>
            )}
            {!errorMessage &&
                <div>
                    <div className={cvs.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"}>
                        {cvs.map(cv => (
                            <div key={cv.id} className="card-offer-list-item">
                                <div className="flex flex-row justify-center items-center hover:scale-110 cursor-pointer"
                                     onClick={() => handlePreviewStudent(cv.studentDTO)}>
                                    <h3 className="profile-section-title">
                                        {getStudentName(cv.studentDTO)}
                                    </h3>
                                    <FontAwesomeIcon icon={faInfoCircle} className="profile-section-title ms-2 self-center"/>
                                </div>
                                <div className='flex flex-row p-1'>
                                    <p className='card-offer-list-item-lable'>{t('profile.discipline')}:</p>
                                    <p className="text-darkpurple">{cv.studentDTO.discipline[lang]}</p>
                                </div>
                                <div className='flex flex-row p-1'>
                                    <p className='card-offer-list-item-lable'>{t('validationCV.status')}:</p>
                                    <p className={cv.status == "validated" ? "text-darkpurple" : "text-red"}>{t("validationCV.approvalStatus." + cv.status)}</p>
                                </div>
                                <div className='flex flex-row p-1'>
                                    <p className='card-offer-list-item-lable'>{t('validationCV.uploadedOn')}:</p>
                                    <p className="text-darkpurple">{cv.dateHeureAjout && format(new Date(cv.dateHeureAjout), 'yyyy-MM-dd hh:mm a')}</p>
                                </div>
                                <div className='flex flex-row p-1'>
                                    <p className='card-offer-list-item-lable'>{t('validationCV.cvFiles')}:</p>
                                    <p className="text-darkpurple"><a href="#" target="_blank"
                                                                      className="text-blue-600 text-xs hover:underline"
                                                                      onClick={(e) => {
                                                                          e.preventDefault()
                                                                          downloadCV(cv.pdfDocu.fileName)
                                                                      }}>
                                        {cv.pdfDocu.fileName}
                                    </a></p>
                                </div>
                                <div className='flex flex-row p-1 justify-center'>
                                    {cv.status === 'waiting' &&
                                        <button
                                            onClick={() => handleValidation(cv.id, true)}
                                            className="btn-confirm p-2 md:w-1/4 w-full ml-2"
                                        >
                                            {t('approveActions.' + 'approve')}
                                        </button>}
                                    {cv.status === 'waiting' &&
                                        <button
                                            onClick={() => handleValidation(cv.id, false)}
                                            className="btn-cancel p-2 md:w-1/4 w-full ml-2"
                                        >
                                            {t('approveActions.' + 'reject')}
                                        </button>}

                                </div>

                            </div>
                        ))}
                    </div>

                </div>}
            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {studentDetail && <ProfilePreview permission={Permission.Full} profile={studentDetail}/>}
                </Modal>
            }
        </div>
    );
};

export default ListCV;