import { useTranslation } from 'react-i18next';
import {differenceInDays, parseISO} from "date-fns";


export const firstName_validation = {
    name: 'prenom',
    label: 'formLabels.firstName',
    type: 'text',
    id: 'firstName',
    placeholder: '',
    validation: {
        required: {
            value: true,
            message: 'formErrors.firstNameRequired',
        },
        maxLength: {
            value: 30,
            message: 'formErrors.firstNameMaxLength',
        },
        minLength: {
            value: 2,
            message: 'formErrors.firstNameMinLength',
        },
    },
};

export const contactPerson_validation = {
    name: 'contactPerson',
    label: 'formLabels.contactPerson',
    type: 'text',
    id: 'contactPerson',
    placeholder: '',
    validation: {
        required: {
            value: true,
            message: 'formErrors.contactPersonRequired',
        },
    },
};

export const lastName_validation = {
    name: 'nom',
    label: 'formLabels.lastName',
    type: 'text',
    id: 'lastName',
    placeholder: '',
    validation: {
        required: {
            value: true,
            message: 'formErrors.lastNameRequired',
        },
        maxLength: {
            value: 30,
            message: 'formErrors.lastNameMaxLength',
        },
        minLength: {
            value: 2,
            message: 'formErrors.lastNameMinLength',
        },
    },
};

export const email_validation = {
    name: 'courriel',
    label: 'formLabels.email',
    type: 'email',
    id: 'email',
    placeholder: 'example@example.com',
    validation: {
        required: {
            value: true,
            message: 'formErrors.emailRequired',
        },
        pattern: {
            value: /.+@.+[.][\w]+/,
            message: 'formErrors.emailPattern',
        }
    },
};

export const telephone_validation = {
    name: 'telephone',
    label: 'formLabels.telephone',
    type: 'text',
    id: 'telephone',
    placeholder: '000-000-0000',
    validation: {
        required: {
            value: true,
            message: 'formErrors.telephoneRequired',
        },
        pattern: {
            value: /^\d{3}-\d{3}-\d{4}$/,
            message: 'formErrors.telephonePattern',
        }
    },
};

export const address_validation = {
    name: 'adresse',
    label: 'formLabels.address',
    type: 'text',
    id: 'address',
    placeholder: '',
    validation: {
        required: {
            value: true,
            message: 'formErrors.addressRequired',
        },
    },
};

export const discipline_validation = {
    name: 'discipline',
    label: 'formLabels.discipline',
    id: 'discipline',
    validation: {
        required: {
            value: true,
            message: 'formErrors.disciplineRequired',
        },
    },
};

export const password_validation = {
    name: 'mdp',
    label: 'formLabels.password',
    type: 'password',
    id: 'password',
    placeholder: '',
    autocomplete: 'new-password',
    validation: {
        required: {
            value: true,
            message: 'formErrors.passwordRequired',
        },
        minLength: {
            value: 6,
            message: 'formErrors.passwordMinLength',
        },
    },
};

export const confirm_password_validation = {
    name: 'confirmPassword',
    label: 'formLabels.confirmPassword',
    type: 'password',
    id: 'confirmPassword',
    placeholder: '',
    autocomplete: 'new-password',
    validation: {
        required: {
            value: true,
            message: 'formErrors.confirmPasswordRequired',
        },
        minLength: {
            value: 6,
            message: 'formErrors.confirmPasswordMinLength',
        },
    },
};


export const terms_read_validation = {
    required: {
        value: true,
        message: 'termsNotAgreed',
    }
};


export const user_type_validation = {
    name: 'userType',
    id: 'userType',
    label: 'formLabels.userTypeSelectorLabel',
    validation: {
        required: {
            value: true,
            message: 'required',
        },
    },
}

export const company_validation = {
    name: 'nomCompagnie',
    label: 'formLabels.company',
    type: 'text',
    id: 'company',
    placeholder: '',
    validation: {
        required: {
            value: true,
            message: 'formErrors.companyRequired',
        },
    },
}

export const fax_validation = {
    name: 'fax',
    label: 'formLabels.fax',
    type: 'text',
    id: 'fax',
    placeholder: '000-000-0000',
    validation: {
        required: {
            value: true,
            message: 'formErrors.faxRequired',
        },
        pattern: {
            // Matches exactly 10 digits, optionally formatted as 000-000-0000
            value: /^\d{3}-\d{3}-\d{4}$/,
            message: 'formErrors.faxInvalidFormat',
        },
    },
};


export const postalCode_validation = {
    name: 'postalCode',
    label: 'formLabels.postalCode',
    type: 'text',
    id: 'postalCode',
    placeholder: 'H2I3H1',
    validation: {
        required: {
            value: true,
            message: 'formErrors.postalCodeRequired',
        },
        pattern: {
            value: /^[A-Z][0-9][A-Z][0-9][A-Z][0-9]$/,
            message: 'formErrors.postalCodeInvalidFormat',
        },
    },
};

export const city_validation = {
    name: 'city',
    label: 'formLabels.city',
    type: 'text',
    id: 'city',
    placeholder: '',
    validation: {
        required: {
            value: true,
            message: 'formErrors.cityRequired',
        },
        pattern: {
            value: /^[a-zA-Z\s\-]+$/,
            message: 'formErrors.cityInvalidFormat',
        },
    },
};


// AJOUTÉ
export const employeeId_validation = {
    name: 'employeeId',
    label: 'employeeIdLabel',
    type: 'text',
    id: 'employeeId',
    placeholder: '',
    validation: {
        required: {
            value: true,
            message: 'errorEmployeeIdRequired',
        },
        pattern: {
            value: /^\d{6}$/,
            message: 'errorEmployeeId7Digits',
        }
    },
}

