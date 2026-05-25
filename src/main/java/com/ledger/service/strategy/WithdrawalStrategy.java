package com.ledger.service.strategy;

import com.ledger.domain.Account;
import com.ledger.domain.AccountStatus;
import com.ledger.domain.Transaction;
import com.ledger.domain.TransactionStatus;
import com.ledger.domain.TransactionType;
import com.ledger.exception.AccountNotActiveException;
import com.ledger.exception.InsufficientFundsException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class WithdrawalStrategy implements TransactionStrategy {

    @Override
    public List<Transaction> execute(Account account, BigDecimal amount, String description, Account relatedAccount) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(account.getId(), account.getStatus());
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(account.getId(), account.getBalance(), amount);
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(amount)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .description(description)
                .build();

        return List.of(transaction);
    }
}
