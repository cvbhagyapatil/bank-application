package com.bank.service;

import com.bank.model.Account;
import com.bank.model.TransactionRecord;
import com.bank.model.TransactionType;
import com.bank.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account createAccount(BigDecimal initialBalance) {
        UUID id = UUID.randomUUID();
        return repository.createAccount(id, initialBalance == null ? BigDecimal.ZERO : initialBalance);
    }

    public Optional<BigDecimal> getBalance(UUID accountId) {
        return repository.findById(accountId).map(Account::getBalance);
    }

    public List<TransactionRecord> getTransactions(UUID accountId) {
        return repository.getTransactions(accountId);
    }
    public TransactionRecord deposit(UUID accountId, BigDecimal amount, String description) {
        Objects.requireNonNull(amount, "amount");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

        Account account = repository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        ReentrantLock lock = repository.getLock(accountId);
        lock.lock();
        try {
            BigDecimal postBalance = account.add(amount);
            TransactionRecord record = new TransactionRecord(UUID.randomUUID(), TransactionType.DEPOSIT, amount, Instant.now(), postBalance, description);
            repository.addTransaction(accountId, record);
            log.info("Deposited {} to account {} resulting balance={}", amount, accountId, postBalance);
            return record;
        } finally {
            lock.unlock();
        }
    }

    public TransactionRecord withdraw(UUID accountId, BigDecimal amount, String description) {
        Objects.requireNonNull(amount, "amount");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

        Account account = repository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        ReentrantLock lock = repository.getLock(accountId);
        lock.lock();
        try {
            BigDecimal post = account.subtractIfEnough(amount);
            if (post == null) throw new IllegalStateException("Insufficient funds");
            TransactionRecord record = new TransactionRecord(UUID.randomUUID(), TransactionType.WITHDRAWAL, amount, Instant.now(), post, description);
            repository.addTransaction(accountId, record);
            return record;
        } finally {
            lock.unlock();
        }
    }

    public TransactionRecord transfer(UUID fromId, UUID toId, BigDecimal amount, String description) {
        Objects.requireNonNull(amount, "amount");
        if (fromId.equals(toId)) throw new IllegalArgumentException("from and to must be different");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

        Account from = repository.findById(fromId).orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account to = repository.findById(toId).orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        // To prevent deadlock, lock in UUID order
        ReentrantLock lock1 = repository.getLock(fromId);
        ReentrantLock lock2 = repository.getLock(toId);
        ReentrantLock first = fromId.compareTo(toId) < 0 ? lock1 : lock2;
        ReentrantLock second = first == lock1 ? lock2 : lock1;

        first.lock();
        second.lock();
        try {
            BigDecimal postFrom = from.subtractIfEnough(amount);
            if (postFrom == null) throw new IllegalStateException("Insufficient funds in source account");
            BigDecimal postTo = to.add(amount);

            TransactionRecord record = new TransactionRecord(UUID.randomUUID(), TransactionType.TRANSFER, amount, Instant.now(), postFrom, description + " -> to " + toId);
            repository.addTransaction(fromId, record);
            TransactionRecord creditRecord = new TransactionRecord(UUID.randomUUID(), TransactionType.DEPOSIT, amount, Instant.now(), postTo, description + " <- from " + fromId);
            repository.addTransaction(toId, creditRecord);
            return record;
        } finally {
            second.unlock();
            first.unlock();
        }
    }
}
