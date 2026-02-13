package com.ledger.command_service.application.exception;

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String txId) {
        super("Original transaction with id " + txId + " not found or invalid.");
    }
}

