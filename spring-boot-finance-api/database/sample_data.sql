-- =====================================================
-- Finance Dashboard - Sample Data for Testing
-- =====================================================

USE finance_dashboard;



-- =====================================================
-- CREATE TEST USERS
-- Current working passwords:
-- testadmin1 / Test@1234
-- viewer1    / Viewer@1234
-- analyst1   / Test@1234
-- =====================================================

INSERT INTO users (username, email, password, first_name, last_name, status) VALUES
                                                                                 ('testadmin1', 'testadmin1@example.com', '$2a$12$H0RgJSI.jpTPjivWvbyB9.ZQsRCFP3235AlElteyfclz9fa76EeKO', 'Test', 'Admin', 'ACTIVE'),
                                                                                 ('viewer1', 'viewer1@example.com', '$2a$12$H0RgJSI.jpTPjivWvbyB9.ZQsRCFP3235AlElteyfclz9fa76EeKO', 'View', 'Only', 'ACTIVE'),
                                                                                 ('analyst1', 'analyst1@example.com', '$2a$12$H0RgJSI.jpTPjivWvbyB9.ZQsRCFP3235AlElteyfclz9fa76EeKO', 'Ana', 'Lyst', 'ACTIVE');

-- =====================================================
-- ASSIGN ROLES
-- =====================================================

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'testadmin1';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ROLE_VIEWER'
WHERE u.username = 'viewer1';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ROLE_ANALYST'
WHERE u.username = 'analyst1';

-- =====================================================
-- GET USER IDS
-- =====================================================

SET @admin_id = (SELECT id FROM users WHERE username = 'testadmin1');
SET @viewer_id = (SELECT id FROM users WHERE username = 'viewer1');
SET @analyst_id = (SELECT id FROM users WHERE username = 'analyst1');

-- =====================================================
-- CATEGORY ID REFERENCE (FIXED DIRECT IDS)
-- 1  = Salary
-- 2  = Freelance
-- 3  = Investments
-- 6  = Housing
-- 7  = Transportation
-- 8  = Food & Dining
-- 9  = Healthcare
-- 10 = Entertainment
-- 11 = Shopping
-- 13 = Bills & Utilities
-- =====================================================

-- =====================================================
-- SAMPLE RECORDS FOR ADMIN
-- =====================================================

