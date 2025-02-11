import { useTranslation } from "react-i18next";
import { FormProvider, useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import Select from "./select";
import {
    offer_description_validation,
    address_validation,
    offer_name_validation,
    offer_type_validation,
    offer_number_people_validation,
    offer_salary_validation,
    offer_date_duration_validation,
    offer_date_begin_validation,
    offer_file_validation,
    offer_weekly_hours_validation,
    offer_daily_schedule_from_validation,
    offer_daily_schedule_to_validation,
    confirm_password_validation
} from "../utils/forms/formValidation";
import Input from "./input";
import React, { useState } from "react";
import Textarea from "./textarea";
import InputFile from "./inputfile";
import SuccessBox from "./successBox";
import { getUserInfo } from "../utils/userInfo";
import { isAfter } from "date-fns";

const UploadOfferForm = () => {
    const { t, i18n } = useTranslation();
    const methods = useForm();
    const { register, handleSubmit, formState: { errors }, getValues, setFocus, reset, resetField } = methods;
    const [successBanner, setSuccess] = useState(false);

    const offerTypeValue = ["", "remote", "hybrid", "office"];

    const generateTimeOptions = () => {
        const times = [];
        for (let hour = 0; hour < 24; hour++) {
            for (let min = 0; min < 60; min += 30) {
                const time = `${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}`;
                times.push(time);
            }
        }
        return times;
    };

    const timeOptions = generateTimeOptions();

    const onSubmit = async (data) => {
        let jobOffer = {
            titre: data.offerName,
            dateDebut: data.offerDateBegin,
            dateFin: data.offerDateDuration,
            lieu: data.adresse,
            typeTravail: offerTypeValue[data.offerType],
            nombreStagiaire: data.offerPeople,
            tauxHoraire: data.offerMoney,
            description: data.offerDescription,
            weeklyHours: data.offerWeeklyHours,
            dailyScheduleFrom: data.offerDailyScheduleFrom,
            dailyScheduleTo: data.offerDailyScheduleTo
        }
        let jobOfferFile = data.offerFile[0]
        console.log('From FE', jobOffer)
        console.log('From FE', jobOfferFile)

        try {
            const formData = new FormData();
            formData.append('jobOffre', new Blob([JSON.stringify(jobOffer)], {
                type: 'application/json'
            }));
            formData.append('file', jobOfferFile, {
                type: 'application/pdf'
            });

            const res = await fetch('http://localhost:8080/employeur/jobOffers', {
                method: 'POST',
                headers: {
                    'Authorization': getUserInfo().token,
                },
                body: formData
            });

            if (res.ok) {
                setSuccess(true)
            } else {
                let err = await res.json();
                console.log(err);
                setSuccess(false)
            }
        } catch (error) {
            console.log(error)
            setSuccess(false)
        }
    }

    return (
        <FormProvider {...methods}>
            <form onSubmit={handleSubmit(onSubmit)} className={"fullpage-form"}>
                <Input cancelBanner={setSuccess} {...offer_name_validation}></Input>
                <Input cancelBanner={setSuccess} {...address_validation}></Input>
                <Select cancelBanner={setSuccess} {...offer_type_validation} options={
                    [
                        { id: 1, customText: t("remote"), value: "Remote" },
                        { id: 2, customText: t("hybrid"), value: "Hybrid" },
                        { id: 3, customText: t("office"), value: "Office" }
                    ]
                }>
                </Select>
                <Input cancelBanner={setSuccess} {...offer_number_people_validation}></Input>
                <Input cancelBanner={setSuccess} {...offer_date_begin_validation}></Input>
                <Input cancelBanner={setSuccess} {...offer_date_duration_validation}
                       validation={{
                           ...offer_date_duration_validation.validation,
                           validate: (value) => {
                               const beforedate = getValues('offerDateBegin');

                               if (beforedate) {
                                   return isAfter(value, beforedate) || t("formErrors.offerDurationMin")
                               }
                               return true
                           }
                       }}></Input>

                <Input cancelBanner={setSuccess} {...offer_salary_validation}></Input>
                <Input cancelBanner={setSuccess} {...offer_weekly_hours_validation}></Input>
                <Input
                    cancelBanner={setSuccess}
                    {...offer_daily_schedule_from_validation}
                    timeOptions={timeOptions}
                    validation={{
                        ...offer_daily_schedule_from_validation.validation,

                    }}
                ></Input>
                <Input
                    cancelBanner={setSuccess}
                    {...offer_daily_schedule_to_validation}
                    timeOptions={timeOptions}
                    validation={{
                        ...offer_daily_schedule_to_validation.validation,

                    }}
                ></Input>

                <InputFile cancelBanner={setSuccess} {...offer_file_validation}></InputFile>

                {!successBanner && <button type="submit" className={"btn-confirm w-full"}>
                    {t(`offreEnvoyer`)}
                </button>}
                {successBanner && <SuccessBox msg={"offerSent"}></SuccessBox>}
            </form>

        </FormProvider>
    );
}

export default UploadOfferForm;
