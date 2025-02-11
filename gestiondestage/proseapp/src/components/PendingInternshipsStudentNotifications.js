import PendingInternshipsStudentSingleNotification from "./PendingInternshipsStudentSingleNotification";

const PendingInternshipsStudentNotifications = ({refIds, notificationIds, setPdfModalHelper}) => {

    return (
        <div className={notificationIds.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"}>
            {
                notificationIds.map((notifId, index) => {
                    return <PendingInternshipsStudentSingleNotification key={notifId}
                                                                   refIds={refIds.at(index)}
                                                                   notificationIds={notifId}
                                                                   setPdfModalHelper={setPdfModalHelper}/>
                })
            }
        </div>
    )
}

export default PendingInternshipsStudentNotifications;