package com.canalprep.auth.model;
import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String salt;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private String role;
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
}