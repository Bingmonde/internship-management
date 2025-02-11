import React, {useEffect, useState} from "react";
import {InternshipManagerAPI} from "../api/programManagerAPI";
import {getEmployerNameFromPdfDocu, getStudentName, getStudentNameFromPdfDocu} from "../api/utils";
import SuccessBox from "./successBox";
import ErrorBox from "./errorBox";
import {Trans, useTranslation} from "react-i18next";
import {getUserInfo} from "../utils/userInfo";
import SuccessBoxNoTrans from "./successBoxNoTrans";
import Modal from "./modal";
import ProfilePreview from "./userProfile/profilePreview";
import {Permission} from "../constants";
import {UserGeneralAPI} from "../api/userAPI";
import {faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import InfoBox from "./infoBox";


const AssignToProf = ({refIds, notificationIds}) => {

    const [profs, setProfs] = useState([]);
    const [interns, setInterns] = useState([]);
    const [profsCurrentInternInfo, setProfsCurrentInternInfo] = useState([]);
    const [indexNum, setIndexNum] = useState('');
    const [errorMessageProf, setErrorMessageProf] = useState('');
    const [errorMessageIntern, setErrorMessageIntern] = useState('');
    const [errorMessageAssign, setErrorMessageAssign] = useState({});
    const [studentDetail, setStudentDetail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => {setIsModalOpen(false); setStudentDetail(null)};

    const { t } = useTranslation();
    const lang  = getUserInfo().lang


    useEffect(() => {
        const getProfessors = async () => {
            const res = await InternshipManagerAPI.getAllProfs();
            if (res.value != null){
                // sort prof according to discipline
                res.value.sort((a, b) => (a.discipline.id > b.discipline.id) ? 1 : -1)
                setProfs(res.value)
            }
            else
                setErrorMessageProf(res["exception"])
        }
        getProfessors();
    }, []);

    // get the list of interns
    useEffect(() => {
        const getInterns = async () => {
            const res = await InternshipManagerAPI.getInternsWaitingForAssignmentToProf();
            if (res.value != null){
                // extract brief info about interns
                const internsBrief = res.value.map((intern) => {
                    return {
                        id: intern.id,
                        discipline: intern.jobOfferApplicationDTO.CV.studentDTO.discipline,
                        // intern: getStudentName(intern.jobOfferApplicationDTO.CV.studentDTO),
                        intern: intern.jobOfferApplicationDTO.CV.studentDTO,
                        employer: getEmployerNameFromPdfDocu(intern.jobOfferApplicationDTO.jobOffer.pdfDocu.fileName),
                        student: intern.jobOfferApplicationDTO.CV.studentDTO,
                        jobTitle: intern.jobOfferApplicationDTO.jobOffer.titre,
                        assignedProf: null
                    }
                })

                console.log("hello")
                console.dir(internsBrief)

                if (refIds)
                    setInterns(internsBrief.filter(intern => refIds.includes(intern.student.id)))
                else
                    setInterns(internsBrief)
            }
            else
                setErrorMessageIntern(res["exception"])
        }
        getInterns();
    }, []);


    const handleShowDetailProf = async (profId, indexIntern) => {
        if (profId === "") {
            setIndexNum('')
            setErrorMessageAssign({})
            setProfsCurrentInternInfo({})
            return
        }

        setIndexNum(indexIntern)

        const listInterns = await InternshipManagerAPI.getInternsByProfId(profId)
        // extract brief info about current interns
        let employers = new Set([])

        listInterns.value.forEach((internEvaluation) => {
            employers.add(getEmployerNameFromPdfDocu(internEvaluation.internshipOffer.jobOfferApplicationDTO.jobOffer.pdfDocu.fileName))
        })

        setProfsCurrentInternInfo(
            {
                profId: profId,
                totalInterns: listInterns.value.length,
                employers: employers.size > 0 ? Array.from(employers).join(', ') : "No info"
            }
        )
    }

    const handleAssign = async (internId) => {
        console.log("Assigning intern: ", internId, " to prof: ", profsCurrentInternInfo.profId)
        const internEvaluation = await InternshipManagerAPI.assignInternToProf(profsCurrentInternInfo.profId, internId)

        if (internEvaluation.value != null){
            interns.forEach((intern) => {
                if (intern.id === internId)
                    intern.assignedProf = internEvaluation.value.teacher
            })
            console.log('updated interns:', interns)
            // remove from notification list
            if (refIds) {
                const index = refIds.indexOf(internId);
                UserGeneralAPI.markReadNotification(notificationIds[index])
            }

            setInterns([...interns])
        }
        else
            setErrorMessageAssign({
                    ...errorMessageAssign,
                    internId: internEvaluation["exception"]
            })

    }
    const handleShowStudentDetail =  (intern) => {
        console.log('show student detail', intern)
        setStudentDetail(intern)
        openModal()
    }

    return (
        <div className="card-list">
            <h2>{interns.length} {t('assignToProf.title')}</h2>
            {errorMessageProf &&
                <ErrorBox msg={t('assignToProf.errors.' + errorMessageProf)}/>
            }
            {errorMessageIntern &&
                <div className="w-full">
                    <InfoBox msg={t('assignToProf.errors.' + errorMessageIntern)}/>
                </div>

            }
            {interns.length > 0 && interns.map(
                (intern, index) => {
                    return (
                        <div key={intern.id} className="card-small">
                            <div className={"flex flex-row items-center justify-center hover:scale-110 cursor-pointer"} onClick={() => {
                                handleShowStudentDetail(intern.intern)
                            }}>
                                <h4 className="profile-section-title">{getStudentName(intern.intern)}</h4>
                                <FontAwesomeIcon icon={faInfoCircle}
                                                 className="profile-section-title ms-2  py-2"/>
                            </div>
                            <p><b>Discipline:</b> {intern.discipline[lang]}</p>
                            <p><b>{t('userTypes.employer')}:</b> {intern.employer}</p>
                            <p><b>{t('assignToProf.jobTitle')}:</b> {intern.jobTitle}</p>

                            {intern.assignedProf &&
                                <SuccessBoxNoTrans msg={t('assignToProf.assignedTo') + intern.assignedProf.prenom + " " +intern.assignedProf.nom }/>
                            }
                            {intern.assignedProf === null &&
                                <>
                                    <select
                                        className="mt-3"
                                        onChange={(e) => handleShowDetailProf(e.target.value, index)}>
                                        <option value="">---{t('assignToProf.selectAProf')}---</option>
                                        {profs.map((prof) => {
                                            return <option key={prof.id}
                                                           value={prof.id}>{prof.prenom} {prof.nom} - {prof.discipline[lang]}</option>
                                        })}
                                    </select>
                                    <>
                                        {indexNum === index &&
                                            <div className="text-center">
                                                <p><Trans i18nKey="assignToProf.profInternsStats" values={{ totalInterns: profsCurrentInternInfo.totalInterns }}>
                                                    Professor has <b></b> intern(s)
                                                    assigned.
                                                </Trans>
                                                </p>

                                                {/*<p>Professor has <b>{profsCurrentInternInfo.totalInterns}</b> interns*/}
                                                {/*    assigned. </p>*/}
                                                {profsCurrentInternInfo.totalInterns > 0 &&
                                                <p><b>{t('userTypes.employer')}s:</b> {profsCurrentInternInfo.employers}</p>
                                                }

                                                { errorMessageAssign[intern.id] &&
                                                    <ErrorBox msg={ t('assignToProf.errors.' + errorMessageAssign[intern.id])}/>
                                                }
                                                <button onClick={() => handleAssign(intern.id)}
                                                        className="button-in-card-neutral w-full"
                                                >{t('assignToProf.buttonAssign')}
                                                </button>

                                            </div>
                                        }

                                    </>
                                </>
                            }
                            {isModalOpen &&
                                <Modal onClose={closeModal}>
                                    {studentDetail && <ProfilePreview permission={Permission.Full} profile={studentDetail} />}
                                </Modal>
                            }
                        </div>
                    )
                }


            )}

            </div>

    )
}

export default AssignToProf