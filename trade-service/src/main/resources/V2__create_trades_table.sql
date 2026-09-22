CREATE SEQUENCE trades_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE trades (
    id BIGINT PRIMARY KEY DEFAULT nextval('trades_seq'),

    trade_id VARCHAR(100) NOT NULL UNIQUE,
    user_id VARCHAR(100) NOT NULL,

    symbol VARCHAR(50) NOT NULL,
    side VARCHAR(10) NOT NULL,

    quantity NUMERIC(38, 18) NOT NULL,
    price NUMERIC(38, 18) NOT NULL,

    status VARCHAR(50) NOT NULL,
    
    asset VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trades_trade_id ON trades(trade_id);
CREATE INDEX idx_trades_user_id ON trades(user_id);
CREATE INDEX idx_trades_created_at ON trades(created_at);

--- --- Drop tables for cleanup (if needed)
DROP TABLE trades;
DROP SEQUENCE trades_seq;