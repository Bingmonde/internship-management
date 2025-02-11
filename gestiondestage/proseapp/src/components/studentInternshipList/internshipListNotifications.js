import ValidatedCvDropdown from "./validatedCvDropdown";
import React, {useState} from "react";
import InternshipListSingleNotification from "./internshipListSingleNotification";
import ErrorBox from "../errorBox";
import {useTranslation} from "react-i18next";

const InternshipListNotifications = ({refIds, notificationIds, setPdfModalHelper}) => {

    const [selectedCvId, setSelectedCvId] = useState(''); // Selected CV ID
    const [noValidCV, setNoValidCV] = useState(false);
    const {t} = useTranslation();

    return (
        <div>
            <ValidatedCvDropdown selectedCvId={selectedCvId} setSelectedCvId={setSelectedCvId} setNoValidatedCv={setNoValidCV}></ValidatedCvDropdown>
            {noValidCV && <div className={"px-8"}><ErrorBox msg={t("noCv")}></ErrorBox></div>}
            {!noValidCV && <div className={notificationIds.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"}>
            {
                notificationIds.map((notifid,index) => {
                    return <InternshipListSingleNotification key={notifid} selectedCvId={selectedCvId} refIds={refIds.at(index)} notificationIds={notifid} setPdfModalHelper={setPdfModalHelper} />
                })
            }
            </div>}
        </div>
    )
}
export default InternshipListNotifications