export const offer_name_validation = {
    name: "offerName",
    label: "formLabels.offerName",
    id: "offerName",
    type: 'text',
    placeholder: "",
    validation: {
        required: {
            value : true,
            message : "formErrors.offerNameRequired"
        },
        minLength: {
            value: 6,
            message: "formErrors.offerNameMinLength"
        }
    }
}

export const offer_type_validation = {
    name: 'offerType',
    id: 'offerType',
    label: 'formLabels.offerType',
    validation: {
        required: {
            value: true,
            message: 'required',
        },
    },
}

export const offer_number_people_validation = {
    name: "offerPeople",
    label: "formLabels.offerNumberPeople",
    id: "offerPeople",
    type: 'text',
    placeholder: "",
    validation: {
        required: {
            value : true,
            message : "formErrors.offerNumberPeopleRequired"
        },
        regex: {
            value: /^[0-9]*/,
            message: "formErrors.offerPeopleRegex"
        }
    }
}

export const offer_date_begin_validation = {
    name: "offerDateBegin",
    label: "formLabels.offerDateBegin",
    id: "offerDateBegin",
    type: "date",
    placeholder: "",
    validation: {
        required: {
            value : true,
            message : "formErrors.offerDateBeginRequired"
        },
        min: {
            value: new Date().toISOString().split('T')[0],
            message: "formErrors.offerDateBeginMin"
        }
    }
}

export const offer_date_duration_validation = {
    name: "offerDateDuration",
    label: "formLabels.offerDateDuration",
    id: "offerDateEnd",
    type: "date",
    placeholder: "",
    validation: {
        required: {
            value : true,
            message : "formErrors.offerDurationRequired"
        },
        min: {
            value: new Date().toISOString().split('T')[0],
            message: "offerDurationMin"
        }
    }
}


export const offer_salary_validation = {
    name: "offerMoney",
    label: "formLabels.offerSalary",
    id: "offerMoney",
    type: "number",
    placeholder: "",
    validation: {
        required: {
            value : true,
            message : "formErrors.offerSalaryRequired"
        },
        min: {
            value : 0,
            message: "formErrors.offerMoneyNegative"
        }
    }
}

export const offer_weekly_hours_validation = {
    name: "offerWeeklyHours",
    label: "formLabels.offerWeeklyHours",
    id: "offerWeeklyHours",
    type: "number",
    placeholder: "",
    validation: {
        required: {
            value : true,
            message : "formErrors.offerWeeklyHoursRequired"
        },
        min: {
            value : 0,
            message: "formErrors.offerWeeklyHoursNegative"
        }
    }
}

export const offer_daily_schedule_from_validation = {
    name: "offerDailyScheduleFrom",
    label: "formLabels.offerDailyScheduleFrom",
    id: "offerDailyScheduleFrom",
    type: "time",
    placeholder: "",
    validation: {
        required: {
            value : true,
            message : "formErrors.offerDailyScheduleFromRequired"
        }
    }
}

export const offer_daily_schedule_to_validation = {
    name: "offerDailyScheduleTo",
    label: "formLabels.offerDailyScheduleTo",
    id: "offerDailyScheduleTo",
    type: "time",
    placeholder: "",
    validation: {
        required: {
            value : true,
            message : "formErrors.offerDailyScheduleToRequired"
        }
    }
}



export const offer_description_validation = {
    name: "offerDescription",
    label: "formLabels.offerDescription",
    id: "offerDescription",
    placeholder: "Entrez une courte description de l'offre...",
}

export const offer_file_validation = {
    name: "offerFile",
    label: "formLabels.offerFile",
    id: "offerFile",
    accept: "application/pdf",
    validation: {
        required: {
            value: true,
            message: "formErrors.fileRequired"
        },
        validate : {
            pdfFile : v => (v != null && v[0].type === "application/pdf") || "formErrors.filePdfRequired",
            fileSize : v => (v != null && v[0].size < (10 * 1024 * 1024)) || "formErrors.fileTooBig"
        }
    }
}

export const cv_file_validation = {
    name: "cvFile",
    label: "formLabels.cvFile",
    id: "cvFile",
    accept: "application/pdf",
    validation: {
        required: {
            value: true,
            message: "formErrors.fileRequired"
        },
        validate : {
            pdfFile : v => (v != null && v[0].type === "application/pdf") || "formErrors.filePdfRequired",
            fileSize : v => (v != null && v[0].size < (10 * 1024 * 1024)) || "formErrors.fileTooBig"
        }
    }
}


export const job_interview_date_time_validation = {
    name: "interviewDate",
    label: "jobInterview.interviewDate",
    id: "jobInterviewDate",
    type: "datetime-local",
    placeholder: "",
    validation: {
        required: {
            value: true,
            message: "formErrors.jobInterviewDateRequired"
        },

        validate: (value) => {
            const selectedDate = parseISO(value);
            const today = new Date();
            return selectedDate >= today || "formErrors.invalidInterviewDate";
        }
    },
};



export const interview_type_validation = {
    name: 'interviewType',
    id: 'interviewType',
    label: 'jobInterview.interviewType',
    validation: {
        required: {
            value: true,
            message: 'formErrors.interviewTypeRequired',
        },
    },
}

export const interview_location_link_validation = {
    name: "interviewLocationOrLink",
    label: "jobInterview.interviewLocationLink",
    id: "interviewLocationOrLink",
    type: "text",
    placeholder: "",
    validation: {
        required: {
            value : true,
            message : "formErrors.interviewLocationLinkRequired"
        }
    }
}

export const searchBor = {
    name: "searchBar",
    label: "formLabels.searchStudent",
    id: "searchBar",
    type: "text",
    placeholder: "",
    validation: {
    }
}