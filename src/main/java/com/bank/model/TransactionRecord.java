package com.bank.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionRecord(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        BigDecimal postBalance,
        String description
) {}
