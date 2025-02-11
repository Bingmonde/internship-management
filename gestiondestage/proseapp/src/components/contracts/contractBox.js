import React, {useRef, useState} from 'react';
import {useTranslation} from "react-i18next";
import {getUserInfo} from "../../utils/userInfo";
import ErrorBox from "../errorBox";
import SuccessBox from "../successBox";
import {format} from "date-fns";
import {faCaretDown} from "@fortawesome/free-solid-svg-icons/faCaretDown";
import {faCaretRight} from "@fortawesome/free-solid-svg-icons/faCaretRight";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {SignatureMaker} from "@docuseal/signature-maker-react";
import {ErrorBoundary} from "react-error-boundary";
import SignaturePad from "react-signature-pad-wrapper";
import SignaturePadWrapper from "../signature/signaturePadWrapper";
import {PdfPrintService} from "../../api/pdfPrintService";
import {useOutletContext} from "react-router-dom";
import {generateContractPdfName} from "../../api/utils";
import {FormProvider, useForm} from "react-hook-form";
import Input from "../input";
import {password_validation} from "../../utils/forms/formValidation";
import {faChevronDown, faChevronRight, faFilePdf, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission} from "../../constants";

const ContractBox = ({role, internshipOffer, beginProcess, setPdfModal, setNewDate}) => {
    const [shown, setShown] = useState(false)

    const [previewErrorMessage, setPreviewErrorMessage] = useState("")
    const [previewError, setPreviewError] = useState(false)

    const [signErrorMessage, setSignErrorMessage] = useState("")
    const [signError, setSignError] = useState(false)

    const [allowSign, setAllowSign] = useState(false)

    const {t, i18n} = useTranslation();

    const password = useRef("");

    const [setPdfModalHelper] = useOutletContext()

    const methods = useForm();

    const [passwordVisible, setPasswordVisible] = useState(false);

    const [userDetails, setUserDetails] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () => setIsModalOpen(true);
    const closeModal = () => {
        setIsModalOpen(false);
        setUserDetails(null)
    };

    const toggleShown = () => {
        if (internshipOffer.contractSignatureDTO != null) {
            setShown(!shown)
        }
    }

    const signaturePadRef = useRef(null);

    const formatDate = (date) => {
        return format(new Date(date), 'yyyy-MM-dd HH:mm')
    }

    const formatPhone = (numberRaw) => {
        let numberFormat = "(";
        numberFormat += numberRaw.substring(0, 3)
        numberFormat += ") "
        numberFormat += numberRaw.substring(3, 6)
        numberFormat += "-"
        numberFormat += numberRaw.substring(6)
        return numberFormat
    }

    const clear = () => {
        signaturePadRef.current.clear()
        setAllowSign(false)
    }

    const sign = async () => {
        setSignError(false)
        console.dir(signaturePadRef)
        const base64Canvas = signaturePadRef.current.toDataURL("image/png").split(';base64,')[1];
        console.log(base64Canvas)
        try {
            const res = await fetch(`http://localhost:8080/contracts/sign/` + internshipOffer.id.toString(), {
                method: 'POST',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-TYpe': "application/json"
                },
                body: JSON.stringify({signature: base64Canvas, extraData: password.current})
            });
            const jsonResponse = await res.json(); // Si erreur network, va dans catch lol
            if (res.ok) {
                setNewDate(internshipOffer.id, role.toLowerCase(), jsonResponse.value)
            } else {
                if (jsonResponse.exception === "MissingPermissions") {
                    setSignError(true);
                    setSignErrorMessage(t('contract.error'));
                } else if (jsonResponse.exception === "Password") {
                    setSignError(true)
                    setSignErrorMessage(t("contract.password"))
                }
            }
        } catch (error) {
            console.error(error);
            setSignError(true);
            setSignErrorMessage(t('error.network'));
        }
    }

    const handlePreview = async (fileName) => {
        try {
            const res = await fetch(`http://localhost:8080/download/file/${fileName}`, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                }
            });

            if (res.ok) {
                const jsonResponse = await res.json();
                const base64String = jsonResponse.value;

                const byteCharacters = atob(base64String);
                const byteNumbers = new Array(byteCharacters.length);
                for (let i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                const byteArray = new Uint8Array(byteNumbers);
                const blob = new Blob([byteArray], {type: 'application/pdf'});

                const url = URL.createObjectURL(blob);
                setPdfModal(url, fileName)
                //setPreviewUrl(url);
            } else {
                setPreviewError(true);
                setPreviewErrorMessage(t('error.fetchFailed'));
            }
        } catch (error) {
            console.error(error);
            setPreviewError(true);
            setPreviewErrorMessage(t('error.network'));
        }
    };

    const isAllSigned = () => {
        let contract = internshipOffer.contractSignatureDTO
        if (contract == null) return false
        return (contract.employer != null && contract.student != null && contract.manager != null)
    }

    const isCurrentSigned = () => {
        let contract = internshipOffer.contractSignatureDTO
        if (contract == null) {
            return false
        }

        switch (role.toLowerCase()) {
            case "student" : {
                return contract.student != null;
            }
            case "employeur" : {
                return contract.employer != null;
            }
            case "projet_manager" : {
                return contract.manager != null
            }
        }
    }

    const printContractPDF = async () => {
        const res = await PdfPrintService.printContact(internshipOffer.id);
        if (res.value != null) {
            const byteCharacters = atob(res.value);
            const byteNumbers = new Array(byteCharacters.length);
            for (let i = 0; i < byteCharacters.length; i++) {
                byteNumbers[i] = byteCharacters.charCodeAt(i);
            }
            const byteArray = new Uint8Array(byteNumbers);

            const blob = new Blob([byteArray], {type: 'application/pdf'});
            const url = window.URL.createObjectURL(blob);
            const fileName = generateContractPdfName(internshipOffer);
            setPdfModalHelper(url, fileName);

        } else
            console.log('error from printPDF', res.exception)
    }

    const getStudentName = (studentDTO) => {
        return studentDTO.prenom + " " + studentDTO.nom
    }

    const defaultContractBoxName = (internshipOffer) => {
        let studentDTO = internshipOffer.jobOfferApplicationDTO.CV.studentDTO
        let jobOffer = internshipOffer.jobOfferApplicationDTO.jobOffer;

        return ((getUserInfo().userType.toLowerCase() !== "employeur") ? jobOffer.employeurDTO.nomCompagnie + " - " : "") + jobOffer.titre + " - " + getStudentName(studentDTO)
    }

    const handleConsultCompanyDetail = (userDTO) => {
        setUserDetails(userDTO)
        openModal()
    }

    const togglePasswordVisibility = () => {
        setPasswordVisible(!passwordVisible);
    };


    console.dir(internshipOffer)
    return (
        <div className="xl:w-1/2 w-full mx-auto p-2">
            <div className="grid grid-cols-2 border-b-2 border-darkpurple justify-items-stretch p-2 cursor-pointer"
                 onClick={() => toggleShown()}>
                <h3 className="text-start text-xl text-darkpurple">{defaultContractBoxName(internshipOffer)}</h3>
                {
                    internshipOffer.contractSignatureDTO == null && role === "PROJET_MANAGER" ?
                        <button className={"button-in-card-neutral-small"}
                                onClick={() => beginProcess(internshipOffer.id)}>
                            {t("contract.actions.begin")}
                        </button>
                        :
                        <div className={"flex flex-row items-center justify-end"}>
                            {isCurrentSigned() ? <span></span> :
                                <span className={"mr-1"}>{t("contract.waitingSign")}</span>}
                            {isAllSigned() ? <span>{t("contract.allSigned")}</span> : <span></span>}
                            <div
                                className="justify-self-end self-center w-1/6 text-end p-2">
                                {shown ?
                                    <FontAwesomeIcon icon={faCaretDown}/> : // Down arrow
                                    <FontAwesomeIcon icon={faCaretRight}/> // Right arrow
                                }
                            </div>
                        </div>}
            </div>
            {shown && <div className={"flex flex-col bg-prose-neutral rounded-b p-2"}>
                <div className={"flex flex-col sm:flex-row"}>
                    <div className={"grow basis-0 px-2"}>
                        <h2 className={"text-xl font-bold text-center py-5"}>Offre de stage</h2>
                        <div className='flex flex-row px-1 items-center'>
                            <p className={"pr-2"}>{t("contract.employerInfo.employer")}</p>
                            <div className="flex flex-row justify-center items-center hover:scale-110 "
                                 onClick={() => handleConsultCompanyDetail(internshipOffer.jobOfferApplicationDTO.jobOffer.employeurDTO)}>
                                <p className="cursor-pointer text-darkpurple">{internshipOffer.jobOfferApplicationDTO.jobOffer.employeurDTO.nomCompagnie}</p>
                                <FontAwesomeIcon icon={faInfoCircle}
                                                 className="profile-section-title ms-2  py-2"/>
                            </div>
                        </div>
                        <div className='flex flex-row px-1 items-center'>
                            <p className={"pr-2"}>{t("contract.employerInfo.offer")}</p>
                            <div className="flex flex-row justify-center items-center hover:scale-110 "
                                 onClick={() => handlePreview(internshipOffer.jobOfferApplicationDTO.jobOffer.pdfDocu.fileName)}>
                                <p className="cursor-pointer text-darkpurple"
                                >{internshipOffer.jobOfferApplicationDTO.jobOffer.titre}</p>
                                <FontAwesomeIcon icon={faFilePdf}
                                                 className="profile-section-title ms-2 py-2"/>
                            </div>
                        </div>
                    </div>
                    <div className={"grow basis-0 px-2"}>
                        <h2 className={"text-xl font-bold text-center py-5"}>Étudiant</h2>
                        <div className='flex flex-row px-1 items-center'>
                            <p className={"pr-2"}>{t("contract.studentInfo.student")}</p>
                            <div className="flex flex-row justify-center items-center hover:scale-110 "
                                 onClick={() => handleConsultCompanyDetail(internshipOffer.jobOfferApplicationDTO.CV.studentDTO)}>
                                <p className="cursor-pointer text-darkpurple">{getStudentName(internshipOffer.jobOfferApplicationDTO.CV.studentDTO)}</p>
                                <FontAwesomeIcon icon={faInfoCircle}
                                                 className="profile-section-title ms-2  py-2"/>
                            </div>
                        </div>
                        <div className='flex flex-row px-1 items-center'>
                            <div className="flex flex-row justify-center items-center hover:scale-110 "
                                 onClick={() => handlePreview(internshipOffer.jobOfferApplicationDTO.CV.pdfDocu.fileName)}>
                                <p className="cursor-pointer text-darkpurple"
                                >{t("contract.studentInfo.cvButton")}</p>
                                <FontAwesomeIcon icon={faFilePdf}
                                                 className="profile-section-title ms-2 py-2"/>
                            </div>

                        </div>
                    </div>
                </div>
                {previewError && <ErrorBox msg={previewErrorMessage}></ErrorBox>}
                <div className={"flex flex-col"}>
                    <div>
                        <h2 className={"text-xl font-bold text-center pt-5"}>{t("contract.studentTerms.title")}</h2>
                        <ul className={"list-disc pl-5"}>
                            {t("contract.studentTerms.terms", {returnObjects: true}).map((item, index) => {
                                return (
                                    <li key={index}>{item}</li>
                                )
                            })}
                        </ul>
                        {role.toLowerCase() !== "student" &&
                            ((internshipOffer.contractSignatureDTO.student == null) ?
                                <ErrorBox msg={t("contract.studentTerms.notSigned")}/> : <SuccessBox
                                    msg={t("contract.signed", {date: formatDate(internshipOffer.contractSignatureDTO.student)})}/>)
                        }
                        {role.toLowerCase() === "student" &&
                            ((internshipOffer.contractSignatureDTO.student == null) ?
                                <ErrorBoundary fallback={<div/>}>
                                    <h3 className={"font-bold text-md"}>Signature : </h3>
                                    <div className={"overflow-x-auto"}>
                                        <SignaturePadWrapper ref={signaturePadRef}
                                                             isEmptyState={setAllowSign}></SignaturePadWrapper>
                                    </div>
                                    <button className={"button-in-card mt-2 mb-1"}
                                            onClick={clear}>{t("signatureMaker.clearButtonText")}</button>
                                    <FormProvider {...methods}>
                                        <Input {...password_validation}
                                               onChange={(e) => password.current = e.target.value}
                                               type={passwordVisible ? "text" : "password"}
                                               togglePasswordVisibility={togglePasswordVisibility}
                                        />
                                    </FormProvider>
                                    <button className={"button-in-card-neutral w-full"}
                                            onClick={sign} disabled={!allowSign}>{t("contract.actions.sign")}</button>
                                    {signError && <ErrorBox msg={signErrorMessage}></ErrorBox>}
                                </ErrorBoundary> :
                                <SuccessBox
                                    msg={t("contract.signed", {date: formatDate(internshipOffer.contractSignatureDTO.student)})}/>)
                        }
                    </div>
                    <div>
                        <h2 className={"text-xl font-bold text-center pt-5"}>{t("contract.employerTerms.title")}</h2>
                        <ul className={"list-disc pl-5"}>
                            {t("contract.employerTerms.terms", {returnObjects: true}).map((item, index) => {
                                return (
                                    <li key={index}>{item}</li>
                                )
                            })}
                        </ul>
                        {role.toLowerCase() !== "employeur" &&
                            ((internshipOffer.contractSignatureDTO.employer == null) ?
                                <ErrorBox msg={t("contract.employerTerms.notSigned")}/> : <SuccessBox
                                    msg={t("contract.signed", {date: formatDate(internshipOffer.contractSignatureDTO.employer)})}/>)
                        }
                        {role.toLowerCase() === "employeur" &&
                            ((internshipOffer.contractSignatureDTO.employer == null) ?
                                <ErrorBoundary fallback={<div/>}>
                                    <h3 className={"font-bold text-md"}>Signature : </h3>
                                    <div className={"overflow-x-auto"}>
                                        <SignaturePadWrapper ref={signaturePadRef}
                                                             isEmptyState={setAllowSign}></SignaturePadWrapper>
                                    </div>
                                    <button className={"button-in-card mt-2 mb-1"}
                                            onClick={clear}>{t("signatureMaker.clearButtonText")}</button>
                                    <FormProvider {...methods}>
                                        <Input {...password_validation}
                                               onChange={(e) => password.current = e.target.value}
                                               type={passwordVisible ? "text" : "password"}
                                               togglePasswordVisibility={togglePasswordVisibility}
                                        />
                                    </FormProvider>
                                    <button className={"button-in-card-neutral w-full"}
                                            onClick={sign} disabled={!allowSign}>{t("contract.actions.sign")}</button>
                                    {signError && <ErrorBox msg={signErrorMessage}></ErrorBox>}
                                </ErrorBoundary> :
                                <SuccessBox
                                    msg={t("contract.signed", {date: formatDate(internshipOffer.contractSignatureDTO.employer)})}/>)
                        }
                    </div>
                    <div>
                        <h2 className={"text-xl font-bold text-center pt-5"}>{t("contract.schoolTerms.title")}</h2>
                        <ul className={"list-disc pl-5"}>
                            {t("contract.schoolTerms.terms", {returnObjects: true}).map((item, index) => {
                                return (
                                    <li key={index}>{item}</li>
                                )
                            })}
                        </ul>
                        {role.toLowerCase() !== "projet_manager" &&
                            ((internshipOffer.contractSignatureDTO.manager == null) ?
                                <ErrorBox msg={t("contract.schoolTerms.notSigned")}/> : <SuccessBox
                                    msg={t("contract.signed", {date: formatDate(internshipOffer.contractSignatureDTO.manager)})}/>)
                        }
                        {role.toLowerCase() === "projet_manager" &&
                            ((internshipOffer.contractSignatureDTO.manager == null) ?
                                <ErrorBoundary fallback={<div/>}>
                                    <h3 className={"font-bold text-md"}>Signature : </h3>
                                    <div className={"overflow-x-auto"}>
                                        <SignaturePadWrapper ref={signaturePadRef}
                                                             isEmptyState={setAllowSign}></SignaturePadWrapper>
                                    </div>
                                    <button className={"button-in-card mt-2 mb-1"}
                                            onClick={clear}>{t("signatureMaker.clearButtonText")}</button>
                                    <FormProvider {...methods}>
                                        <Input {...password_validation}
                                               onChange={(e) => password.current = e.target.value}
                                               type={passwordVisible ? "text" : "password"}
                                               togglePasswordVisibility={togglePasswordVisibility}
                                        />
                                    </FormProvider>
                                    <button className={"button-in-card-neutral w-full"}
                                            onClick={sign} disabled={!allowSign}>{t("contract.actions.sign")}</button>
                                    {signError && <ErrorBox msg={signErrorMessage}></ErrorBox>}
                                </ErrorBoundary> :
                                <SuccessBox
                                    msg={t("contract.signed", {date: formatDate(internshipOffer.contractSignatureDTO.manager)})}/>)
                        }
                    </div>
                    {(isAllSigned()) && <button onClick={printContractPDF}
                                                className="button-in-card-neutral w-full mt-5"
                    >{t('printpdf.printContract')}
                    </button>}
                </div>
            </div>}
            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {userDetails && <ProfilePreview permission={Permission.Full} profile={userDetails}/>}
                </Modal>
            }
        </div>)
}

export default ContractBox;