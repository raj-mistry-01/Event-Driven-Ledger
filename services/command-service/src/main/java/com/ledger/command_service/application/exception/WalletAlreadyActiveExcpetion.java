package com.ledger.command_service.application.exception;

public class WalletAlreadyActiveExcpetion extends RuntimeException {
    public WalletAlreadyActiveExcpetion() {
        super("Wallet is already active");
    }
}
