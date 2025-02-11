package com.prose.utils;

public class ResultValue<T> {
    private T value;
    private String exception;

    // Getter 和 Setter
    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
