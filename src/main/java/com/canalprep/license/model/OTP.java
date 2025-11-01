package com.canalprep.license.model;
import java.time.LocalDateTime;

public class OTP {
    private int id;
    private String otpCode;
    private String email;
    private String operation; // RENEW, REMOVE
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean used;
    private String ipAddress;
    
    // Constructors
    public OTP() {}
    
    public OTP(String otpCode, String email, String operation, String ipAddress) {
        this.otpCode = otpCode;
        this.email = email;
        this.operation = operation;
        this.ipAddress = ipAddress;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusMinutes(10); // 10 minutes expiration
        this.used = false;
    }
    
    // Check if OTP is valid (not expired and not used)
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
    
    // Check if OTP is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    // Mark OTP as used
    public void markAsUsed() {
        this.used = true;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    @Override
    public String toString() {
        return "OTP{" +
                "id=" + id +
                ", otpCode='" + otpCode + '\'' +
                ", email='" + email + '\'' +
                ", operation='" + operation + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", used=" + used +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}