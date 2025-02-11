import {getUserInfo} from "../utils/userInfo";
import {isAfter, isBefore, startOfToday} from "date-fns";


const API_BASE_URL = "http://localhost:8080/student/";

export const StudentAPI ={

    getInterviews : async (pageIndex, startDate, endDate, status, currentSession) => {

        try {
            // const currentSession = JSON.parse(localStorage.getItem("currentSession"));
            const res = await fetch("http://localhost:8080/student/jobInterview?" +
                "season="+currentSession.season+"&" +
                "year="+currentSession.year+
                '&page='+pageIndex+
                '&size=10'+
                '&startDate='+startDate+
                '&endDate='+endDate+
                '&status='+status
                , {
                headers: {
                    'Authorization': getUserInfo().token,
                }
            });

            // console.log(res);

            const data = await res.json();
            // console.log('interviews from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },

    sortInterViewsByDate : (interviews) => {
        if (interviews != null || interviews.length > 0)
            return interviews.sort((a, b) => isBefore(new Date(a.interviewDate), new Date(b.interviewDate)) ? -1 : 1);
        else
            return []
    },

    getValidInterviews : (interviews) => {
        if (interviews != null || interviews.length > 0){
            const filterdInterviews =  interviews.filter(interview => {
                return isAfter(new Date(interview.interviewDate), startOfToday()) &&
                interview.cancelledDate === null}
            );
            return filterdInterviews
        }
        else
            return []
    },

    getCancelledInterviews : (interviews) => {
        if (interviews != null)
            return interviews.filter(interview => interview.cancelledDate != null);
        else
            return interviews
    },


    getUnconfirmedInterviews:  (interviews) => {
        if (interviews != null)
            return interviews.filter(interview => !interview.isConfirmedByStudent);

        else
            return interviews
    },

    getConfirmedInterviews : (interviews) => {
        if (interviews != null)
            return interviews.filter(interview => interview.isConfirmedByStudent === true);

        else
            return interviews

    },

    filterInterviewByApplicationId : (applicationId, interviews) => {
        if (interviews != null || interviews.length > 0)
            return interviews.filter(interview => interview.jobOfferApplication.id === applicationId && interview.cancelledDate === null && isAfter(new Date(interview.interviewDate), startOfToday()));

        else
            return interviews
    },


    filterInterviewByDate : (startDate, endDate, interviews) => {
        if (startDate !== '' && endDate !== '')
            return interviews.filter(interview => isAfter(new Date(interview.interviewDate), new Date(startDate)) && isBefore(new Date(interview.interviewDate), new Date(endDate)))
        else if (startDate !== '')
            return interviews.filter(interview => isAfter(new Date(interview.interviewDate), new Date(startDate)))
        else if (endDate !== '')
            return interviews.filter(interview => isBefore(new Date(interview.interviewDate), new Date(endDate)))
        else
            return interviews

    },

    filterInterviewByStartDate : (startDate, interviews) => {
            return interviews.filter(interview => isAfter(new Date(interview.interviewDate), new Date(startDate)))
    },

    filterInterviewByEndDate : ( endDate, interviews) => {
            return interviews.filter(interview => isBefore(new Date(interview.interviewDate), new Date(endDate)))
    },

    confirmInterview : async (jobInterviewId) => {
        try {
            const res = await fetch(`${API_BASE_URL}jobInterview/confirmation/${jobInterviewId}` , {
                method: 'POST',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });

            const data = await res.json();
            console.log('confirm interview', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },
}


