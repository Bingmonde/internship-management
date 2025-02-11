import StudentCvNotification from "../studentCVs/studentCvNotification";
import InternshipListNotifications from "../studentInternshipList/internshipListNotifications";
import InterviewListStudentNotifications from "../InterviewListStudentNotifications";
import PendingInternshipsStudentNotifications from "../PendingInternshipsStudentNotifications";
import ContractNotificationMap from "../contracts/contractNotificationMap";
import {useTranslation} from "react-i18next";

const WelcomePageStudent = ({notification, setPdfModalHelper}) => {

    const {t} = useTranslation();

    const notificationType = notification.key;
    switch (notificationType) {
        case "user_created":
            return (
                <div key={notification.key}>
                    <h3 className="text-2xl text-center mt-5 text-greenish">{t("welcomePageStudent.accountCreated")}</h3>
                </div>
            )
        case "cv_validated":
            return (
                <div key={notification.key}>
                    <h5>{t("welcomePageStudent.cvValidated")}</h5>
                    <div className={notification.value.ids.length < 2 ? "card-offer-list-single-child" : "card-offer-list mt-5"}>
                        {
                            notification.value.ids.map((notifId,index) => {
                                console.dir(notification)
                                return <StudentCvNotification refIds={notification.value.refIds.at(index)} notificationIds={notifId} setPdfModalHelper={setPdfModalHelper} />
                            })
                        }
                    </div>
                </div>
            )
        case "new_job_offer":
            return <div key={notification.key}>
                {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.jobOffer")}</h5>}
                {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.jobOffers")}</h5>}
                <InternshipListNotifications notificationIds={notification.value.ids} refIds={notification.value.refIds} setPdfModalHelper={setPdfModalHelper}></InternshipListNotifications>
            </div>
        case "new_interview":
            return <div key={notification.key}>
                {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.interview")}</h5>}
                {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.interviews")}</h5>}
                <InterviewListStudentNotifications refIds={notification.value.refIds} notificationIds={notification.value.ids} setPdfModalHelper={setPdfModalHelper}/>
            </div>
        case "internship_offer_received":
            return <div key={notification.key}>
                {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.internship")}</h5>}
                {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.internships")}</h5>}
                <PendingInternshipsStudentNotifications refIds={notification.value.refIds} notificationIds={notification.value.ids} setPdfModalHelper={setPdfModalHelper}/>
            </div>
        case "contract_to_sign":
            return <div key={notification.key}>
                    {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.contract")}</h5>}
                    {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.contracts")}</h5>}
                    <ContractNotificationMap refIds={notification.value.refIds} notificationIds={notification.value.ids} setPdfModal={setPdfModalHelper}></ContractNotificationMap>
            </div>
        case "contract_signed":
            return <div key={notification.key}>
                {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.contractSigned")}</h5>}
                {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageStudent.contractsSigned")}</h5>}
                <ContractNotificationMap refIds={notification.value.refIds} notificationIds={notification.value.ids} setPdfModal={setPdfModalHelper}></ContractNotificationMap>
            </div>
    }
};

export default WelcomePageStudent;