package com.ledger.command_service.application.exception;

public class InvalidReversalException extends RuntimeException {
        public InvalidReversalException(String txId) {
            super("Invalid reversal attempt for transaction with id " + txId + ".");
        }
}
