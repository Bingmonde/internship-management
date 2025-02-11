import {useTranslation} from "react-i18next";
import React, {useState} from "react";
import {faRightFromBracket, faX} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";


const PdfPreview = ({pdfModal, setPdfModal}) => {
    const {t, i18n} = useTranslation();

    const close = () => {
        URL.revokeObjectURL(pdfModal.pdfUrl)
        setPdfModal({pdfUrl:pdfModal.pdfUrl,filename:pdfModal.filename,showModal:false})
    }

    const download = () => {
        const link = document.createElement('a');
        link.href = pdfModal.pdfUrl;
        link.download = pdfModal.filename;
        document.body.appendChild(link); //TODO : Ceci marche seulement pour les liens sur la même origine et les blobs... Heureusement qu'on a un blob ;)
        link.click();
        link.remove()
    }

    return (
        <div className={"fixed h-screen w-screen bg-black/50 z-30 py-11"} onClick={close}>
            <div className={"form-border border-box h-full my-0 mx-5 p-2"} onClick={(event) => event.stopPropagation()}>
                <div className={"h-full pb-12"}>
                    <div className={"flex items-center justify-between mb-1"}>
                        <button className={"p-2 border-2 border-black rounded-lg bg-selected text-white mx-2 group mr-0 md:mr-10"}
                                onClick={close}>
                            <FontAwesomeIcon icon={faX}
                                             className="px-4 self-center group-hover:scale-125 transition-transform duration-300 pb-0 "/>
                        </button>
                        <span className={"font-bold truncate"}>{pdfModal.filename}</span>
                        <a href={pdfModal.pdfUrl} download={pdfModal.filename} className={"p-2 border-2 border-black rounded-lg bg-greenish text-white mx-2 group"}>
                            <p className={"font-bold group-hover:scale-105 transition-transform duration-300"}>{t("DownloadGeneric")}</p>
                        </a>
                    </div>
                    <div className={"h-full grow min-h-0"}>
                        <iframe src={pdfModal.pdfUrl} className={"w-full h-full rounded-lg"}
                                title="PDF Preview"></iframe>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default PdfPreview