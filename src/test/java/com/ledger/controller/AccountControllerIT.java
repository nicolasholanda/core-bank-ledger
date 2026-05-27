package com.ledger.controller;

import com.ledger.domain.AccountStatus;
import com.ledger.domain.AccountType;
import com.ledger.dto.AccountRequest;
import com.ledger.dto.AccountResponse;
import com.ledger.exception.ErrorResponse;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AccountControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAccount_shouldReturn201_withCorrectFields() {
        AccountRequest request = new AccountRequest("Jane Doe", AccountType.SAVINGS);

        ResponseEntity<AccountResponse> response = restTemplate.postForEntity(
                "/api/accounts", request, AccountResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().ownerName()).isEqualTo("Jane Doe");
        assertThat(response.getBody().accountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(response.getBody().status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.getBody().accountNumber()).hasSize(10);
    }

    @Test
    void findById_shouldReturn200_whenAccountExists() {
        AccountRequest request = new AccountRequest("John Smith", AccountType.CHECKING);
        AccountResponse created = restTemplate.postForObject("/api/accounts", request, AccountResponse.class);

        ResponseEntity<AccountResponse> response = restTemplate.getForEntity(
                "/api/accounts/" + created.id(), AccountResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(created.id());
    }

    @Test
    void findById_shouldReturn404_whenAccountDoesNotExist() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/api/accounts/" + UUID.randomUUID(), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void findByAccountNumber_shouldReturn200_whenAccountExists() {
        AccountRequest request = new AccountRequest("Alice", AccountType.CHECKING);
        AccountResponse created = restTemplate.postForObject("/api/accounts", request, AccountResponse.class);

        ResponseEntity<AccountResponse> response = restTemplate.getForEntity(
                "/api/accounts/number/" + created.accountNumber(), AccountResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().accountNumber()).isEqualTo(created.accountNumber());
    }

    @Test
    void freeze_shouldReturn200_andSetStatusToFrozen() {
        AccountRequest request = new AccountRequest("Bob", AccountType.CHECKING);
        AccountResponse created = restTemplate.postForObject("/api/accounts", request, AccountResponse.class);

        restTemplate.patchForObject("/api/accounts/" + created.id() + "/freeze", null, AccountResponse.class);

        ResponseEntity<AccountResponse> response = restTemplate.getForEntity(
                "/api/accounts/" + created.id(), AccountResponse.class);

        assertThat(response.getBody().status()).isEqualTo(AccountStatus.FROZEN);
    }

    @Test
    void close_shouldReturn200_andSetStatusToClosed() {
        AccountRequest request = new AccountRequest("Carol", AccountType.SAVINGS);
        AccountResponse created = restTemplate.postForObject("/api/accounts", request, AccountResponse.class);

        restTemplate.patchForObject("/api/accounts/" + created.id() + "/close", null, AccountResponse.class);

        ResponseEntity<AccountResponse> response = restTemplate.getForEntity(
                "/api/accounts/" + created.id(), AccountResponse.class);

        assertThat(response.getBody().status()).isEqualTo(AccountStatus.CLOSED);
    }
}
