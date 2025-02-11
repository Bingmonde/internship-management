import {faCheck, faHourglassHalf, faXmark} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import React from "react";

export const WaitingStatusIcon = () => {
    return (
        <FontAwesomeIcon
            icon={faHourglassHalf}
            from={"fas"}
            size={"2xl"}
            className=" text-left text-yellow-400 p-2 rounded-xl w-7 h-7"/>
    )
}

export const ConfirmedStatusIcon = () => {
    return (
        <FontAwesomeIcon
            icon={faCheck}
            from={"fas"}
            size={"2xl"}
            className="text-left text-greenish p-2 rounded-xl w-8 h-8"/>
    )
}

export const CancelledStatusIcon = () => {
    return (
        <FontAwesomeIcon icon={faXmark} from={"fas"}
                         size={"2xl"}
                         className=" text-left text-red p-2 rounded-xl w-8 h-8"/>
    )
}
