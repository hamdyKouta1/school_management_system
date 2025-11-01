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
 * Service for handling user password reset OTP operations
 */
public class UserOTPService {
    
    private static final String OPERATION_PASSWORD_RESET = "PASSWORD_RESET";
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom random = new SecureRandom();
    
    private final OTPDAO otpDAO;
    private final UserDAO userDAO;
    
    // Store pending password reset requests
    private final ConcurrentMap<String, PendingPasswordReset> pendingResets = new ConcurrentHashMap<>();
    
    public UserOTPService() {
        this.otpDAO = new OTPDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Generate and send OTP for password reset
     * @param userId the user ID requesting password reset
     * @param requestedByUserId the ID of user making the request (can be same as userId for self-service)
     * @param requestedByRole the role of user making the request
     * @param ipAddress the IP address of the request
     * @return The generated OTP code
     * @throws DataAccessException if operation fails
     */
    public String generatePasswordResetOTP(int userId, int requestedByUserId, String requestedByRole, String ipAddress) 
            throws DataAccessException {
        try {
            // Get user details
            User user = userDAO.getUserById(userId);
            if (user == null) {
                throw new DataAccessException("User not found with ID: " + userId, null);
            }
            
            // Validate authorization
            if (!isAuthorizedForPasswordReset(userId, requestedByUserId, requestedByRole)) {
                throw new DataAccessException("Unauthorized password reset request", null);
            }
            
            // Generate 6-digit OTP
            String otpCode = generateOTPCode();
            
            // Create pending reset record
            PendingPasswordReset pendingReset = new PendingPasswordReset(
                userId, user.getUsername(), user.getEmail(), requestedByUserId, requestedByRole, 
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
            );
            pendingResets.put(otpCode, pendingReset);
            
            // Create OTP record in database
            OTP otp = new OTP(otpCode, user.getEmail(), OPERATION_PASSWORD_RESET, ipAddress);
            otpDAO.createOTP(otp);
            
            // Send OTP email
            sendPasswordResetOTPEmail(otpCode, user.getUsername(), user.getEmail(), requestedByRole, ipAddress);
            
            LoggerUtil.logSecurity("PASSWORD_RESET_OTP_GENERATED", requestedByRole,
                "Password reset OTP generated for user: " + user.getUsername() + " by: " + requestedByRole + " from IP: " + ipAddress);
            
            return otpCode;
            
        } catch (Exception e) {
            LoggerUtil.logError("UserOTPService", "Failed to generate password reset OTP", e);
            throw new DataAccessException("Failed to generate password reset OTP", e);
        }
    }
    
    /**
     * Validate OTP and reset password
     * @param otpCode the OTP code
     * @param newPassword the new password
     * @param ipAddress the IP address of the request
     * @return true if password reset successful
     * @throws DataAccessException if operation fails
     */
    public boolean validateOTPAndResetPassword(String otpCode, String newPassword, String ipAddress) 
            throws DataAccessException {
        try {
            // Check if OTP exists in pending resets
            PendingPasswordReset pendingReset = pendingResets.get(otpCode);
            if (pendingReset == null) {
                LoggerUtil.logSecurity("PASSWORD_RESET_OTP_VALIDATION_FAILED", "system",
                    "Invalid password reset OTP attempt: " + otpCode + " from IP: " + ipAddress);
                return false;
            }
            
            // Check if OTP is expired
            if (pendingReset.getExpiresAt().isBefore(LocalDateTime.now())) {
                pendingResets.remove(otpCode);
                LoggerUtil.logSecurity("PASSWORD_RESET_OTP_VALIDATION_FAILED", "system",
                    "Expired password reset OTP attempt: " + otpCode + " from IP: " + ipAddress);
                return false;
            }
            
            // Validate OTP in database
            OTP otp = otpDAO.getValidOTP(otpCode, OPERATION_PASSWORD_RESET);
            if (otp == null || !otp.isValid()) {
                pendingResets.remove(otpCode);
                LoggerUtil.logSecurity("PASSWORD_RESET_OTP_VALIDATION_FAILED", "system",
                    "Invalid or expired password reset OTP in database: " + otpCode + " from IP: " + ipAddress);
                return false;
            }
            
            // Update password
            boolean passwordUpdated = userDAO.updatePassword(pendingReset.getUserId(), newPassword);
            if (!passwordUpdated) {
                LoggerUtil.logError("UserOTPService", "Failed to update password for user ID: " + pendingReset.getUserId(), null);
                return false;
            }
            
            // Mark OTP as used
            otpDAO.markOTPAsUsed(otp.getId());
            
            // Remove from pending resets
            pendingResets.remove(otpCode);
            
            LoggerUtil.logSecurity("PASSWORD_RESET_SUCCESS", "system",
                "Password reset successful for user: " + pendingReset.getUsername() + " from IP: " + ipAddress);
            
            return true;
            
        } catch (Exception e) {
            LoggerUtil.logError("UserOTPService", "Failed to validate OTP and reset password", e);
            throw new DataAccessException("Failed to validate OTP and reset password", e);
        }
    }
    
    /**
     * Clean up expired pending password resets
     */
    public void cleanupExpiredResets() {
        LocalDateTime now = LocalDateTime.now();
        pendingResets.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));
        LoggerUtil.logInfo("UserOTPService", "Cleaned up expired pending password resets");
    }
    
    /**
     * Check if user is authorized to request password reset
     * @param targetUserId the user whose password is being reset
     * @param requestedByUserId the user making the request
     * @param requestedByRole the role of user making the request
     * @return true if authorized
     */
    private boolean isAuthorizedForPasswordReset(int targetUserId, int requestedByUserId, String requestedByRole) {
        // Self-service is always allowed
        if (targetUserId == requestedByUserId) {
            return true;
        }
        
        // Admin and developer can reset any user's password
        return "ADMIN".equals(requestedByRole) || "DEVELOPER".equals(requestedByRole);
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
     * Send OTP via email for password reset
     * @param otpCode the OTP code
     * @param username the username
     * @param email the email address
     * @param requestedByRole the role of user making the request
     * @param ipAddress the IP address
     * @throws MessagingException if email sending fails
     */
    private void sendPasswordResetOTPEmail(String otpCode, String username, String email, 
                                         String requestedByRole, String ipAddress) throws MessagingException {
        
        // Email configuration - using ConfigLoader for secure credential management
        String EMAIL_USERNAME = ConfigLoader.getString("email.from_address", "canalprepschool@gmail.com");
        String EMAIL_PASSWORD = ConfigLoader.getString("email.password");
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", ConfigLoader.getString("email.auth.enable", "true"));
        props.put("mail.smtp.starttls.enable", ConfigLoader.getString("email.tls.enable", "true"));
        props.put("mail.smtp.host", ConfigLoader.getString("email.host", "smtp.gmail.com"));
        props.put("mail.smtp.port", ConfigLoader.getString("email.port", "587"));
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject("Password Reset OTP - School Management System");
        
        String emailBody = String.format(
            "Dear %s,\n\n" +
            "A password reset has been requested for your account.\n\n" +
            "OTP Code: %s\n\n" +
            "This OTP is valid for 10 minutes.\n\n" +
            "Request Details:\n" +
            "- Requested by: %s\n" +
            "- IP Address: %s\n\n" +
            "If you did not request this password reset, please contact your administrator immediately.\n\n" +
            "Best regards,\n" +
            "School Management System",
            username, otpCode, requestedByRole, ipAddress
        );
        
        message.setText(emailBody);
        
        Transport.send(message);
        
        LoggerUtil.logInfo("UserOTPService", "Password reset OTP email sent successfully for user: " + username);
    }
    
    /**
     * Inner class to store pending password reset information
     */
    public static class PendingPasswordReset {
        private final int userId;
        private final String username;
        private final String email;
        private final int requestedByUserId;
        private final String requestedByRole;
        private final LocalDateTime expiresAt;
        
        public PendingPasswordReset(int userId, String username, String email, 
                                  int requestedByUserId, String requestedByRole, LocalDateTime expiresAt) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.requestedByUserId = requestedByUserId;
            this.requestedByRole = requestedByRole;
            this.expiresAt = expiresAt;
        }
        
        // Getters
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public int getRequestedByUserId() { return requestedByUserId; }
        public String getRequestedByRole() { return requestedByRole; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
    }
}