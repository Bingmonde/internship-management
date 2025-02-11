package com.prose.security.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends APIException {

    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }

    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
