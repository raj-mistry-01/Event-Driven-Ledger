package com.ledger.command_service.application.exception;

public class OptimisticLockException extends RuntimeException{
    public OptimisticLockException(String message) {
        super(message);
    }
}
