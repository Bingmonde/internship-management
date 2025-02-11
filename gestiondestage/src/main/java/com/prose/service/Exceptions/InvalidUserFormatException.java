package com.prose.service.Exceptions;

public class InvalidUserFormatException extends Exception {
    public InvalidUserFormatException() {
        super();
    }

    public InvalidUserFormatException(String msg) {
        super(msg);
    }
}
