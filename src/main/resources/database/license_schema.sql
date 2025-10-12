-- License Management System Database Schema
-- This script creates the necessary tables for license and OTP management

-- Create licenses table
CREATE TABLE IF NOT EXISTS licenses (
    id SERIAL PRIMARY KEY,
    license_key VARCHAR(255) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    remaining_days INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    
    CONSTRAINT chk_license_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED', 'EXPIRING_SOON', 'REMOVED')),
    CONSTRAINT chk_license_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_remaining_days CHECK (remaining_days >= 0)
);

-- Create otps table
CREATE TABLE IF NOT EXISTS otps (
    id SERIAL PRIMARY KEY,
    otp_code VARCHAR(10) NOT NULL,
    email VARCHAR(255) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address VARCHAR(45),
    
    CONSTRAINT chk_otp_operation CHECK (operation IN ('RENEW', 'REMOVE', 'ADMIN_CREATION')),
    CONSTRAINT chk_otp_expires CHECK (expires_at > created_at)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_licenses_status ON licenses(status);
CREATE INDEX IF NOT EXISTS idx_licenses_created_at ON licenses(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_licenses_end_date ON licenses(end_date);
CREATE INDEX IF NOT EXISTS idx_licenses_license_key ON licenses(license_key);

CREATE INDEX IF NOT EXISTS idx_otps_code_operation ON otps(otp_code, operation);
CREATE INDEX IF NOT EXISTS idx_otps_expires_at ON otps(expires_at);
CREATE INDEX IF NOT EXISTS idx_otps_used ON otps(used);
CREATE INDEX IF NOT EXISTS idx_otps_email ON otps(email);

-- Create a function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at for licenses table
DROP TRIGGER IF EXISTS update_licenses_updated_at ON licenses;
CREATE TRIGGER update_licenses_updated_at
    BEFORE UPDATE ON licenses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data (optional - for development/testing)
-- 3-day test license for development and testing purposes

-- Insert a 3-day test license
INSERT INTO licenses (license_key, start_date, end_date, remaining_days, status, created_by)
VALUES (
    'LIC-TEST-3DAY-001',
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '3 days',
    3,
    'ACTIVE',
    'system'
) ON CONFLICT (license_key) DO NOTHING;

-- Insert additional test license with different key for backup
INSERT INTO licenses (license_key, start_date, end_date, remaining_days, status, created_by)
VALUES (
    'LIC-TRIAL-' || EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::TEXT,
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '3 days',
    3,
    'ACTIVE',
    'system'
) ON CONFLICT (license_key) DO NOTHING;

-- Grant permissions (adjust as needed for your database user)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON licenses TO your_app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON otps TO your_app_user;
-- GRANT USAGE, SELECT ON SEQUENCE licenses_id_seq TO your_app_user;
-- GRANT USAGE, SELECT ON SEQUENCE otps_id_seq TO your_app_user;

-- Add comments to tables and columns for documentation
COMMENT ON TABLE licenses IS 'Stores license information for the application';
COMMENT ON COLUMN licenses.license_key IS 'Unique license key identifier';
COMMENT ON COLUMN licenses.start_date IS 'License validity start date';
COMMENT ON COLUMN licenses.end_date IS 'License validity end date';
COMMENT ON COLUMN licenses.remaining_days IS 'Number of days remaining until license expires';
COMMENT ON COLUMN licenses.status IS 'Current status of the license (ACTIVE, INACTIVE, EXPIRED, EXPIRING_SOON, REMOVED)';
COMMENT ON COLUMN licenses.created_by IS 'Username of the user who created this license';

COMMENT ON TABLE otps IS 'Stores one-time passwords for license operations';
COMMENT ON COLUMN otps.otp_code IS 'The 6-digit OTP code';
COMMENT ON COLUMN otps.email IS 'Email address where OTP was sent';
COMMENT ON COLUMN otps.operation IS 'Operation type (RENEW, REMOVE)';
COMMENT ON COLUMN otps.expires_at IS 'When the OTP expires (10 minutes from creation)';
COMMENT ON COLUMN otps.used IS 'Whether the OTP has been used';
COMMENT ON COLUMN otps.ip_address IS 'IP address of the requester';