INSERT INTO financial_records (amount, type, category_id, user_id, description, transaction_date, status, created_by) VALUES
-- January 2024
(5000.00, 'INCOME', 1, @admin_id, 'Monthly Salary - January', '2024-01-15', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', 6, @admin_id, 'Rent Payment - January', '2024-01-01', 'CONFIRMED', @admin_id),
(150.00, 'EXPENSE', 7, @admin_id, 'Gas and Parking', '2024-01-10', 'CONFIRMED', @admin_id),
(300.00, 'EXPENSE', 8, @admin_id, 'Groceries', '2024-01-12', 'CONFIRMED', @admin_id),
(85.00, 'EXPENSE', 13, @admin_id, 'Internet Bill', '2024-01-05', 'CONFIRMED', @admin_id),

-- February 2024
(5000.00, 'INCOME', 1, @admin_id, 'Monthly Salary - February', '2024-02-15', 'CONFIRMED', @admin_id),
(800.00, 'INCOME', 2, @admin_id, 'Freelance Project', '2024-02-20', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', 6, @admin_id, 'Rent Payment - February', '2024-02-01', 'CONFIRMED', @admin_id),
(200.00, 'EXPENSE', 7, @admin_id, 'Car Maintenance', '2024-02-18', 'CONFIRMED', @admin_id),
(350.00, 'EXPENSE', 8, @admin_id, 'Groceries and Dining', '2024-02-14', 'CONFIRMED', @admin_id),
(120.00, 'EXPENSE', 9, @admin_id, 'Doctor Visit', '2024-02-22', 'CONFIRMED', @admin_id),

-- March 2024
(5000.00, 'INCOME', 1, @admin_id, 'Monthly Salary - March', '2024-03-15', 'CONFIRMED', @admin_id),
(250.00, 'INCOME', 3, @admin_id, 'Dividend Income', '2024-03-10', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', 6, @admin_id, 'Rent Payment - March', '2024-03-01', 'CONFIRMED', @admin_id),
(180.00, 'EXPENSE', 7, @admin_id, 'Gas', '2024-03-08', 'CONFIRMED', @admin_id),
(400.00, 'EXPENSE', 11, @admin_id, 'New Clothes', '2024-03-20', 'CONFIRMED', @admin_id),
(50.00, 'EXPENSE', 10, @admin_id, 'Movie Night', '2024-03-22', 'CONFIRMED', @admin_id),
(90.00, 'EXPENSE', 13, @admin_id, 'Phone Bill', '2024-03-05', 'CONFIRMED', @admin_id),

-- April 2024
(5200.00, 'INCOME', 1, @admin_id, 'Monthly Salary + Bonus - April', '2024-04-15', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', 6, @admin_id, 'Rent Payment - April', '2024-04-01', 'CONFIRMED', @admin_id),
(160.00, 'EXPENSE', 7, @admin_id, 'Gas', '2024-04-12', 'CONFIRMED', @admin_id),
(320.00, 'EXPENSE', 8, @admin_id, 'Groceries', '2024-04-10', 'CONFIRMED', @admin_id),
(200.00, 'EXPENSE', 10, @admin_id, 'Concert Tickets', '2024-04-25', 'CONFIRMED', @admin_id),

-- May 2024
(5000.00, 'INCOME', 1, @admin_id, 'Monthly Salary - May', '2024-05-15', 'CONFIRMED', @admin_id),
(1500.00, 'INCOME', 2, @admin_id, 'Big Freelance Project', '2024-05-28', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', 6, @admin_id, 'Rent Payment - May', '2024-05-01', 'CONFIRMED', @admin_id),
(500.00, 'EXPENSE', 7, @admin_id, 'Car Insurance (6 months)', '2024-05-05', 'CONFIRMED', @admin_id),
(280.00, 'EXPENSE', 8, @admin_id, 'Groceries', '2024-05-18', 'CONFIRMED', @admin_id),
(150.00, 'EXPENSE', 9, @admin_id, 'Pharmacy', '2024-05-20', 'CONFIRMED', @admin_id),

-- June 2024
(5000.00, 'INCOME', 1, @admin_id, 'Monthly Salary - June', '2024-06-15', 'CONFIRMED', @admin_id),
(300.00, 'INCOME', 3, @admin_id, 'Dividend Income Q2', '2024-06-30', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', 6, @admin_id, 'Rent Payment - June', '2024-06-01', 'CONFIRMED', @admin_id),
(170.00, 'EXPENSE', 7, @admin_id, 'Gas', '2024-06-08', 'CONFIRMED', @admin_id),
(350.00, 'EXPENSE', 8, @admin_id, 'Groceries and Dining Out', '2024-06-20', 'CONFIRMED', @admin_id),
(600.00, 'EXPENSE', 11, @admin_id, 'Electronics', '2024-06-25', 'CONFIRMED', @admin_id),
(85.00, 'EXPENSE', 13, @admin_id, 'Internet Bill', '2024-06-05', 'CONFIRMED', @admin_id);

-- =====================================================
-- SAMPLE RECORDS FOR VIEWER
-- =====================================================

INSERT INTO financial_records (amount, type, category_id, user_id, description, transaction_date, status, created_by) VALUES
                                                                                                                          (4000.00, 'INCOME', 1, @viewer_id, 'Monthly Salary', '2024-06-15', 'CONFIRMED', @viewer_id),
                                                                                                                          (1000.00, 'EXPENSE', 6, @viewer_id, 'Rent', '2024-06-01', 'CONFIRMED', @viewer_id),
                                                                                                                          (250.00, 'EXPENSE', 8, @viewer_id, 'Groceries', '2024-06-10', 'CONFIRMED', @viewer_id),
                                                                                                                          (100.00, 'EXPENSE', 7, @viewer_id, 'Public Transit', '2024-06-05', 'CONFIRMED', @viewer_id);

-- =====================================================
-- SAMPLE RECORDS FOR ANALYST
-- =====================================================

INSERT INTO financial_records (amount, type, category_id, user_id, description, transaction_date, status, created_by) VALUES
                                                                                                                          (6000.00, 'INCOME', 1, @analyst_id, 'Monthly Salary', '2024-06-15', 'CONFIRMED', @analyst_id),
                                                                                                                          (1500.00, 'INCOME', 2, @analyst_id, 'Consulting', '2024-06-20', 'CONFIRMED', @analyst_id),
                                                                                                                          (1400.00, 'EXPENSE', 6, @analyst_id, 'Rent', '2024-06-01', 'CONFIRMED', @analyst_id),
                                                                                                                          (400.00, 'EXPENSE', 8, @analyst_id, 'Groceries and Dining', '2024-06-15', 'CONFIRMED', @analyst_id),
                                                                                                                          (200.00, 'EXPENSE', 7, @analyst_id, 'Gas', '2024-06-10', 'CONFIRMED', @analyst_id),
                                                                                                                          (100.00, 'EXPENSE', 10, @analyst_id, 'Streaming Services', '2024-06-05', 'CONFIRMED', @analyst_id);

-- =====================================================
-- CUSTOM CATEGORIES CREATED BY ADMIN
-- =====================================================

INSERT INTO categories (name, description, type, color, icon, is_active, is_system, created_by) VALUES
                                                                                                    ('Side Business', 'Income from side business', 'INCOME', '#22C55E', 'briefcase', TRUE, FALSE, @admin_id),
                                                                                                    ('Pet Expenses', 'Pet food and vet bills', 'EXPENSE', '#F97316', 'paw-print', TRUE, FALSE, @admin_id),
                                                                                                    ('Subscriptions', 'Monthly subscriptions', 'EXPENSE', '#8B5CF6', 'credit-card', TRUE, FALSE, @admin_id);

SELECT 'Sample data inserted successfully!' AS status;