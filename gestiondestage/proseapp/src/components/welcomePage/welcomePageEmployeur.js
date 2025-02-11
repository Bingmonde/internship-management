import ListCandidates from "../candidates/listCandidates";
import InterviewListNotifications from "../InterviewListNotifications";
import ContractNotificationMap from "../contracts/contractNotificationMap";
import StudentsListEmployer from "../studentsListEmployer";
import {useTranslation} from "react-i18next";

const WelcomePageEmployeur = ({notification, setPdfModalHelper}) => {

    const {t} = useTranslation();

    const notificationType = notification.key;
    switch (notificationType) {
        case "user_created":
            return (
                <div key={notification.key}>
                    <h3 className="text-2xl text-center mt-5 text-greenish">{t("welcomePageEmployer.accountCreated")}</h3>
                </div>
            )
        case "job_offer_validated":
            return (
                <div key={notification.key}>
                    {notification.value.refIds.length === 1 &&
                        <h5>
                            {notification.value.refIds.length} {t("welcomePageEmployer.jobOfferValidated")}
                        </h5>}
                    {notification.value.refIds.length > 1 &&
                        <h5>
                            {notification.value.refIds.length} {t("welcomePageEmployer.jobOffersValidated")}
                        </h5>}
                    <ListCandidates
                        refIds={notification.value.refIds}
                        notificationIds={notification.value.ids}
                    />
                </div>
            )
        case "new_applicant":
            return (
                <div key={notification.key}>
                    <h5>{t("welcomePageEmployer.statusCandidates")}</h5>
                    <ListCandidates
                        refIds={notification.value.refIds}
                        notificationIds={notification.value.ids}
                    />
                </div>
            )
        case "interview_confirmed":
            return (
                <div key={notification.key}>
                    <h5>{t("welcomePageEmployer.interviewConfirmed")}</h5>
                    <InterviewListNotifications refIds={notification.value.refIds}
                                                notificationIds={notification.value.ids}
                                                setPdfModalHelper={setPdfModalHelper}
                    />
                </div>
            )
        case "internship_offer_accepted":
            // TODO: reorganise components
            return (
                <div key={notification.key}>
                    <h5>{t("welcomePageEmployer.internshipOfferConfirmed")}</h5>
                    <ListCandidates
                        refIds={notification.value.refIds}
                        notificationIds={notification.value.ids}
                    />
                </div>
            )
        case "contract_to_sign":
            return (
                <div key={notification.key}>
                    {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageEmployer.contract")}</h5>}
                    {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageEmployer.contracts")}</h5>}
                    <ContractNotificationMap
                        refIds={notification.value.refIds}
                        notificationIds={notification.value.ids}
                        setPdfModal={setPdfModalHelper}
                    />
                </div>
            )
        case "intern_to_review":
            return (
                <div key={notification.key}>
                    {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageEmployer.internEvaluation")}</h5>}
                    {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageEmployer.internEvaluations")}</h5>}
                    <StudentsListEmployer
                        refIds={notification.value.refIds}
                        notificationIds={notification.value.ids}
                    />
                </div>
            )
        case "contract_signed":
            return (
                <div key={notification.key}>
                    {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageEmployer.contractSigned")}</h5>}
                    {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageEmployer.contractsSigned")}</h5>}
                    <ContractNotificationMap
                        refIds={notification.value.refIds}
                        notificationIds={notification.value.ids}
                        setPdfModal={setPdfModalHelper}
                    />
                </div>
            )
    }

}

export default WelcomePageEmployeur;