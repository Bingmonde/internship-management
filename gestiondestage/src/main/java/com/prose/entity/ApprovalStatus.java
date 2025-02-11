package com.prose.entity;

public enum ApprovalStatus {
    NOT_APPLICABLE("not_applicable"),
    WAITING("waiting"),
    VALIDATED("validated"),
    REJECTED("rejected"),
    ACCEPTED("accepted");

    private final String val;

    ApprovalStatus(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }

    public static ApprovalStatus toEnum(String val) {
        for (ApprovalStatus status : values()) {
            if (status.toString().equalsIgnoreCase(val)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ApprovalStatus value: " + val);
    }
}
