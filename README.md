# Core Bank Ledger

A core banking ledger built with Java and Spring Boot. Supports account management and financial transactions — deposits, withdrawals, and transfers — with full transaction history and balance tracking.

## Tech Stack

- Java 21
- Spring Boot 3.4
- PostgreSQL 16
- Flyway (schema migrations)
- Spring Data JPA / Hibernate
- Lombok
- Bean Validation
- JUnit 5 + Mockito (unit tests)
- Testcontainers (integration tests)
- Maven

## Features

- Create checking and savings accounts
- Deposit and withdraw funds with balance validation
- Transfer between accounts (creates paired TRANSFER_OUT / TRANSFER_IN records)
- Freeze or close accounts
- Query full transaction history per account, optionally filtered by type
- Consistent JSON error responses for all failure cases

## Design Patterns

- **Strategy** — `TransactionStrategy` interface with separate implementations for deposit, withdrawal, and transfer logic
- **Factory** — `TransactionStrategyFactory` resolves the correct strategy from the transaction type
- **Builder** — Lombok `@Builder` on all domain entities
- **Repository** — Spring Data JPA repositories with custom query methods

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/accounts` | Create account |
| GET | `/api/accounts/{id}` | Get account by id |
| GET | `/api/accounts/number/{number}` | Get account by account number |
| PATCH | `/api/accounts/{id}/freeze` | Freeze account |
| PATCH | `/api/accounts/{id}/close` | Close account |
| POST | `/api/transactions/deposit` | Deposit funds |
| POST | `/api/transactions/withdraw` | Withdraw funds |
| POST | `/api/transactions/transfer` | Transfer between accounts |
| GET | `/api/transactions/account/{accountId}` | Transaction history (optional `?type=` filter) |

## How to Run

**Prerequisites:** Docker, Java 21, Maven

```bash
# Start the database
docker compose up -d

# Run the application
./mvnw spring-boot:run

# Run tests (Testcontainers spins up its own DB)
./mvnw test
```

The API will be available at `http://localhost:8080`.
