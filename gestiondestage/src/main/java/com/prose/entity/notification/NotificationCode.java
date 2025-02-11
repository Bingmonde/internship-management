package com.prose.entity.notification;

import lombok.Getter;

public enum NotificationCode {
    TEST_STACKABLE("stest",true),
    TEST_UNSTACKABLE("utest",false),
    USER_CREATED("user_created",false),
    CV_VALIDATION_REQUIRED("cv_validation_required",true),
    OFFER_VALIDATION_REQUIRED("offer_validation_required",true),
    CV_VALIDATED("cv_validated",false),
    JOB_OFFER_VALIDATED("job_offer_validated",false),
    NEW_JOB_OFFER("new_job_offer",true),
    NEW_APPLICANT("new_applicant",true),
    NEW_INTERVIEW("new_interview",true),
    INTERVIEW_CONFIRMED("interview_confirmed",false),
    INTERNSHIP_OFFER_RECEIVED("internship_offer_received",true),
    INTERNSHIP_OFFER_ACCEPTED("internship_offer_accepted",false),

    CONTRACT_TO_START("contract_to_start",true),
    CONTRACT_TO_SIGN("contract_to_sign",false),
    CONTRACT_SIGNED("contract_signed",false),

    INTERN_REQUIRE_ASSIGNEMENT("intern_require_assignement",false),
    INTERN_ASSIGNMENT("intern_assignment",false),

    INTERNSHIP_ENVIRONMENT_TO_REVIEW("internship_environment_to_review",true),
    INTERN_TO_REVIEW("intern_to_review",true),

    ;
    private final String keyword;
    @Getter
    private final boolean stackable;
    NotificationCode(String keyword, boolean stackable) {
        this.keyword = keyword;
        this.stackable = stackable;
    }

    @Override
    public String toString() {
        return "notifications." + keyword;
    }

}
