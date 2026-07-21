package com.bank.controller;

import com.bank.dto.AmountRequest;
import com.bank.dto.CreateAccountRequest;
import com.bank.dto.CreateAccountResponse;
import com.bank.dto.TransferRequest;
import com.bank.model.TransactionRecord;
import com.bank.model.TransactionType;
import com.bank.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService service;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void createAccountReturnsId() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(service.createAccount(any())).thenReturn(new com.bank.model.Account(id, BigDecimal.ZERO));

        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new CreateAccountResponse(id))));
    }

    @Test
    public void getBalanceNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(service.getBalance(eq(id))).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/api/accounts/" + id + "/balance")).andExpect(status().isNotFound());
    }

    @Test
    public void getTransactions() throws Exception {
        UUID id = UUID.randomUUID();
        TransactionRecord r = new TransactionRecord(UUID.randomUUID(), TransactionType.DEPOSIT, BigDecimal.TEN, Instant.now(), BigDecimal.TEN, "d");
        Mockito.when(service.getTransactions(eq(id))).thenReturn(List.of(r));
        mockMvc.perform(get("/api/accounts/" + id + "/transactions")).andExpect(status().isOk());
    }

    @Test
    public void depositEndpoint() throws Exception {
        UUID id = UUID.randomUUID();
        AmountRequest req = new AmountRequest(BigDecimal.TEN, "d");
        TransactionRecord r = new TransactionRecord(UUID.randomUUID(), TransactionType.DEPOSIT, BigDecimal.TEN, Instant.now(), BigDecimal.TEN, "d");
        Mockito.when(service.deposit(eq(id), eq(BigDecimal.TEN), any())).thenReturn(r);

        mockMvc.perform(post("/api/accounts/" + id + "/deposit").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    public void transferEndpoint() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        TransferRequest req = new TransferRequest(from, to, BigDecimal.ONE, "t");
        TransactionRecord r = new TransactionRecord(UUID.randomUUID(), TransactionType.TRANSFER, BigDecimal.ONE, Instant.now(), BigDecimal.ZERO, "t");
        Mockito.when(service.transfer(eq(from), eq(to), eq(BigDecimal.ONE), any())).thenReturn(r);

        mockMvc.perform(post("/api/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
