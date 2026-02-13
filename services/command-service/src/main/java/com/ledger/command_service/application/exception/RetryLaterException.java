package com.ledger.command_service.application.exception;

public class RetryLaterException extends RuntimeException {
    public RetryLaterException() {
        super("Please retry the request.");
    }
}

