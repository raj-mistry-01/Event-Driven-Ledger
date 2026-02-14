package com.ledger.command_service.application.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String walletId) {
        super("Wallet with id " + walletId + " does not have enough balance.");
    }
}

