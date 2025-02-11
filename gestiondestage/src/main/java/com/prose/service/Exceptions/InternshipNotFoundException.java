package com.prose.service.Exceptions;

public class InternshipNotFoundException extends Exception {

    public InternshipNotFoundException() {
        super();
    }

    public InternshipNotFoundException(String studentHasNoInternshipOffer) {
        super(studentHasNoInternshipOffer);
    }
}
