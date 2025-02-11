import {getUserInfo} from "../utils/userInfo";
import {isAfter, isBefore, startOfToday} from "date-fns";


const API_BASE_URL = "http://localhost:8080/employeur/";

export const EmployeurAPI ={
    getFullJobApplications : async (offerId, currentSession, filter, pageIndex, size) => {
        try {
            // console.log("session api : " + JSON.stringify(currentSession))
            const res = await fetch(`${API_BASE_URL}jobOffers/listeJobApplications/${offerId}` +
                "?season="+currentSession.season+"&" +
                "year="+currentSession.year+
                '&page=0'+
                '&size=5'+
                '&q='+filter,{
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });
            const data = await res.json();
            console.log('full applications from API', data)
            return data;
        }
        catch(error) {
            console.log(error)
        }
    },
    // getFullJobApplications : async (offerId, currentSession, filter, candidatesStatusFilter, pageIndex, size) => {
    //     try {
    //        // console.log("session api : " + JSON.stringify(currentSession))
    //         const res = await fetch(`${API_BASE_URL}jobOffers/listeJobApplications/${offerId}` +
    //             "?season="+currentSession.season+"&" +
    //             "year="+currentSession.year+
    //             '&page=0'+
    //             '&size=5'+
    //             '&q='+filter+
    //             '&candidatesStatusFilter='+candidatesStatusFilter, {
    //             method: 'GET',
    //             headers: {
    //                 'Authorization': getUserInfo().token,
    //                 'Content-Type': 'application/json',
    //             }
    //         });
    //         const data = await res.json();
    //         console.log('full applications from API', data)
    //         return data;
    //     }
    //     catch(error) {
    //         console.log(error)
    //     }
    // },
    getJobOffersByEmployer : async (currentSession) => {
        try {
            const res = await fetch(`${API_BASE_URL}jobOffers?` +
                "season=" + currentSession.season + "&" +
                "year=" + currentSession.year, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });
            const data = await res.json();
            console.log('job offers from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },

    getInterviews : async (pageIndex, startDate, endDate, status, currentSession, size = 10) => {

        try {
            console.log("session api : " + JSON.stringify(currentSession))
            const res = await fetch(`${API_BASE_URL}jobInterview?` +
                "season="+currentSession.season+"&" +
                "year="+currentSession.year+
                '&page='+pageIndex+
                '&size='+size+
                '&startDate='+startDate+
                '&endDate='+endDate+
                '&status='+status
                , {
                headers: {
                    'Authorization': getUserInfo().token,
                }
            });
            const data = await res.json();
            // console.log('interviews from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },
    createJobInterview : async (jobInterview) => {
        try {
            const res = await fetch(`${API_BASE_URL}jobInterview`, {
                method: 'POST',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(jobInterview),
            });
            const data = await res.json();
            console.log('create interview from API', data)
            return data;

        } catch (err) {
            console.error('Network Error:', err);
        }

    },

    cancelInterveiw : async (interviewId) => {
        try {
            const res = await fetch(`${API_BASE_URL}jobInterview/`+interviewId , {
                method: 'DELETE',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });

            const data = await res.json();
            console.log('cancel interview from API', data)
            return data;
        } catch (error) {
            console.log(error)
        }
    },

    inviteToInternship: async (jobApplicationId, offerExpireInDays) => {
        try {
            const res = await fetch(`${API_BASE_URL}offerInternshipToStudent?jobApplicationId=${jobApplicationId}&offerExpireInDays=${offerExpireInDays}`, {
                method: 'PUT',
                headers: {
                    'Authorization': getUserInfo().token,
                }
            });
            const data = await res.json();
            console.log('invite internship from API', data)
            return data;
        } catch (err) {
            console.error(err);
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
        console.log('start date', startDate)
        console.log('end date', endDate)
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

    getJobOffersStats : async (currentSession) => {
        try {
            const res = await fetch(`${API_BASE_URL}jobOffers/stats?` +
                "season="+currentSession.season+"&" +
                "year="+currentSession.year
                , {
                    headers: {
                        'Authorization': getUserInfo().token,
                    }
                }
            );
            console.log("statistics about job application:", res);
            return res;
        } catch (err) {
            console.error('Network Error:', err);
        }
    }



}


