import React, { useEffect, useState } from "react";
import { useTranslation } from 'react-i18next';

import './studentInternshipList/InternshipList.css';
import { getUserInfo } from "../utils/userInfo";
import InternEvaluationForm from '../components/InternEvaluationForm';
import {
    FaUserCheck,
    FaUserPlus,
    FaPaperPlane,
    FaTimes,
    FaUser,
    FaBuilding,
    FaPhone,
    FaComments,
    FaCalendarAlt,
    FaEnvelope,
    FaHome,
    FaBook
} from 'react-icons/fa';
import { FiCheckCircle } from 'react-icons/fi';
import {PdfPrintService} from "../api/pdfPrintService";
import {useOutletContext} from "react-router-dom";
import {generateStudentEvaluationPdfName} from "../api/utils";
import {useSession} from "./CurrentSession";
import Modal from "./modal";
import ProfilePreview from "./userProfile/profilePreview";
import {Permission} from "../constants";

const StudentsListEmployer = ({refIds, notificationIds}) => {
    const { t } = useTranslation();
    const [errorMessage, setErrorMessage] = useState('');
    const [students, setStudents] = useState([]);
    const [studentsExpandedData, setStudentsExpandedData] = useState([]);
    const [filter, setFilter] = useState(''); // État du filtre

    const [selectedStudent, setSelectedStudent] = useState(null); // État de l'étudiant sélectionné

    const [showEvaluationForm, setShowEvaluationForm] = useState(false); // Contrôle l'affichage du modal
    const [setPdfModalHelper] = useOutletContext()
    const [pdfError, setPdfError] = useState('');
    const {currentSession} = useSession();
    const [studentDetail, setStudentDetail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => {setIsModalOpen(false); setStudentDetail(null)};

    // Filtrer les étudiants en fonction du filtre saisi
    const filteredStudents = students.filter((student) => {
        return (
            (student.prenom || "").toLowerCase().includes(filter.toLowerCase()) ||
            (student.nom || "").toLowerCase().includes(filter.toLowerCase()) ||
            (student.courriel || "").toLowerCase().includes(filter.toLowerCase()) ||
            (student.adresse || "").toLowerCase().includes(filter.toLowerCase()) ||
            (student.telephone || "").toLowerCase().includes(filter.toLowerCase()) ||
            (t("disciplines." + (student.discipline?.id || "")) || "").toLowerCase().includes(filter.toLowerCase())
        );
    });


    useEffect(() => {
        if(!currentSession) return;
        fetchStudents();
    }, [currentSession]);

    useEffect(() => {
        if (errorMessage) {
            const timer = setTimeout(() => {
                setErrorMessage('');
            }, 5000);
            return () => clearTimeout(timer); // Effacer le timer lors du démontage
        }
    }, [errorMessage]);

    const fetchStudents = async () => {
        const token = getUserInfo().token;
        try {
            const res = await fetch('http://localhost:8080/employer/students' +
                '?season='+currentSession.season +
                "&year="+currentSession.year, {
                headers: {
                    'Authorization': token,
                    'Content-Type': 'application/json',
                }
            });
            const data = await res.json();
            console.log(data);
            if (res.ok && data.value) {
                const fetchedStudents = data.value.map(item => ({
                    ...item,
                    discipline: item.discipline || {},
                    status: item.status || 'N/A',
                    evaluation: item.evaluation || null, // Ajout de l'évaluation
                    jobOfferApplicationDTO: item.jobOfferApplicationDTO || {}, // Ajout de jobOfferApplicationDTO par défaut
                }));
                if (refIds)
                    setStudents(fetchedStudents.filter(student => refIds.includes(student.id)));
                else
                    setStudents(fetchedStudents);
            } else {
                throw new Error(data.message || 'Une erreur est survenue lors de la récupération des étudiants');
            }
        } catch (error) {
            setErrorMessage(t('studentList.cantFetchStudents') + ": " + error.message);
            console.log(error);
        }
    };

    const toggleStudentDetails = (studentId) => {
        if (studentsExpandedData.includes(studentId)) {
            setStudentsExpandedData(studentsExpandedData.filter((id) => id !== studentId));
        } else {
            setStudentsExpandedData([...studentsExpandedData, studentId]);
        }
    };

    const collapseAll = () => {
        setStudentsExpandedData([]);
    }

    const expandAll = () => {
        setStudentsExpandedData(students.map((student) => student.id));
    }

    const openEvaluationForm = (student) => {
        setSelectedStudent(student);
        setShowEvaluationForm(true);

    }


    const closeEvaluationForm = () => {
        setSelectedStudent(null);
        setShowEvaluationForm(false);
    }

    // Fonction appelée après une soumission réussie
    const handleEvaluationSuccess = (studentId) => {
        setStudents(prevStudents =>
            prevStudents.map(student =>
                student.id === studentId ? { ...student, evaluation: { submitted: true } } : student
            )
        );
        closeEvaluationForm();
    }

    const printInternEvaluationPDF = async (internshipOffer) => {
        console.log('internship offer id', internshipOffer);
        const res = await PdfPrintService.printInternEvaluation(internshipOffer.id);
        if (res.value != null) {
            const byteCharacters = atob(res.value);
            const byteNumbers = new Array(byteCharacters.length);
            for (let i = 0; i < byteCharacters.length; i++) {
                byteNumbers[i] = byteCharacters.charCodeAt(i);
            }
            const byteArray = new Uint8Array(byteNumbers);

            const blob = new Blob([byteArray], {type: 'application/pdf'});
            const url = window.URL.createObjectURL(blob);
            const fileName = generateStudentEvaluationPdfName(internshipOffer);
            setPdfModalHelper(url, fileName);
        } else{
            console.log('error from printPDF', res.exception)
            setPdfError(t(res.exception))
        }

    }

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
                <h2 className="mb-10 text-xl font-bold">
                    {t('studentList.studentsInMyCare')}
                </h2>
                {students.length === 0 ? (
                    <p>{t('studentList.noStudents')}</p>
                ) : (
                    <div>
                        {/* Boutons "Expand All" et "Collapse All" visibles uniquement sur les petits écrans */}
                        <div className="mb-4 flex space-x-2 md:hidden">
                            <button
                                className="bg-darkpurple-option text-white py-1 px-2 rounded border border-darkpurple flex items-center"
                                onClick={expandAll}
                            >
                                <FaUserPlus className="mr-2" />
                                {t('studentList.expandAll')}
                            </button>
                            <button
                                className="bg-darkpurple-option text-white py-1 px-2 rounded border border-darkpurple flex items-center"
                                onClick={collapseAll}
                            >
                                <FaUserCheck className="mr-2" />
                                {t('studentList.collapseAll')}
                            </button>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2 max-h-[calc(100vh-350px)] overflow-y-auto">
                            {filteredStudents.map((student) => {
                                const isExpanded = studentsExpandedData.includes(student.id);
                                const isEvaluated = student.evaluation && student.evaluation.submitted; // Vérifier si évalué
                                return (
                                    <div key={student.id} className="my-2 p-4 border border-darkpurple rounded shadow-md bg-white">
                                        <div className="flex items-center justify-between">
                                            <div className="flex-grow profile-section-title hover:scale-110 cursor-pointer" onClick={() => {handleShowInternDetail(student.jobOfferApplicationDTO?.CV?.studentDTO)}}>
                                                <strong className="block md:inline"> {student.jobOfferApplicationDTO?.CV?.studentDTO?.prenom} </strong>
                                                <strong className="block md:inline"> {student.jobOfferApplicationDTO?.CV?.studentDTO?.nom} </strong>
                                            </div>
                                            <button
                                                className="bg-darkpurple-option text-white py-1 px-2 rounded border border-darkpurple flex items-center md:hidden"
                                                onClick={() => toggleStudentDetails(student.id)}
                                            >
                                                {isExpanded ? (
                                                    <>
                                                        <FaUserCheck className="mr-2" />
                                                        {t('studentList.hideDetails')}
                                                    </>
                                                ) : (
                                                    <>
                                                        <FaUserPlus className="mr-2" />
                                                        {t('studentList.viewDetails')}
                                                    </>
                                                )}
                                            </button>
                                        </div>
                                        <div className={`mt-2 ${isExpanded ? 'block' : 'hidden'} md:block`}>
                                            <div className="mt-2">
                                                <div className="mb-2 flex items-center">
                                                    <strong className="mr-1">{t('formLabels.email')}</strong>
                                                    <span>{student.jobOfferApplicationDTO?.CV?.studentDTO?.courriel || ""}</span>
                                                </div>
                                                <div className="mb-2 flex items-center">
                                                    <strong className="mr-1">{t('formLabels.address')}</strong>
                                                    <span>{student.jobOfferApplicationDTO?.CV?.studentDTO?.adresse || ""}</span>
                                                </div>
                                                <div className="mb-2 flex items-center">
                                                    <strong className="mr-1">{t('formLabels.telephone')}</strong>
                                                    <span>{student.jobOfferApplicationDTO?.CV?.studentDTO?.telephone || ""}</span>
                                                </div>
                                                <div className="mb-2 flex items-center">
                                                    <strong className="mr-1">{t('studentList.discipline')}</strong>
                                                    <span>{student.jobOfferApplicationDTO?.CV?.studentDTO?.discipline?.fr || ""}</span>
                                                </div>

                                                {/* Bouton d'évaluation */}
                                                <button
                                                    className={`py-2 px-4 mt-4 rounded w-full flex items-center justify-center space-x-2 ${isEvaluated ? 'btn-disabled cursor-not-allowed' : 'btn-neutral'}`}
                                                    onClick={() => !isEvaluated && openEvaluationForm(student)}
                                                    disabled={isEvaluated}
                                                >
                                                    {isEvaluated ? (
                                                        <>
                                                            <FiCheckCircle />
                                                            <span>{t('studentList.evaluated')}</span>
                                                        </>
                                                    ) : (
                                                        <>
                                                            <FaPaperPlane />
                                                            <span>{t('studentList.evaluateIntern')}</span>
                                                        </>
                                                    )}
                                                </button>
                                                {pdfError && <p className="text-red-500">{pdfError}</p>}
                                                {isEvaluated &&
                                                    // here, student.id is actually internshipId
                                                    <button onClick={() => printInternEvaluationPDF(student)}
                                                            className="btn-confirm mt-2 w-full">
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

            {/* Modal d'évaluation */}
            {showEvaluationForm && selectedStudent && (
                <InternEvaluationForm
                    student={selectedStudent}
                    evaluation={selectedStudent.evaluation}
                    onClose={closeEvaluationForm}
                    onSuccess={handleEvaluationSuccess} // Passer la fonction de succès
                />
            )}

            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {studentDetail && <ProfilePreview permission={Permission.Full} profile={studentDetail} />}
                </Modal>
            }
        </div>
    )
}

export default StudentsListEmployer;
