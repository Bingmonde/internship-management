import './App.css';
import { useTranslation } from 'react-i18next';
import Inscription from "./components/inscription";
import {useEffect, useState} from "react";
import {PageInfo} from "./components/page_info";
import {Navigate, Route, Routes, useNavigate} from "react-router-dom";
import Dashboard from "./components/dashboard/dashboard";
import Connection from "./components/connection";

import * as PropTypes from "prop-types";
import ChangeLanguage from "./components/changeLanguage";
import UploadOfferForm from "./components/uploadOfferForm";
import Building from "./components/building";
import ValidateOffer from "./components/validateOffer";
import UploadCVForm from "./components/studentCVs/uploadCVForm";
import ListCV from "./components/listCV";
import InternshipList from "./components/studentInternshipList/InternshipList";
import ListCandidates from "./components/candidates/listCandidates";
import JobInterviewForm from "./components/jobInterviewForm";
import InterviewsListStudent from "./components/interview/interviewsListStudent";
import InterviewList from "./components/interview/interviewList";
import PdfPreview from "./components/pdfPreview";
import NotificationRefresh from "./components/notificationRefresh";
import InterviewListStudent from "./components/interview/interviewsListStudent";
import PendingInternshipsStudent from "./components/pendingInternshipsStudent";
import ContractList from "./components/contracts/contractList";
import StudentsListTeacher from "./components/studentsListTeacher";
import AssignToProf from "./components/assignToProf";
import ProfEvaluationForm from "./components/profEvaluationForm";
import StudentsListEmployer from "./components/studentsListEmployer";
import MyProfile from "./components/userProfile/myProfile";
import Reports from "./components/reports/reports";
import WelcomePage from "./components/welcomePage/welcomePage";

