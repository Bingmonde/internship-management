package com.prose.service.Exceptions;

public class JobApplicationNotActiveException extends Exception {
    public JobApplicationNotActiveException(String message) {
        super(message);
    }
    public JobApplicationNotActiveException() {
        super();
    }
}
