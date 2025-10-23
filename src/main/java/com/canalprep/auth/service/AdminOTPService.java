package com.canalprep.auth.service;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdminOTPService {
    private static final String ADMIN_EMAIL = "hamdyhkouta@gmail.com";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "canalprepschool@gmail.com";
    private static final String EMAIL_PASSWORD = "eebb hamx ycyp pvui";
    private static final String OPERATION_ADMIN_CREATION = "ADMIN_CREATION";
    
    private final OTPDAO otpDAO;
    private final SecureRandom random;
    
    // Store pending admin registrations temporarily
    private static final Map<String, PendingAdminRegistration> pendingRegistrations = new ConcurrentHashMap<>();
    
    public AdminOTPService() {
        this.otpDAO = new OTPDAO();
        this.random = new SecureRandom();
    }
    
    /**
     * Inner class to store pending admin registration data
     */
    public static class PendingAdminRegistration {
        private final String username;
        private final String email;
        private final String password;
        private final String role;
        private final String requesterRole;
        private final String ipAddress;
        private final LocalDateTime createdAt;
        
        public PendingAdminRegistration(String username, String email, String password, 
                                      String role, String requesterRole, String ipAddress) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.role = role;
            this.requesterRole = requesterRole;
            this.ipAddress = ipAddress;
            this.createdAt = LocalDateTime.now();
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getRole() { return role; }
        public String getRequesterRole() { return requesterRole; }
        public String getIpAddress() { return ipAddress; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        
        // Check if registration is expired (valid for 15 minutes)
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(createdAt.plusMinutes(15));
        }
    }
    
    /**
     * Generate and send OTP for admin user creation
     * @param username The username for the new admin
     * @param email The email for the new admin
     * @param password The password for the new admin
     * @param role The role for the new admin
     * @param requesterRole The role of the user making the request
     * @param ipAddress The IP address of the requester
     * @return The generated OTP code
     * @throws DataAccessException if database operation fails
     */
    public String generateAndSendAdminOTP(String username, String email, String password, 
                                        String role, String requesterRole, String ipAddress) 
                                        throws DataAccessException {
        try {
            // Validate requester role
            if (!"ADMIN".equals(requesterRole) && !"DEVELOPER".equals(requesterRole)) {
                throw new DataAccessException("Only ADMIN or DEVELOPER roles can create admin users", null);
            }
            
            // Generate 6-digit OTP
            String otpCode = generateOTPCode();

            
            // Store pending registration
            PendingAdminRegistration pendingReg = new PendingAdminRegistration(
                username, email, password, role, requesterRole, ipAddress
            );
            pendingRegistrations.put(otpCode, pendingReg);
            
            // Create OTP record in database
            OTP otp = new OTP(otpCode, ADMIN_EMAIL, OPERATION_ADMIN_CREATION, ipAddress);
            otpDAO.createOTP(otp);
            
            // Send email
            sendAdminOTPEmail(otpCode, username, email, role, requesterRole, ipAddress);
            
            LoggerUtil.logSecurity("ADMIN_OTP_GENERATED", requesterRole, 
                "OTP generated for admin creation: " + username + " (" + role + ") from IP: " + ipAddress);
            
            return otpCode;
            
        } catch (SQLException e) {
            LoggerUtil.logError("AdminOTPService", "Failed to generate admin OTP", e);
            throw new DataAccessException("Failed to generate admin OTP", e);
        } catch (MessagingException e) {
            LoggerUtil.logError("AdminOTPService", "Failed to send admin OTP email", e);
            throw new DataAccessException("Failed to send admin OTP email", e);
        }
    }
    
    /**
     * Validate OTP and return pending registration data
     * @param otpCode The OTP code to validate
     * @param ipAddress The IP address of the requester
     * @return PendingAdminRegistration if valid, null otherwise
     * @throws DataAccessException if database operation fails
     */
    public PendingAdminRegistration validateAdminOTP(String otpCode, String ipAddress) 
                                                   throws DataAccessException {
        try {
            // Check if OTP exists in pending registrations
            PendingAdminRegistration pendingReg = pendingRegistrations.get(otpCode);
            if (pendingReg == null) {
                LoggerUtil.logSecurity("ADMIN_OTP_VALIDATION_FAILED", "system", 
                    "Invalid admin OTP attempt: " + otpCode + " from IP: " + ipAddress);
                return null;
            }
            
            // Check if pending registration is expired
            if (pendingReg.isExpired()) {
                pendingRegistrations.remove(otpCode);
                LoggerUtil.logSecurity("ADMIN_OTP_VALIDATION_FAILED", "system", 
                    "Expired admin OTP attempt: " + otpCode + " from IP: " + ipAddress);
                return null;
            }
            
            // Validate OTP in database
            OTP otp = otpDAO.getValidOTP(otpCode, OPERATION_ADMIN_CREATION);
            if (otp == null || !otp.isValid()) {
                pendingRegistrations.remove(otpCode);
                LoggerUtil.logSecurity("ADMIN_OTP_VALIDATION_FAILED", "system", 
                    "Invalid or expired admin OTP in database: " + otpCode + " from IP: " + ipAddress);
                return null;
            }
            
            // Mark OTP as used
            otpDAO.markOTPAsUsed(otp.getId());
            
            // Remove from pending registrations
            pendingRegistrations.remove(otpCode);
            
            LoggerUtil.logSecurity("ADMIN_OTP_VALIDATION_SUCCESS", "system", 
                "Admin OTP validated successfully for user: " + pendingReg.getUsername() + " from IP: " + ipAddress);
            
            return pendingReg;
            
        } catch (SQLException e) {
            LoggerUtil.logError("AdminOTPService", "Failed to validate admin OTP", e);
            throw new DataAccessException("Failed to validate admin OTP", e);
        }
    }
    
    /**
     * Clean up expired pending registrations
     */
    public void cleanupExpiredRegistrations() {
        pendingRegistrations.entrySet().removeIf(entry -> entry.getValue().isExpired());
        LoggerUtil.logInfo("AdminOTPService", "Cleaned up expired pending admin registrations");
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
     * Send OTP via email for admin creation
     * @param otpCode The OTP code
     * @param username The username for the new admin
     * @param email The email for the new admin
     * @param role The role for the new admin
     * @param requesterRole The role of the requester
     * @param ipAddress The requester's IP address
     * @throws MessagingException if email sending fails
     */
    private void sendAdminOTPEmail(String otpCode, String username, String email, 
                                 String role, String requesterRole, String ipAddress) 
                                 throws MessagingException {
        // Skip email sending if credentials are not configured
        if (EMAIL_USERNAME == null || EMAIL_PASSWORD == null) {
            LoggerUtil.logInfo("AdminOTPService", "Email credentials not configured. Admin OTP: " + otpCode);
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
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(ADMIN_EMAIL));
        message.setSubject("Admin User Creation OTP - School Management System");
        
        String emailBody = String.format(
            "Dear Administrator,\n\n" +
            "A request has been made to create a new admin user in the School Management System.\n\n" +
            "OTP Code: %s\n" +
            "New Admin Username: %s\n" +
            "New Admin Email: %s\n" +
            "New Admin Role: %s\n" +
            "Requested by: %s role\n" +
            "Request IP: %s\n" +
            "Valid for: 10 minutes\n\n" +
            "If you did not authorize this admin creation, please ignore this email.\n\n" +
            "Best regards,\n" +
            "School Management System",
            otpCode, username, email, role, requesterRole, ipAddress
        );
        
        message.setText(emailBody);
        
        Transport.send(message);
        
        LoggerUtil.logInfo("AdminOTPService", "Admin OTP email sent successfully for user: " + username);
    }
}