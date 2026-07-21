package com.bank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(@NotNull UUID from,
                              @NotNull UUID to,
                              @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                              String description) { }
