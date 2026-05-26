package com.ledger.dto;

import com.ledger.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountRequest(
        @NotBlank(message = "Owner name is required") String ownerName,
        @NotNull(message = "Account type is required") AccountType accountType
) {}
