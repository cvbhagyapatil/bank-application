package com.bank.service;

import com.bank.model.TransactionRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AccountServiceTest {

    @Test
    public void testDepositWithdrawTransfer() {
        AccountService service = new AccountService();

        var a1 = service.createAccount(BigDecimal.valueOf(100));
        var a2 = service.createAccount(BigDecimal.valueOf(50));

        UUID id1 = a1.getId();
        UUID id2 = a2.getId();

        TransactionRecord d = service.deposit(id1, BigDecimal.valueOf(25), "deposit1");
        Assertions.assertEquals(0, d.postBalance().compareTo(BigDecimal.valueOf(125)));

        TransactionRecord w = service.withdraw(id1, BigDecimal.valueOf(50), "withdraw1");
        Assertions.assertEquals(0, w.postBalance().compareTo(BigDecimal.valueOf(75)));

        TransactionRecord t = service.transfer(id1, id2, BigDecimal.valueOf(25), "transfer1");
        Assertions.assertEquals(0, t.postBalance().compareTo(BigDecimal.valueOf(50)));

        var b1 = service.getBalance(id1).orElse(BigDecimal.ZERO);
        var b2 = service.getBalance(id2).orElse(BigDecimal.ZERO);

        Assertions.assertEquals(0, b1.compareTo(BigDecimal.valueOf(50)));
        Assertions.assertEquals(0, b2.compareTo(BigDecimal.valueOf(75)));
    }

    @Test
    public void testConcurrentDepositsWithVirtualThreads() throws InterruptedException, ExecutionException {
        AccountService service = new AccountService();
        var account = service.createAccount(BigDecimal.valueOf(0));
        UUID id = account.getId();

        int tasks = 200;
        BigDecimal per = BigDecimal.valueOf(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Void>> callables = new ArrayList<>();
            for (int i = 0; i < tasks; i++) {
                callables.add(() -> {
                    service.deposit(id, per, "vt-deposit");
                    return null;
                });
            }

            List<Future<Void>> futures = executor.invokeAll(callables);
            for (Future<Void> f : futures) f.get();
        }

        var finalBalance = service.getBalance(id).orElse(BigDecimal.ZERO);
        Assertions.assertEquals(0, finalBalance.compareTo(BigDecimal.valueOf(tasks)));
    }

    @Test
    public void invalidAmountAndErrors() {
        AccountService service = new AccountService();
        var acc = service.createAccount(BigDecimal.valueOf(10));
        UUID id = acc.getId();

        // negative amount
        try {
            service.deposit(id, BigDecimal.valueOf(-1), "bad");
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        // withdraw too much
        try {
            service.withdraw(id, BigDecimal.valueOf(1000), "over");
            Assertions.fail("expected insufficient funds");
        } catch (IllegalStateException ex) {
            // expected
        }

        // transfer to same
        try {
            service.transfer(id, id, BigDecimal.valueOf(1), "x");
            Assertions.fail("expected illegal arg");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }
}
