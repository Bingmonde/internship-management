import React, { useEffect, useState } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next"; // Importing i18next
import { cv_file_validation } from "../../utils/forms/formValidation";
import InputFile from "../inputfile";
import SuccessBox from "../successBox";
import ErrorBox from "../errorBox";
import {getUserInfo} from "../../utils/userInfo";
import PaginationComponent from "../pagination/paginationComponent";
import {useLocation, useOutletContext} from "react-router-dom";
import {welcomeRoute} from "../../constants";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faAdd, faSubtract} from "@fortawesome/free-solid-svg-icons";
import {isNotWelcomePage} from "../../api/utils";
import {UserGeneralAPI} from "../../api/userAPI";
import StudentCVCard from "./studentCVCard";

const UploadCVForm = ({refIds, notificationIds}) => {
    const [setPdfModalHelper] = useOutletContext()
    const { t, i18n } = useTranslation(); // Added i18n for language change
    const methods = useForm();
    const { handleSubmit } = methods;
    const [successBanner, setSuccess] = useState(false);
    const [errorBanner, setError] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [cvList, setCvList] = useState([]);

    const [currentPageIndex, setCurrentPageIndex] = useState(0);
    const [totalPagesFromBackend, setTotalPagesFromBackend] = useState(0);
    const [uploadNewCV, setUploadNewCV] = useState(false);

    const location = useLocation();
    const currentRoute = location.pathname;

    useEffect(() => {
        fetchCvList();
    }, [t, currentPageIndex]);

    const fetchCvList = async () => {
        try {
            const res = await fetch('http://localhost:8080/students/CV/all?'+
                'page='+currentPageIndex+
                '&size=5'
                , {
                    method: 'GET',
                    headers: {
                        'Authorization': getUserInfo().token,
                    },
                });
            if (res.ok) {
                const result = await res.json();
                // only show the CVs in the refIds list if it is provided
                if (refIds){
                    setCvList(result.value.filter(cv => refIds.includes(cv.id)));
                    // remove this notification
                    await UserGeneralAPI.markReadNotification(notificationIds);
                }
                else
                    setCvList(result.value);


                // set total pages from backend (to inform pagination component of numbers to show)
                setTotalPagesFromBackend(parseInt(result.totalPages, 10));
                console.log("total pages from backend: " + parseInt(result.totalPages, 10));

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

    // Fetch the list of CVs when the component mount
    const onSubmit = async (data) => {
        let cvFile = data.cvFile[0];
        const formData = new FormData();
        formData.append('file', cvFile);

        try {
            const res = await fetch('http://localhost:8080/students/CV', {
                method: 'POST',
                headers: {
                    'Authorization': getUserInfo().token,
                },
                body: formData,
            });

            const result = await res.json();
            const resultValue = result.value;
            if (res.ok) {
                setSuccess(true);
                setCvList([...cvList, resultValue]);
            } else {
                setError(true);
                setErrorMessage(result.message || t('error.uploadFailed'));
            }
        } catch (error) {
            console.error(error);
            setError(true);
            setErrorMessage(t('error.network'));
        }
    };

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

    const changeLanguage = (language) => {
        i18n.changeLanguage(language);
    };

    const handleUploadNewCV = () => setUploadNewCV(true)
    const handleHideUpload = () => setUploadNewCV(false)

    return (
        <div className="flex flex-col w-full">
            <h2>Liste des CVs</h2>
            {isNotWelcomePage(currentRoute) && (
                uploadNewCV ? (
                    <>
                        <button
                            className="flex flex-row cursor-pointer border-solid hover:bg-prose-neutral mb-5 px-10 py-1 text-center self-center"
                            onClick={handleHideUpload}
                        >
                            <FontAwesomeIcon icon={faSubtract} from="solid" className="self-center me-3" />
                            <p className="align-content-center">{t('Hide')}</p>
                        </button>

                        <FormProvider {...methods}>
                            <div className="w-full p-2 bg-prose-neutral rounded rounded-xl flex justify-center border border-2">
                                <form onSubmit={handleSubmit(onSubmit)} className="mx-auto">
                                    <InputFile cancelBanner={setSuccess} {...cv_file_validation} />
                                    {!successBanner && (
                                        <button type="submit" className="btn-confirm w-full p-2 mb-2">
                                            {t('formLabels.upload')}
                                        </button>
                                    )}
                                    {successBanner && <SuccessBox msg={t('fileSent')} />}
                                </form>
                            </div>
                        </FormProvider>
                    </>
                ) : (
                    <button
                        className="flex flex-row cursor-pointer hover:bg-prose-neutral p-3 self-center"
                        onClick={handleUploadNewCV}
                    >
                        <FontAwesomeIcon icon={faAdd} from="solid" className="self-center me-3" />
                        <p className="align-content-center">{t('Upload new')}</p>
                    </button>
                )
            )}


        {cvList.length > 0 && <div className={"w-full mx-auto"}>
            {errorBanner && <ErrorBox msg={errorMessage} />}
            <div className={cvList.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"}>
                {cvList.map((cv, index) => {
                    console.log('refids:', refIds);
                    return <StudentCVCard cv={cv} handlePreview={handlePreview}></StudentCVCard>
                    }
                )}
            < /div>


            {/*pagination widget*/}
            <div className="flex justify-center mt-1">
                <PaginationComponent totalPages={totalPagesFromBackend} paginate={(pageNumber) => {
                    setCurrentPageIndex(pageNumber - 1); // page index starts at zero in backend, not the same as page "number"
                }}/>
            </div>

        </div>}
        </div>
    );
};

export default UploadCVForm;