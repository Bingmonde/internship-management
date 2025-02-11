
import {getUserInfo} from "../utils/userInfo";

const API_BASE_URL = "http://localhost:8080/internshipManager/";


export const InternshipManagerAPI ={

    getAllProfs : async () => {
        try {
            const res = await fetch(`${API_BASE_URL}profs/all` , {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });

            const data = await res.json();
            console.log('profS from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },

    getInternsByProfId : async (profId) => {
        try {
            const res = await fetch(`${API_BASE_URL}profs/${profId}/currentInterns` , {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });

            const data = await res.json();
            console.log('interns of prof from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },

    getInternsWaitingForAssignmentToProf : async () => {
        try {
            const res = await fetch(`${API_BASE_URL}interns/waitingForAssignmentToProf` , {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });

            const data = await res.json();
            console.log('interns from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },

    assignInternToProf : async (profId, internId) => {
        try {
            const res = await fetch(`${API_BASE_URL}interns/assignToProf?profId=${profId}&internshipOfferId=${internId}` , {
                method: 'POST',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                },
            });
            const data = await res.json();
            console.log('internship evalutation from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    }

}