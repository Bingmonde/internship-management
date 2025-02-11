package com.prose.service.Exceptions;

public class DisciplineNotFoundException extends Exception {
    public DisciplineNotFoundException() {
        super();
    }

    public DisciplineNotFoundException(String msg) {
        super(msg);
    }
}