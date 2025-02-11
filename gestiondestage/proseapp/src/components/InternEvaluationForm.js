import React, {useRef, useState} from 'react';
import { useTranslation } from 'react-i18next';
import { getUserInfo } from "../utils/userInfo";
import { FaPaperPlane, FaTimes, FaUser, FaBuilding, FaPhone, FaComments, FaCalendarAlt, FaEnvelope, FaHome, FaBook } from 'react-icons/fa';
import { FiCheckCircle } from 'react-icons/fi';
import {ErrorBoundary} from "react-error-boundary";
import SignaturePadWrapper from "./signature/signaturePadWrapper";
import {UserGeneralAPI} from "../api/userAPI";
import {telephoneFormat} from "../api/utils";

const InternEvaluationForm = ({ student, onClose, evaluation = {}, onSuccess, refIds, notificationIds }) => {
    const { t, i18n } = useTranslation();

    const { name: teacherName, email: teacherEmail } = getUserInfo();

    // Assurez-vous que evaluation est toujours un objet
    const safeEvaluation = evaluation || {};

    const [formData, setFormData] = useState({
        companyName: student.jobOfferApplicationDTO?.jobOffer?.employeurDTO?.nomCompagnie || '',
        telephone: student.jobOfferApplicationDTO?.jobOffer?.employeurDTO?.telephone || '',
        supervisorName: safeEvaluation.supervisorName || '',
        function: safeEvaluation.function || '',
        program: student.jobOfferApplicationDTO?.CV?.studentDTO?.discipline?.[i18n.language] || '',
        internName: student.jobOfferApplicationDTO?.CV?.studentDTO?.nom || '',

        productivityEvaluation: safeEvaluation.productivityEvaluation || {},
        qualityOfWorkEvaluation: safeEvaluation.qualityOfWorkEvaluation || {},
        interpersonalRelationshipsEvaluation: safeEvaluation.interpersonalRelationshipsEvaluation || {},
        personalSkillsEvaluation: safeEvaluation.personalSkillsEvaluation || {},

        employerSignature: safeEvaluation.employerSignature || '',
        date: new Date().toISOString().split('T')[0],
        overallComments: safeEvaluation.overallComments || '',
        supervisionHoursPerWeek: safeEvaluation.supervisionHoursPerWeek || '',
        willingnessToRehire: safeEvaluation.willingnessToRehire || '',
        technicalTrainingComments: safeEvaluation.technicalTrainingComments || '',
        productivityComments: safeEvaluation.productivityComments || '',
        qualityOfWorkComments: safeEvaluation.qualityOfWorkComments || '',
        interpersonalRelationshipsComments: safeEvaluation.interpersonalRelationshipsComments || '',
        personalSkillsComments: safeEvaluation.personalSkillsComments || '',
        evaluationDiscussedWithIntern: safeEvaluation.evaluationDiscussedWithIntern === true ? 'true' : 'false',
        returnFormToName: safeEvaluation.returnFormToName || 'François Lacoursière',
        returnFormToEmail: safeEvaluation.returnFormToEmail || 'francois.lacoursiere@claurendeau.qc.ca',
        name: safeEvaluation.name || '',
        overallAppreciation: safeEvaluation.overallAppreciation || '',
    });

    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isSubmitted, setIsSubmitted] = useState(false); // Nouveau état
    const signaturePadRef = useRef(null);
    const [allowSign, setAllowSign] = useState(false)
    const clear = () => {
        signaturePadRef.current.clear()
        setAllowSign(false)
    }


    // Gérer les changements de champ
    const handleChange = (section, field, value) => {
        console.log("Section:", section, "Field:", field, "Value:", value);
        if (section && ['productivityEvaluation', 'qualityOfWorkEvaluation', 'interpersonalRelationshipsEvaluation', 'personalSkillsEvaluation'].includes(section)) {
            setFormData(prevState => ({
                ...prevState,
                [section]: {
                    ...prevState[section],
                    [field]: value
                }
            }));
        } else if (section === null && field) {
            setFormData(prevState => ({
                ...prevState,
                [field]: value
            }));
        }
    };

    // Gérer la soumission du formulaire
    const handleSubmit = async (e) => {
        e.preventDefault();
        console.dir(signaturePadRef)
        const base64Canvas = signaturePadRef.current.toDataURL("image/png").split(';base64,')[1];

        const evaluationInternDto = {
            ...formData,
            employerSignature: base64Canvas
        }
        console.log(evaluationInternDto)


        const { token } = getUserInfo();
        if (!token || token.trim() === '') {
            setErrorMessage(t('form.jwt_missing'));
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/employer/evaluations/${student.id}`, {
                method: 'POST',
                headers: {
                    'Authorization': token,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(evaluationInternDto),
            });


            if (!response.ok) {
                console.log(response)
                const errorData = response.status === 401 ? { exception: 'Unauthorized' } : await response.json();
                throw new Error(errorData.exception || t('form.submit_error'));
            }
            if (response.ok){
                const data = await response.json();
                setSuccessMessage(t('form.submit_success'));
                setIsSubmitted(true); // Marquer comme soumis

                // remove from notification
                if (refIds){
                    const index = refIds.indexOf(student.id);
                    await UserGeneralAPI.markReadNotification(notificationIds[index])
                }

                if (onSuccess) onSuccess(student.id); // Appeler onSuccess avec l'ID de l'étudiant
                setTimeout(() => {
                    onClose();
                }, 5000);
            }
            else {
                var err = await response.json()
                console.log(err)
                console.log(err["exception"])
                // Extraire le message d'erreur de la réponse
                setErrorMessage(t('form.submit_error') + ": " + err["exception"]);
            }
        } catch (error) {
            console.error("Erreur lors de la soumission du formulaire:", error);
            setErrorMessage(`${t('form.submit_error')}: ${error.message}`);
        }
    };

    // Définir les options d'évaluation
    const evaluationOptions = [
        { label: t('form.Totalement en accord'), value: 'TOTAL_AGREEMENT' },
        { label: t('form.Plutôt en accord'), value: 'STRONG_AGREEMENT' },
        { label: t('form.Plutôt en désaccord'), value: 'SOMEWHAT_DISAGREEMENT' },
        { label: t('form.Totalement en désaccord'), value: 'TOTAL_DISAGREEMENT' },
        { label: t('form.N/A'), value: 'NOT_APPLICABLE' }
    ];

    // Fonction pour rendre une section d'évaluation
    const renderEvaluationSection = (sectionName, questions, commentField, sectionKey) => (
        <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
            <h3 className="text-xl font-semibold mb-6 text-selected flex items-center">
                <FaComments className="mr-2 text-green-600" />
                {sectionName}
            </h3>
            <div className="overflow-x-auto">
                <table className="min-w-full text-center">
                    <thead>
                    <tr className="bg-gray-200">
                        <th scope="col" className="p-2 text-left">{t('form.Questions')}</th>
                        {evaluationOptions.map((option, idx) => (
                            <th scope="col" key={idx}>{option.label}</th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {questions.map((question, index) => (
                        <tr key={index} className={index % 2 === 0 ? "bg-white" : "bg-gray-100"}>
                            <td className="p-2 text-left">{question.label}</td>
                            {evaluationOptions.map((option, idx) => (
                                <td key={idx}>
                                    <input
                                        type="radio"
                                        name={`${sectionKey}_${question.field}`}
                                        value={option.value}
                                        checked={formData[sectionKey]?.[question.field] === option.value}
                                        onChange={(e) => handleChange(sectionKey, question.field, e.target.value)}
                                        className="mr-2"
                                        required
                                    />
                                </td>
                            ))}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            {/* Commentaires */}
            <div className="mt-4 flex items-start">
                <FaComments className="mt-1 text-green-600 mr-2" />
                <textarea
                    name={commentField}
                    placeholder={t('form.Commentaires')}
                    value={formData[commentField]}
                    onChange={(e) => handleChange(null, commentField, e.target.value)}
                    className="border border-greenish p-4 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                    rows="3"
                ></textarea>
            </div>
        </div>
    );

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <div className="bg-white rounded-lg shadow-lg overflow-y-auto max-h-full w-full max-w-7xl p-6 relative">
                {/* Bouton de fermeture */}
                <button
                    className="absolute top-4 right-4 text-red-500 hover:text-red-700 text-3xl"
                    onClick={onClose}
                >
                    <FaTimes />
                </button>
                <form onSubmit={handleSubmit} className="space-y-8">
                    {/* Titre du formulaire */}
                    <h2 className="text-3xl font-bold mb-6 text-center text-selected flex items-center justify-center">
                        <FiCheckCircle className="mr-2 text-green-600"/>
                        {t('form.Fiche d’Évaluation du Stagiaire')}
                    </h2>

                    {/* Identification de l'Entreprise */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected flex items-center">
                            <FaBuilding className="mr-2 text-green-600"/>
                            {t('form.Identification de l\'Entreprise')}
                        </h3>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="flex items-center">
                                <FaBuilding className="mr-2 text-gray-700"/>
                                <div className="w-full">
                                    <label className="block text-sm font-medium mb-2 text-gray-700">
                                        {t('form.Nom de l’entreprise')}:
                                    </label>
                                    <input
                                        type="text"
                                        name="companyName"
                                        value={formData.companyName}
                                        onChange={(e) => handleChange(null, 'companyName', e.target.value)}
                                        className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                            </div>
                            <div className="flex items-center">
                                <FaUser className="mr-2 text-gray-700"/>
                                <div className="w-full">
                                    <label className="block text-sm font-medium mb-2 text-gray-700">
                                        {t('form.Superviseur')}:
                                    </label>
                                    <input
                                        type="text"
                                        name="supervisorName"
                                        value={formData.supervisorName}
                                        onChange={(e) => handleChange(null, 'supervisorName', e.target.value)}
                                        className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                            <div className="flex items-center">
                                <FaUser className="mr-2 text-gray-700"/>
                                <div className="w-full">
                                    <label className="block text-sm font-medium mb-2 text-gray-700">
                                        {t('form.Fonction')}:
                                    </label>
                                    <input
                                        type="text"
                                        name="function"
                                        value={formData.function}
                                        onChange={(e) => handleChange(null, 'function', e.target.value)}
                                        className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                            </div>
                            <div className="flex items-center">
                                <FaPhone className="mr-2 text-gray-700"/>
                                <div className="w-full">
                                    <label className="block text-sm font-medium mb-2 text-gray-700">
                                        {t('form.Téléphone')}:
                                    </label>
                                    <input
                                        type="text"
                                        name="telephone"
                                        value={telephoneFormat(formData.telephone)}
                                        onChange={(e) => handleChange(null, 'telephone', e.target.value)}
                                        className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Identification de l'Etudiant */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected flex items-center">
                            <FaUser className="mr-2 text-green-600"/>
                            {t('form.Identification de l\'Étudiant')}
                        </h3>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="flex items-center">
                                <FaUser className="mr-2 text-gray-700"/>
                                <div className="w-full">
                                    <label className="block text-sm font-medium mb-2 text-gray-700">
                                        {t('form.Nom de l’élève')}:
                                    </label>
                                    <input
                                        type="text"
                                        name="internName"
                                        value={formData.internName}
                                        onChange={(e) => handleChange(null, 'internName', e.target.value)}
                                        className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        disabled
                                    />
                                </div>
                            </div>
                            <div className="flex items-center">
                                <FaBuilding className="mr-2 text-gray-700"/>
                                <div className="w-full">
                                    <label className="block text-sm font-medium mb-2 text-gray-700">
                                        {t('form.Programme d’études')}:
                                    </label>
                                    <input
                                        type="text"
                                        name="program"
                                        value={formData.program}
                                        onChange={(e) => handleChange(null, 'program', e.target.value)}
                                        className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Sections d'Évaluation */}
                    {renderEvaluationSection(t('form.productivityEvaluation'), [
                        {label: t('form.productivity_a'), field: 'production_a'},
                        {label: t('form.productivity_b'), field: 'production_b'},
                        {label: t('form.productivity_c'), field: 'production_c'},
                        {label: t('form.productivity_d'), field: 'production_d'},
                        {label: t('form.productivity_e'), field: 'production_e'},
                    ], 'productivityComments', 'productivityEvaluation')}

                    {renderEvaluationSection(t('form.quality_of_work'), [
                        {label: t('form.quality_of_work_a'), field: 'quality_a'},
                        {label: t('form.quality_of_work_b'), field: 'quality_b'},
                        {label: t('form.quality_of_work_c'), field: 'quality_c'},
                        {label: t('form.quality_of_work_d'), field: 'quality_d'},
                        {label: t('form.quality_of_work_e'), field: 'quality_e'},
                    ], 'qualityOfWorkComments', 'qualityOfWorkEvaluation')}

                    {renderEvaluationSection(t('form.interpersonal_relationships'), [
                        {label: t('form.interpersonal_relationships_a'), field: 'interPersonal_a'},
                        {label: t('form.interpersonal_relationships_b'), field: 'interPersonal_b'},
                        {label: t('form.interpersonal_relationships_c'), field: 'interPersonal_c'},
                        {label: t('form.interpersonal_relationships_d'), field: 'interPersonal_d'},
                        {label: t('form.interpersonal_relationships_e'), field: 'interPersonal_e'},
                        {label: t('form.interpersonal_relationships_f'), field: 'interPersonal_f'},
                    ], 'interpersonalRelationshipsComments', 'interpersonalRelationshipsEvaluation')}

                    {renderEvaluationSection(t('form.personal_skills'), [
                        {label: t('form.personal_skills_a'), field: 'personalStills_a'},
                        {label: t('form.personal_skills_b'), field: 'personalStills_b'},
                        {label: t('form.personal_skills_c'), field: 'personalStills_c'},
                        {label: t('form.personal_skills_d'), field: 'personalStills_d'},
                        {label: t('form.personal_skills_e'), field: 'personalStills_e'},
                        {label: t('form.personal_skills_f'), field: 'personalStills_f'},
                    ], 'personalSkillsComments', 'personalSkillsEvaluation')}

                    {/* Appréciation Générale */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected flex items-center">
                            <FaComments className="mr-2 text-green-600"/>
                            {t('form.overall_appreciation')}
                        </h3>
                        <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                            <h3 className="text-xl font-semibold mb-6 text-selected">{t('form.APPRECIATION GLOBALE DU STAGIAIRE')}</h3>
                            <div className="mb-6">
                                <label className="block text-sm font-medium mb-2 text-gray-700">
                                    {t('form.overall_evaluation')}:
                                </label>
                                <div className="flex flex-col space-y-2">
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="overallAppreciation"
                                            value="TOTAL_AGREEMENT"
                                            checked={formData.overallAppreciation === 'TOTAL_AGREEMENT'}
                                            onChange={(e) => handleChange(null, 'overallAppreciation', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.habiletés_dépassent_beaucoup')}
                                    </label>
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="overallAppreciation"
                                            value="STRONG_AGREEMENT"
                                            checked={formData.overallAppreciation === 'STRONG_AGREEMENT'}
                                            onChange={(e) => handleChange(null, 'overallAppreciation', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.habiletés_dépassent')}
                                    </label>
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="overallAppreciation"
                                            value="SOMEWHAT_DISAGREEMENT"
                                            checked={formData.overallAppreciation === 'SOMEWHAT_DISAGREEMENT'}
                                            onChange={(e) => handleChange(null, 'overallAppreciation', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.habiletés_répondent_pleinement')}
                                    </label>
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="overallAppreciation"
                                            value="TOTAL_DISAGREEMENT"
                                            checked={formData.overallAppreciation === 'TOTAL_DISAGREEMENT'}
                                            onChange={(e) => handleChange(null, 'overallAppreciation', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.habiletés_répondent_partiellement')}
                                    </label>
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="overallAppreciation"
                                            value="NOT_APPLICABLE"
                                            checked={formData.overallAppreciation === 'NOT_APPLICABLE'}
                                            onChange={(e) => handleChange(null, 'overallAppreciation', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.habiletés_ne_répondent_pas')}
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div className="mb-6 flex items-center">
                            <FaComments className="mr-2 text-gray-700"/>
                            <textarea
                                name="overallComments"
                                value={formData.overallComments}
                                onChange={(e) => handleChange(null, 'overallComments', e.target.value)}
                                className="border border-greenish p-4 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                rows="4"
                                required
                                placeholder={t('form.précisez_votre_appréciation')}
                            ></textarea>
                        </div>

                        {/* Discussion avec l'Intern */}
                        <div className="mb-6 flex items-center">
                            <FaUser className="mr-2 text-gray-700"/>
                            <div className="w-full">
                                <label className="block text-sm font-medium mb-2 text-gray-700">
                                    {t('form.evaluation_discussed_with_intern')}:
                                </label>
                                <div className="flex space-x-6">
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="evaluationDiscussedWithIntern"
                                            value="true"
                                            checked={formData.evaluationDiscussedWithIntern === 'true'}
                                            onChange={(e) => handleChange(null, 'evaluationDiscussedWithIntern', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.Oui')}
                                    </label>
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="evaluationDiscussedWithIntern"
                                            value="false"
                                            checked={formData.evaluationDiscussedWithIntern === 'false'}
                                            onChange={(e) => handleChange(null, 'evaluationDiscussedWithIntern', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.Non')}
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Heures de Supervision */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected flex items-center">
                            <FaCalendarAlt className="mr-2 text-green-600"/>
                            {t('form.supervision_hours')}
                        </h3>
                        <div className="mb-6 flex items-center">
                            <FaCalendarAlt className="mr-2 text-gray-700"/>
                            <div className="w-full">
                                <label className="block text-sm font-medium mb-2 text-gray-700">
                                    {t('form.indicate_supervision_hours')}:
                                </label>
                                <input
                                    type="number"
                                    name="supervisionHours"
                                    value={formData.supervisionHoursPerWeek}
                                    onChange={(e) => handleChange(null, 'supervisionHoursPerWeek', e.target.value)}
                                    className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                    required
                                    placeholder="7.5"
                                    step="0.1"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Réembauche */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected flex items-center">
                            <FaUser className="mr-2 text-green-600"/>
                            {t('Réembauche')}
                        </h3>
                        <div className="mb-6 flex items-center">
                            <FaUser className="mr-2 text-gray-700"/>
                            <div className="w-full">
                                <label className="block text-sm font-medium mb-2 text-gray-700">
                                    {t('form.company_would_rehire')}:
                                </label>
                                <div className="flex space-x-6">
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="willingnessToRehire"
                                            value="YES"
                                            checked={formData.willingnessToRehire === 'YES'}
                                            onChange={(e) => handleChange(null, 'willingnessToRehire', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.Oui')}
                                    </label>
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="willingnessToRehire"
                                            value="NO"
                                            checked={formData.willingnessToRehire === 'NO'}
                                            onChange={(e) => handleChange(null, 'willingnessToRehire', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.Non')}
                                    </label>
                                    <label className="flex items-center">
                                        <input
                                            type="radio"
                                            name="willingnessToRehire"
                                            value="MAYBE"
                                            checked={formData.willingnessToRehire === 'MAYBE'}
                                            onChange={(e) => handleChange(null, 'willingnessToRehire', e.target.value)}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {t('form.Peut-être')}
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Commentaires Supplémentaires */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected flex items-center">
                            <FaComments className="mr-2 text-green-600"/>
                            {t('form.Commentaires Supplémentaires')}
                        </h3>
                        <div className="flex items-start">
                            <FaComments className="mt-1 mr-2 text-gray-700"/>
                            <textarea
                                name="technicalTrainingComments"
                                value={formData.technicalTrainingComments}
                                onChange={(e) => handleChange(null, 'technicalTrainingComments', e.target.value)}
                                className="border border-greenish p-4 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                rows="4"
                                placeholder={t('form.enter_additional_comments')}
                            ></textarea>
                        </div>
                    </div>

                    {/* Instructions pour retourner le formulaire */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected flex items-center">
                            <FaUser className="mr-2 text-green-600"/>
                            {t('form.return_form_to')}
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="flex items-center">
                                <FaUser className="mr-2 text-gray-700"/>
                                <div className="w-full">
                                    <label className="block text-sm font-medium mb-2 text-gray-700">
                                        {t('form.return_form_to_name')}:
                                    </label>
                                    <input
                                        type="text"
                                        name="returnFormToName"
                                        value={formData.returnFormToName}
                                        onChange={(e) => handleChange(null, 'returnFormToName', e.target.value)}
                                        className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                            </div>
                            <div className="flex items-center">
                                <FaPaperPlane className="mr-2 text-gray-700"/>
                                <div className="w-full">
                                    <label className="block text-sm font-medium mb-2 text-gray-700">
                                        {t('form.return_form_to_email')}:
                                    </label>
                                    <input
                                        type="email"
                                        name="returnFormToEmail"
                                        value={formData.returnFormToEmail}
                                        onChange={(e) => handleChange(null, 'returnFormToEmail', e.target.value)}
                                        className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Signature de l'Employeur */}
                    <div className="mb-8 ps-5 bg-gray-200 lg:w-1/2 w-full">
                        <ErrorBoundary fallback={<div/>}>
                            <h3 className={"font-bold text-md m-2"}>Signature : </h3>

                            <div className={"overflow-x-auto"}>
                                <SignaturePadWrapper ref={signaturePadRef}
                                                     isEmptyState={setAllowSign}></SignaturePadWrapper>
                            </div>
                            <button className="small-button mt-2"
                                    onClick={clear}>{t("signatureMaker.clearButtonText")}</button>
                        </ErrorBoundary>
                    </div>

                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected flex items-center">
                            <FaUser className="mr-2 text-green-600" />
                            {t('form.signName')}
                        </h3>
                        <div className="flex items-center">
                            <FaUser className="mr-2 text-gray-700" />
                            <div className="w-full">
                                <label className="block text-sm font-medium mb-2 text-gray-700">
                                    {t('form.name')}
                                </label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={(e) => handleChange(null, 'name', e.target.value)}
                                    className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                    required
                                />
                            </div>
                        </div>
                    </div>

                    {/* Messages d'erreur et de succès */}
                    {errorMessage && (
                        <div
                            className="bg-red-500 text-white p-3 rounded mt-4 text-center flex items-center justify-center">
                            <FaTimes className="mr-2"/>
                            {errorMessage}
                        </div>
                    )}
                    {successMessage && (
                        <div
                            className="bg-green-500 text-white p-3 rounded mt-4 text-center flex items-center justify-center">
                            <FiCheckCircle className="mr-2"/>
                            {successMessage}
                        </div>
                    )}

                    {/* Boutons de soumission et d'annulation */}
                    <div className="flex space-x-4">
                        <button
                            type="submit"
                            className={`btn-confirm flex items-center justify-center w-1/2  space-x-2 ${isSubmitted ? 'btn-disabled flex items-center justify-center w-1/2  space-x-2' : ''}`}
                            disabled={isSubmitted} // Désactiver le bouton si soumis
                        >
                            {isSubmitted ? (
                                <>
                                    <FiCheckCircle/>
                                    <span className="ml-2">{t('form.sent')}</span>
                                </>
                            ) : (
                                <>
                                    <FaPaperPlane/>
                                    <span>{t('form.submit')}</span>
                                </>
                            )}
                        </button>
                        <button
                            type="button"
                            onClick={onClose}
                            className="btn-cancel flex items-center justify-center w-1/2 space-x-2"
                        >
                            <FaTimes/>
                            <span>{t('form.cancel')}</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}

export default InternEvaluationForm;
