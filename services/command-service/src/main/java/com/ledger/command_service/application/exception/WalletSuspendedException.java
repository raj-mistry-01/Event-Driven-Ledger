package com.ledger.command_service.application.exception;

public class WalletSuspendedException extends RuntimeException {
    public WalletSuspendedException(String walletId) {
        super("Wallet with id " + walletId + " is currently suspended.");
    }
}
