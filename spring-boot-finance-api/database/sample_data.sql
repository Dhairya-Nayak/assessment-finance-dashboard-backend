-- =====================================================
-- Finance Dashboard - Sample Data for Testing
-- =====================================================

USE finance_dashboard;

-- Create test users (passwords are 'Test@123' encoded with BCrypt)
INSERT INTO users (username, email, password, first_name, last_name, status) VALUES
    ('viewer_user', 'viewer@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.tTTmCXhVzGIPHi', 'John', 'Viewer', 'ACTIVE'),
    ('analyst_user', 'analyst@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.tTTmCXhVzGIPHi', 'Jane', 'Analyst', 'ACTIVE'),
    ('test_admin', 'testadmin@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.tTTmCXhVzGIPHi', 'Test', 'Admin', 'ACTIVE');

-- Assign roles to test users
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'viewer_user' AND r.name = 'ROLE_VIEWER';

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'analyst_user' AND r.name = 'ROLE_ANALYST';

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'test_admin' AND r.name = 'ROLE_ADMIN';

-- Get user IDs for financial records
SET @admin_id = (SELECT id FROM users WHERE username = 'admin');
SET @viewer_id = (SELECT id FROM users WHERE username = 'viewer_user');
SET @analyst_id = (SELECT id FROM users WHERE username = 'analyst_user');

-- Get category IDs
SET @salary_id = (SELECT id FROM categories WHERE name = 'Salary');
SET @freelance_id = (SELECT id FROM categories WHERE name = 'Freelance');
SET @investments_id = (SELECT id FROM categories WHERE name = 'Investments');
SET @housing_id = (SELECT id FROM categories WHERE name = 'Housing');
SET @transportation_id = (SELECT id FROM categories WHERE name = 'Transportation');
SET @food_id = (SELECT id FROM categories WHERE name = 'Food & Dining');
SET @healthcare_id = (SELECT id FROM categories WHERE name = 'Healthcare');
SET @entertainment_id = (SELECT id FROM categories WHERE name = 'Entertainment');
SET @shopping_id = (SELECT id FROM categories WHERE name = 'Shopping');
SET @bills_id = (SELECT id FROM categories WHERE name = 'Bills & Utilities');

