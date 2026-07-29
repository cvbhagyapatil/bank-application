package com.bank.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountTest {

    @Test
    public void addIncreasesBalance() {
        UUID id = UUID.randomUUID();
        Account a = new Account(id, BigDecimal.valueOf(10));
        BigDecimal after = a.add(BigDecimal.valueOf(5));
        Assertions.assertEquals(0, after.compareTo(BigDecimal.valueOf(15)));
        Assertions.assertEquals(0, a.getBalance().compareTo(BigDecimal.valueOf(15)));
    }

    @Test
    public void subtractIfEnoughSucceeds() {
        UUID id = UUID.randomUUID();
        Account a = new Account(id, BigDecimal.valueOf(10));
        BigDecimal after = a.subtractIfEnough(BigDecimal.valueOf(4));
        Assertions.assertNotNull(after);
        Assertions.assertEquals(0, after.compareTo(BigDecimal.valueOf(6)));
    }

    @Test
    public void subtractIfEnoughFailsWhenInsufficient() {
        UUID id = UUID.randomUUID();
        Account a = new Account(id, BigDecimal.valueOf(2));
        BigDecimal after = a.subtractIfEnough(BigDecimal.valueOf(5));
        Assertions.assertNull(after);
        Assertions.assertEquals(0, a.getBalance().compareTo(BigDecimal.valueOf(2)));
    }
}
