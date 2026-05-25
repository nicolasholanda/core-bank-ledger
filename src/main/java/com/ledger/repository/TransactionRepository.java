package com.ledger.repository;

import com.ledger.domain.Transaction;
import com.ledger.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    List<Transaction> findByAccountIdAndTypeOrderByCreatedAtDesc(UUID accountId, TransactionType type);
}
