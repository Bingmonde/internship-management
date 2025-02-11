import React, {useEffect, useState} from "react";
import {getUserInfo} from "../../utils/userInfo";
import {UserGeneralAPI} from "../../api/userAPI";
import StudentCVCard from "./studentCVCard";
import ErrorBox from "../errorBox";
import {useTranslation} from "react-i18next";

const StudentCvNotification = ({refIds, notificationIds, setPdfModalHelper}) => {

    const { t, i18n } = useTranslation();
    const [cv, setCv] = useState(null)
    const [error, setError] = useState(false)
    const [errorMessage, setErrorMessage] = useState("")

    const handlePreview = async (fileName) => {
        try {
            const res = await fetch(`http://localhost:8080/download/file/${fileName}`, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                }
            });

            if (res.ok) {
                const jsonResponse = await res.json();
                const base64String = jsonResponse.value;

                const byteCharacters = atob(base64String);
                const byteNumbers = new Array(byteCharacters.length);
                for (let i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                const byteArray = new Uint8Array(byteNumbers);
                const blob = new Blob([byteArray], { type: 'application/pdf' });

                const url = URL.createObjectURL(blob);
                setPdfModalHelper(url,fileName)
                //setPreviewUrl(url);
            } else {
                setError(true);
                setErrorMessage(t('error.fetchFailed'));
            }
        } catch (error) {
            console.error(error);
            setError(true);
            setErrorMessage(t('error.network'));
        }
    };


    const fetchCV = async () => {
        try {
            const res = await fetch('http://localhost:8080/cvs/' + refIds.toString()
                , {
                    method: 'GET',
                    headers: {
                        'Authorization': getUserInfo().token,
                    },
                });
            if (res.ok) {
                const result = await res.json();
                // remove this notification
                await UserGeneralAPI.markReadNotification([notificationIds]);
                setCv(result.value)

            } else {
                setError(true);
                setErrorMessage(t('error.fetchFailed'));
            }
        } catch (error) {
            console.error(error);
            setError(true);
            setErrorMessage(t('error.network'));
        }
    }

    useEffect(() => {
        fetchCV()
    }, []);

    return (
        <>
            {error && <ErrorBox msg={errorMessage} />}
            {cv && <StudentCVCard cv={cv} handlePreview={handlePreview}/>}
        </>
    )
}
export default StudentCvNotification