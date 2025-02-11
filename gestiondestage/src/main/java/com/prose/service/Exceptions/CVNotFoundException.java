package com.prose.service.Exceptions;

public class CVNotFoundException extends RuntimeException {

    public CVNotFoundException() {
        super();
    }

    public CVNotFoundException(String message) {
        super(message);
    }
}