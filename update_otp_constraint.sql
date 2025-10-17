-- Update OTP table constraint to include ADMIN_CREATION and PASSWORD_RESET operations
ALTER TABLE otps DROP CONSTRAINT IF EXISTS chk_otp_operation;
ALTER TABLE otps ADD CONSTRAINT chk_otp_operation CHECK (operation IN ('RENEW', 'REMOVE', 'ADMIN_CREATION', 'PASSWORD_RESET'));