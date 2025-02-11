package com.prose.entity.users;

public enum UserState {
    DEFAULT(""),
    SIGN_CONTRACT("signContract"),
    // Student
    MISSING_CV("uploadCV"),
    APPLY_JOBOFFER("applyJobOffer"),
    RESPOND_INTERVIEW("respondInterview"),
    RESPOND_INTERNSHIP("respondInternship"),

    // Employer
    MISSING_JOBOFFER("uploadJobOffer"),
    LIST_CANDIDATES("listCandidates"),
    LIST_INTERVIEW("listInterview"),
    // Teacher

    // ProgramManager
    VALIDATE_CV("validateCV"),
    VALIDATE_JOBOFFER("validateJobOffer"),
    START_CONTRACT("startContract"),


    // Professor
    TEACHER_DEFAULT("teacher");




    private final String keyword;
    UserState(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return keyword;
    }
}
