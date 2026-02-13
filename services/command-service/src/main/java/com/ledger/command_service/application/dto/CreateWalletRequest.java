package com.ledger.command_service.application.dto;

public record CreateWalletRequest(
        String clientId,
        String clientRequestId
) {}
