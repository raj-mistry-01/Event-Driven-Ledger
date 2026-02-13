package com.ledger.command_service.application.exception;

public class WalletClosedException extends RuntimeException {
    public WalletClosedException(String walletId) {
        super("Wallet with id " + walletId + " is closed.");
    }
}

