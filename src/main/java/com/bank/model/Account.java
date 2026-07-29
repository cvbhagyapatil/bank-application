package com.bank.model;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Account {
    private final UUID id;
    private final AtomicReference<BigDecimal> balance;

    public Account(UUID id, BigDecimal initialBalance) {
        this.id = id;
        this.balance = new AtomicReference<>(initialBalance == null ? BigDecimal.ZERO : initialBalance);
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance.get();
    }

    // Adds amount
    public BigDecimal add(BigDecimal amount) {
        return balance.updateAndGet(b -> b.add(amount));
    }

    // Attempts to subtract amount and returns the new balance or null if insufficient funds
    public BigDecimal subtractIfEnough(BigDecimal amount) {
        while (true) {
            BigDecimal current = balance.get();
            if (current.compareTo(amount) < 0) return null;
            BigDecimal updated = current.subtract(amount);
            if (balance.compareAndSet(current, updated)) return updated;
        }
    }
}
