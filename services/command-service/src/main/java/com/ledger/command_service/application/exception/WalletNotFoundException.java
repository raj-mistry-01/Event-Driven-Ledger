package com.ledger.command_service.application.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String walletId) {
        super("Wallet with id " + walletId + " not found.");
    }
}
