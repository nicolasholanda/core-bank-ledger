package com.ledger.service;

import com.ledger.domain.Account;
import com.ledger.domain.AccountStatus;
import com.ledger.domain.AccountType;
import com.ledger.exception.AccountNotFoundException;
import com.ledger.exception.AccountNotActiveException;
import com.ledger.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    public Account create(String ownerName, AccountType accountType) {
        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .ownerName(ownerName)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account findById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    public Account freeze(UUID id) {
        Account account = findById(id);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountNotActiveException(id, account.getStatus());
        }
        account.setStatus(AccountStatus.FROZEN);
        return accountRepository.save(account);
    }

    public Account close(UUID id) {
        Account account = findById(id);
        account.setStatus(AccountStatus.CLOSED);
        return accountRepository.save(account);
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public List<Account> saveAll(List<Account> accounts) {
        return accountRepository.saveAll(accounts);
    }

    private String generateUniqueAccountNumber() {
        String number;
        do {
            number = String.format("%010d", ThreadLocalRandom.current().nextLong(0, 10_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
