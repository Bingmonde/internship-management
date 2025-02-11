import React, {useContext, useEffect, useState} from 'react';
import {getUserInfo} from "../../utils/userInfo";
import ContractBox from "./contractBox";
import {useTranslation} from "react-i18next";
import ErrorBox from "../errorBox";
import {useOutletContext} from "react-router-dom";
import {useSession} from "../CurrentSession";
import PaginationComponent from "../pagination/paginationComponent";
import {UserGeneralAPI} from "../../api/userAPI";
import InfoBox from "../infoBox";

const ContractList = ({refIds, notificationIds}) => {

    const [setPdfModalHelper] = useOutletContext()
    const [contracts, setContracts] = useState([])
    const {t, i18n} = useTranslation();
    const [startError, setStartError] = useState(false)
    const {currentSession} = useSession();

    const [currentPageIndex, setCurrentPageIndex] = useState(0);
    const [totalPagesFromBackend, setTotalPagesFromBackend] = useState(0);


    const getContractOffers = async () => {
        try {
            const res = await fetch('http://localhost:8080/contracts/?' + (new URLSearchParams({
                "season": currentSession.season, "year": currentSession.year,
                "page": currentPageIndex, "size": 3
            })), {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                }
            });
            const data = await res.json();
            console.log(data);

            if (refIds){
                setContracts(data.value.filter(contract => refIds.includes(contract.id)))
            }
            else
                setContracts(data.value);
        }
        catch (e) {
            setContracts([]);
            // console.log(e);
        }
    }

    useEffect(() => {
        if (!currentSession.id) return;
        getContractOffers();
    }, [currentPageIndex]);

    const beginContractProcess = async (applicationId) => {
        setStartError(false)
        const res = await fetch('http://localhost:8080/internshipManager/contracts/begin/' + applicationId.toString(), {
            method: 'POST',
            headers: {
                'Authorization': getUserInfo().token,
            }
        });
        if (res.ok) {
            setContracts(contracts.map(value => {
                if (value.id === applicationId) {

                    // notification index and mark as read
                    if (refIds) {
                        const notificationIndex = refIds.indexOf(value.id);
                        UserGeneralAPI.markReadNotification([notificationIds[notificationIndex]]);
                    }

                    return {...value, contractSignatureDTO:{employer:null,student:null,manager:null}}
                } else {
                    return value;
                }
            }))
        } else {
            setStartError(true)
        }
    }

    const setNewDate = (internshipId,type,newDate) => {
        setContracts(contracts.map(value => {
            if (value.id === internshipId) {
                switch (type) {
                    case "student" : {
                        return {...value, contractSignatureDTO : {...value.contractSignatureDTO, student:newDate}}
                    }
                    case "employeur" : {
                        return {...value, contractSignatureDTO : {...value.contractSignatureDTO, employer:newDate}}
                    }
                    case "projet_manager" : {
                        return {...value, contractSignatureDTO : {...value.contractSignatureDTO, manager:newDate}}
                    }
                }

            }
            return value
        }))
    }

    useEffect(() => {
        if (!currentSession.id) return;
        getContractOffers()
    }, [currentSession]);

    return (<div className={"w-full"}>
        <h1 className={"text-2xl font-bold text-center py-5"}>{t("contractList.title")}</h1>
            {startError && <ErrorBox msg={t("error.cannotStart")}></ErrorBox>}
        {contracts.length === 0 &&
            <InfoBox msg={t('contractList.noContracts') }></InfoBox>}
        {contracts.map(internshipOffer => {
            return (
                <div key={internshipOffer.id}><ContractBox setNewDate={setNewDate} setPdfModal={setPdfModalHelper} role={getUserInfo().userType} internshipOffer={internshipOffer} beginProcess={beginContractProcess}></ContractBox></div>
            )
        })}
        {/*pagination widget*/}
        <div className="flex justify-center mt-2">
            <PaginationComponent totalPages={totalPagesFromBackend} paginate={(pageNumber) => {
                setCurrentPageIndex(pageNumber - 1); // page index starts at zero in backend, not the same as page "number"
            }}/>
        </div>
    </div>);
}

export default ContractList;