-- Stock Bite database (MySQL / XAMPP)
CREATE DATABASE IF NOT EXISTS stockbite_db;
USE stockbite_db;

CREATE TABLE IF NOT EXISTS products (
    product_id   INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category     VARCHAR(50)  NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    stock        INT NOT NULL DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stock_log (
    log_id         INT AUTO_INCREMENT PRIMARY KEY,
    product_id     INT NOT NULL,
    quantity_added INT NOT NULL,
    log_date       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount     DECIMAL(10,2) NOT NULL,
    cash_received    DECIMAL(10,2) NOT NULL,
    change_amount    DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS transaction_items (
    item_id        INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    product_id     INT NULL,
    product_name   VARCHAR(100) NOT NULL,
    quantity       INT NOT NULL,
    unit_price     DECIMAL(10,2) NOT NULL,
    subtotal       DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE SET NULL
);

INSERT INTO products (product_name, category, price, stock) VALUES
('Chicken Adobo Meal', 'Meals',    55.00, 30),
('Hotdog Sandwich',    'Snacks',   25.00, 40),
('Fries',              'Snacks',   30.00, 25),
('Bottled Water',      'Drinks',   15.00, 60),
('Iced Tea',           'Drinks',   20.00, 50),
('Turon',              'Desserts', 15.00,  8),
('Pancit Canton',      'Meals',    35.00, 20);
