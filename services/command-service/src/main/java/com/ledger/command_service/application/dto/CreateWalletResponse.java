package com.ledger.command_service.application.dto;


import java.util.UUID;

public record CreateWalletResponse(
        UUID walletId,
        String status
) {}

