-- 1. Sequences
CREATE SEQUENCE cash_balances_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE positions_seq START WITH 1 INCREMENT BY 50;

-- 2. Tables & Unique Constraints
CREATE TABLE cash_balances (
    id BIGINT PRIMARY KEY DEFAULT nextval('cash_balances_seq'),
    user_id VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount NUMERIC(38, 8) NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    CONSTRAINT uk_cash_user_currency UNIQUE (user_id, currency)
);

CREATE TABLE positions (
    id BIGINT PRIMARY KEY DEFAULT nextval('positions_seq'),
    user_id VARCHAR(255) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    asset_class VARCHAR(30) NOT NULL,
    quantity NUMERIC(38, 8) NOT NULL,
    avg_cost NUMERIC(38, 8) NOT NULL,
    current_price NUMERIC(38, 8) NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    CONSTRAINT uk_position_user_symbol UNIQUE (user_id, symbol)
);

-- 3. Dedicated Indexes
CREATE INDEX idx_cash_balances_user_id ON cash_balances(user_id);
CREATE INDEX idx_positions_user_id ON positions(user_id);