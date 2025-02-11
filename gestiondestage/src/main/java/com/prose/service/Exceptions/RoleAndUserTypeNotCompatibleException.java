package com.prose.service.Exceptions;

public class RoleAndUserTypeNotCompatibleException extends Exception {

    public RoleAndUserTypeNotCompatibleException() {
        super();
    }
    public RoleAndUserTypeNotCompatibleException(String message) {
        super(message);
    }
}
