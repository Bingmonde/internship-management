import ListCV from "../listCV";
import ValidateOffer from "../validateOffer";
import ContractNotificationMap from "../contracts/contractNotificationMap";
import AssignToProf from "../assignToProf";
import {useTranslation} from "react-i18next";

const WelcomePageProjetManager = ({notification, setPdfModalHelper}) => {

    const {t} = useTranslation();

    const notificationType = notification.key;
    switch (notificationType) {
        case "user_created":
            return <div key={notification.key}>
                <h3 className="text-2xl text-center mt-5 text-greenish">{t("welcomePageProjectManager.accountCreated")}</h3>
            </div>
        case "cv_validation_required":
            return <div key={notification.key}>
                {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.cvValidation")}</h5>}
                {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.cvValidations")}</h5>}
                <ListCV refIds={notification.value.refIds} notificationIds={notification.value.ids}/>
            </div>
        case "offer_validation_required":
            return <div key={notification.key}>
                {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.jobOfferValidation")}</h5>}
                {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.jobOffersValidation")}</h5>}
                <ValidateOffer refIds={notification.value.refIds} notificationIds={notification.value.ids}/>
            </div>
        case "contract_to_start":
            return <div key={notification.key}>
                {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.contractStart")}</h5>}
                {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.contractsStart")}</h5>}
                <ContractNotificationMap refIds={notification.value.refIds} notificationIds={notification.value.ids} setPdfModal={setPdfModalHelper}></ContractNotificationMap>
            </div>
        case "contract_to_sign":
            return <div key={notification.key}>
                {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.contractSign")}</h5>}
                {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.contractsSign")}</h5>}
                <ContractNotificationMap refIds={notification.value.refIds} notificationIds={notification.value.ids} setPdfModal={setPdfModalHelper}></ContractNotificationMap></div>
        case "intern_require_assignement":
            return <div key={notification.key}>
                {notification.value.refIds.length === 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.contractSigned")}</h5>}
                {notification.value.refIds.length > 1 && <h5>{notification.value.refIds.length} {t("welcomePageProjectManager.contractsSigned")}</h5>}
                <AssignToProf refIds={notification.value.refIds} notificationIds={notification.value.ids}/>
            </div>

    }

}

export default WelcomePageProjetManager;