package com.ledger.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID id) {
        super("Account not found with id: " + id);
    }

    public AccountNotFoundException(String accountNumber) {
        super("Account not found with number: " + accountNumber);
    }
}
