package com.ledger.command_service.application.exception;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException() {
        super("Amount must be a positive number.");
    }
}

