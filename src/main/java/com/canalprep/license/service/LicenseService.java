package com.canalprep.license.service;

import com.canalprep.license.model.License;
import com.canalprep.license.dao.LicenseDAO;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.exception.DataAccessException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class LicenseService {
    
    private final LicenseDAO licenseDAO;
    
    public LicenseService() {
        this.licenseDAO = new LicenseDAO();
    }
    
    /**
     * Get current license information
     * @return License object with current status and remaining days
     * @throws DataAccessException if database operation fails
     */
    public License getCurrentLicense() throws DataAccessException {
        try {
            License license = licenseDAO.getCurrentLicense();
            
            if (license != null) {
                // Update remaining days based on current date
                updateRemainingDays(license);
            }
            
            return license;
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseService", "Failed to get current license", e);
            throw new DataAccessException("Failed to get current license", e);
        }
    }
    
    /**
     * Check if the application has a valid license
     * @return true if license is valid, false otherwise
     * @throws DataAccessException if database operation fails
     */
    public boolean isLicenseValid() throws DataAccessException {
        License license = getCurrentLicense();
        
        if (license == null) {
            LoggerUtil.logSecurity("LICENSE_CHECK", "system", "No license found - access will be restricted");
            return false;
        }
        
        boolean isValid = license.isValid();
        
        LoggerUtil.logSecurity("LICENSE_CHECK", "system", 
            "License validation result: " + isValid + ", remaining days: " + license.getRemainingDays() + 
            ", status: " + license.getStatus());
        
        return isValid;
    }
    
    /**
     * Get license status for endpoint access control
     * @return LicenseStatus object containing validation result and details
     * @throws DataAccessException if database operation fails
     */
    public LicenseStatus getLicenseStatus() throws DataAccessException {
        License license = getCurrentLicense();
        
        if (license == null) {
            LoggerUtil.logSecurity("LICENSE_STATUS_CHECK", "system", "No license found - endpoints restricted");
            return new LicenseStatus(false, "NO_LICENSE", "No license found", 0);
        }
        
        boolean isValid = license.isValid();
        String status = license.getStatus();
        String message = isValid ? "License is valid" : "License expired or invalid";
        
        LoggerUtil.logSecurity("LICENSE_STATUS_CHECK", "system", 
            "License status: " + status + ", valid: " + isValid + ", remaining days: " + license.getRemainingDays());
        
        return new LicenseStatus(isValid, status, message, license.getRemainingDays());
    }
    
    /**
     * Renew license with specified duration and optional start date
     * @param durationDays The duration in days
     * @param startDate Optional start date (if null, starts today)
     * @param createdBy The user who created the license
     * @return The new license object
     * @throws DataAccessException if database operation fails
     */
    public License renewLicense(int durationDays, LocalDate startDate, String createdBy) throws DataAccessException {
        try {
            // Deactivate all existing licenses
            licenseDAO.deactivateAllLicenses();
            
            // Set start date to today if not provided
            if (startDate == null) {
                startDate = LocalDate.now();
            }
            
            // Calculate end date
            LocalDate endDate = startDate.plusDays(durationDays);
            
            // Generate new license key
            String licenseKey = generateLicenseKey();
            
            // Create new license
            License newLicense = new License(
                licenseKey,
                startDate,
                endDate,
                "ACTIVE"
            );
            newLicense.setCreatedBy(createdBy);
            
            // Save to database
            licenseDAO.createLicense(newLicense);
            
            LoggerUtil.logSecurity("LICENSE_RENEWED", createdBy, 
                "License renewed for " + durationDays + " days, start date: " + startDate + ", end date: " + endDate + 
                ", license key: " + licenseKey);
            
            // Log endpoint re-enablement
            LoggerUtil.logSecurity("ENDPOINTS_REENABLED", createdBy, 
                "All system endpoints re-enabled after successful license renewal");
            
            return newLicense;
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseService", "Failed to renew license", e);
            throw new DataAccessException("Failed to renew license", e);
        }
    }
    
    /**
     * Remove current license
     * @param removedBy The user who removed the license
     * @throws DataAccessException if database operation fails
     */
    public void removeLicense(String removedBy) throws DataAccessException {
        try {
            License currentLicense = licenseDAO.getCurrentLicense();
            
            if (currentLicense == null) {
                throw new DataAccessException("No active license found to remove", null);
            }
            
            // Deactivate the license instead of deleting for audit purposes
            licenseDAO.updateLicense(currentLicense.getId(), 0, "REMOVED");
            
            LoggerUtil.logSecurity("LICENSE_REMOVED", removedBy, 
                "License removed: " + currentLicense.getLicenseKey());
            
            // Log endpoint restriction
            LoggerUtil.logSecurity("ENDPOINTS_RESTRICTED", removedBy, 
                "System endpoints restricted after license removal");
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseService", "Failed to remove license", e);
            throw new DataAccessException("Failed to remove license", e);
        }
    }
    
    /**
     * Perform daily license check and update remaining days
     * This method should be called by a scheduler daily
     * @throws DataAccessException if database operation fails
     */
    public void performDailyLicenseCheck() throws DataAccessException {
        try {
            License license = licenseDAO.getCurrentLicense();
            
            if (license == null) {
                LoggerUtil.logInfo("LicenseService", "No active license found during daily check");
                return;
            }
            
            // Calculate remaining days
            long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), license.getEndDate());
            
            String newStatus;
            if (remainingDays <= 0) {
                newStatus = "EXPIRED";
                remainingDays = 0;
            } else if (remainingDays <= 7) {
                newStatus = "EXPIRING_SOON";
            } else {
                newStatus = "ACTIVE";
            }
            
            // Update license in database
            licenseDAO.updateLicense(license.getId(), (int) remainingDays, newStatus);
            
            LoggerUtil.logInfo("LicenseService", 
                "Daily license check completed. Remaining days: " + remainingDays + ", Status: " + newStatus);
            
            // Log security event for expired license
            if (remainingDays <= 0) {
                LoggerUtil.logSecurity("LICENSE_EXPIRED", "system", 
                    "License has expired: " + license.getLicenseKey() + " - endpoints will be restricted");
                LoggerUtil.logSecurity("ENDPOINTS_RESTRICTED", "system", 
                    "System endpoints restricted due to license expiration");
            } else if (remainingDays <= 7) {
                LoggerUtil.logSecurity("LICENSE_EXPIRING_SOON", "system", 
                    "License expiring in " + remainingDays + " days: " + license.getLicenseKey());
            } else if ("EXPIRED".equals(license.getStatus()) && remainingDays > 0) {
                // License was renewed and is now active again
                LoggerUtil.logSecurity("LICENSE_REACTIVATED", "system", 
                    "License reactivated: " + license.getLicenseKey() + " - endpoints re-enabled");
                LoggerUtil.logSecurity("ENDPOINTS_REENABLED", "system", 
                    "System endpoints re-enabled after license reactivation");
            }
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseService", "Failed to perform daily license check", e);
            throw new DataAccessException("Failed to perform daily license check", e);
        }
    }
    
    /**
     * Get all licenses (for admin purposes)
     * @return List of all licenses
     * @throws DataAccessException if database operation fails
     */
    public List<License> getAllLicenses() throws DataAccessException {
        try {
            return licenseDAO.getAllLicenses();
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseService", "Failed to get all licenses", e);
            throw new DataAccessException("Failed to get all licenses", e);
        }
    }
    
    /**
     * Update remaining days for a license based on current date
     * @param license The license to update
     */
    private void updateRemainingDays(License license) {
        long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), license.getEndDate());
        
        if (remainingDays < 0) {
            remainingDays = 0;
        }
        
        license.setRemainingDays((int) remainingDays);
        
        // Update status based on remaining days
        if (remainingDays <= 0) {
            license.setStatus("EXPIRED");
        } else if (remainingDays <= 7) {
            license.setStatus("EXPIRING_SOON");
        } else if (license.getStatus().equals("EXPIRED") || license.getStatus().equals("EXPIRING_SOON")) {
            license.setStatus("ACTIVE");
        }
        
        license.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
    }
    
    /**
     * Generate a unique license key
     * @return A unique license key
     */
    private String generateLicenseKey() {
        return "LIC-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 16);
    }
    
    /**
     * Validate license duration
     * @param durationDays The duration in days
     * @throws IllegalArgumentException if duration is invalid
     */
    public void validateLicenseDuration(int durationDays) throws IllegalArgumentException {
        if (durationDays <= 0) {
            throw new IllegalArgumentException("License duration must be greater than 0 days");
        }
        
        if (durationDays > 3650) { // Max 10 years
            throw new IllegalArgumentException("License duration cannot exceed 3650 days (10 years)");
        }
    }
    
    /**
     * Validate start date
     * @param startDate The start date
     * @throws IllegalArgumentException if start date is invalid
     */
    public void validateStartDate(LocalDate startDate) throws IllegalArgumentException {
        if (startDate != null && startDate.isBefore(LocalDate.now().minusDays(1))) {
            throw new IllegalArgumentException("Start date cannot be in the past (before yesterday)");
        }
        
        if (startDate != null && startDate.isAfter(LocalDate.now().plusYears(1))) {
            throw new IllegalArgumentException("Start date cannot be more than 1 year in the future");
        }
    }
    
    /**
     * Inner class to represent license status for endpoint access control
     */
    public static class LicenseStatus {
        private final boolean valid;
        private final String status;
        private final String message;
        private final int remainingDays;
        
        public LicenseStatus(boolean valid, String status, String message, int remainingDays) {
            this.valid = valid;
            this.status = status;
            this.message = message;
            this.remainingDays = remainingDays;
        }
        
        public boolean isValid() { return valid; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public int getRemainingDays() { return remainingDays; }
    }
}