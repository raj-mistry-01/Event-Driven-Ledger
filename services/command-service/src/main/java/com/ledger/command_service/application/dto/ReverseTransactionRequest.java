package com.ledger.command_service.application.dto;

import java.util.UUID;

public record ReverseTransactionRequest(
        UUID walletId,
        UUID originalTransactionId,
        String clientId,
        String clientRequestId
) {
}