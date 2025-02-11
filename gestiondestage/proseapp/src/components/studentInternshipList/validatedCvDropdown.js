import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {getUserInfo} from "../../utils/userInfo";
import ErrorBox from "../errorBox";

const ValidatedCvDropdown = ({selectedCvId, setSelectedCvId, setNoValidatedCv}) => {
    const { t } = useTranslation();
    const [cvList, setCvList] = useState([]); // List of validated CVs

    const [errorMessage, setErrorMessage] = useState("")

    useEffect(() => {
        fetchValidatedCVs();
    }, []);

    const fetchValidatedCVs = async () => {
        const token = getUserInfo().token; // Assume token management is in place
        try {
            const response = await fetch('http://localhost:8080/students/CV/validated', {
                headers: { 'Authorization': token }
            });
            const data = await response.json();

            if (response.ok) {
                console.log('CV data:', data.value); // Add log
                if (data.value.length === 0) {
                    setNoValidatedCv(true)
                }
                setCvList(data.value); // Assume the server returns an array of CVs
                setSelectedCvId(data.value.length > 0 ? data.value[0].id : ''); // Automatically select the first CV if available
            } else {
                throw new Error(data.message);
            }
        } catch (error) {
            setErrorMessage(t('Unable to fetch CVs: ') + error.message);
        }
    };

    return (
        <div className="w-1/2 mx-auto">
            <h2>{t('Select a Validated CV')}</h2>
            <select value={selectedCvId} onChange={e => setSelectedCvId(e.target.value)}>
                <option value="">{t('Select your CV')}</option>
                {cvList.map((cv, index) => (
                    <option key={cv.id} value={cv.id}>
                        {cv.name || `${t('CV')} ${index + 1}`}
                    </option>
                ))}
            </select>
            {errorMessage.trim().length !== 0 && <ErrorBox msg={errorMessage}></ErrorBox>}
        </div>
    )
}

export default ValidatedCvDropdown;