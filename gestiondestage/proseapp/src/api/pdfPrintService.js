import {getUserInfo} from "../utils/userInfo";


const BASE_URL = 'http://localhost:8080/print/';

export const PdfPrintService = {

    printContact: async (intershipId) => {
        try {
            const res = await fetch(`${BASE_URL}contracts/${intershipId}`, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                },
            });
            const data = await res.json();
            console.log('contact pdf from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },

    printInternEvaluation: async (evaluationInternId) => {
        try {
            const res = await fetch(`${BASE_URL}evaluationIntern/${evaluationInternId}`, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                },
            });
            const data = await res.json();
            console.log('intern evaluation pdf from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },

    printEmployerEvaluation: async (evaluationId) => {
        try {
            const res = await fetch(`${BASE_URL}evaluationEmployer/${evaluationId}`, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                },
            });
            const data = await res.json();
            console.log('employer evaluation pdf from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    }



}