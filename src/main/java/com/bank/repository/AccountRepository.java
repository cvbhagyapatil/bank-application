package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.TransactionRecord;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class AccountRepository {
    // account storage
    private final ConcurrentHashMap<UUID, Account> accounts = new ConcurrentHashMap<>();
    // per-account locks to avoid race conditions during multi-step operations
    private final ConcurrentHashMap<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();
    // ledger per account
    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<TransactionRecord>> ledger = new ConcurrentHashMap<>();

    public Account createAccount(UUID id, BigDecimal initialBalance) {
        Account account = new Account(id, initialBalance == null ? BigDecimal.ZERO : initialBalance);
        accounts.put(id, account);
        locks.put(id, new ReentrantLock());
        ledger.put(id, new CopyOnWriteArrayList<>());
        return account;
    }

    public Optional<Account> findById(UUID id) {
        return Optional.ofNullable(accounts.get(id));
    }

    public ReentrantLock getLock(UUID id) {
        return locks.get(id);
    }

    public void addTransaction(UUID accountId, TransactionRecord record) {
        ledger.computeIfAbsent(accountId, k -> new CopyOnWriteArrayList<>()).add(record);
    }

    public List<TransactionRecord> getTransactions(UUID accountId) {
        return Collections.unmodifiableList(ledger.getOrDefault(accountId, new CopyOnWriteArrayList<>()));
    }

    public Collection<Account> listAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }
}
