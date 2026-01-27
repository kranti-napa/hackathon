package com.fulfilment.application.monolith.common.exceptions;

public class ConflictException extends DomainException {
    public ConflictException(String message) {
        super(message);
    }
}
