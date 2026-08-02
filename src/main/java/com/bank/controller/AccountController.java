package com.bank.controller;

import com.bank.dto.AmountRequest;
import com.bank.dto.CreateAccountRequest;
import com.bank.dto.CreateAccountResponse;
import com.bank.dto.TransferRequest;
import com.bank.model.TransactionRecord;
import com.bank.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreateAccountResponse> create(@Valid @RequestBody(required = false) CreateAccountRequest req) {
        BigDecimal initial = req == null ? BigDecimal.ZERO : req.initialBalance();
        log.info("Create account request received with initial={}", initial);
        var account = service.createAccount(initial);
        return ResponseEntity.ok(new CreateAccountResponse(account.getId()));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, BigDecimal>> balance(@PathVariable("id") UUID id) {
        log.info("Balance requested for account={}", id);
        return service.getBalance(id)
                .map(b -> ResponseEntity.ok(Map.of("balance", b)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionRecord>> transactions(@PathVariable("id") UUID id) {
        log.info("Transactions requested for account={}", id);
        return ResponseEntity.ok(service.getTransactions(id));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<TransactionRecord> deposit(@PathVariable("id") UUID id, @Valid @RequestBody AmountRequest req) {
        log.info("Deposit request to account={} amount={}", id, req.amount());
        return ResponseEntity.ok(service.deposit(id, req.amount(), req.description()));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<TransactionRecord> withdraw(@PathVariable("id") UUID id, @Valid @RequestBody AmountRequest req) {
        log.info("Withdraw request from account={} amount={}", id, req.amount());
        return ResponseEntity.ok(service.withdraw(id, req.amount(), req.description()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionRecord> transfer(@Valid @RequestBody TransferRequest req) {
        log.info("Transfer request from={} to={} amount={}", req.from(), req.to(), req.amount());
        return ResponseEntity.ok(service.transfer(req.from(), req.to(), req.amount(), req.description()));
    }
}
