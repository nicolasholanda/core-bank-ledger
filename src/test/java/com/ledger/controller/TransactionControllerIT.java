package com.ledger.controller;

import com.ledger.domain.AccountType;
import com.ledger.domain.TransactionType;
import com.ledger.dto.AccountRequest;
import com.ledger.dto.AccountResponse;
import com.ledger.dto.TransactionRequest;
import com.ledger.dto.TransactionResponse;
import com.ledger.dto.TransferRequest;
import com.ledger.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TransactionControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID accountId;
    private UUID secondAccountId;

    @BeforeEach
    void setUp() {
        AccountResponse account = restTemplate.postForObject(
                "/api/accounts", new AccountRequest("Test User", AccountType.CHECKING), AccountResponse.class);
        accountId = account.id();

        AccountResponse second = restTemplate.postForObject(
                "/api/accounts", new AccountRequest("Second User", AccountType.CHECKING), AccountResponse.class);
        secondAccountId = second.id();
    }

    @Test
    void deposit_shouldReturn201_andIncreaseBalance() {
        TransactionRequest request = new TransactionRequest(accountId, new BigDecimal("500.00"), "Initial deposit");

        ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(
                "/api/transactions/deposit", request, TransactionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.getBody().amount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void withdraw_shouldReturn201_whenSufficientFunds() {
        restTemplate.postForObject("/api/transactions/deposit",
                new TransactionRequest(accountId, new BigDecimal("1000.00"), "Funding"), TransactionResponse.class);

        TransactionRequest request = new TransactionRequest(accountId, new BigDecimal("200.00"), "Withdrawal");

        ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(
                "/api/transactions/withdraw", request, TransactionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().type()).isEqualTo(TransactionType.WITHDRAWAL);
    }

    @Test
    void withdraw_shouldReturn422_whenInsufficientFunds() {
        TransactionRequest request = new TransactionRequest(accountId, new BigDecimal("9999.00"), "Over limit");

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/transactions/withdraw", request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().error()).isEqualTo("Insufficient Funds");
    }

    @Test
    void transfer_shouldReturn201_andCreateTwoTransactions() {
        restTemplate.postForObject("/api/transactions/deposit",
                new TransactionRequest(accountId, new BigDecimal("500.00"), "Funding"), TransactionResponse.class);

        TransferRequest request = new TransferRequest(accountId, secondAccountId, new BigDecimal("100.00"), "Transfer");

        ResponseEntity<TransactionResponse[]> response = restTemplate.postForEntity(
                "/api/transactions/transfer", request, TransactionResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()[0].type()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(response.getBody()[1].type()).isEqualTo(TransactionType.TRANSFER_IN);
    }

    @Test
    void findByAccount_shouldReturnTransactionHistory() {
        restTemplate.postForObject("/api/transactions/deposit",
                new TransactionRequest(accountId, new BigDecimal("300.00"), "Deposit"), TransactionResponse.class);

        ResponseEntity<TransactionResponse[]> response = restTemplate.getForEntity(
                "/api/transactions/account/" + accountId, TransactionResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void deposit_shouldReturn400_whenAmountIsMissing() {
        String payload = "{\"accountId\":\"" + UUID.randomUUID() + "\"}";

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/transactions/deposit",
                new org.springframework.http.HttpEntity<>(payload,
                        new org.springframework.http.HttpHeaders() {{ setContentType(org.springframework.http.MediaType.APPLICATION_JSON); }}),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
