package com.ledger.command_service.application.exception;

public class WalletAlreadyClosedException extends RuntimeException {
    public WalletAlreadyClosedException() {
        super("The wallet is already closed.");
    }
}
