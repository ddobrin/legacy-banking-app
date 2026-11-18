INSERT INTO products (product_code, name, category, interest_rate, is_active)
SELECT v.product_code, v.name, v.category, v.interest_rate, v.is_active
FROM (
    VALUES
        ('CHK-STD', 'Standard Checking', 'CHECKING', 0.0000, TRUE),
        ('SAV-HYS', 'High Yield Savings', 'SAVINGS', 0.0450, TRUE)
) AS v(product_code, name, category, interest_rate, is_active)
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.product_code = v.product_code);

INSERT INTO customers (cif_number, first_name, last_name, date_of_birth, email, kyc_status)
SELECT v.cif_number, v.first_name, v.last_name, v.date_of_birth, v.email, v.kyc_status
FROM (
    VALUES
        ('CIF-100001', 'John', 'Smith', DATE '1980-01-01', 'john.smith@example.com', 'COMPLETED'),
        ('CIF-100002', 'Jane', 'Doe', DATE '1982-02-02', 'jane.doe@example.com', 'COMPLETED'),
        ('CIF-100003', 'Frodo', 'Baggins', DATE '1984-03-03', 'frodo.baggins@example.com', 'COMPLETED')
) AS v(cif_number, first_name, last_name, date_of_birth, email, kyc_status)
WHERE NOT EXISTS (SELECT 1 FROM customers);

INSERT INTO accounts (customer_id, product_code, account_number, balance, status)
SELECT v.customer_id, v.product_code, v.account_number, v.balance, v.status
FROM (
    VALUES
        ((SELECT customer_id FROM customers WHERE cif_number = 'CIF-100001'), 'CHK-STD', 10000001, 1500.00, 'ACTIVE'),
        ((SELECT customer_id FROM customers WHERE cif_number = 'CIF-100001'), 'SAV-HYS', 10000002, 5000.00, 'ACTIVE'),
        ((SELECT customer_id FROM customers WHERE cif_number = 'CIF-100001'), 'CHK-STD', 10000003, 250.00, 'ACTIVE'),
        ((SELECT customer_id FROM customers WHERE cif_number = 'CIF-100002'), 'CHK-STD', 10000004, 3200.00, 'ACTIVE'),
        ((SELECT customer_id FROM customers WHERE cif_number = 'CIF-100002'), 'SAV-HYS', 10000005, 12000.00, 'ACTIVE'),
        ((SELECT customer_id FROM customers WHERE cif_number = 'CIF-100003'), 'CHK-STD', 10000006, 50.00, 'ACTIVE'),
        ((SELECT customer_id FROM customers WHERE cif_number = 'CIF-100003'), 'SAV-HYS', 10000007, 1000.00, 'ACTIVE'),
        ((SELECT customer_id FROM customers WHERE cif_number = 'CIF-100003'), 'CHK-STD', 10000008, 75.00, 'ACTIVE'),
        ((SELECT customer_id FROM customers WHERE cif_number = 'CIF-100003'), 'SAV-HYS', 10000009, 2500.00, 'ACTIVE')
) AS v(customer_id, product_code, account_number, balance, status)
WHERE NOT EXISTS (SELECT 1 FROM accounts);

