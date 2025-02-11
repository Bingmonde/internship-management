package com.prose.service.Exceptions;

public class NotEnoughTimeAllocatedException extends RuntimeException {
    public NotEnoughTimeAllocatedException(String message) {
        super(message);
    }
}
