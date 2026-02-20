-- Legacy Banking CIF Application DDL

DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS products;

CREATE TABLE products (
    product_code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    interest_rate NUMERIC(5, 4),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    cif_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    address_line1 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country_code CHAR(2),
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    kyc_documents VARCHAR(4000) DEFAULT '{}',
    risk_rating VARCHAR(10) DEFAULT 'LOW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    account_number INTEGER UNIQUE NOT NULL,
    iban VARCHAR(34),
    balance NUMERIC(15, 2) DEFAULT 0.00,
    overdraft_limit NUMERIC(15, 2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    configurations VARCHAR(4000) DEFAULT '{}',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (product_code) REFERENCES products(product_code)
);

CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    account_id INTEGER NOT NULL,
    reference_code VARCHAR(50),
    transaction_type VARCHAR(20) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    description VARCHAR(1000),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    balance_after NUMERIC(15, 2),
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
