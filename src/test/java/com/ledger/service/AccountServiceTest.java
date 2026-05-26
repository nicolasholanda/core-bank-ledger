package com.ledger.service;

import com.ledger.domain.Account;
import com.ledger.domain.AccountStatus;
import com.ledger.domain.AccountType;
import com.ledger.exception.AccountNotFoundException;
import com.ledger.exception.AccountNotActiveException;
import com.ledger.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void create_shouldSaveAccountWithCorrectFields() {
        Account saved = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("1234567890")
                .ownerName("John Doe")
                .accountType(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any())).thenReturn(saved);

        Account result = accountService.create("John Doe", AccountType.CHECKING);

        assertThat(result.getOwnerName()).isEqualTo("John Doe");
        assertThat(result.getAccountType()).isEqualTo(AccountType.CHECKING);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void findById_shouldReturnAccount_whenExists() {
        UUID id = UUID.randomUUID();
        Account account = Account.builder().id(id).build();
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        Account result = accountService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldThrowAccountNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findById(id))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void findByAccountNumber_shouldReturnAccount_whenExists() {
        String number = "0000000001";
        Account account = Account.builder().accountNumber(number).build();
        when(accountRepository.findByAccountNumber(number)).thenReturn(Optional.of(account));

        Account result = accountService.findByAccountNumber(number);

        assertThat(result.getAccountNumber()).isEqualTo(number);
    }

    @Test
    void findByAccountNumber_shouldThrowAccountNotFoundException_whenNotFound() {
        when(accountRepository.findByAccountNumber(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findByAccountNumber("9999999999"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void freeze_shouldSetStatusToFrozen_whenAccountIsActive() {
        UUID id = UUID.randomUUID();
        Account account = Account.builder().id(id).status(AccountStatus.ACTIVE).build();
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.freeze(id);

        assertThat(result.getStatus()).isEqualTo(AccountStatus.FROZEN);
    }

    @Test
    void freeze_shouldAlsoWorkWhenAccountIsAlreadyFrozen() {
        UUID id = UUID.randomUUID();
        Account account = Account.builder().id(id).status(AccountStatus.FROZEN).build();
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.freeze(id);

        assertThat(result.getStatus()).isEqualTo(AccountStatus.FROZEN);
    }

    @Test
    void freeze_shouldThrowAccountNotActiveException_whenAccountIsClosed() {
        UUID id = UUID.randomUUID();
        Account account = Account.builder().id(id).status(AccountStatus.CLOSED).build();
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.freeze(id))
                .isInstanceOf(AccountNotActiveException.class);
    }

    @Test
    void close_shouldSetStatusToClosed() {
        UUID id = UUID.randomUUID();
        Account account = Account.builder().id(id).status(AccountStatus.ACTIVE).build();
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.close(id);

        assertThat(result.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }
}
