package com.fulfilment.application.monolith.common.exceptions;

public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(message);
    }
}
