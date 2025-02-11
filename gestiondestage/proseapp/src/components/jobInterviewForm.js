import { FormProvider, useForm } from "react-hook-form";
import Input from "./input";
import {
    interview_location_link_validation,
    interview_type_validation,
    job_interview_date_time_validation,
} from "../utils/forms/formValidation";
import Select from "./select";
import { useTranslation } from "react-i18next";
import { useState } from "react";
import SuccessBox from "./successBox";
import ErrorBox from "./errorBox";

const JobInterviewForm = ({jobApplication, createInterview, onClose}) => {
    const methods = useForm();
    const { register, handleSubmit , getValues} = methods;
    const { t } = useTranslation();
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const [locationLabel, setLocationLabel] = useState(false)

    const onSubmit = async (data) => {
        clearMessages();
        console.log('Form Data:', data);

        const jobApplicationId = jobApplication.id;
        const jobInterview = {
            ...data,
            jobOfferApplicationId: jobApplicationId,
        };
        console.log('Before Submit:', jobInterview);
        await createInterview(jobInterview)
        onClose();
    };

    const clearMessages = () => {
        console.log('Clearing Messages');
        setErrorMessage('');
        setSuccessMessage('');
    };

    return (
        <div className="mx-auto">
            <FormProvider {...methods}>
                <form onSubmit={handleSubmit(onSubmit)} className=" w-1/2  mx-auto">
                    <h2 className="text-2xl font-bold text-center py-5">
                        {t('jobInterview.interviewFormTitle') + jobApplication.CV.studentDTO.nom + " " + jobApplication.CV.studentDTO.prenom}
                    </h2>

                    {/* 输入面试日期时间 */}
                    <Input {...job_interview_date_time_validation} onFocus={clearMessages} />

                    {/* 选择面试类型 */}
                    <Select
                        {...interview_type_validation}
                        options={[
                            { id: "Online", en: "Online", fr: "En ligne" },
                            { id: "InPerson", en: "In Person", fr: "En personne" },
                        ]}
                        onFocus={clearMessages}
                        onChange={() => setLocationLabel(getValues('interviewType') === "InPerson")}
                    />
                    {/* 输入面试地点或链接 */}
                    {<Input {...interview_location_link_validation} label={locationLabel ? "jobInterview.interviewLocation" : "jobInterview.interviewLink"} onFocus={clearMessages} />}

                    {successMessage && (
                        <div>
                            <SuccessBox msg={successMessage} />
                        </div>
                    )}

                    {errorMessage && <ErrorBox msg={errorMessage} />}

                    {!successMessage && (
                        <button type="submit" className="form-button">
                            {t('submit')}
                        </button>
                    )}
                </form>
            </FormProvider>
        </div>
    );
};

export default JobInterviewForm;
