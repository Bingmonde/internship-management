import InterviewListSingeNotification from "./InterviewListSingeNotification";

const InterviewListNotifications = ({ refIds, notificationIds, setPdfModalHelper }) => {

    return (
        <div>
            {
                notificationIds.map((notifId, index) => {
                    return <InterviewListSingeNotification key={notifId}
                                                        refIds={refIds.at(index)}
                                                        notificationIds={notifId}
                                                        setPdfModalHelper={setPdfModalHelper}/>
                })
            }
        </div>
    )

}

export default InterviewListNotifications;