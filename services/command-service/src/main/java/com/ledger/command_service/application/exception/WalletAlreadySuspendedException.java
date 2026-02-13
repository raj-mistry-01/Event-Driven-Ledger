package com.ledger.command_service.application.exception;

public class WalletAlreadySuspendedException extends RuntimeException {
    public WalletAlreadySuspendedException() {
        super("Wallet is already suspended");
    }
}

