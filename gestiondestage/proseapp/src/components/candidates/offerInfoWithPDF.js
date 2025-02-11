import JobOfferDetail from "../jobOfferDetail";
import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {UserGeneralAPI} from "../../api/userAPI";

const OfferInfoWithPDF = ({ offer }) => {
    const {t} = useTranslation();
    const [url, setUrl] = useState(null);

    useEffect(() => {
        const fetchPdf = async () => {
            const url = await UserGeneralAPI.previewPDF(offer.pdfDocu.fileName);
            setUrl(url);
        }
        fetchPdf();
    }, [])

    const download = async (fileName) => {
        const link = document.createElement('a');
        link.href = url
        link.download = fileName
        document.body.appendChild(link);
        link.click();
        link.remove()
    }

    return (
        // <div className="flex lg:flex-row flex-col justify-between">
        //     <div className="lg:w-1/3 md:1/2 w-full border-2 border-darkpurple rounded-2xl mt-2 p-2 text-greenish hover:bg-prose-neutral overflow-y-auto ">
        //         <JobOfferDetail jobOffer={offer}/>
        //     </div>
        //     <div className="lg:w-2/3 md:1/2 w-full m-2">
        //         <div className={"h-full min-h-fit w-full lg:visible invisible"}>
        //             <iframe src={url} className={"w-full h-full rounded-lg"}
        //                     title="PDF Preview"></iframe>
        //         </div>
        //         <div className="text-center me-2">
        //             <p className="lg:invisible visible text-red">
        //                 {t("PDFPreviewInvisibleInSmallScreen")}
        //             </p>
        //             <button
        //                 className={"p-2 border-2 border-black rounded-lg bg-greenish text-white mx-2 mb-5 group"}
        //                 onClick={() => download(offer.pdfDocu.fileName)
        //                 }><p
        //                 className={"font-bold group-hover:scale-105 transition-transform duration-300"}>{t("Download PDF")}</p>
        //             </button>
        //         </div>
        //     </div>
        // </div>
        

        <div className="flex lg:flex-row flex-col justify-between w-full h-screen">
            <div
                className="lg:w-1/3 w-full h-full border-2 border-darkpurple rounded-2xl mt-2 p-2 text-greenish hover:bg-prose-neutral">
                <JobOfferDetail jobOffer={offer}/>
            </div>
            <div className="lg:w-2/3 w-full h-full m-2 flex flex-col">
                <div className="flex-grow w-full">
                    <iframe src={url} className="w-full h-full rounded-lg" title="PDF Preview"></iframe>
                </div>
                <div className="text-center me-2">
                    <button
                        className="p-2 border-2 border-black rounded-lg bg-greenish text-white mx-2 m-2 group"
                        onClick={() => download(offer.pdfDocu.fileName)}
                    >
                        <p className="font-bold group-hover:scale-105 transition-transform duration-300">
                            {t("Download PDF")}
                        </p>
                    </button>
                </div>
            </div>
        </div>


    )
}

export default OfferInfoWithPDF;