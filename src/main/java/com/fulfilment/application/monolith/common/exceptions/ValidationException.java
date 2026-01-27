package com.fulfilment.application.monolith.common.exceptions;

public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
