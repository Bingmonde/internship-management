import ContractNotificationMap from "../contracts/contractNotificationMap";
import StudentsListTeacher from "../studentsListTeacher";
import {useTranslation} from "react-i18next";

const WelcomePageTeacher = ({notification, setPdfModalHelper}) => {

    const {t} = useTranslation();

    const notificationType = notification.key;
    switch (notificationType) {
        case "user_created":
            return (
                <div key={notification.key}>
                    <h3 className="text-2xl text-center mt-5 text-greenish">{t("welcomePageStudent.accountCreated")}</h3>
                </div>
            )
        case "contract_to_sign":
            return (
                <div key={notification.key}>
                    <h5>There are {notification.value.ids.length} contract(s) need your attention.</h5>
                    {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageTeacher.contractToSign")}</h5>}
                    {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageTeacher.contractsToSign")}</h5>}
                    <ContractNotificationMap
                        refIds={notification.value.refIds}
                        notificationIds={notification.value.ids}
                        setPdfModal={setPdfModalHelper}/>
                </div>
            )
        case "intern_assignment":
            return (
                <div key={notification.key}>
                    {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageTeacher.internAssignment")}</h5>}
                    {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageTeacher.internAssignments")}</h5>}
                    <StudentsListTeacher refIds={notification.value.refIds}
                                         notificationIds={notification.value.ids}/>
                </div>
            )
        case "internship_environment_to_review":
            return (
                <div key={notification.key}>
                    {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageTeacher.internshipEvaluation")}</h5>}
                    {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageTeacher.internshipEvaluations")}</h5>}
                    <StudentsListTeacher refIds={notification.value.refIds}
                                         notificationIds={notification.value.ids}/>
                </div>
            )

    }

}

export default WelcomePageTeacher;