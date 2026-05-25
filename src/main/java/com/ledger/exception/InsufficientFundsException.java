package com.ledger.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID accountId, BigDecimal balance, BigDecimal requested) {
        super("Account " + accountId + " has insufficient funds. Balance: " + balance + ", requested: " + requested);
    }
}
