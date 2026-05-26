package com.ledger.controller;

import com.ledger.domain.TransactionType;
import com.ledger.dto.TransactionRequest;
import com.ledger.dto.TransactionResponse;
import com.ledger.dto.TransferRequest;
import com.ledger.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(@Valid @RequestBody TransactionRequest request) {
        return TransactionResponse.from(
                transactionService.deposit(request.accountId(), request.amount(), request.description())
        );
    }

    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse withdraw(@Valid @RequestBody TransactionRequest request) {
        return TransactionResponse.from(
                transactionService.withdraw(request.accountId(), request.amount(), request.description())
        );
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public List<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(
                request.sourceAccountId(),
                request.targetAccountId(),
                request.amount(),
                request.description()
        ).stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionResponse> findByAccount(
            @PathVariable UUID accountId,
            @RequestParam(required = false) TransactionType type) {
        if (type != null) {
            return transactionService.findByAccountAndType(accountId, type)
                    .stream().map(TransactionResponse::from).toList();
        }
        return transactionService.findByAccount(accountId)
                .stream().map(TransactionResponse::from).toList();
    }
}
