import React, { useEffect, useState } from "react";
import { useTranslation } from 'react-i18next';
import './studentInternshipList/InternshipList.css';
import { getUserInfo } from "../utils/userInfo";
import ProfEvaluationForm from './profEvaluationForm';
import {PdfPrintService} from "../api/pdfPrintService";
import {generateContractPdfName, generateEmployerEvaluationPdfName, getEmployernameFromJobOffer} from "../api/utils";
import {useOutletContext} from "react-router-dom";
import {useSession} from "./CurrentSession";
import Modal from "./modal";
import ProfilePreview from "./userProfile/profilePreview";
import {Permission} from "../constants";

const StudentsListTeacher = ({refIds, notificationIds}) => {
    const { t } = useTranslation();
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState(''); // Define successMessage state
    const [internshipEvaluations, setInternshipEvaluations] = useState([]); // Store InternshipEvaluationDTO data
    const [expandedData, setExpandedData] = useState([]);
    const [filter, setFilter] = useState(''); // Filter state
    const [selectedStudent, setSelectedStudent] = useState(null); // For modal
    const [setPdfModalHelper] = useOutletContext()
    const {currentSession} = useSession();
    const [returnedEvaluatiionEmployer, setReturnedEvaluationEmployer] = useState(null);
    const [studentDetail, setStudentDetail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => {setIsModalOpen(false); setStudentDetail(null)};
    const handleFormSuccess = () => {
        setSuccessMessage(t('studentList.evaluationSentSuccess'));
        setSelectedStudent(null);
    };
    const filteredEvaluations = internshipEvaluations.filter((evaluation) => {
        const student = evaluation.internshipOffer?.jobOfferApplicationDTO?.CV?.studentDTO;
        console.log(student)

        if (!student) return false;

        return (
            student.prenom?.toLowerCase().includes(filter.toLowerCase()) ||
            student.nom?.toLowerCase().includes(filter.toLowerCase()) ||
            student.courriel?.toLowerCase().includes(filter.toLowerCase()) ||
            student.adresse?.toLowerCase().includes(filter.toLowerCase()) ||
            student.telephone?.toLowerCase().includes(filter.toLowerCase()) ||
            t("disciplines." + student.discipline?.id)?.toLowerCase().includes(filter.toLowerCase())
        );

    });

    useEffect(() => {
        if(!currentSession.id) return;
        fetchInternshipEvaluations();
    }, [currentSession]);

    useEffect(() => {
        if (errorMessage) {
            const timer = setTimeout(() => {
                setErrorMessage('');
                setSuccessMessage('');
            }, 5000);
            return () => clearTimeout(timer);
        }
    }, [errorMessage,successMessage]);

    const fetchInternshipEvaluations = async () => {
        const token = getUserInfo().token;
        try {
            const response = await fetch('http://localhost:8080/teacher/internshipEvaluations' +
                '?season='+currentSession.season +
                "&year="+currentSession.year, {
                method: 'GET',
                headers: {
                    'Authorization': token,
                    'Content-Type': 'application/json',
                }
            });
            const data = await response.json();
            if (response.ok) {
                if (refIds)
                    setInternshipEvaluations(data.value.filter(evaluation => refIds.includes(evaluation.id)));
                else
                    setInternshipEvaluations(data.value);
                console.log("Fetched Internship Evaluations:", data.value);
            } else {
                throw new Error(data.message);
            }
        } catch (error) {
            setErrorMessage(t('studentList.cantFetchData') + ": " + error.message);
            console.log(error);
        }
    };

    const toggleDetails = (evaluationId) => {
        if (expandedData.includes(evaluationId)) {
            setExpandedData(expandedData.filter((id) => id !== evaluationId));
        } else {
            setExpandedData([...expandedData, evaluationId]);
        }
    };

    const collapseAll = () => {
        setExpandedData([]);
    };

    const expandAll = () => {
        setExpandedData(internshipEvaluations.map((evaluation) => evaluation.id));
    };


    const printEvaluationPDF = async (evaluation) => {
        // console.log('evaluation, ', evaluation)
        const res = await PdfPrintService.printEmployerEvaluation(evaluation.id);
        if (res.value != null) {
            const byteCharacters = atob(res.value);
            const byteNumbers = new Array(byteCharacters.length);
            for (let i = 0; i < byteCharacters.length; i++) {
                byteNumbers[i] = byteCharacters.charCodeAt(i);
            }
            const byteArray = new Uint8Array(byteNumbers);

            const blob = new Blob([byteArray], {type: 'application/pdf'});
            const url = window.URL.createObjectURL(blob);
            const fileName = generateEmployerEvaluationPdfName(evaluation)
            setPdfModalHelper(url, fileName);

        } else
            console.log('error from printPDF', res.exception)

    }

    const updateIntershipEvaluations = (evaluationEmployeur, evaluationId) => {
        filteredEvaluations.filter((e) => {
            if (e.id === evaluationId) {
                e.evaluationEmployer = evaluationEmployeur;
            }
    })}
    const handleShowInternDetail = (student) => {
        console.log('show student detail', student)
        setStudentDetail(student)
        openModal()
    }

    return (
        <div className="p-5 fullpage-form-large">
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
                <h2 className="mb-10 text-xl font-bold">{t('studentList.studentsInMyCare')}</h2>
                <input
                    type="text"
                    placeholder={t('studentList.search')}
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                    className="w-full p-2 mb-5 border border-gray-300 rounded"
                />
                {internshipEvaluations.length === 0 ? (
                    <p>{t('studentList.noStudents')}</p>
                ) : (
                    <div>
                        <button
                            className="bg-darkpurple-option text-white py-1 px-2 mb-4 rounded border border-darkpurple ml-auto md:hidden"
                            onClick={expandAll}
                        >
                            {t('studentList.expandAll')}
                        </button>
                        <button
                            className="bg-darkpurple-option text-white py-1 px-2 mb-4 ml-2 rounded border border-darkpurple md:hidden"
                            onClick={collapseAll}
                        >
                            {t('studentList.collapseAll')}
                        </button>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2 max-h-[calc(100vh-350px)] overflow-y-auto">
                            {filteredEvaluations.map((evaluation) => {
                                const student = evaluation.internshipOffer?.jobOfferApplicationDTO?.CV?.studentDTO;
                                const employeur = evaluation.internshipOffer?.jobOfferApplicationDTO?.jobOffer?.employeurDTO;
                                const isExpanded = expandedData.includes(evaluation.id);

                                if (!student) return null;

                                return (
                                    <div key={evaluation.id} className="my-2 p-4 border border-darkpurple rounded">
                                        <div className="flex items-center justify-between">
                                            <div className="flex-grow profile-section-title hover:scale-110 cursor-pointer" onClick={() => handleShowInternDetail(student)}>
                                                <strong className="block md:inline"> {student.prenom} </strong>
                                                <strong className="block md:inline"> {student.nom} </strong>
                                            </div>
                                            <button
                                                className="bg-darkpurple-option text-white py-1 px-2 rounded border border-darkpurple ml-auto md:hidden"
                                                onClick={() => toggleDetails(evaluation.id)}
                                            >
                                                {isExpanded ? t('studentList.hideDetails') : t('studentList.viewDetails')}
                                            </button>
                                        </div>
                                        <div className={`mt-2 ${isExpanded ? 'block' : 'hidden'} md:block`}>
                                            <div className="mt-2">
                                                <div className="mb-2">
                                                    <strong>{t('formLabels.email')}</strong> {student.courriel}
                                                </div>
                                                <div className="mb-2">
                                                    <strong>{t('formLabels.address')}</strong> {student.adresse}
                                                </div>
                                                <div className="mb-2">
                                                    <strong>{t('formLabels.telephone')}</strong> {student.telephone}
                                                </div>
                                                <div>
                                                    <strong>{t('studentList.discipline')}</strong> {t("disciplines." + student.discipline?.id)}
                                                </div>

                                                {!evaluation.evaluationEmployer &&
                                                    <button
                                                        className="btn-neutral w-full p-2"
                                                        onClick={() => {
                                                            setSelectedStudent({student, employeur, evaluation});
                                                        }}
                                                    >
                                                        {t('studentList.openEvaluationForm')}
                                                    </button>
                                                }

                                                {evaluation.evaluationEmployer &&
                                                    <button onClick={() => printEvaluationPDF(evaluation)}
                                                            className="btn-confirm w-full p-2">
                                                    {t('printpdf.print')}
                                                    </button>}
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}
            </div>
            {selectedStudent && (
                <div className="modal ">
                    <div className="modal-overlay" onClick={() => setSelectedStudent(null)}></div>
                    <div className="modal-content">
                        <button className="modal-close" onClick={() => setSelectedStudent(null)}>×</button>
                        <ProfEvaluationForm
                            student={selectedStudent.student}
                            employeur={selectedStudent.employeur}
                            evaluation={selectedStudent.evaluation}
                            onClose={() => setSelectedStudent(null)}
                            onSuccess={handleFormSuccess}
                            updateEvaluation={updateIntershipEvaluations}
                            refIds={refIds}
                            notificationIds={notificationIds}

                        />
                    </div>
                </div>
            )}
            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {studentDetail && <ProfilePreview permission={Permission.Full} profile={studentDetail} />}
                </Modal>
            }
        </div>
    );
};

export default StudentsListTeacher
