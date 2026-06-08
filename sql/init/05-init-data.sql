USE trading_user;
INSERT INTO user_account (user_id, balance, version)
VALUES ('user001', 1000.00, 0)
ON DUPLICATE KEY UPDATE balance = VALUES(balance), version = VALUES(version);

USE trading_merchant;
INSERT INTO merchant_account (merchant_id, balance, version)
VALUES ('merchant001', 0.00, 0)
ON DUPLICATE KEY UPDATE balance = VALUES(balance), version = VALUES(version);

INSERT INTO product_inventory (merchant_id, sku, product_name, price, available_quantity, sold_quantity, version)
VALUES ('merchant001', 'SKU-001', 'Demo Product', 99.00, 100, 0, 0)
ON DUPLICATE KEY UPDATE product_name = VALUES(product_name), price = VALUES(price), available_quantity = VALUES(available_quantity);
