package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.TransactionRecord;
import com.bank.model.TransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AccountRepositoryTest {

    @Test
    public void createAndFindAccount() {
        AccountRepository repo = new AccountRepository();
        UUID id = UUID.randomUUID();
        Account a = repo.createAccount(id, BigDecimal.valueOf(12.5));
        Assertions.assertEquals(id, a.getId());
        Assertions.assertTrue(repo.findById(id).isPresent());
    }

    @Test
    public void ledgerAndLocksAreCreated() {
        AccountRepository repo = new AccountRepository();
        UUID id = UUID.randomUUID();
        repo.createAccount(id, BigDecimal.ZERO);
        Assertions.assertNotNull(repo.getLock(id));
        List<TransactionRecord> txs = repo.getTransactions(id);
        Assertions.assertNotNull(txs);

        TransactionRecord r = new TransactionRecord(UUID.randomUUID(), TransactionType.DEPOSIT, BigDecimal.TEN, Instant.now(), BigDecimal.TEN, "desc");
        repo.addTransaction(id, r);
        Assertions.assertEquals(1, repo.getTransactions(id).size());
    }

    @Test
    public void listAccountsReturnsCollection() {
        AccountRepository repo = new AccountRepository();
        repo.createAccount(UUID.randomUUID(), BigDecimal.ONE);
        repo.createAccount(UUID.randomUUID(), BigDecimal.ONE);
        Assertions.assertTrue(repo.listAccounts().size() >= 2);
    }
}
