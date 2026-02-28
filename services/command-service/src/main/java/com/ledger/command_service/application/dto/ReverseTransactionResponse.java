package com.ledger.command_service.application.dto;

import java.util.UUID;

public record ReverseTransactionResponse(
        UUID walletId,
        UUID originalTransactionId,
        UUID transactionId   // reversal event id
) {
}