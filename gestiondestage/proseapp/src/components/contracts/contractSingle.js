import ContractBox from "./contractBox";
import {getUserInfo} from "../../utils/userInfo";
import {UserGeneralAPI} from "../../api/userAPI";
import ErrorBox from "../errorBox";
import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";

const ContractSingle = ({notificationIds, refIds, setPdfModal}) => {
    const {t, i18n} = useTranslation();

    const [contract, setContract] = useState(null)
    const [startError, setStartError] = useState()
    const [noContract, setNoContract] = useState(false)

    useEffect(() => {
        fetchContract()
    }, []);

    const fetchContract = async () => {
        try {
            const res = await fetch('http://localhost:8080/internshipOffers/' + refIds, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                }
            });
            const data = await res.json();
            console.log(data);
            setContract(data.value);
        }
        catch (e) {
            setNoContract(true)
            // console.log(e);
        }
    }

    const beginContractProcess = async (applicationId) => {
        setStartError(false)
        const res = await fetch('http://localhost:8080/internshipManager/contracts/begin/' + applicationId.toString(), {
            method: 'POST',
            headers: {
                'Authorization': getUserInfo().token,
            }
        });
        if (res.ok) {
            UserGeneralAPI.markReadNotification([notificationIds]);
            setContract({...contract, contractSignatureDTO:{employer:null,student:null,manager:null}})
        } else {
            setStartError(true)
        }
    }

    const setNewDate = (internshipId,type,newDate) => {
        switch (type) {
            case "student" : {
                setContract({...contract, contractSignatureDTO : {...contract.contractSignatureDTO, student:newDate}})
                break;
            }
            case "employeur" : {
                setContract({...contract, contractSignatureDTO : {...contract.contractSignatureDTO, employer:newDate}})
                break;
            }
            case "projet_manager" : {
                setContract({...contract, contractSignatureDTO : {...contract.contractSignatureDTO, manager:newDate}})
                break;
            }
        }
    }

    return <>
            {startError && <ErrorBox msg={t("error.cannotStart")}></ErrorBox>}
            {noContract && <p>{t("contractList.noContracts")}</p>}
            {contract && <ContractBox setPdfModal={setPdfModal} beginProcess={beginContractProcess} internshipOffer={contract} role={getUserInfo().userType} setNewDate={setNewDate}></ContractBox>}
        </>
}
export default ContractSingle;