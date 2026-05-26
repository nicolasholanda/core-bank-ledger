package com.ledger.service;

import com.ledger.domain.Account;
import com.ledger.domain.Transaction;
import com.ledger.domain.TransactionType;
import com.ledger.repository.TransactionRepository;
import com.ledger.service.strategy.TransactionStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionStrategyFactory strategyFactory;
    private final AccountService accountService;

    public Transaction deposit(UUID accountId, BigDecimal amount, String description) {
        Account account = accountService.findById(accountId);
        List<Transaction> transactions = strategyFactory.getStrategy(TransactionType.DEPOSIT)
                .execute(account, amount, description, null);
        accountService.save(account);
        return transactionRepository.saveAll(transactions).get(0);
    }

    public Transaction withdraw(UUID accountId, BigDecimal amount, String description) {
        Account account = accountService.findById(accountId);
        List<Transaction> transactions = strategyFactory.getStrategy(TransactionType.WITHDRAWAL)
                .execute(account, amount, description, null);
        accountService.save(account);
        return transactionRepository.saveAll(transactions).get(0);
    }

    public List<Transaction> transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, String description) {
        Account source = accountService.findById(sourceAccountId);
        Account target = accountService.findById(targetAccountId);
        List<Transaction> transactions = strategyFactory.getStrategy(TransactionType.TRANSFER_OUT)
                .execute(source, amount, description, target);
        accountService.saveAll(List.of(source, target));
        return transactionRepository.saveAll(transactions);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByAccount(UUID accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByAccountAndType(UUID accountId, TransactionType type) {
        return transactionRepository.findByAccountIdAndTypeOrderByCreatedAtDesc(accountId, type);
    }
}
