package com.prose.service.Exceptions;

public class MissingPermissionsExceptions extends Exception {
    public MissingPermissionsExceptions() {
        super();
    }

    public MissingPermissionsExceptions(String msg) {
        super(msg);
    }
}
