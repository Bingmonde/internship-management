package com.prose.entity;

public enum InterviewType {
    ONLINE("Online"),
    IN_PERSON("InPerson");

    private final String val;

    InterviewType(String val) {
        this.val = val;
    }

    public String toString() {
        return val;
    }

    public static InterviewType toEnum(String val) {
        for (InterviewType v : values()) {
            if (v.toString().equalsIgnoreCase(val)) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }

}
