// src/constants.js

export const StageNumber = {
    STAGE_1: 'STAGE_1',
    STAGE_2: 'STAGE_2',
};

export const StagePreference = {
    PREMIER_STAGE: 'PREMIER_STAGE',
    DEUXIEME_STAGE: 'DEUXIEME_STAGE',
};

export const WillingnessToRehire = {
    OUI: 'OUI',
    NON: 'NON',
    PEUT_ETRE: 'PEUT_ETRE',
    YES: 'YES',
    NO: 'NO'
};

export const EvaluationOption = {
    TOTAL_AGREEMENT: 'TOTAL_AGREEMENT',
    STRONG_AGREEMENT: 'STRONG_AGREEMENT',
    SOMEWHAT_DISAGREEMENT: 'SOMEWHAT_DISAGREEMENT',
    TOTAL_DISAGREEMENT: 'TOTAL_DISAGREEMENT',
    NOT_APPLICABLE: 'NOT_APPLICABLE'
};


export const DisciplineOptions = [
    { val: "informatique", en: "Computer Science", fr: "Informatique" },
    { val: "tech_infermiere", en: "Nursing", fr: "Soins infirmiers" },
    { val: "architecture", en: "Architect", fr: "Architecte" },
    { val: "civil_engineer", en: "Civil Engineering", fr: "Génie civil" },
    { val: "accounting", en: "Accounting and Management", fr: "Comptabilité et gestion" },
    { val: "marketing", en: "Marketing", fr: "Commercialisation" },
    { val: "social_worker", en: "Social Worker", fr: "Travailleur(euse) social" },
    { val: "ec_education", en: "Early Childhood Education", fr: "Éducation de la petite enfance" },
];

export const Permission = {
    Limited: 'limited',
    Full: 'full'
}

// export const welcomeRoute = '/dashboard/';