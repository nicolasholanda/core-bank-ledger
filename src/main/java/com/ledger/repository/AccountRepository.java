package com.ledger.repository;

import com.ledger.domain.Account;
import com.ledger.domain.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByStatus(AccountStatus status);

    boolean existsByAccountNumber(String accountNumber);
}
