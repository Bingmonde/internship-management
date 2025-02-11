package com.prose.service.Exceptions;

public class DateExpiredException extends Exception {
    public DateExpiredException() {
        super();
    }
    public DateExpiredException(String thisInternshipOfferHasExpired) {
        super(thisInternshipOfferHasExpired);
    }
}
