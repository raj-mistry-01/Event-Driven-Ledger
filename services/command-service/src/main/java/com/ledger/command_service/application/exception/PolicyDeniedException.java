package com.ledger.command_service.application.exception;

public class PolicyDeniedException extends RuntimeException {
    public PolicyDeniedException(String message) {
        super(message);
    }
}
