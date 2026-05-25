CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number VARCHAR(20) NOT NULL UNIQUE,
    owner_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_accounts_account_number ON accounts (account_number);
CREATE INDEX idx_accounts_status ON accounts (status);
