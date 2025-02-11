import React, {useRef, useState} from 'react';
import { useTranslation } from 'react-i18next';
import { getUserInfo } from "../utils/userInfo";
import {
    StageNumber,
    StagePreference,
    WillingnessToRehire,
    EvaluationOption
} from '../constants';
import {ErrorBoundary} from "react-error-boundary";
import SignaturePadWrapper from "./signature/signaturePadWrapper";
import {useOutletContext} from "react-router-dom";
import {UserGeneralAPI} from "../api/userAPI";

const ProfEvaluationForm = ({ student, employeur, evaluation, onClose,onSuccess,updateEvaluation, refIds, notificationIds }) => {
    const { t } = useTranslation();

    const [formData, setFormData] = useState({
        employerId: employeur.id || '',
        companyName: employeur.nomCompagnie || '',
        contactPerson: employeur.contactPerson || '',
        address: employeur.adresse || '',
        city: employeur.city || '',
        postalCode: employeur.postalCode || '',
        telephone: employeur.telephone || '',
        fax: employeur.fax || '',
        internName: `${student.prenom} ${student.nom}`,
        internshipDebut: evaluation.internshipOffer.jobOfferApplicationDTO.jobOffer.dateDebut || '',
        salary: evaluation.internshipOffer.jobOfferApplicationDTO.jobOffer.tauxHoraire || '',
        stageNumber: evaluation?.stageNumber || '',
        preferredStage: evaluation?.preferredStage || '',
        numberOfInterns: evaluation?.numberOfInterns || '',
        willingToRehire: evaluation?.willingToRehire || '',
        schedule1Start: evaluation?.schedule1Start || '',
        schedule1End: evaluation?.schedule1End || '',
        schedule2Start: evaluation?.schedule2Start || '',
        schedule2End: evaluation?.schedule2End || '',
        schedule3Start: evaluation?.schedule3Start || '',
        schedule3End: evaluation?.schedule3End || '',
        comments: evaluation?.comments || '',
        observations: evaluation?.observations || '',
        // teacherSignature: evaluation?.teacherSignature || '',
        date: evaluation?.date || '',
        tasksMetExpectations: evaluation?.tasksMetExpectations || '',
        integrationSupport: evaluation?.integrationSupport || '',
        supervisionSufficient: evaluation?.supervisionSufficient || '',
        workEnvironment: evaluation?.workEnvironment || '',
        workClimate: evaluation?.workClimate || '',
        accessibleTransport: evaluation?.accessibleTransport || '',
        salaryInteresting: evaluation?.salaryInteresting || '',
        communicationWithSupervisor: evaluation?.communicationWithSupervisor || '',
        equipmentAdequate: evaluation?.equipmentAdequate || '',
        workloadAcceptable: evaluation?.workloadAcceptable || '',
        // 分开的工作小时数字段
        firstMonthHours: evaluation?.firstMonthHours || '',
        secondMonthHours: evaluation?.secondMonthHours || '',
        thirdMonthHours: evaluation?.thirdMonthHours || '',
    });

    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const signaturePadRef = useRef(null);
    const [allowSign, setAllowSign] = useState(false)

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };
    const handleNumberChange = (e) => {
        const { name, value } = e.target;
        if (/^\d*$/.test(value)) {
            setFormData({ ...formData, [name]: value });
        }
    };

    const clear = () => {
        signaturePadRef.current.clear()
        setAllowSign(false)
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        console.dir(signaturePadRef)
        const base64Canvas = signaturePadRef.current.toDataURL("image/png").split(';base64,')[1];
        const evaluationEmployerDto =  {
            employerId: employeur.id,
            stageNumber: formData.stageNumber,
            firstMonthHours: formData.firstMonthHours,
            secondMonthHours:formData.secondMonthHours,
            thirdMonthHours: formData.thirdMonthHours,
            preferredStage: formData.preferredStage,
            numberOfInterns:formData.numberOfInterns,
            willingToRehire: formData.willingToRehire,
            schedule1Start: formData.schedule1Start,
            schedule1End: formData.schedule1End,
            schedule2Start: formData.schedule2Start,
            schedule2End: formData.schedule2End,
            schedule3Start: formData.schedule3Start,
            schedule3End: formData.schedule3End,
            comments: formData.comments,
            observations: formData.observations,
            signatureDTO: base64Canvas,
            date: '2000-01-01',
            tasksMetExpectations: formData.tasksMetExpectations,
            integrationSupport: formData.integrationSupport,
             supervisionSufficient: formData.supervisionSufficient,
             workEnvironment: formData.workEnvironment,
             workClimate: formData.workClimate,
             accessibleTransport: formData.accessibleTransport,
             salaryInteresting: formData.salaryInteresting,
             communicationWithSupervisor: formData.communicationWithSupervisor,
             equipmentAdequate: formData.equipmentAdequate,
             workloadAcceptable: formData.workloadAcceptable};
        try {
            const response = await fetch(`http://localhost:8080/teacher/evaluations/${evaluation.id}`, {
                method: 'POST',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(evaluationEmployerDto),
            });
            if (response.ok){
                const data = await response.json();
                setSuccessMessage(t('form.submit_success'));
                updateEvaluation(data.value, evaluation.id)

                // remove from notificaiton
                if (refIds) {
                    const index = refIds.indexOf(evaluation.id);
                    await UserGeneralAPI.markReadNotification(notificationIds[index])
                }

                // if (onSuccess) onSuccess();
                setTimeout(() => {
                    onClose();
                }, 5000);
            }
            else {
                var err = await response.json()
                console.log(err)
                console.log(err["exception"])
                // todo extract the error message from the response
                setErrorMessage(t('form.submit_error') + ": " + err["exception"]);
            }
            //

        } catch (error) {
            console.error("提交表单时出错:", error);
            setErrorMessage(t('form.submit_error') + ": " + error.message);
        }
    };

    const evaluationOptions = [
        { label: t('form.Totalement en accord'), value: EvaluationOption.TOTAL_AGREEMENT },
        { label: t('form.Plutôt en accord'), value: EvaluationOption.STRONG_AGREEMENT },
        { label: t('form.Plutôt en désaccord'), value: EvaluationOption.SOMEWHAT_DISAGREEMENT },
        { label: t('form.Totalement en désaccord'), value: EvaluationOption.TOTAL_DISAGREEMENT },
        { label: t('form.N/A'), value: EvaluationOption.NOT_APPLICABLE }
    ];

    const evaluationQuestions = [
        {
            field: 'tasksMetExpectations',
            label: t('form.evaluation.tasks_met_expectations'),
        },
        {
            field: 'integrationSupport',
            label: t('form.evaluation.integration_support'),
        },
        {
            field: 'supervisionSufficient',
            label: t('form.evaluation.supervision_sufficient'),
        },
        {
            field: 'workEnvironment',
            label: t('form.evaluation.work_environment'),
        },
        {
            field: 'workClimate',
            label: t('form.evaluation.work_climate'),
        },
        {
            field: 'accessibleTransport',
            label: t('form.evaluation.accessible_transport'),
        },
        {
            field: 'salaryInteresting',
            label: t('form.evaluation.salary_interesting'),
        },
        {
            field: 'communicationWithSupervisor',
            label: t('form.evaluation.communication_with_supervisor'),
        },
        {
            field: 'equipmentAdequate',
            label: t('form.evaluation.equipment_adequate'),
        },
        {
            field: 'workloadAcceptable',
            label: t('form.evaluation.workload_acceptable'),
        },
    ];
    const generateTimeOptions = () => {
        const times = [];
        for (let hour = 0; hour < 24; hour++) {
            for (let minute = 0; minute < 60; minute += 30) {
                const formattedTime = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
                times.push(formattedTime);
            }
        }
        return times;
    };


    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50 ">
            <div className="bg-white rounded-lg shadow-lg overflow-y-auto max-h-full w-full max-w-7.5xl p-6 relative">
                <button
                    className="absolute top-4 right-4 text-red hover:text-red-hover text-7xl"
                    onClick={onClose}
                >
                    &times;
                </button>
                <form onSubmit={handleSubmit} className="space-y-8">
                    <h2 className="text-3xl font-bold mb-6 text-center text-selected">
                        {t('form.title')}
                    </h2>

                    {/* company */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected">
                            {t('form.company_identification')}
                        </h3>

                        <div className="mb-6">
                            <label className="block text-sm font-medium mb-2 text-selected">
                                {t('form.company_name')}:
                            </label>
                            <input
                                type="text"
                                name="companyName"
                                value={formData.companyName}
                                onChange={handleChange}
                                className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                disabled
                                required
                            />
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-medium mb-2 text-gray-700">
                                {t('form.contact_person')}:
                            </label>
                            <input
                                type="text"
                                name="contactPerson"
                                value={formData.contactPerson}
                                onChange={handleChange}
                                className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                disabled
                                required
                            />
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-medium mb-2 text-gray-700">
                                {t('form.address')}:
                            </label>
                            <input
                                type="text"
                                name="address"
                                value={formData.address}
                                onChange={handleChange}
                                className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                disabled
                                required
                            />
                        </div>

                        <div className="flex space-x-6 mb-6">
                            <div className="w-1/2">
                                <label className="block text-sm font-medium mb-2 text-gray-700">
                                    {t('form.city')}:
                                </label>
                                <input
                                    type="text"
                                    name="city"
                                    value={formData.city}
                                    onChange={handleChange}
                                    className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                    disabled
                                    required
                                />
                            </div>
                            <div className="w-1/2">
                                <label className="block text-sm font-medium mb-2 text-gray-700">
                                    {t('form.postal_code')}:
                                </label>
                                <input
                                    type="text"
                                    name="postalCode"
                                    value={formData.postalCode}
                                    onChange={handleChange}
                                    className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                    disabled
                                    required
                                />
                            </div>
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-medium mb-2 text-gray-700">
                                {t('form.phone')}:
                            </label>
                            <input
                                type="text"
                                name="phone"
                                value={formData.telephone}
                                onChange={handleChange}
                                className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                disabled
                                required
                            />
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-medium mb-2 text-gray-700">
                                {t('form.fax')}:
                            </label>
                            <input
                                type="text"
                                name="fax"
                                value={formData.fax}
                                onChange={handleChange}
                                className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                disabled
                                required
                            />
                        </div>
                    </div>


                    {/* student */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected">
                            {t('form.intern_identification')}
                        </h3>

                        <div className="mb-6">
                            <label className="block text-sm font-medium mb-2 text-gray-700">
                                {t('form.intern_name')}:
                            </label>
                            <input
                                type="text"
                                name="internName"
                                value={formData.internName}
                                onChange={handleChange}
                                className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                disabled
                                required
                            />
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-medium mb-2 text-gray-700">
                                {t('form.internship_date')}:
                            </label>
                            <input
                                type="date"
                                name="internshipDebut"
                                value={formData.internshipDebut}
                                onChange={handleChange}
                                className="border border-greenish p-3 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                required
                            />
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-medium mb-2 text-gray-700">
                                {t('form.stage_number')}:
                            </label>
                            <div className="flex space-x-6">
                                <label className="flex items-center">
                                    <input
                                        type="radio"
                                        name="stageNumber"
                                        value={StageNumber.STAGE_1}
                                        checked={formData.stageNumber === StageNumber.STAGE_1}
                                        onChange={handleChange}
                                        className="mr-3 focus:ring-blue-500"
                                        required
                                    />
                                    {t('form.stage_1')}
                                </label>
                                <label className="flex items-center">
                                    <input
                                        type="radio"
                                        name="stageNumber"
                                        value={StageNumber.STAGE_2}
                                        checked={formData.stageNumber === StageNumber.STAGE_2}
                                        onChange={handleChange}
                                        className="mr-3 focus:ring-blue-500"
                                        required
                                    />
                                    {t('form.stage_2')}
                                </label>
                            </div>
                        </div>
                    </div>


                    <h3 className="text-xl font-semibold mb-6 text-selected text-center">{t('form.evaluation.evaluation')}</h3>
                    <div className="border border-greenish rounded-lg shadow-md bg-white mb-8">

                        <div
                            className="bg-lightpurple grid grid-cols-5 sm:grid-cols-6 gap-4 px-6 py-4 border-b border-greenish">
                            <div
                                className="text-gray-700 text-sm font-medium col-span-2 sm:col-span-1">{t('form.questions')}</div>
                            {evaluationOptions.map((option, index) => (
                                <div key={index} className="text-center text-gray-700 text-sm font-medium">
                                    {option.label}
                                </div>
                            ))}
                        </div>

                        <div className="divide-y divide-prose-neutral">
                            {evaluationQuestions.map(({ field, label }, index) => (
                                <div key={index} className={`grid grid-cols-5 sm:grid-cols-6 gap-4 p-6 ${index % 2 === 0 ? 'bg-white' : 'bg-blue-50'}`}>

                                    <div className="col-span-2 sm:col-span-1 text-gray-700 text-sm font-medium">
                                        {field === 'salaryInteresting'
                                            ? `${t('evaluation.salary_attractive')} ${formData.salary} ${t('form.per_hour')}`
                                            : label
                                        }
                                    </div>


                                    {evaluationOptions.map((option, idx) => (
                                        <div key={idx} className="flex justify-center items-center">
                                            <input
                                                type="radio"
                                                name={field}
                                                value={option.value}
                                                checked={formData[field] === option.value}
                                                onChange={handleChange}
                                                className="focus:ring-blue-500 h-5 w-5 text-selected"
                                                required
                                            />
                                        </div>
                                    ))}
                                </div>
                            ))}
                        </div>

                    </div>

                    {/* 指定每周工作小时数 */}
                    <div className="border border-greenish rounded-lg p-8 mb-8 shadow-md bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected">
                            {t('form.supervision_sufficient')}
                        </h3>

                        <div className="mb-6">
                            <label className="block text-sm font-medium mb-2 text-gray-700">
                                {t('form.specify_hours_per_week')}:
                            </label>
                            <div className="space-y-4">
                                <div className="flex items-center space-x-4">
                                    <label className="text-gray-600">{t('form.first_month')}:</label>
                                    <input
                                        type="number"
                                        name="firstMonthHours"
                                        value={formData.firstMonthHours}
                                        onChange={handleNumberChange}
                                        className="border border-greenish p-2 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 w-full"
                                        required
                                    />
                                </div>
                                <div className="flex items-center space-x-4">
                                    <label className="text-gray-600">{t('form.second_month')}:</label>
                                    <input
                                        type="number"
                                        name="secondMonthHours"
                                        value={formData.secondMonthHours}
                                        onChange={handleNumberChange}
                                        className="border border-greenish p-2 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 w-full"
                                        required
                                    />
                                </div>
                                <div className="flex items-center space-x-4">
                                    <label className="text-gray-600">{t('form.third_month')}:</label>
                                    <input
                                        type="number"
                                        name="thirdMonthHours"
                                        value={formData.thirdMonthHours}
                                        onChange={handleNumberChange}
                                        className="border border-greenish p-2 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 w-full"
                                        required
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="mb-8">
                        <label className="block text-sm font-medium mb-2 text-gray-700">
                            {t('form.comments')}:
                        </label>
                        <textarea
                            name="comments"
                            value={formData.comments}
                            onChange={handleChange}
                            className="border border-greenish p-4 w-full rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                            rows="4"
                        />
                    </div>

                    {/* 综合观察部分 */}
                    <div className="border border-greenish rounded-lg p-8 shadow-md mb-8 bg-white">
                        <h3 className="text-xl font-semibold mb-6 text-selected">
                            {t('form.general_observations')}
                        </h3>

                        {/* 优先实习阶段 */}
                        <div className="mb-8">
                            <p className="text-gray-700">{t('form.preferred_stage2')}:</p>
                            <div className="flex space-x-6 mt-4">
                                <label className="flex items-center">
                                    <input
                                        type="radio"
                                        name="preferredStage"
                                        value={StagePreference.PREMIER_STAGE}
                                        checked={formData.preferredStage === StagePreference.PREMIER_STAGE}
                                        onChange={handleChange}
                                        className="mr-3 focus:ring-blue-500"
                                        required
                                    />
                                    {t('form.first_stage')}
                                </label>
                                <label className="flex items-center">
                                    <input
                                        type="radio"
                                        name="preferredStage"
                                        value={StagePreference.DEUXIEME_STAGE}
                                        checked={formData.preferredStage === StagePreference.DEUXIEME_STAGE}
                                        onChange={handleChange}
                                        className="mr-3 focus:ring-blue-500"
                                        required
                                    />
                                    {t('form.second_stage')}
                                </label>
                            </div>
                        </div>

                        <div className="mb-8">
                            <p className="text-gray-700">{t('form.intern_acceptance')}:</p>
                            <div className="flex space-x-6 mt-4">
                                {[
                                    {label: t('form.one_intern'), value: '1'},
                                    {label: t('form.two_interns'), value: '2'},
                                    {label: t('form.three_interns'), value: '3'},
                                    {label: t('form.more_than_three'), value: 'MORE_THAN_THREE'},
                                ].map((option) => (
                                    <label key={option.value} className="flex items-center">
                                        <input
                                            type="radio"
                                            name="numberOfInterns"
                                            value={option.value}
                                            checked={formData.numberOfInterns === option.value}
                                            onChange={handleChange}
                                            className="mr-3 focus:ring-blue-500"
                                            required
                                        />
                                        {option.label}
                                    </label>
                                ))}
                            </div>
                        </div>


                        <div className="mb-8">
                            <p className="text-gray-700">{t('form.willing_to_rehire')}:</p>
                            <div className="flex space-x-6 mt-4">
                                <label className="flex items-center">
                                    <input
                                        type="radio"
                                        name="willingToRehire"
                                        value={WillingnessToRehire.YES}
                                        checked={formData.willingToRehire === WillingnessToRehire.YES}
                                        onChange={handleChange}
                                        className="mr-3 focus:ring-blue-500"
                                        required
                                    />
                                    {t('form.yes')}
                                </label>
                                <label className="flex items-center">
                                    <input
                                        type="radio"
                                        name="willingToRehire"
                                        value={WillingnessToRehire.NO}
                                        checked={formData.willingToRehire === WillingnessToRehire.NO}
                                        onChange={handleChange}
                                        className="mr-3 focus:ring-blue-500"
                                        required
                                    />
                                    {t('form.no')}
                                </label>
                            </div>
                        </div>

                        <div className="mb-8">
                            <p className="text-gray-700">{t('form.variable_schedules')}:</p>
                            <div className="mt-4 space-y-4">
                                {[1, 2, 3].map((index) => (
                                    <div key={index} className="flex items-center space-x-4">
                                        <label className="text-gray-600">{t('form.from')}</label>
                                        <select
                                            name={`schedule${index}Start`}
                                            value={formData[`schedule${index}Start`]}
                                            onChange={handleChange}
                                            className="border border-greenish p-3 w-1/2 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        >
                                            {generateTimeOptions().map((time) => (
                                                <option key={time} value={time}>
                                                    {time}
                                                </option>
                                            ))}
                                        </select>
                                        <label className="text-gray-600">{t('form.to')}</label>
                                        <select
                                            name={`schedule${index}End`}
                                            value={formData[`schedule${index}End`]}
                                            onChange={handleChange}
                                            className="border border-greenish p-3 w-1/2 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500"
                                        >
                                            {generateTimeOptions().map((time) => (
                                                <option key={time} value={time}>
                                                    {time}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                ))}
                            </div>
                        </div>


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


                        <div className="flex space-x-4">
                            <button
                                type="submit"
                                className="btn-confirm w-full"
                            >
                                {t('form.submit')}
                            </button>
                            <button
                                type="button"
                                onClick={onClose}
                                className="btn-cancel w-full"
                            >
                                {t('form.cancel')}
                            </button>
                        </div>

                    </div>
                </form>

                {errorMessage && (
                    <div className="bg-success text-white p-3 rounded mb-4 text-center">
                        {t(errorMessage)}
                    </div>
                )}
                {successMessage && (
                    <div className="bg-greenish text-white p-3 rounded mb-4 text-center">
                        {t(successMessage)}
                    </div>
                )}
            </div>
        </div>
    );
};

export default ProfEvaluationForm;

