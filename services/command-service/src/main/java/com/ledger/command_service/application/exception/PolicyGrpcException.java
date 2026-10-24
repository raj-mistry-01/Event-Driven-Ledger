package com.ledger.command_service.application.exception;

public class PolicyGrpcException extends RuntimeException {

    public PolicyGrpcException(String message, Throwable cause) {
        super(message, cause);
    }
}