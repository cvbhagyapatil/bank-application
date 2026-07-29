package com.bank.dto;

import java.math.BigDecimal;

public record CreateAccountRequest(BigDecimal initialBalance) { }
