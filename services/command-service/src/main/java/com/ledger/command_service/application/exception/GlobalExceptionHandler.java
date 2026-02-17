package com.ledger.command_service.application.exception;

import com.ledger.command_service.application.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("WALLET_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(WalletSuspendedException.class)
    public ResponseEntity<ErrorResponse> handleWalletSuspended(WalletSuspendedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("WALLET_SUSPENDED", ex.getMessage()));
    }

    @ExceptionHandler(WalletClosedException.class)
    public ResponseEntity<ErrorResponse> handleWalletClosed(WalletClosedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("WALLET_CLOSED", ex.getMessage()));
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmountException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_AMOUNT", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("INSUFFICIENT_FUNDS", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransaction(InvalidTransactionException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("INVALID_TRANSACTION", ex.getMessage()));
    }

    @ExceptionHandler(AlreadyReversedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyReversed(AlreadyReversedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("ALREADY_REVERSED", ex.getMessage()));
    }

    @ExceptionHandler(WalletAlreadySuspendedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadySuspended(WalletAlreadySuspendedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("WALLET_ALREADY_SUSPENDED", ex.getMessage()));
    }

    @ExceptionHandler(WalletAlreadyActiveExcpetion.class)
    public ResponseEntity<ErrorResponse> handlerAlreadyActive(WalletAlreadyActiveExcpetion ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("WALLET_ALREADY_ACTIVE", ex.getMessage()));
    }



    @ExceptionHandler(WalletAlreadyClosedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyClosed(WalletAlreadyClosedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("WALLET_ALREADY_CLOSED", ex.getMessage()));
    }

    @ExceptionHandler(RetryLaterException.class)
    public ResponseEntity<ErrorResponse> handleRetryLater(RetryLaterException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of("RETRY_LATER", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later."
                ));
    }
}
