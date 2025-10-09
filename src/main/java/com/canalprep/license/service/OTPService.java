package com.canalprep.license.service;

import com.canalprep.license.model.OTP;
import com.canalprep.license.dao.OTPDAO;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.exception.DataAccessException;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class OTPService {
    private static final String DEVELOPER_EMAIL = "hamdyhkouta@gmail.com";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "canalprepschool@gmail.com";//System.getenv("EMAIL_USERNAME");
    private static final String EMAIL_PASSWORD = "eebb hamx ycyp pvui";//System.getenv("EMAIL_PASSWORD");
    
    private final OTPDAO otpDAO;
    private final SecureRandom random;
    
    public OTPService() {
        this.otpDAO = new OTPDAO();
        this.random = new SecureRandom();
    }
    
    /**
     * Generate and send OTP for license operations
     * @param operation The operation type (RENEW or REMOVE)
     * @param ipAddress The IP address of the requester
     * @return The generated OTP code
     * @throws DataAccessException if database operation fails
     */
    public String generateAndSendOTP(String operation, String ipAddress) throws DataAccessException {
        try {
            // Generate 6-digit OTP
            String otpCode = generateOTPCode();
            
            // Create OTP record
            OTP otp = new OTP(otpCode, DEVELOPER_EMAIL, operation, ipAddress);
            
            // Save to database
            otpDAO.createOTP(otp);
            
            // Send email
            sendOTPEmail(otpCode, operation, ipAddress);
            
            LoggerUtil.logSecurity("OTP_GENERATED", "system", 
                "OTP generated for operation: " + operation + " from IP: " + ipAddress);
            
            return otpCode;
            
        } catch (SQLException e) {
            LoggerUtil.logError("OTPService", "Failed to generate OTP", e);
            throw new DataAccessException("Failed to generate OTP", e);
        } catch (MessagingException e) {
            LoggerUtil.logError("OTPService", "Failed to send OTP email", e);
            throw new DataAccessException("Failed to send OTP email", e);
        }
    }
    
    /**
     * Validate OTP for license operations
     * @param otpCode The OTP code to validate
     * @param operation The operation type
     * @param ipAddress The IP address of the requester
     * @return true if OTP is valid, false otherwise
     * @throws DataAccessException if database operation fails
     */
    public boolean validateOTP(String otpCode, String operation, String ipAddress) throws DataAccessException {
        try {
            OTP otp = otpDAO.getValidOTP(otpCode, operation);
            
            if (otp == null) {
                LoggerUtil.logSecurity("OTP_VALIDATION_FAILED", "system", 
                    "Invalid OTP attempt: " + otpCode + " for operation: " + operation + " from IP: " + ipAddress);
                return false;
            }
            
            if (!otp.isValid()) {
                LoggerUtil.logSecurity("OTP_VALIDATION_FAILED", "system", 
                    "Expired or used OTP attempt: " + otpCode + " for operation: " + operation + " from IP: " + ipAddress);
                return false;
            }
            
            // Mark OTP as used
            otpDAO.markOTPAsUsed(otp.getId());
            
            LoggerUtil.logSecurity("OTP_VALIDATION_SUCCESS", "system", 
                "OTP validated successfully for operation: " + operation + " from IP: " + ipAddress);
            
            return true;
            
        } catch (SQLException e) {
            LoggerUtil.logError("OTPService", "Failed to validate OTP", e);
            throw new DataAccessException("Failed to validate OTP", e);
        }
    }
    
    /**
     * Clean up expired OTPs
     * @throws DataAccessException if database operation fails
     */
    public void cleanupExpiredOTPs() throws DataAccessException {
        try {
            int deletedCount = otpDAO.deleteExpiredOTPs();
            if (deletedCount > 0) {
                LoggerUtil.logInfo("OTPService", "Cleaned up " + deletedCount + " expired OTPs");
            }
        } catch (SQLException e) {
            LoggerUtil.logError("OTPService", "Failed to cleanup expired OTPs", e);
            throw new DataAccessException("Failed to cleanup expired OTPs", e);
        }
    }
    
    /**
     * Generate a 6-digit OTP code
     * @return 6-digit OTP code
     */
    private String generateOTPCode() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    /**
     * Send OTP via email
     * @param otpCode The OTP code
     * @param operation The operation type
     * @param ipAddress The requester's IP address
     * @throws MessagingException if email sending fails
     */
    private void sendOTPEmail(String otpCode, String operation, String ipAddress) throws MessagingException {
        // Skip email sending if credentials are not configured
        if (EMAIL_USERNAME == null || EMAIL_PASSWORD == null) {
            LoggerUtil.logInfo("OTPService", "Email credentials not configured. OTP: " + otpCode + " for operation: " + operation);
            return;
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(DEVELOPER_EMAIL));
        message.setSubject("License " + operation + " OTP - School Management System");
        
        String emailBody = String.format(
            "Dear Developer,\n\n" +
            "A request has been made to %s the license for the School Management System.\n\n" +
            "OTP Code: %s\n" +
            "Operation: %s\n" +
            "Request IP: %s\n" +
            "Valid for: 10 minutes\n\n" +
            "If you did not request this operation, please ignore this email.\n\n" +
            "Best regards,\n" +
            "School Management System",
            operation.toLowerCase(), otpCode, operation, ipAddress
        );
        
        message.setText(emailBody);
        
        Transport.send(message);
        
        LoggerUtil.logInfo("OTPService", "OTP email sent successfully for operation: " + operation);
    }
}