package com.ledger.controller;

import com.ledger.dto.AccountRequest;
import com.ledger.dto.AccountResponse;
import com.ledger.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody AccountRequest request) {
        return AccountResponse.from(accountService.create(request.ownerName(), request.accountType()));
    }

    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable UUID id) {
        return AccountResponse.from(accountService.findById(id));
    }

    @GetMapping("/number/{accountNumber}")
    public AccountResponse findByAccountNumber(@PathVariable String accountNumber) {
        return AccountResponse.from(accountService.findByAccountNumber(accountNumber));
    }

    @PatchMapping("/{id}/freeze")
    public AccountResponse freeze(@PathVariable UUID id) {
        return AccountResponse.from(accountService.freeze(id));
    }

    @PatchMapping("/{id}/close")
    public AccountResponse close(@PathVariable UUID id) {
        return AccountResponse.from(accountService.close(id));
    }
}
