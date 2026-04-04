-- =====================================================
-- Finance Dashboard Database Schema
-- MySQL Database Script
-- =====================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS finance_dashboard
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE finance_dashboard;

-- =====================================================
-- Drop existing tables (in correct order due to FK)
-- =====================================================
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS financial_records;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS refresh_tokens;

-- =====================================================
-- ROLES TABLE
-- =====================================================
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- USERS TABLE
-- =====================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED') DEFAULT 'ACTIVE',
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_by BIGINT,
    last_login_at TIMESTAMP NULL,
    
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_status (status),
    INDEX idx_users_created_at (created_at),
    
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_users_modified_by FOREIGN KEY (last_modified_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- USER_ROLES TABLE (Many-to-Many relationship)
-- =====================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    
    PRIMARY KEY (user_id, role_id),
    
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_user_roles_user_id (user_id),
    INDEX idx_user_roles_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- REFRESH_TOKENS TABLE
-- =====================================================
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    
    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_user_id (user_id),
    INDEX idx_refresh_tokens_expires_at (expires_at),
    
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- CATEGORIES TABLE
-- =====================================================
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    type ENUM('INCOME', 'EXPENSE', 'BOTH') NOT NULL DEFAULT 'BOTH',
    color VARCHAR(7) DEFAULT '#6B7280',
    icon VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    is_system BOOLEAN DEFAULT FALSE,
    parent_id BIGINT,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_by BIGINT,
    
    INDEX idx_categories_name (name),
    INDEX idx_categories_type (type),
    INDEX idx_categories_is_active (is_active),
    INDEX idx_categories_parent_id (parent_id),
    
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_categories_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_categories_modified_by FOREIGN KEY (last_modified_by) REFERENCES users(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_categories_name_type (name, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- FINANCIAL_RECORDS TABLE
-- =====================================================
CREATE TABLE financial_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(15, 2) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    description VARCHAR(500),
    reference_number VARCHAR(50),
    transaction_date DATE NOT NULL,
    notes TEXT,
    tags VARCHAR(255),
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'DELETED') DEFAULT 'CONFIRMED',
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_frequency ENUM('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'),
    attachment_url VARCHAR(500),
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_by BIGINT,
    
    INDEX idx_financial_records_user_id (user_id),
    INDEX idx_financial_records_category_id (category_id),
    INDEX idx_financial_records_type (type),
    INDEX idx_financial_records_transaction_date (transaction_date),
    INDEX idx_financial_records_status (status),
    INDEX idx_financial_records_created_at (created_at),
    INDEX idx_financial_records_amount (amount),
    INDEX idx_financial_records_user_date (user_id, transaction_date),
    INDEX idx_financial_records_user_type_date (user_id, type, transaction_date),
    INDEX idx_financial_records_category_date (category_id, transaction_date),
    
    CONSTRAINT fk_financial_records_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_financial_records_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_financial_records_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_financial_records_modified_by FOREIGN KEY (last_modified_by) REFERENCES users(id) ON DELETE SET NULL,
    
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- AUDIT_LOGS TABLE
-- =====================================================
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action ENUM('CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'EXPORT') NOT NULL,
    user_id BIGINT,
    username VARCHAR(50),
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_user_id (user_id),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_created_at (created_at),
    INDEX idx_audit_logs_entity_type_date (entity_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERT DEFAULT DATA
-- =====================================================

-- Insert default roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_VIEWER', 'Read-only access to records and dashboards'),
    ('ROLE_ANALYST', 'Read access plus ability to use analytics features'),
    ('ROLE_ADMIN', 'Full CRUD access to records and user management');

-- Insert default categories
INSERT INTO categories (name, description, type, color, icon, is_system) VALUES
    -- Income Categories
    ('Salary', 'Monthly salary income', 'INCOME', '#10B981', 'briefcase', TRUE),
    ('Freelance', 'Freelance work income', 'INCOME', '#3B82F6', 'laptop', TRUE),
    ('Investments', 'Investment returns and dividends', 'INCOME', '#8B5CF6', 'trending-up', TRUE),
    ('Rental Income', 'Property rental income', 'INCOME', '#F59E0B', 'home', TRUE),
    ('Other Income', 'Miscellaneous income', 'INCOME', '#6B7280', 'plus-circle', TRUE),
    
    -- Expense Categories
    ('Housing', 'Rent, mortgage, utilities', 'EXPENSE', '#EF4444', 'home', TRUE),
    ('Transportation', 'Gas, public transit, car maintenance', 'EXPENSE', '#F97316', 'car', TRUE),
    ('Food & Dining', 'Groceries, restaurants, delivery', 'EXPENSE', '#84CC16', 'utensils', TRUE),
    ('Healthcare', 'Medical expenses, insurance, pharmacy', 'EXPENSE', '#EC4899', 'heart', TRUE),
    ('Entertainment', 'Movies, games, subscriptions', 'EXPENSE', '#A855F7', 'film', TRUE),
    ('Shopping', 'Clothing, electronics, household items', 'EXPENSE', '#14B8A6', 'shopping-bag', TRUE),
    ('Education', 'Courses, books, training', 'EXPENSE', '#6366F1', 'book', TRUE),
    ('Bills & Utilities', 'Phone, internet, electricity', 'EXPENSE', '#F43F5E', 'file-text', TRUE),
    ('Travel', 'Vacations, flights, hotels', 'EXPENSE', '#0EA5E9', 'plane', TRUE),
    ('Other Expenses', 'Miscellaneous expenses', 'EXPENSE', '#6B7280', 'minus-circle', TRUE);

-- Insert default admin user (password: Admin@123)
-- BCrypt hash for 'Admin@123'
INSERT INTO users (username, email, password, first_name, last_name, status) VALUES
    ('admin', 'admin@financedashboard.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.tTTmCXhVzGIPHi', 'System', 'Administrator', 'ACTIVE');

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- =====================================================
-- CREATE VIEWS FOR REPORTING
-- =====================================================

-- Monthly Summary View
CREATE OR REPLACE VIEW vw_monthly_summary AS
SELECT 
    user_id,
    YEAR(transaction_date) as year,
    MONTH(transaction_date) as month,
    type,
    SUM(amount) as total_amount,
    COUNT(*) as transaction_count
FROM financial_records
WHERE status = 'CONFIRMED'
GROUP BY user_id, YEAR(transaction_date), MONTH(transaction_date), type;

-- Category Summary View
CREATE OR REPLACE VIEW vw_category_summary AS
SELECT 
    fr.user_id,
    fr.category_id,
    c.name as category_name,
    c.type as category_type,
    fr.type as transaction_type,
    SUM(fr.amount) as total_amount,
    COUNT(*) as transaction_count,
    AVG(fr.amount) as avg_amount
FROM financial_records fr
JOIN categories c ON fr.category_id = c.id
WHERE fr.status = 'CONFIRMED'
GROUP BY fr.user_id, fr.category_id, c.name, c.type, fr.type;

-- Daily Balance View
CREATE OR REPLACE VIEW vw_daily_balance AS
SELECT 
    user_id,
    transaction_date,
    SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as daily_income,
    SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as daily_expense,
    SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END) as daily_balance
FROM financial_records
WHERE status = 'CONFIRMED'
GROUP BY user_id, transaction_date;

-- =====================================================
-- STORED PROCEDURES
-- =====================================================

DELIMITER //

-- Procedure to get user dashboard summary
CREATE PROCEDURE sp_get_dashboard_summary(IN p_user_id BIGINT, IN p_start_date DATE, IN p_end_date DATE)
BEGIN
    -- Total Income
    SELECT 
        COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as total_income,
        COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as total_expense,
        COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) as net_balance,
        COUNT(*) as total_transactions
    FROM financial_records
    WHERE user_id = p_user_id
      AND status = 'CONFIRMED'
      AND transaction_date BETWEEN p_start_date AND p_end_date;
END //

-- Procedure to get category breakdown
CREATE PROCEDURE sp_get_category_breakdown(IN p_user_id BIGINT, IN p_type VARCHAR(10), IN p_start_date DATE, IN p_end_date DATE)
BEGIN
    SELECT 
        c.id as category_id,
        c.name as category_name,
        c.color,
        c.icon,
        SUM(fr.amount) as total_amount,
        COUNT(*) as transaction_count,
        ROUND((SUM(fr.amount) / (SELECT SUM(amount) FROM financial_records WHERE user_id = p_user_id AND type = p_type AND status = 'CONFIRMED' AND transaction_date BETWEEN p_start_date AND p_end_date)) * 100, 2) as percentage
    FROM financial_records fr
    JOIN categories c ON fr.category_id = c.id
    WHERE fr.user_id = p_user_id
      AND fr.type = p_type
      AND fr.status = 'CONFIRMED'
      AND fr.transaction_date BETWEEN p_start_date AND p_end_date
    GROUP BY c.id, c.name, c.color, c.icon
    ORDER BY total_amount DESC;
END //

-- Procedure to get monthly trends
CREATE PROCEDURE sp_get_monthly_trends(IN p_user_id BIGINT, IN p_months INT)
BEGIN
    SELECT 
        DATE_FORMAT(transaction_date, '%Y-%m') as month,
        YEAR(transaction_date) as year,
        MONTH(transaction_date) as month_num,
        SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income,
        SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense,
        SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END) as net
    FROM financial_records
    WHERE user_id = p_user_id
      AND status = 'CONFIRMED'
      AND transaction_date >= DATE_SUB(CURDATE(), INTERVAL p_months MONTH)
    GROUP BY DATE_FORMAT(transaction_date, '%Y-%m'), YEAR(transaction_date), MONTH(transaction_date)
    ORDER BY year DESC, month_num DESC;
END //

-- Procedure to soft delete a record
CREATE PROCEDURE sp_soft_delete_record(IN p_record_id BIGINT, IN p_user_id BIGINT)
BEGIN
    UPDATE financial_records 
    SET status = 'DELETED', 
        last_modified_by = p_user_id,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_record_id;
    
    -- Log the deletion
    INSERT INTO audit_logs (entity_type, entity_id, action, user_id, description)
    VALUES ('FINANCIAL_RECORD', p_record_id, 'DELETE', p_user_id, 'Record soft deleted');
END //

DELIMITER ;

-- =====================================================
-- TRIGGERS FOR AUDIT LOGGING
-- =====================================================

DELIMITER //

-- Trigger for financial_records INSERT
CREATE TRIGGER trg_financial_records_insert
AFTER INSERT ON financial_records
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (entity_type, entity_id, action, user_id, new_values, description)
    VALUES (
        'FINANCIAL_RECORD',
        NEW.id,
        'CREATE',
        NEW.created_by,
        JSON_OBJECT(
            'amount', NEW.amount,
            'type', NEW.type,
            'category_id', NEW.category_id,
            'transaction_date', NEW.transaction_date,
            'description', NEW.description
        ),
        CONCAT('Financial record created: ', NEW.type, ' - ', NEW.amount)
    );
END //

-- Trigger for financial_records UPDATE
CREATE TRIGGER trg_financial_records_update
AFTER UPDATE ON financial_records
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (entity_type, entity_id, action, user_id, old_values, new_values, description)
    VALUES (
        'FINANCIAL_RECORD',
        NEW.id,
        'UPDATE',
        NEW.last_modified_by,
        JSON_OBJECT(
            'amount', OLD.amount,
            'type', OLD.type,
            'category_id', OLD.category_id,
            'transaction_date', OLD.transaction_date,
            'description', OLD.description,
            'status', OLD.status
        ),
        JSON_OBJECT(
            'amount', NEW.amount,
            'type', NEW.type,
            'category_id', NEW.category_id,
            'transaction_date', NEW.transaction_date,
            'description', NEW.description,
            'status', NEW.status
        ),
        CONCAT('Financial record updated: ID ', NEW.id)
    );
END //

-- Trigger for users INSERT
CREATE TRIGGER trg_users_insert
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (entity_type, entity_id, action, user_id, new_values, description)
    VALUES (
        'USER',
        NEW.id,
        'CREATE',
        NEW.created_by,
        JSON_OBJECT(
            'username', NEW.username,
            'email', NEW.email,
            'status', NEW.status
        ),
        CONCAT('User created: ', NEW.username)
    );
END //

-- Trigger for users UPDATE
CREATE TRIGGER trg_users_update
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (entity_type, entity_id, action, user_id, old_values, new_values, description)
    VALUES (
        'USER',
        NEW.id,
        'UPDATE',
        NEW.last_modified_by,
        JSON_OBJECT(
            'username', OLD.username,
            'email', OLD.email,
            'status', OLD.status
        ),
        JSON_OBJECT(
            'username', NEW.username,
            'email', NEW.email,
            'status', NEW.status
        ),
        CONCAT('User updated: ', NEW.username)
    );
END //

DELIMITER ;

-- =====================================================
-- GRANT PERMISSIONS (adjust as needed)
-- =====================================================
-- GRANT ALL PRIVILEGES ON finance_dashboard.* TO 'your_app_user'@'localhost';
-- FLUSH PRIVILEGES;
