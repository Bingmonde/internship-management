package com.prose.entity;

public enum CandidatesStatusFilterForJobApplicationsFull {
    ALL("all"),
    INTERVIEWEE("interviewees"),
    JOB_OFFERED("internshipOfferSent");

    private final String val;

    CandidatesStatusFilterForJobApplicationsFull(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }

    public static CandidatesStatusFilterForJobApplicationsFull toEnum(String val) {
        for (CandidatesStatusFilterForJobApplicationsFull filter : values()) {
            if (filter.toString().equalsIgnoreCase(val)) {
                return filter;
            }
        }
        throw new IllegalArgumentException("Invalid CandidatesStatusFilterForJobApplicationsFull value: " + val);
    }
}
