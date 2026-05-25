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
public class TransferStrategy implements TransactionStrategy {

    @Override
    public List<Transaction> execute(Account source, BigDecimal amount, String description, Account target) {
        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(source.getId(), source.getStatus());
        }

        if (target.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(target.getId(), target.getStatus());
        }

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(source.getId(), source.getBalance(), amount);
        }

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        Transaction outgoing = Transaction.builder()
                .account(source)
                .relatedAccountId(target.getId())
                .amount(amount)
                .type(TransactionType.TRANSFER_OUT)
                .status(TransactionStatus.COMPLETED)
                .description(description)
                .build();

        Transaction incoming = Transaction.builder()
                .account(target)
                .relatedAccountId(source.getId())
                .amount(amount)
                .type(TransactionType.TRANSFER_IN)
                .status(TransactionStatus.COMPLETED)
                .description(description)
                .build();

        return List.of(outgoing, incoming);
    }
}
