// src/components/NotificationBox.js

import { useTranslation } from "react-i18next";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCheck, faTimes, faInfoCircle, faExclamationTriangle } from "@fortawesome/free-solid-svg-icons";
import PropTypes from "prop-types";
import React from "react";

const NotificationBox = ({ notif, remove }) => {
    const { t } = useTranslation();

    const getNotificationDetails = (type) => {
        switch (type) {
            case "success":
                return { icon: faCheck, bgColor: "bg-green-500", textColor: "text-white" };
            case "error":
                return { icon: faTimes, bgColor: "bg-red-500", textColor: "text-white" };
            case "info":
                return { icon: faInfoCircle, bgColor: "bg-blue-500", textColor: "text-white" };
            case "warning":
                return { icon: faExclamationTriangle, bgColor: "bg-yellow-500", textColor: "text-black" };
            default:
                return { icon: faInfoCircle, bgColor: "bg-gray-500", textColor: "text-white" };
        }
    };

    const { icon, bgColor, textColor } = getNotificationDetails(notif.type);

    return (
        <div
            className={`fullpage-column text-left mb-5 ${bgColor} ${textColor} font-bold p-3 rounded-xl border-2 border-black border-solid`}>
            <div className="flex justify-between items-center">
                <div className="flex items-center">
                    <FontAwesomeIcon icon={icon} className="mr-2" />
                    <p>
                        {notif.amount > 1 ? `(${notif.amount}) ` : ""}
                        {t(notif.type, { notifValue: notif.extraInfo })}
                    </p>
                </div>
                <FontAwesomeIcon
                    icon={faTimes}
                    className="px-4 self-center hover:scale-125 transition-transform duration-300 pb-0 cursor-pointer"
                    onClick={remove}
                />
            </div>
        </div>
    );
};

NotificationBox.propTypes = {
    notif: PropTypes.shape({
        amount: PropTypes.number,
        type: PropTypes.string.isRequired,
        extraInfo: PropTypes.string,
    }).isRequired,
    remove: PropTypes.func.isRequired,
};

export default NotificationBox;
