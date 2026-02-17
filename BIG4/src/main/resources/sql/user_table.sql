-- Optional: run this once if you want to create the user table manually.
-- AuthService also creates it automatically (CREATE TABLE IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    role VARCHAR(32) NOT NULL,
    reference_id BIGINT NULL,
    full_name VARCHAR(255) NULL,
    phone VARCHAR(64) NULL,
    UNIQUE KEY uk_email_role (email, role)
);
