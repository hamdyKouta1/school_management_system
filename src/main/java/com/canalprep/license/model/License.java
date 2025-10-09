package com.canalprep.license.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Set;

public class License {
    private int id;
    private String licenseKey;
    private LocalDate startDate;
    private LocalDate endDate;
    private int remainingDays;
    private String status; // ACTIVE, EXPIRED, SUSPENDED
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String createdBy;
    
    // Constructors
    public License() {}
    
    public License(String licenseKey, LocalDate startDate, LocalDate endDate, String status) {
        this.licenseKey = licenseKey;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.remainingDays = calculateRemainingDays();
    }
    
    // Calculate remaining days from current date to end date
    public int calculateRemainingDays() {
        if (endDate == null) return 0;
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) {
            return 0;
        }
        return (int) today.until(endDate).getDays();
    }
    
    // Check if license is currently valid
 public boolean isValid() {
    LocalDate today = LocalDate.now();
    Set<String> validStatuses = Set.of("ACTIVE", "EXPIRING_SOON");
    
    return validStatuses.contains(status) && 
           startDate != null && 
           endDate != null && 
           !today.isBefore(startDate) && 
           !today.isAfter(endDate);
}
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getLicenseKey() { return licenseKey; }
    public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate;
        this.remainingDays = calculateRemainingDays();
    }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate;
        this.remainingDays = calculateRemainingDays();
    }
    
    public int getRemainingDays() { 
        this.remainingDays = calculateRemainingDays();
        return remainingDays; 
    }
    public void setRemainingDays(int remainingDays) { this.remainingDays = remainingDays; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    @Override
    public String toString() {
        return "License{" +
                "id=" + id +
                ", licenseKey='" + licenseKey + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", remainingDays=" + remainingDays +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}