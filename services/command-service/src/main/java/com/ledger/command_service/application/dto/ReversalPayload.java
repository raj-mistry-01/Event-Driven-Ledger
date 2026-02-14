package com.ledger.command_service.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReversalPayload(
        UUID originalTransactionId,
        BigDecimal amount
) {}