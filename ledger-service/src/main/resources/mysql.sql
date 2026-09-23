-- Idempotency table
CREATE TABLE IF NOT EXISTS processed_trades (
    trade_id VARCHAR(100) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index (processed_trades)  for auditing or retention/cleanup jobs
CREATE INDEX IF NOT EXISTS idx_processed_trades_processed_at 
    ON processed_trades (processed_at ASC);

-- Ledger entries
CREATE SEQUENCE ledger_entries_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE ledger_entries (
    id BIGINT PRIMARY KEY DEFAULT nextval('ledger_entries_seq'),

    trade_id VARCHAR(100) NOT NULL UNIQUE,
    user_id VARCHAR(100) NOT NULL,

    symbol VARCHAR(50) NOT NULL,
    side VARCHAR(10) NOT NULL,
    asset VARCHAR(50),

    quantity NUMERIC(38, 18) NOT NULL,
    price NUMERIC(38, 18) NOT NULL,
    cash_amount NUMERIC(38, 18) NOT NULL,

    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_entries_user_id ON ledger_entries(user_id);
CREATE INDEX idx_ledger_entries_trade_id ON ledger_entries(trade_id);

-- Account balances
CREATE SEQUENCE account_balances_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE account_balances (
    id BIGINT PRIMARY KEY DEFAULT nextval('account_balances_seq'),

    user_id VARCHAR(100) NOT NULL,
    symbol VARCHAR(50) NOT NULL,

    position_quantity NUMERIC(38, 18) NOT NULL DEFAULT 0,
    cash_balance NUMERIC(38, 18) NOT NULL DEFAULT 0,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_user_symbol UNIQUE (user_id, symbol)
);

CREATE INDEX idx_account_balances_user_id ON account_balances(user_id);

--- Drop tables for cleanup (if needed)
DROP TABLE IF EXISTS ledger_entries CASCADE;
DROP TABLE IF EXISTS account_balances CASCADE;
DROP TABLE IF EXISTS processed_trades CASCADE;

DROP SEQUENCE IF EXISTS ledger_entries_seq;
DROP SEQUENCE IF EXISTS account_balances_seq;