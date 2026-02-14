package com.ledger.command_service.application.exception;

public class AlreadyReversedException extends RuntimeException {
    public AlreadyReversedException(String txId) {
        super("Original transaction with id " + txId + " has already been reversed.");
    }
}