-- Insert sample financial records for admin user (last 6 months)
INSERT INTO financial_records (amount, type, category_id, user_id, description, transaction_date, status, created_by) VALUES
-- January 2024
(5000.00, 'INCOME', @salary_id, @admin_id, 'Monthly Salary - January', '2024-01-15', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', @housing_id, @admin_id, 'Rent Payment - January', '2024-01-01', 'CONFIRMED', @admin_id),
(150.00, 'EXPENSE', @transportation_id, @admin_id, 'Gas and Parking', '2024-01-10', 'CONFIRMED', @admin_id),
(300.00, 'EXPENSE', @food_id, @admin_id, 'Groceries', '2024-01-12', 'CONFIRMED', @admin_id),
(85.00, 'EXPENSE', @bills_id, @admin_id, 'Internet Bill', '2024-01-05', 'CONFIRMED', @admin_id),

-- February 2024
(5000.00, 'INCOME', @salary_id, @admin_id, 'Monthly Salary - February', '2024-02-15', 'CONFIRMED', @admin_id),
(800.00, 'INCOME', @freelance_id, @admin_id, 'Freelance Project', '2024-02-20', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', @housing_id, @admin_id, 'Rent Payment - February', '2024-02-01', 'CONFIRMED', @admin_id),
(200.00, 'EXPENSE', @transportation_id, @admin_id, 'Car Maintenance', '2024-02-18', 'CONFIRMED', @admin_id),
(350.00, 'EXPENSE', @food_id, @admin_id, 'Groceries and Dining', '2024-02-14', 'CONFIRMED', @admin_id),
(120.00, 'EXPENSE', @healthcare_id, @admin_id, 'Doctor Visit', '2024-02-22', 'CONFIRMED', @admin_id),

-- March 2024
(5000.00, 'INCOME', @salary_id, @admin_id, 'Monthly Salary - March', '2024-03-15', 'CONFIRMED', @admin_id),
(250.00, 'INCOME', @investments_id, @admin_id, 'Dividend Income', '2024-03-10', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', @housing_id, @admin_id, 'Rent Payment - March', '2024-03-01', 'CONFIRMED', @admin_id),
(180.00, 'EXPENSE', @transportation_id, @admin_id, 'Gas', '2024-03-08', 'CONFIRMED', @admin_id),
(400.00, 'EXPENSE', @shopping_id, @admin_id, 'New Clothes', '2024-03-20', 'CONFIRMED', @admin_id),
(50.00, 'EXPENSE', @entertainment_id, @admin_id, 'Movie Night', '2024-03-22', 'CONFIRMED', @admin_id),
(90.00, 'EXPENSE', @bills_id, @admin_id, 'Phone Bill', '2024-03-05', 'CONFIRMED', @admin_id),

-- April 2024
(5200.00, 'INCOME', @salary_id, @admin_id, 'Monthly Salary + Bonus - April', '2024-04-15', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', @housing_id, @admin_id, 'Rent Payment - April', '2024-04-01', 'CONFIRMED', @admin_id),
(160.00, 'EXPENSE', @transportation_id, @admin_id, 'Gas', '2024-04-12', 'CONFIRMED', @admin_id),
(320.00, 'EXPENSE', @food_id, @admin_id, 'Groceries', '2024-04-10', 'CONFIRMED', @admin_id),
(200.00, 'EXPENSE', @entertainment_id, @admin_id, 'Concert Tickets', '2024-04-25', 'CONFIRMED', @admin_id),

-- May 2024
(5000.00, 'INCOME', @salary_id, @admin_id, 'Monthly Salary - May', '2024-05-15', 'CONFIRMED', @admin_id),
(1500.00, 'INCOME', @freelance_id, @admin_id, 'Big Freelance Project', '2024-05-28', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', @housing_id, @admin_id, 'Rent Payment - May', '2024-05-01', 'CONFIRMED', @admin_id),
(500.00, 'EXPENSE', @transportation_id, @admin_id, 'Car Insurance (6 months)', '2024-05-05', 'CONFIRMED', @admin_id),
(280.00, 'EXPENSE', @food_id, @admin_id, 'Groceries', '2024-05-18', 'CONFIRMED', @admin_id),
(150.00, 'EXPENSE', @healthcare_id, @admin_id, 'Pharmacy', '2024-05-20', 'CONFIRMED', @admin_id),

-- June 2024
(5000.00, 'INCOME', @salary_id, @admin_id, 'Monthly Salary - June', '2024-06-15', 'CONFIRMED', @admin_id),
(300.00, 'INCOME', @investments_id, @admin_id, 'Dividend Income Q2', '2024-06-30', 'CONFIRMED', @admin_id),
(1200.00, 'EXPENSE', @housing_id, @admin_id, 'Rent Payment - June', '2024-06-01', 'CONFIRMED', @admin_id),
(170.00, 'EXPENSE', @transportation_id, @admin_id, 'Gas', '2024-06-08', 'CONFIRMED', @admin_id),
(350.00, 'EXPENSE', @food_id, @admin_id, 'Groceries and Dining Out', '2024-06-20', 'CONFIRMED', @admin_id),
(600.00, 'EXPENSE', @shopping_id, @admin_id, 'Electronics', '2024-06-25', 'CONFIRMED', @admin_id),
(85.00, 'EXPENSE', @bills_id, @admin_id, 'Internet Bill', '2024-06-05', 'CONFIRMED', @admin_id);

-- Insert some records for viewer user
INSERT INTO financial_records (amount, type, category_id, user_id, description, transaction_date, status, created_by) VALUES
(4000.00, 'INCOME', @salary_id, @viewer_id, 'Monthly Salary', '2024-06-15', 'CONFIRMED', @viewer_id),
(1000.00, 'EXPENSE', @housing_id, @viewer_id, 'Rent', '2024-06-01', 'CONFIRMED', @viewer_id),
(250.00, 'EXPENSE', @food_id, @viewer_id, 'Groceries', '2024-06-10', 'CONFIRMED', @viewer_id),
(100.00, 'EXPENSE', @transportation_id, @viewer_id, 'Public Transit', '2024-06-05', 'CONFIRMED', @viewer_id);

-- Insert some records for analyst user  
INSERT INTO financial_records (amount, type, category_id, user_id, description, transaction_date, status, created_by) VALUES
(6000.00, 'INCOME', @salary_id, @analyst_id, 'Monthly Salary', '2024-06-15', 'CONFIRMED', @analyst_id),
(1500.00, 'INCOME', @freelance_id, @analyst_id, 'Consulting', '2024-06-20', 'CONFIRMED', @analyst_id),
(1400.00, 'EXPENSE', @housing_id, @analyst_id, 'Rent', '2024-06-01', 'CONFIRMED', @analyst_id),
(400.00, 'EXPENSE', @food_id, @analyst_id, 'Groceries and Dining', '2024-06-15', 'CONFIRMED', @analyst_id),
(200.00, 'EXPENSE', @transportation_id, @analyst_id, 'Gas', '2024-06-10', 'CONFIRMED', @analyst_id),
(100.00, 'EXPENSE', @entertainment_id, @analyst_id, 'Streaming Services', '2024-06-05', 'CONFIRMED', @analyst_id);

-- Custom categories created by admin
INSERT INTO categories (name, description, type, color, icon, is_active, is_system, created_by) VALUES
('Side Business', 'Income from side business', 'INCOME', '#22C55E', 'briefcase', TRUE, FALSE, @admin_id),
('Pet Expenses', 'Pet food and vet bills', 'EXPENSE', '#F97316', 'paw-print', TRUE, FALSE, @admin_id),
('Subscriptions', 'Monthly subscriptions', 'EXPENSE', '#8B5CF6', 'credit-card', TRUE, FALSE, @admin_id);

SELECT 'Sample data inserted successfully!' as status;
