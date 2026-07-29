package com.bank.controller;

import com.bank.dto.AmountRequest;
import com.bank.dto.CreateAccountRequest;
import com.bank.dto.CreateAccountResponse;
import com.bank.dto.TransferRequest;
import com.bank.model.TransactionRecord;
import com.bank.service.AccountService;
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
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreateAccountResponse> create(@RequestBody(required = false) CreateAccountRequest req) {
        BigDecimal initial = req == null ? BigDecimal.ZERO : req.initialBalance();
        var account = service.createAccount(initial);
        return ResponseEntity.ok(new CreateAccountResponse(account.getId()));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, BigDecimal>> balance(@PathVariable("id") UUID id) {
        return service.getBalance(id)
                .map(b -> ResponseEntity.ok(Map.of("balance", b)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionRecord>> transactions(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.getTransactions(id));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<TransactionRecord> deposit(@PathVariable("id") UUID id, @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(service.deposit(id, req.amount(), req.description()));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<TransactionRecord> withdraw(@PathVariable("id") UUID id, @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(service.withdraw(id, req.amount(), req.description()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionRecord> transfer(@Valid @RequestBody TransferRequest req) {
        return ResponseEntity.ok(service.transfer(req.from(), req.to(), req.amount(), req.description()));
    }
}
