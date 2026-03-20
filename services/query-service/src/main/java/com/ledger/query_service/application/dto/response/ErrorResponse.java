package com.ledger.query_service.application.dto.response;


import java.time.Instant;

public record ErrorResponse(
        String error_code,
        String message,
        Instant timestamp
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, Instant.now());
    }
}
