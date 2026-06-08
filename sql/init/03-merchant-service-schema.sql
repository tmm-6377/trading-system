USE trading_merchant;

CREATE TABLE IF NOT EXISTS merchant_account (
    merchant_id VARCHAR(64) PRIMARY KEY,
    balance DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id VARCHAR(64) NOT NULL,
    sku VARCHAR(64) NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    price DECIMAL(18, 2) NOT NULL,
    available_quantity INT NOT NULL,
    sold_quantity INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_merchant_sku (merchant_id, sku)
);

CREATE TABLE IF NOT EXISTS settlement_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id VARCHAR(64) NOT NULL,
    settlement_date DATE NOT NULL,
    expected_amount DECIMAL(18, 2) NOT NULL,
    actual_amount DECIMAL(18, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    remark VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL
);
