package com.ledger.service;

import com.ledger.domain.Account;
import com.ledger.domain.AccountStatus;
import com.ledger.domain.Transaction;
import com.ledger.domain.TransactionStatus;
import com.ledger.domain.TransactionType;
import com.ledger.exception.AccountNotActiveException;
import com.ledger.exception.InsufficientFundsException;
import com.ledger.repository.TransactionRepository;
import com.ledger.service.strategy.TransactionStrategy;
import com.ledger.service.strategy.TransactionStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionStrategyFactory strategyFactory;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionStrategy transactionStrategy;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void deposit_shouldExecuteStrategyAndReturnTransaction() {
        UUID accountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        Account account = Account.builder().id(accountId).status(AccountStatus.ACTIVE).balance(BigDecimal.ZERO).build();
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .account(account)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .build();

        when(accountService.findById(accountId)).thenReturn(account);
        when(strategyFactory.getStrategy(TransactionType.DEPOSIT)).thenReturn(transactionStrategy);
        when(transactionStrategy.execute(account, amount, null, null)).thenReturn(List.of(transaction));
        when(accountService.save(account)).thenReturn(account);
        when(transactionRepository.saveAll(List.of(transaction))).thenReturn(List.of(transaction));

        Transaction result = transactionService.deposit(accountId, amount, null);

        assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.getAmount()).isEqualByComparingTo(amount);
    }

    @Test
    void deposit_shouldPropagateAccountNotActiveException_whenAccountNotActive() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).status(AccountStatus.FROZEN).balance(BigDecimal.ZERO).build();

        when(accountService.findById(accountId)).thenReturn(account);
        when(strategyFactory.getStrategy(TransactionType.DEPOSIT)).thenReturn(transactionStrategy);
        when(transactionStrategy.execute(any(), any(), any(), any()))
                .thenThrow(new AccountNotActiveException(accountId, AccountStatus.FROZEN));

        assertThatThrownBy(() -> transactionService.deposit(accountId, new BigDecimal("100.00"), null))
                .isInstanceOf(AccountNotActiveException.class);
    }

    @Test
    void withdraw_shouldExecuteStrategyAndReturnTransaction() {
        UUID accountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");
        Account account = Account.builder().id(accountId).status(AccountStatus.ACTIVE).balance(new BigDecimal("200.00")).build();
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .account(account)
                .amount(amount)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .build();

        when(accountService.findById(accountId)).thenReturn(account);
        when(strategyFactory.getStrategy(TransactionType.WITHDRAWAL)).thenReturn(transactionStrategy);
        when(transactionStrategy.execute(account, amount, null, null)).thenReturn(List.of(transaction));
        when(accountService.save(account)).thenReturn(account);
        when(transactionRepository.saveAll(List.of(transaction))).thenReturn(List.of(transaction));

        Transaction result = transactionService.withdraw(accountId, amount, null);

        assertThat(result.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(result.getAmount()).isEqualByComparingTo(amount);
    }

    @Test
    void withdraw_shouldPropagateInsufficientFundsException_whenBalanceTooLow() {
        UUID accountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500.00");
        Account account = Account.builder().id(accountId).status(AccountStatus.ACTIVE).balance(new BigDecimal("100.00")).build();

        when(accountService.findById(accountId)).thenReturn(account);
        when(strategyFactory.getStrategy(TransactionType.WITHDRAWAL)).thenReturn(transactionStrategy);
        when(transactionStrategy.execute(any(), any(), any(), any()))
                .thenThrow(new InsufficientFundsException(accountId, new BigDecimal("100.00"), amount));

        assertThatThrownBy(() -> transactionService.withdraw(accountId, amount, null))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void transfer_shouldExecuteStrategyAndReturnBothTransactions() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200.00");
        Account source = Account.builder().id(sourceId).status(AccountStatus.ACTIVE).balance(new BigDecimal("500.00")).build();
        Account target = Account.builder().id(targetId).status(AccountStatus.ACTIVE).balance(BigDecimal.ZERO).build();
        Transaction outgoing = Transaction.builder().id(UUID.randomUUID()).account(source).type(TransactionType.TRANSFER_OUT).amount(amount).build();
        Transaction incoming = Transaction.builder().id(UUID.randomUUID()).account(target).type(TransactionType.TRANSFER_IN).amount(amount).build();

        when(accountService.findById(sourceId)).thenReturn(source);
        when(accountService.findById(targetId)).thenReturn(target);
        when(strategyFactory.getStrategy(TransactionType.TRANSFER_OUT)).thenReturn(transactionStrategy);
        when(transactionStrategy.execute(source, amount, null, target)).thenReturn(List.of(outgoing, incoming));
        when(accountService.saveAll(any())).thenReturn(List.of(source, target));
        when(transactionRepository.saveAll(any())).thenReturn(List.of(outgoing, incoming));

        List<Transaction> result = transactionService.transfer(sourceId, targetId, amount, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(result.get(1).getType()).isEqualTo(TransactionType.TRANSFER_IN);
    }

    @Test
    void findByAccount_shouldDelegateToRepository() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).build();
        Transaction transaction = Transaction.builder().id(UUID.randomUUID()).account(account).build();
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId)).thenReturn(List.of(transaction));

        List<Transaction> result = transactionService.findByAccount(accountId);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByAccountAndType_shouldDelegateToRepository() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).build();
        Transaction transaction = Transaction.builder().id(UUID.randomUUID()).account(account).type(TransactionType.DEPOSIT).build();
        when(transactionRepository.findByAccountIdAndTypeOrderByCreatedAtDesc(accountId, TransactionType.DEPOSIT))
                .thenReturn(List.of(transaction));

        List<Transaction> result = transactionService.findByAccountAndType(accountId, TransactionType.DEPOSIT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(TransactionType.DEPOSIT);
    }
}