function App() {
    const { t, i18n } = useTranslation();
    const navigate = useNavigate();
    const [pdfModal, setPdfModal] = useState({pdfUrl:"about:blank",filename:"file.pdf",showModal:false})
    const [reload, triggerReload] = useState(false)

    const [notifications, setNotifications] = useState([]);
    const [notificationError, setNotificationError] = useState("");

    const setPdfModalHelper = (pdfUrl, filename) => {
        setPdfModal({pdfUrl:pdfUrl,filename:filename,showModal:true})
    }

    const setUser = (username) => {
        //localStorage.setItem("username", username);
        sessionStorage.setItem("username", username);

    };

    const user = () => {
        // return { displayName: localStorage.getItem("username") };
        return { displayName: sessionStorage.getItem("username") };
    };

    const getUsername = async (token, doReload) => {
        let user;
        try {
            const response = await fetch('http://localhost:8080/userinfo/username', {
                method: 'GET',
                headers: {
                    'Authorization': token,
                },
            });
            if (!response.ok) {
                throw new Error('Pas réussi à récupérer le nom d utilisateur');
            }
            const data = await response.json();
            if (data.exception) {
                console.error('Error:', data.exception);
            } else {
                console.log('Username:', data.value);
                user = {
                    displayName: data.value,
                };
            }
        } catch (error) {
            console.error('Error:', error);
        }

        if (user) {
            setUser(user.displayName);
            // localStorage.setItem("username", user.displayName);
            sessionStorage.setItem("username", user.displayName);
            if (doReload) triggerReload(!reload)
        }
    }

    const connectUser = async (email, password) => {
        const endpointStart = 'http://localhost:8080/connect';
        const userData = {
            email: email.toLowerCase(),
            password: password,
        };

        console.log(
            'trying to log into account with data: ',
            JSON.stringify(userData),
            ' selected POST endpoint: ',
            endpointStart
        );

        // TODO faut clean up tout ca un peu
        try {
            const res = await fetch(endpointStart, {
                method: 'POST',
                headers: {
                    'Content-type': 'application/json',
                },
                body: JSON.stringify(userData)
            })
            if (res.ok){
                const data = await res.json();
                sessionStorage.setItem('token', data.value.accessToken);
                sessionStorage.setItem('userType', data.value.role);
                // localStorage.setItem('token', data.value.accessToken);
                // localStorage.setItem('userType', data.value.role);

                // const token = localStorage.getItem('token');
                const token = sessionStorage.getItem('token');
                if (!token) {
                    console.error('Token not found in localStorage');
                    return;
                }
                await getUsername(token,false)
                navigate('/dashboard');
            } else if (res.status === 401) {
                let error = await res.text();
                console.log(error);
                navigate('/error', { state: { info: 'formErrors.errorLoginCredentials' } });
            } else if (res.status === 404) {
                let error = await res.text();
                console.log(error);
                navigate('/error', { state: { info: 'bruh 404' } });
            }
        } catch (error) {
            console.log(error);
            navigate('/error', { state: { info: 'formErrors.errorLoginCredentials' } });
        }
    };

    function ProtectedRoute({element}) {

        const publicRoutes = ['/inscription', '/connection'];

        if (publicRoutes.includes(window.location.pathname)) {
            console.log('public route', window.location.pathname)
            return element;
        }

        if (!user().displayName) {
            return <Navigate to="/connection" />
        }
        return element;
    }

  return (
    <div className="App">
        <ChangeLanguage />
        <Routes>
            <Route path="/dashboard" element={<ProtectedRoute element={<Dashboard notifications={notifications} setNotifications={setNotifications} notificationError={notificationError}/>}></ProtectedRoute>}>
                {/*<Route index element={<NotificationRefresh notifications={notifications} setNotifications={setNotifications} setError={setNotificationError}/>} />*/}
                <Route index element={<WelcomePage/>} />
                <Route path="/dashboard/uploadOffers" element={<UploadOfferForm/>} />
                <Route path="/dashboard/candidatesList" element={<ListCandidates/>} />
                <Route path="/dashboard/interviewForm" element={<JobInterviewForm/>} />
                <Route path="/dashboard/interviewListStudent" element={<InterviewListStudent />} />
                <Route path="/dashboard/interviewList" element={<InterviewList/>} />
                <Route path="/dashboard/validateOffers" element={<ValidateOffer/>} />
                <Route path="/dashboard/uploadCV" element={<UploadCVForm/>}/>
                <Route path="/dashboard/welcome" element={<WelcomePage/>} />
                <Route path="/dashboard/listCV" element={<ListCV/>} />
                <Route path="/dashboard/pendingInternships" element={<PendingInternshipsStudent/>} />
                <Route path="internships" element={<InternshipList/>} />
                <Route path={"/dashboard/contracts"} element={<ContractList></ContractList>}></Route>
                <Route path="/dashboard/evaluateStudent" element={<StudentsListEmployer />} />
                <Route path={"/dashboard/assignToProf"} element={<AssignToProf />}></Route>
                <Route path={"/dashboard/myStudents"} element={<StudentsListTeacher />}></Route>
                <Route path={"/dashboard/myProfile"} element={<MyProfile updateUsername={getUsername} />}></Route>
                <Route path={"/dashboard/reports"} element={<Reports />}></Route>
                <Route path={"*"} element={<Navigate to={"/dashboard"}/>}/>

                <Route path="teacher/company/:companyId" element={<ProfEvaluationForm />} />

            </Route>
            <Route path="/inscription" element={<Inscription setLogin={connectUser} role={'student'}/>} />
            <Route path="/connection" element={<Connection setLogin={connectUser} />} />
            <Route path="/error" element={<PageInfo />} />

            <Route
                path="*"
                element={<Navigate to="/" replace={true} />}
            />
            <Route path="/"
                   element={ <ProtectedRoute element={<Navigate to={"/dashboard"}></Navigate>}> </ProtectedRoute>}
            />


            </Routes>

        </div>
    );
}

export default App;
