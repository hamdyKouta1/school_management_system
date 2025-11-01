package com.canalprep.service;

import com.canalprep.license.model.OTP;
import com.canalprep.license.dao.OTPDAO;
import com.canalprep.auth.dao.UserDAO;
import com.canalprep.auth.model.User;
import com.canalprep.exception.DataAccessException;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.config.ConfigLoader;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.time.LocalDateTime;

/**
 * Service for handling unauthenticated password recovery operations
 */
public class PasswordRecoveryService {
    
    private static final String OPERATION_PASSWORD_RECOVERY = "PASSWORD_RECOVERY";
    private static final int OTP_EXPIRY_MINUTES = 15;
    private static final SecureRandom random = new SecureRandom();
    
    private final OTPDAO otpDAO;
    private final UserDAO userDAO;
    
    // Store pending password recovery requests (static to share across instances)
    private static final ConcurrentMap<String, PendingRecoveryRequest> pendingRequests = new ConcurrentHashMap<>();
    
    public PasswordRecoveryService() {
        this.otpDAO = new OTPDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Initiate password recovery process
     * @param username the username requesting password recovery
     * @param email the email address for verification
     * @param ipAddress the IP address of the request
     * @return The generated OTP code if successful
     * @throws DataAccessException if operation fails
     */
    public String initiatePasswordRecovery(String username, String email, String ipAddress) 
            throws DataAccessException {
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                throw new DataAccessException("Username is required", null);
            }
            if (email == null || email.trim().isEmpty()) {
                throw new DataAccessException("Email is required", null);
            }
            
            // Get user by username
            User user = userDAO.getUserByUsername(username.trim());
            if (user == null) {
                LoggerUtil.logSecurity("PASSWORD_RECOVERY_INVALID_USERNAME", "anonymous", 
                    "Password recovery attempt with invalid username: " + username + " from IP: " + ipAddress);
                throw new DataAccessException("Username not found", null);
            }
            
            // Verify email matches
            if (!user.getEmail().equalsIgnoreCase(email.trim())) {
                LoggerUtil.logSecurity("PASSWORD_RECOVERY_EMAIL_MISMATCH", username, 
                    "Password recovery attempt with mismatched email for user: " + username + " from IP: " + ipAddress);
                throw new DataAccessException("Email does not match registered email", null);
            }
            
            // Generate OTP
            String otpCode = String.format("%06d", random.nextInt(1000000));
            
            // Store pending request
            PendingRecoveryRequest pendingRequest = new PendingRecoveryRequest(
                user.getId(), user.getUsername(), user.getEmail(), 
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
            );
            pendingRequests.put(otpCode, pendingRequest);
            
            // Store OTP in database
            OTP otp = new OTP(otpCode, user.getEmail(), OPERATION_PASSWORD_RECOVERY, ipAddress);
            otpDAO.createOTP(otp);
            

            
            // Send OTP email
            sendPasswordRecoveryOTPEmail(otpCode, user.getUsername(), user.getEmail(), ipAddress);
            
            LoggerUtil.logSecurity("PASSWORD_RECOVERY_OTP_GENERATED", username,
                "Password recovery OTP generated for user: " + username + " from IP: " + ipAddress);
            
            return otpCode;
            
        } catch (Exception e) {
            LoggerUtil.logError("PasswordRecoveryService", "Failed to initiate password recovery", e);
            throw new DataAccessException("Failed to initiate password recovery", e);
        }
    }
    
    /**
     * Verify OTP and reset password
     * @param otpCode the OTP code to verify
     * @param newPassword the new password to set
     * @param ipAddress the IP address of the request
     * @return true if password reset successful
     * @throws DataAccessException if operation fails
     */
    public boolean verifyOTPAndResetPassword(String otpCode, String newPassword, String ipAddress) 
            throws DataAccessException {
        try {
            // Validate input
            if (otpCode == null || otpCode.trim().isEmpty()) {
                throw new DataAccessException("OTP code is required", null);
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new DataAccessException("New password is required", null);
            }
            

            // Check pending request
            PendingRecoveryRequest pendingRequest = pendingRequests.get(otpCode);
            if (pendingRequest == null) {
                LoggerUtil.logSecurity("PASSWORD_RECOVERY_OTP_VALIDATION_FAILED", "anonymous",
                    "Invalid password recovery OTP attempt: " + otpCode + " from IP: " + ipAddress);
                throw new DataAccessException("Invalid or expired OTP", null);
            }
            
            // Check if expired
            if (LocalDateTime.now().isAfter(pendingRequest.getExpiryTime())) {
                pendingRequests.remove(otpCode);
                LoggerUtil.logSecurity("PASSWORD_RECOVERY_OTP_VALIDATION_FAILED", pendingRequest.getUsername(),
                    "Expired password recovery OTP attempt: " + otpCode + " from IP: " + ipAddress);
                throw new DataAccessException("OTP has expired", null);
            }
            
            // Verify OTP in database
            OTP otp = otpDAO.getValidOTP(otpCode, OPERATION_PASSWORD_RECOVERY);
            if (otp == null) {
                pendingRequests.remove(otpCode);
                LoggerUtil.logSecurity("PASSWORD_RECOVERY_OTP_VALIDATION_FAILED", pendingRequest.getUsername(),
                    "Invalid or expired password recovery OTP in database: " + otpCode + " from IP: " + ipAddress);
                throw new DataAccessException("Invalid or expired OTP", null);
            }
            
            // Update password
            boolean passwordUpdated = userDAO.updatePassword(pendingRequest.getUserId(), newPassword);
            if (!passwordUpdated) {
                LoggerUtil.logError("PasswordRecoveryService", "Failed to update password for user ID: " + pendingRequest.getUserId(), null);
                throw new DataAccessException("Failed to update password", null);
            }
            
            // Clean up
            pendingRequests.remove(otpCode);
            otpDAO.markOTPAsUsed(otp.getId());
            
            LoggerUtil.logSecurity("PASSWORD_RECOVERY_SUCCESS", pendingRequest.getUsername(),
                "Password recovery successful for user: " + pendingRequest.getUsername() + " from IP: " + ipAddress);
            
            return true;
            
        } catch (Exception e) {
            LoggerUtil.logError("PasswordRecoveryService", "Failed to verify OTP and reset password", e);

            throw new DataAccessException("Failed to verify OTP and reset password", e);
        }
    }
    
    /**
     * Clean up expired pending recovery requests
     */
    public void cleanupExpiredRequests() {
        LocalDateTime now = LocalDateTime.now();
        pendingRequests.entrySet().removeIf(entry -> now.isAfter(entry.getValue().getExpiryTime()));
        LoggerUtil.logInfo("PasswordRecoveryService", "Cleaned up expired pending recovery requests");
    }
    
    /**
     * Send OTP via email for password recovery
     */
    private void sendPasswordRecoveryOTPEmail(String otpCode, String username, String email, String ipAddress) {
        try {
            // Email configuration - using ConfigLoader for secure credential management
            Properties props = new Properties();
            props.put("mail.smtp.auth", ConfigLoader.getString("email.auth.enable", "true"));
            props.put("mail.smtp.starttls.enable", ConfigLoader.getString("email.tls.enable", "true"));
            props.put("mail.smtp.host", ConfigLoader.getString("email.host", "smtp.gmail.com"));
            props.put("mail.smtp.port", ConfigLoader.getString("email.port", "587"));
            
            // Use ConfigLoader for secure credential management
            String emailUsername = ConfigLoader.getString("email.from_address", "canalprepschool@gmail.com");
            String emailPassword = ConfigLoader.getString("email.password");
            
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailUsername, emailPassword);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Password Recovery OTP - School Management System");
            
            String emailBody = "Dear " + username + ",\n\n" +
                "You have requested to recover your password for the School Management System.\n\n" +
                "Your One-Time Password (OTP) is: " + otpCode + "\n\n" +
                "This OTP will expire in " + OTP_EXPIRY_MINUTES + " minutes.\n\n" +
                "Please use this OTP to verify your identity and set a new password.\n\n" +
                "If you did not request this password recovery, please contact your administrator immediately.\n\n" +
                "Request made from IP: " + ipAddress + "\n\n" +
                "Best regards,\n" +
                "School Management System";
            
            message.setText(emailBody);
            
            Transport.send(message);
            
            LoggerUtil.logInfo("PasswordRecoveryService", "Password recovery OTP email sent successfully for user: " + username);
            
        } catch (Exception e) {
            LoggerUtil.logError("PasswordRecoveryService", "Failed to send password recovery OTP email for user: " + username + ". Error: " + e.getMessage(), e);

            e.printStackTrace();
            // Don't throw exception here - we still want the OTP to be valid even if email fails
        }
    }
    
    /**
     * Inner class to store pending password recovery information
     */
    public static class PendingRecoveryRequest {
        private final int userId;
        private final String username;
        private final String email;
        private final LocalDateTime expiryTime;
        
        public PendingRecoveryRequest(int userId, String username, String email, LocalDateTime expiryTime) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.expiryTime = expiryTime;
        }
        
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
    }
}