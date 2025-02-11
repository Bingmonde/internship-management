import React from 'react';
import {faX} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {useTranslation} from "react-i18next";

const Modal = ({ onClose,children}) => {

    const {t} = useTranslation();

    return (
        <div className="modal z-50" onClick={onClose}>
            <div className="modal-content flex flex-col" onClick={(e) => e.stopPropagation()}>
                {/*<div className={"flex items-center justify-between mb-1"}>*/}
                {/*    <button*/}
                {/*        className={"p-2 border-2 border-black rounded-lg bg-selected text-white mx-2 group mr-0 mb-1 md:mr-10"}*/}
                {/*        onClick={onClose}>*/}
                {/*        <FontAwesomeIcon icon={faX}*/}
                {/*                         className="px-4 self-center group-hover:scale-125 transition-transform duration-300 pb-0 "/>*/}
                {/*    </button>*/}
                {/*</div>*/}

                {/*<div className="flex justify-center items-center h-full ">*/}
                {/*    {children}*/}
                {/*</div>*/}
                <button
                    className="p-2 border-2 border-black rounded-lg bg-selected text-white mx-2 mb-2 group mr-0 md:mr-10 w-16"
                    onClick={onClose}
                >
                    <FontAwesomeIcon
                        icon={faX}
                        className="px-4 self-center group-hover:scale-125 transition-transform duration-300 pb-0"
                    />
                </button>

                <div className="flex justify-center items-center h-auto w-full">
                    {children}
                </div>

            </div>
        </div>
    );
};

export default Modal;