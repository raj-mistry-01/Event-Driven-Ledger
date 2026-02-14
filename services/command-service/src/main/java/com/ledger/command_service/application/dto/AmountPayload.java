package com.ledger.command_service.application.dto;

import java.math.BigDecimal;

public record AmountPayload(
        BigDecimal amount
) {}
