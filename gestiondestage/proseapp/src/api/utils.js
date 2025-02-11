import {format} from "date-fns";

export const getStudentName = (student) => {
    return student.prenom + " " + student.nom
}

export const getEmployerNameFromPdfDocu = (filename) => {
    return filename.split("_")[0]
}

export const generateContractPdfName = (internshipOffer) => {
    const jobTitle = internshipOffer?.jobOfferApplicationDTO?.jobOffer?.titre;
    const employerName = internshipOffer?.jobOfferApplicationDTO?.jobOffer?.employeurDTO?.nomCompagnie;
    const studentName = getStudentName(internshipOffer?.jobOfferApplicationDTO?.CV?.studentDTO);
    const date = new Date().toISOString().split('T')[0];
    return  "contract_stage_" + jobTitle + "_" +  employerName + "_" +studentName + "_" + date +  ".pdf";
}


export const generateEmployerEvaluationPdfName = (evaluation) => {

    const employerName = evaluation?.internshipOffer?.jobOfferApplicationDTO?.jobOffer?.employeurDTO?.nomCompagnie;
    const jobTitle = evaluation?.internshipOffer?.jobOfferApplicationDTO?.jobOffer?.titre;
    const date = new Date().toISOString().split('T')[0];
    return  "evaluation_entreprise_" + employerName + "_" +jobTitle + "_" + date +  ".pdf";

}

export const generateStudentEvaluationPdfName = (internshipOffer) => {
    const studentName = getStudentName(internshipOffer.jobOfferApplicationDTO?.CV?.studentDTO);
    const jobTitle = internshipOffer.jobOfferApplicationDTO?.jobOffer?.titre;
    const date = new Date().toISOString().split('T')[0];
    return  "evaluation_etudiant_" + studentName + "_" +jobTitle + "_" + date +  ".pdf";
}

export const telephoneFormat = (telephone) => {
    // if (!telephone) return ''
    return telephone.replace(/(\d{3})(\d{3})(\d{4})/, '($1) $2-$3');
}

export const formatDate = (date) => {
    return format(new Date(date), 'yyyy-MM-dd')
}
export const formatWeekday = (date) => {
    return format(new Date(date), 'eee')
}
export const formatTime = (date) => {
    return format(new Date(date), 'HH:mm')
}

export const isNotWelcomePage = (pathname) => {
    return pathname !== "/dashboard" && pathname !== "/dashboard/" && pathname !== "/dashboard/welcome";
}