package com.ledger.exception;

import com.ledger.domain.AccountStatus;

import java.util.UUID;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(UUID accountId, AccountStatus status) {
        super("Account " + accountId + " is not active. Current status: " + status);
    }
}
