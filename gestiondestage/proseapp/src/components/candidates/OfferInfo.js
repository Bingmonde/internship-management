import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChevronDown, faChevronRight} from "@fortawesome/free-solid-svg-icons";
import JobOfferDetail from "../jobOfferDetail";
import PaginationComponent from "../pagination/paginationComponent";
import React, {useState} from "react";
import {useTranslation} from "react-i18next";
import CandidateInfo from "./candidateInfo";
import {useOutletContext} from "react-router-dom";
import {UserGeneralAPI} from "../../api/userAPI";
import OfferInfoWithPDF from "./offerInfoWithPDF";
import Modal from "../modal";
import ProfilePreview from "../userProfile/profilePreview";
import {Permission} from "../../constants";


const OfferInfo = ({ refIds,offer, offerIndex, jobOffersStats, setErrorMessage,
                       toggleShowOfferCandidates, fetchFullApplicationsByOfferId,createInterview,
                       cancelInterview,resetPage,handleInviteClick }) => {

    const { t } = useTranslation();
    const [setPdfModalHelper] = useOutletContext()
    const applications = offer.fullInfoApplications;

    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => setIsModalOpen(false);

    const previewPdf = async (fileName) => {
        const url = await UserGeneralAPI.previewPDF(fileName);
        if (url) {
            setPdfModalHelper(url, fileName);
        } else {
            setErrorMessage(t('error.fetchFailed'));
        }
    }

    return (
        <div className="flex flex-col mb-4">
            <div className="grid grid-cols-2 border-b-2 border-darkpurple justify-items-stretch p-2 cursor-pointer"
                 onClick={() => toggleShowOfferCandidates(offerIndex, offer.id)}>
                <h3 className="text-start text-xl text-darkpurple">{offer.titre}</h3>

                <div
                    className="justify-self-end self-center w-1/6 text-end p-2"
                >
                    {offer.expanded ?
                        <FontAwesomeIcon icon={faChevronDown}/> :
                        <FontAwesomeIcon icon={faChevronRight}/>
                    }
                </div>
            </div>

            {/*Info job offer*/}
            <div
                className="flex flex-col text-darkpurple px-4 items-center mx-auto bg-prose-neutral w-full">
                <div className="flex flex-row">
                    <p>{t('listCandidates.stats.totalNbApplications')}</p>
                    <p className="px-2  font-bold">{jobOffersStats[offer.id]?.totalNbApplications}</p>
                </div>
                <div className="flex flex-row">
                    <p>{t('listCandidates.stats.nbInternsNeeded')}</p>
                    <p className="px-2 font-bold">{jobOffersStats[offer.id]?.nbInternsNeeded}</p>
                </div>
                <div className="flex flex-row">
                    <p>{t('listCandidates.stats.nbInternshipOffersSent')}</p>
                    <p className="px-2  font-bold">{jobOffersStats[offer.id]?.nbInternshipOffersSent}</p>
                </div>
                <div className="flex flex-row">
                    <p>{t('listCandidates.stats.nbInternshipOffersAccepted')}</p>
                    <p className="px-2  font-bold">{jobOffersStats[offer.id]?.nbInternshipOffersAccepted}</p>
                </div>
                {/*candidate state filter select for internship*/}

                {/*{offer.expanded &&*/}
                {/*    <select className="w-3/5 text-center"*/}
                {/*            // on change, set filter key: id of offer, value: selected option*/}
                {/*            onChange={(e) => {*/}
                {/*                setFilterForCandidatesByJobId(prevState => ({*/}
                {/*                    ...prevState,*/}
                {/*                    [offer.id]: e.target.value,*/}
                {/*                }));*/}
                {/*            }}*/}
                {/*    >*/}
                {/*        <option*/}
                {/*            value="all">{t('listCandidates.dropDownFilterForCandidates.all')}</option>*/}
                {/*        <option*/}
                {/*            value="interviewees">{t('listCandidates.dropDownFilterForCandidates.interviewee')}</option>*/}
                {/*        <option*/}
                {/*            value="internshipOfferSent">{t('listCandidates.dropDownFilterForCandidates.internshipOffer')}</option>*/}
                {/*    </select>*/}
                {/*}*/}
            </div>
            <br/>

            {/* List of Candidates */}
            {offer.expanded &&
                <div className="flex flex-col rounded-b p-2">
                    {applications.length < 0 ? (
                        <>{!refIds &&
                            <div className="text-center text-gray-600">{t('listCandidates.noCandidate')}</div>}</>

                    ) : (
                        <div className="flex flex-col">
                            <div className="text-center mb-2">
                                <button className="btn-outlined-neutral"
                                        onClick={() => openModal()
                                        }>{t('jobDetails.previewJobDescription')}
                                </button>
                            </div>

                            {applications
                                .map((candidate, candidateIndex) => {
                                    return (
                                        <CandidateInfo key={candidate.id} candidate={candidate}
                                                       offer={offer}
                                                       setErrorMessage={setErrorMessage}
                                                       previewPdf={previewPdf} handleInviteClick={handleInviteClick}
                                                       cancelInterview={cancelInterview}
                                                       createInterview={createInterview}

                                        />
                                    )
                                })
                            }
                        </div>
                    )
                    }
                    <div className="flex justify-center mt-2">
                        {offer.page && offer.page[offerIndex] && offer.page[offerIndex].totalPage > 1 &&
                            <PaginationComponent resetCurrentPage={resetPage}
                                                 totalPages={offer.page[offerIndex].totalPage}
                                                 paginate={(newPage) => fetchFullApplicationsByOfferId(offer.id, offerIndex, newPage - 1)}>
                            </PaginationComponent>
                        }
                    </div>
                </div>


            }
            {isModalOpen &&
                <Modal onClose={closeModal}>
                    {offer && <OfferInfoWithPDF offer={offer} />}
                </Modal>
            }
        </div>
    )

}

export default OfferInfo;