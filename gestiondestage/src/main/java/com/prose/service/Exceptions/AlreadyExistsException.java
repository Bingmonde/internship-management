package com.prose.service.Exceptions;

public class AlreadyExistsException extends Exception {

    public AlreadyExistsException() {
        super();
    }

    public AlreadyExistsException(String msg) {
        super(msg);
    }
}
