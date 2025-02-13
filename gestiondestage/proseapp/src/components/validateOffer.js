import React, {useContext, useEffect, useState} from "react";
import {getUserInfo, userInfo} from "../utils/userInfo";
import {useTranslation} from "react-i18next";
import SuccessBox from "./successBox";
import Input from "./input";
import {FormProvider, useForm} from "react-hook-form";
import {searchBor} from "../utils/forms/formValidation";
import {useOutletContext} from "react-router-dom";
import {useSession} from "./CurrentSession";
import SearchComponent from "./search/searchComponent";
import PaginationComponent from "./pagination/paginationComponent";
import Modal from "./modal";
import ProfilePreview from "./userProfile/profilePreview";
import {Permission} from "../constants";
import {UserGeneralAPI} from "../api/userAPI";
import {faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import InfoBox from "./infoBox";


const ValidateOffer = ({refIds, notificationIds}) => {

    const [setPdfModalHelper] = useOutletContext()

    const [jobOffers, setJobOffers] = useState([])
    const [selectedOffer, setSelectedOffer] = useState({})
    const [offerIsSelected, setOfferIsSelected] = useState(false)
    const [disciplines, setDisciplines] = useState([])
    const [disciplinesSelected, setDisciplinesSelected] = useState(false)
    const [selectedDiscipline, setSelectedDiscipline] = useState("")
    const [students, setStudents] = useState([])
    const [studentsID, setStudentsID] = useState([])
    const [studentSelected, setStudentSelected] = useState([])
    const { currentSession } = useSession();

    const [currentPageIndex, setCurrentPageIndex] = useState(0);
    const [totalPagesFromBackend, setTotalPagesFromBackend] = useState(0);

    const methods = useForm();

    const { t } = useTranslation();

    const [success, setSuccess] = useState(false)

    const [selectedOfferOption, setSelectedOfferOption] = useState('0');
    const [searchQuery, setSearchQuery] = useState("");
    const [companyDetail, setCompanyDetail] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const openModal = () =>  setIsModalOpen(true);
    const closeModal = () => {setIsModalOpen(false); setCompanyDetail(null)};

    const filterName = (name) => {
        return (name.toLowerCase().trim().includes(searchQuery.trim().toLowerCase()));
    }

    useEffect(() => {
        if (!currentSession.id) return;
        fetchJobOffers()
        setOfferIsSelected(false);
    }, [currentSession]);

    useEffect(() => {
        if (!currentSession.id) return;
        if(localStorage.getItem('selectedOffer')){
            setSelectedOffer(JSON.parse(localStorage.getItem('selectedOffer')))
            // setOfferIsSelected(true)
        }
    }, []);

    useEffect(() => {
        // Sauvegarder l'offre dans le localStorage lorsque celle-ci change
        if (offerIsSelected) {
            localStorage.setItem('selectedOffer', JSON.stringify(selectedOffer));
        }
    }, [selectedOffer]);



    useEffect(() => {
        console.log('Selected discipline:', selectedDiscipline);
        if (selectedDiscipline !== "") {
            getStudents(selectedDiscipline).then();
        }
    }, [selectedDiscipline, searchQuery, currentPageIndex]);

    useEffect(() => {
        console.log('Student selected with check:', studentSelected)
        setStudentsID(()=> studentSelected)
    }, [studentSelected]);

    useEffect(() => {
        console.log('stud id:', studentsID)
    }, [studentsID]);

    const getDisciplines = async () => {
        const res = await fetch('http://localhost:8080/disciplines', {
            method: 'GET',
            headers: {
                'Authorization': getUserInfo().token,
            }
        });
        return res.json();
    }

    useEffect(() => {
        const fetchDisciplines = async () => {
            try {
                const resultGot = await getDisciplines();
                setDisciplines(()=> resultGot)
            } catch (error) {
                console.error("Error fetching disciplines:", error);
            }
        };
        fetchDisciplines().then(() => {
        });
    }, [])

    const getStudentsByDiscipline = async (discipline) => {
        const res = await fetch('http://localhost:8080/students/disciplines/'+discipline+
            '?page='+currentPageIndex+
            '&size=10'+
            '&q='+searchQuery
            , {
            method: 'GET',
            headers: {
                'Authorization': getUserInfo().token,
                'Content-Type': 'application/json',
            }
        });
        return res.json();
    }

    const getStudents = async (discipline) => {
        try {
            const resultGot = await getStudentsByDiscipline(discipline);
            setStudents(()=> resultGot.value);
            console.log('Students got:', resultGot);

            // set total pages from backend (to inform pagination component of numbers to show)
            setTotalPagesFromBackend(parseInt(resultGot.totalPages, 10));
            console.log("total pages from backend: " + parseInt(resultGot.totalPages, 10));

        } catch (error) {
            console.error("Error fetching students:", error);
        }
    }

    const getAllJobOffers = async () => {
        const res = await fetch('http://localhost:8080/jobOffers/waitingForApproval?' +
            "season="+currentSession.season+"&" +
            "year="+currentSession.year, {
            method: 'GET',
            headers: {
                'Authorization': getUserInfo().token,
            },
        });
        return res.json();
    };

    const fetchJobOffers = async () => {
        try {
            const resultGot = await getAllJobOffers();
            setJobOffers(()=> resultGot.value);
            setSelectedOfferOption("0");
            if (currentPageIndex !== 0) {
                setCurrentPageIndex(0); // TODO this prob causes a second refresh but I'm lazy
            }
        } catch (error) {
            console.error("Error fetching jobs offer:", error);
        }
    };

    useEffect(() => {
        fetchJobOffers().then(() => {
        });
    }, []);

    const validateJobOffer = async (id) => {
        const res = await fetch('http://localhost:8080/internshipManager/validateInternship/' + id, {
            method: 'POST',
            headers: {
                'Authorization': getUserInfo().token,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                disciplines: selectedDiscipline === "" ? [] : [selectedDiscipline],
                studentIds: studentsID,
                expirationDate: new Date().toISOString().split('T')[0],  // Formater en 'YYYY-MM-DD'
                isApproved: true
            })
        });
        return res.json();

    };

    const validateOffer = async (id) => {
        try {
            const resultGot = await validateJobOffer(id);
            console.log('Job offer validated:', resultGot)
            // remove from notification
            if (refIds) {
                const notificationIndex = refIds.indexOf(id);
                UserGeneralAPI.markReadNotification([notificationIds[notificationIndex]]);
            }
            setSuccess(true);
            await fetchJobOffers();
        } catch (error) {
            console.error("Error validating job offer:", error);
        }
    }

    const downloadCV = async (filename) => {
        try {
            const res = await fetch(`http://localhost:8080/download/file/${filename}`, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                    'Content-Type': 'application/json',
                }
            });
            if (res.ok) {
                const jsonResponse = await res.json();
                const base64String = jsonResponse.value;

                // Decode the Base64 string
                const byteCharacters = atob(base64String);
                const byteNumbers = new Array(byteCharacters.length);
                for (let i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                const byteArray = new Uint8Array(byteNumbers);

                const blob = new Blob([byteArray], { type: 'application/pdf' });
                const url = window.URL.createObjectURL(blob);
                setPdfModalHelper(url,filename)
            }
            else {
                var err = await res.json()
                console.log(err)
                console.log(err["exception"])
                // TODO create a modal to show the error
            }

        } catch (err) {
            console.log(err)
            console.log(err["exception"])
        }
    }

    const handleConsultCompanyDetail = (compagnie) => {
        setCompanyDetail(compagnie)
        openModal()
    }

    return (
        <>
            <div className="w-full">
                <div className="mx-auto">
                    {!refIds && (
                        <>
                            {jobOffers === null &&
                                <InfoBox msg={t('validateOffers.noMoreOffer')}  />
                            }
                            {jobOffers !== null && jobOffers.length === 0 &&
                                <InfoBox msg={t('validateOffers.noOffer')}  />

                    }
                            </>
                    )}
                    {jobOffers !== null &&
                        <div className="w-1/2 mx-auto">
                            <form className={"card-offer-list-item"}>
                                <h1 className="text-gray-500 font-bold mb-1 md:mb-0 pr-4" >
                                    {t("validateOffers.H1")}
                                </h1>
                                <select
                                    value={selectedOfferOption}
                                    onChange={(e) => {
                                        if(e.target.value === "0"){
                                            setOfferIsSelected(false);
                                            setSelectedOfferOption("0")
                                            localStorage.removeItem('selectedOffer');
                                        }
                                        const selectedOfferId = e.target.value;
                                        const selectedOffer = jobOffers.find(offer => offer.id === parseInt(selectedOfferId));
                                        if (selectedOffer) {
                                            setSelectedOffer(() => selectedOffer);
                                            localStorage.setItem('selectedOffer', JSON.stringify(selectedOffer));
                                            setOfferIsSelected(true);
                                            setSuccess(false)
                                            setSelectedOfferOption(selectedOfferId);
                                        }
                                    }}
                                >
                                    <option value={0}>{t("validateOffers.selectOption")}</option>
                                    {jobOffers.map((jobOffer, index) => (
                                        <option key={index} value={jobOffer.id}>{jobOffer.titre}</option>
                                    ))}
                                </select>
                            </form>
                        </div>
                    }
                </div>
                <div>
                    {offerIsSelected &&

                        <form className={"fullpage-form-center mx-auto bg-prose-neutral"}
                              onSubmit={event => event.preventDefault()}>
                            <div className="flex flex-row justify-center items-center hover:scale-110">
                                <h3 className="profile-section-title mb-2 cursor-pointer"
                                    onClick={() => handleConsultCompanyDetail(selectedOffer.employeurDTO)}>{selectedOffer.employeurDTO.nomCompagnie}</h3>
                                <FontAwesomeIcon icon={faInfoCircle} className="profile-section-title ms-2 self-center"/>
                            </div>

                            <div className="md:flex md:items-center mb-6">
                                <div className="md:w-1/3">
                                    <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                               htmlFor="inline-full-name">
                                            {t("validateOffers.title")}
                                        </label>
                                    </div>
                                    <div className="md:w-2/3">
                                        <input
                                            className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full
                                        py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white
                                        focus:border-purple-500"
                                            id="inline-full-name" type="text" value={selectedOffer.titre}
                                            readOnly={true}
                                        />
                                    </div>
                                </div>
                                <div className="md:flex md:items-center mb-6">
                                    <div className="md:w-1/3">
                                        <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                               htmlFor="inline-full-name">
                                            {t("validateOffers.startDate")}
                                        </label>
                                    </div>
                                    <div className="md:w-2/3">
                                        <input
                                            className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full
                                        py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white
                                        focus:border-purple-500"
                                            id="inline-full-name" type="text" value={selectedOffer.dateDebut}
                                            readOnly={true}
                                        />
                                    </div>
                                </div>
                                <div className="md:flex md:items-center mb-6">
                                    <div className="md:w-1/3">
                                        <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                               htmlFor="inline-full-name">
                                            {t("validateOffers.duration")}
                                        </label>
                                    </div>
                                    <div className="md:w-2/3">
                                        <input
                                            className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full
                                        py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white
                                        focus:border-purple-500"
                                            id="inline-full-name" type="text" value={selectedOffer.dateFin}
                                            readOnly={true}
                                        />
                                    </div>
                                </div>
                                <div className="md:flex md:items-center mb-6">
                                    <div className="md:w-1/3">
                                        <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                               htmlFor="inline-full-name">
                                            {t("validateOffers.location")}
                                        </label>
                                    </div>
                                    <div className="md:w-2/3">
                                        <input
                                            className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full
                                        py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white
                                        focus:border-purple-500"
                                            id="inline-full-name" type="text" value={selectedOffer.lieu}
                                            readOnly={true}
                                        />
                                    </div>
                                </div>
                                <div className="md:flex md:items-center mb-6">
                                    <div className="md:w-1/3">
                                        <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                               htmlFor="inline-full-name">
                                            {t("validateOffers.type")}
                                        </label>
                                    </div>
                                    <div className="md:w-2/3">
                                        <input
                                            className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full
                                        py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white
                                        focus:border-purple-500"
                                            id="inline-full-name" type="text" value={t(selectedOffer.typeTravail)}
                                            readOnly={true}
                                        />
                                    </div>
                                </div>
                                <div className="md:flex md:items-center mb-6">
                                    <div className="md:w-1/3">
                                        <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                               htmlFor="inline-full-name">
                                            {t("validateOffers.internsAccepted")}
                                        </label>
                                    </div>
                                    <div className="md:w-2/3">
                                        <input
                                            className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full
                                        py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white
                                        focus:border-purple-500"
                                            id="inline-full-name" type="text" value={selectedOffer.nombreStagiaire}
                                            readOnly={true}
                                        />
                                    </div>
                                </div>
                                <div className="md:flex md:items-center mb-6">
                                    <div className="md:w-1/3">
                                        <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                               htmlFor="inline-full-name">
                                            {t("validateOffers.salary")}
                                        </label>
                                    </div>
                                    <div className="md:w-2/3">
                                        <input
                                            className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full
                                        py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white
                                        focus:border-purple-500"
                                            id="inline-full-name" type="text" value={selectedOffer.tauxHoraire}
                                            readOnly={true}
                                        />
                                    </div>
                                </div>
                                <div className="md:flex md:items-center mb-6">
                                    <div className="md:w-1/3">
                                        <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                               htmlFor="inline-full-name">
                                            {t("validateOffers.file")}
                                        </label>
                                    </div>
                                    <div className="md:w-2/3">
                                        {selectedOffer.pdfDocu !== null &&
                                            <a href="#" target="_blank"
                                               className="text-blue-600 text-xs hover:underline" onClick={(e) => {
                                                e.preventDefault()
                                                downloadCV(selectedOffer.pdfDocu.fileName).then(r => console.log(r))
                                            }}>
                                                {selectedOffer.pdfDocu.fileName}
                                            </a>
                                        }
                                        {selectedOffer.pdfDocu === null &&
                                            <input
                                                className="bg-gray-200 appearance-none border-2 border-gray-200 rounded w-full
                                        py-2 px-4 text-gray-700 leading-tight focus:outline-none focus:bg-white
                                        focus:border-purple-500"
                                                id="inline-full-name" type="text" value={t("validateOffers.noFile")}
                                                readOnly={true}
                                            />
                                        }

                                    </div>
                                </div>
                                <div className="md:flex md:items-center mb-6">
                                    <div className="md:w-1/3">
                                        <label className="block text-gray-500 font-bold md:text-right mb-1 md:mb-0 pr-4"
                                               htmlFor="inline-full-name">
                                            {t("validateOffers.programs")}
                                        </label>
                                    </div>
                                    <div className="md:w-2/3">
                                        {/*disciplines selector for filtering students*/}
                                        <select className="text-gray-500"
                                            onChange={(e) => {
                                                setStudentSelected([])
                                                if (e.target.value !== "0") {
                                                    setDisciplinesSelected(true);
                                                    const selectedDisciplineId = e.target.value;
                                                    const selectedDiscipline = disciplines.find(discipline =>
                                                        discipline.id === selectedDisciplineId);
                                                    if (selectedDiscipline) {
                                                        setSelectedDiscipline(() => selectedDiscipline.id);
                                                    }
                                                }
                                                if (e.target.value === "0") {
                                                    setStudents(null);
                                                    setDisciplinesSelected(false);
                                                }
                                            }}
                                        >
                                            <option value={0}>{t("validateOffers.selectProgram")}</option>
                                            {disciplines.map((discipline, index) => (
                                                <option key={index} value={discipline.id}>
                                                    {discipline[getUserInfo().lang]}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                                {disciplinesSelected &&
                                    <>
                                        <SearchComponent placeholderText={t('formLabels.searchStudent')}
                                                         stoppedTyping={setSearchQuery}/>
                                        {students &&
                                            <div className="md:flex md:items-center mb-6">
                                                <div className="md:w-2/3">
                                                    <ul>
                                                        {students.map((student, index) => (
                                                            <li className={(filterName(student.prenom + " " + student.nom) ? "" : "hidden ") + "p-1"}
                                                                key={index} value={student.id}>
                                                                <input className="me-2" onClick={(e) => {
                                                                    if (e.target.checked) {
                                                                        setStudentSelected([...studentSelected,
                                                                            student.id])
                                                                    }
                                                                    if (!e.target.checked) {
                                                                        setStudentSelected(studentSelected
                                                                            .filter(id => id !== student.id))
                                                                    }
                                                                }} id={student.id} type="checkbox" checked={studentSelected.includes(student.id)}
                                                                />
                                                                <label
                                                                    className="text-gray-500 font-bold mb-1 md:mb-0 pr-4"
                                                                    htmlFor={student.id}>{student.prenom + " " + student.nom}
                                                                </label>
                                                            </li>
                                                        ))}
                                                    </ul>
                                                </div>
                                            </div>
                                        }
                                        <div className="flex justify-center mt-2">
                                            <PaginationComponent totalPages={totalPagesFromBackend}
                                                                 paginate={(pageNumber) => {
                                                                     setCurrentPageIndex(pageNumber - 1); // page index starts at zero in backend, not the same as page "number"
                                                                 }}/>
                                        </div>

                                    </>
                                }
                                <div>
                                    {!success &&
                                        <button className={"button-in-card-neutral w-full"}
                                                onClick={(e) => {
                                                    localStorage.removeItem('selectedOffer');
                                                    // e.preventDefault();
                                                    // console.log('offer is sent')
                                                    validateOffer(selectedOffer.id).then();
                                                }}
                                        >
                                            {t("validateOffers.btnValidate")}
                                        </button>
                                    }
                                    {success &&
                                        <SuccessBox msg={"validateOffers.validated"}></SuccessBox>
                                    }
                                </div>
                        </form>
                    }
                </div>
                {isModalOpen &&
                    <Modal onClose={closeModal}>
                        {companyDetail && <ProfilePreview permission={Permission.Full} profile={companyDetail} />}
                    </Modal>
                }
            </div>

        </>
    )

}

export default ValidateOffer