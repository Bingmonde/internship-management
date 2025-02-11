import InternshipListSingleNotification from "../studentInternshipList/internshipListSingleNotification";
import ContractSingle from "./contractSingle";

const ContractNotificationMap = ({notificationIds, refIds, setPdfModal}) => {

    /*<div className={notificationIds.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"}> */
    return (

        <div className={"card-offer-list-full-required"}>
            {
                notificationIds.map((notifid, index) => {
                    return <ContractSingle key={notifid} refIds={refIds.at(index)} notificationIds={notifid}
                                                             setPdfModal={setPdfModal}/>
                })
            }
        </div>
    )
}
export default ContractNotificationMap