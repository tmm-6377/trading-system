USE trading_user;

CREATE TABLE IF NOT EXISTS user_account (
    user_id VARCHAR(64) PRIMARY KEY,
    balance DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trade_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(64) NOT NULL,
    merchant_id VARCHAR(64) NOT NULL,
    sku VARCHAR(64) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(18, 2) NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS account_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL,
    order_no VARCHAR(64) NULL,
    transaction_type VARCHAR(32) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    balance_after DECIMAL(18, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL
);
