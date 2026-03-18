package com.ledger.query_service.application.exception;


public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String transactionId) {
        super("Transaction with id " + transactionId + " not found.");
    }
}
