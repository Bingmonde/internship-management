import React from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {
    faXmark
} from "@fortawesome/free-solid-svg-icons";


const ErrorBox = ({msg}) => {
    return <div className="w-full border-l-red border-l-4 border-l-solid p-2 my-2 flex flex-row bg-red/[0.15]">
        <FontAwesomeIcon icon={faXmark}
                         className="pl-2 pr-4 w-8 h-8 self-center pb-0 text-red"/>
        <p className={"font-bold"}>{msg}</p>
    </div>;
}

export default ErrorBox;
