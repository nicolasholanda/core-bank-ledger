package com.ledger.service.strategy;

import com.ledger.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionStrategyFactory {

    private final DepositStrategy depositStrategy;
    private final WithdrawalStrategy withdrawalStrategy;
    private final TransferStrategy transferStrategy;

    public TransactionStrategy getStrategy(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> depositStrategy;
            case WITHDRAWAL -> withdrawalStrategy;
            case TRANSFER_IN, TRANSFER_OUT -> transferStrategy;
        };
    }
}
