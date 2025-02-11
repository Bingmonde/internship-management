import InterviewListStudentSingleNotification from "./InterviewListStudentSingleNotification";

const InterviewListStudentNotifications = ({ refIds, notificationIds, setPdfModalHelper }) => {
    console.log('refIds:', refIds);

    return (
        <div>
            {
                notificationIds.map((notifid, index) => {
                    return <InterviewListStudentSingleNotification key={notifid}
                                                             refIds={refIds.at(index)}
                                                             notificationIds={notifid}
                                                             setPdfModalHelper={setPdfModalHelper}/>
                })
            }
        </div>
    )
}

export default InterviewListStudentNotifications;