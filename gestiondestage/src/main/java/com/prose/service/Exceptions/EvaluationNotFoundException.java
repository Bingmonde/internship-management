package com.prose.service.Exceptions;

public class EvaluationNotFoundException extends Exception {
    public EvaluationNotFoundException() {
        super();
    }

    public EvaluationNotFoundException(String evaluationNotFound) {
        super(evaluationNotFound);
    }
}
