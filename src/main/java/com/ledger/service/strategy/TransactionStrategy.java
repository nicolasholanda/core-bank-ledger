package com.ledger.service.strategy;

import com.ledger.domain.Account;
import com.ledger.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionStrategy {

    List<Transaction> execute(Account account, BigDecimal amount, String description, Account relatedAccount);
}
