import InterviewListStudent from "../components/interview/interviewsListStudent";
import UploadCVForm from "../components/studentCVs/uploadCVForm";
import {getUserInfo} from "../utils/userInfo";
import {t} from "../utils/i18n";
import {useTranslation} from "react-i18next";

const BASE_URL = 'http://localhost:8080/notifications';
export const UserGeneralAPI = {
    markReadNotification: async (notificationIds) => {
        try {
            const res = await fetch(`${BASE_URL}/read`, {
                method: 'PUT',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(notificationIds),
            });
            console.log('updateNotification', res);
            //return res.json();
        }
        catch (error) {
            console.error(error);
        }
    },

    previewPDF: async (fileName) => {
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
                return pdfUrl;
            } else {
                const error = await res.json();
                throw new Error(error.exception);
            }
        } catch (error) {
            console.log(('Unable to preview PDF: ') + error.message)
        }
    }


    //     user_created: {},
    //     cv_validation_required: {},
    //     offer_validation_required: {},
    //     cv_validated: {
    //         UploadCVForm
    //     },
    //     job_offer_validated: {},
    //     new_job_offer: {},
    //     new_applicant: {},
    //     new_interview: {
    //         InterviewListStudent
    //     },
    //     interview_confirmed: {},
    //     internship_offer_received: {
    //     },
    //     internship_offer_accepted: {},
    //     contract_to_start: {},
    //     contract_to_sign: {},
    //     contract_signed: {},
    //     intern_assignment: {},


}