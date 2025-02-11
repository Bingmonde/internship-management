package com.prose.service.Exceptions;

public class JobApplicationNotFoundException extends Exception {
    public JobApplicationNotFoundException(String message) {
        super(message);
    }
    public JobApplicationNotFoundException() {
        super();
    }
}