INSERT INTO transactions (account_id, transaction_type, amount, description, balance_after)
SELECT v.account_id, v.transaction_type, v.amount, v.description, v.balance_after
FROM (
    VALUES
        -- John Acc 1: 10 txns
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'DEPOSIT', 1000.00, 'Paycheck', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'WITHDRAWAL', 50.00, 'ATM', 950.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'DEPOSIT', 200.00, 'Venmo', 1150.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'WITHDRAWAL', 100.00, 'Groceries', 1050.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'DEPOSIT', 500.00, 'Gift', 1550.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'WITHDRAWAL', 20.00, 'Coffee', 1530.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'WITHDRAWAL', 40.00, 'Gas', 1490.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'DEPOSIT', 100.00, 'Refund', 1590.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'WITHDRAWAL', 60.00, 'Dinner', 1530.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000001), 'WITHDRAWAL', 30.00, 'Movies', 1500.00),
        -- John Acc 2: 10 txns
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'DEPOSIT', 5000.00, 'Transfer from External', 5000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'DEPOSIT', 50.00, 'Interest', 5050.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'WITHDRAWAL', 50.00, 'Transfer to Check', 5000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'DEPOSIT', 10.00, 'Promo', 5010.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'WITHDRAWAL', 10.00, 'Service Fee', 5000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'DEPOSIT', 100.00, 'Savings Bonus', 5100.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'WITHDRAWAL', 100.00, 'Correction', 5000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'DEPOSIT', 25.00, 'Round-up', 5025.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'WITHDRAWAL', 25.00, 'Round-up Reversal', 5000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000002), 'DEPOSIT', 0.00, 'Zero Txn', 5000.00),
        -- John Acc 3: 10 txns
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'DEPOSIT', 500.00, 'Initial', 500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'WITHDRAWAL', 25.00, 'Bus Pass', 475.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'WITHDRAWAL', 25.00, 'Bus Pass', 450.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'WITHDRAWAL', 25.00, 'Bus Pass', 425.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'WITHDRAWAL', 25.00, 'Bus Pass', 400.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'WITHDRAWAL', 25.00, 'Bus Pass', 375.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'WITHDRAWAL', 25.00, 'Bus Pass', 350.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'WITHDRAWAL', 25.00, 'Bus Pass', 325.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'WITHDRAWAL', 25.00, 'Bus Pass', 300.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000003), 'WITHDRAWAL', 50.00, 'Uber', 250.00),
        -- Jane Acc 1: 10 txns
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'DEPOSIT', 4000.00, 'Salary', 4000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'WITHDRAWAL', 100.00, 'Target', 3900.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'WITHDRAWAL', 100.00, 'Target', 3800.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'WITHDRAWAL', 100.00, 'Target', 3700.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'WITHDRAWAL', 100.00, 'Target', 3600.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'WITHDRAWAL', 100.00, 'Target', 3500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'WITHDRAWAL', 100.00, 'Target', 3400.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'WITHDRAWAL', 100.00, 'Target', 3300.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'WITHDRAWAL', 100.00, 'Target', 3200.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000004), 'WITHDRAWAL', 0.00, 'Void', 3200.00),
        -- Jane Acc 2: 10 txns
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'DEPOSIT', 10000.00, 'Inheritance', 10000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'DEPOSIT', 2000.00, 'Transfer', 12000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'DEPOSIT', 1.00, 'Micro', 12001.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'WITHDRAWAL', 1.00, 'Micro', 12000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'DEPOSIT', 1.00, 'Micro', 12001.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'WITHDRAWAL', 1.00, 'Micro', 12000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'DEPOSIT', 1.00, 'Micro', 12001.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'WITHDRAWAL', 1.00, 'Micro', 12000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'DEPOSIT', 1.00, 'Micro', 12001.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000005), 'WITHDRAWAL', 1.00, 'Micro', 12000.00),
        -- Frodo Acc 1: 10 txns
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'DEPOSIT', 100.00, 'Found gold', 100.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'WITHDRAWAL', 5.00, 'Ale', 95.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'WITHDRAWAL', 5.00, 'Ale', 90.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'WITHDRAWAL', 5.00, 'Ale', 85.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'WITHDRAWAL', 5.00, 'Ale', 80.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'WITHDRAWAL', 5.00, 'Ale', 75.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'WITHDRAWAL', 5.00, 'Ale', 70.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'WITHDRAWAL', 5.00, 'Ale', 65.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'WITHDRAWAL', 5.00, 'Ale', 60.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000006), 'WITHDRAWAL', 10.00, 'Lembas', 50.00),
        -- Frodo Acc 2: 10 txns
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 1000.00, 'Bag End Rent', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 0.00, 'Ping', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 0.00, 'Ping', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 0.00, 'Ping', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 0.00, 'Ping', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 0.00, 'Ping', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 0.00, 'Ping', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 0.00, 'Ping', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 0.00, 'Ping', 1000.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000007), 'DEPOSIT', 0.00, 'Ping', 1000.00),
        -- Frodo Acc 3: 10 txns
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'DEPOSIT', 500.00, 'Bree gold', 500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'WITHDRAWAL', 50.00, 'Pony', 450.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'WITHDRAWAL', 50.00, 'Pony', 400.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'WITHDRAWAL', 50.00, 'Pony', 350.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'WITHDRAWAL', 50.00, 'Pony', 300.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'WITHDRAWAL', 50.00, 'Pony', 250.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'WITHDRAWAL', 50.00, 'Pony', 200.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'WITHDRAWAL', 50.00, 'Pony', 150.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'WITHDRAWAL', 50.00, 'Pony', 100.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000008), 'WITHDRAWAL', 25.00, 'Feed', 75.00),
        -- Frodo Acc 4: 10 txns
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 2500.00, 'Rivendell treasury', 2500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 0.00, 'Marker', 2500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 0.00, 'Marker', 2500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 0.00, 'Marker', 2500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 0.00, 'Marker', 2500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 0.00, 'Marker', 2500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 0.00, 'Marker', 2500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 0.00, 'Marker', 2500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 0.00, 'Marker', 2500.00),
        ((SELECT account_id FROM accounts WHERE account_number = 10000009), 'DEPOSIT', 0.00, 'Marker', 2500.00)
) AS v(account_id, transaction_type, amount, description, balance_after)
WHERE NOT EXISTS (SELECT 1 FROM transactions);
