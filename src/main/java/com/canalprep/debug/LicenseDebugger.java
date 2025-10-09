package com.canalprep.debug;

import com.canalprep.license.service.LicenseService;
import com.canalprep.license.model.License;
import com.canalprep.exception.DataAccessException;
import com.canalprep.utilities.LoggerUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LicenseDebugger {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== License Debug Information ===");
            
            LicenseService licenseService = new LicenseService();
            
            // Get current license
            License currentLicense = licenseService.getCurrentLicense();
            
            if (currentLicense == null) {
                System.out.println("❌ No license found in database");
                return;
            }
            
            // Display license information
            System.out.println("\n📄 License Information:");
            System.out.println("ID: " + currentLicense.getId());
            System.out.println("License Key: " + currentLicense.getLicenseKey());
            System.out.println("Start Date: " + currentLicense.getStartDate());
            System.out.println("End Date: " + currentLicense.getEndDate());
            System.out.println("Remaining Days: " + currentLicense.getRemainingDays());
            System.out.println("Status: " + currentLicense.getStatus());
            System.out.println("Created At: " + currentLicense.getCreatedAt());
            System.out.println("Updated At: " + currentLicense.getUpdatedAt());
            
            // Current date information
            LocalDate today = LocalDate.now();
            System.out.println("\n📅 Date Information:");
            System.out.println("Today's Date: " + today);
            System.out.println("Today (formatted): " + today.format(DateTimeFormatter.ofPattern("d-M-yy")));
            
            // Validation checks
            System.out.println("\n🔍 Validation Checks:");
            System.out.println("Status is ACTIVE: " + "ACTIVE".equals(currentLicense.getStatus()));
            System.out.println("Start Date not null: " + (currentLicense.getStartDate() != null));
            System.out.println("End Date not null: " + (currentLicense.getEndDate() != null));
            
            if (currentLicense.getStartDate() != null) {
                boolean notBeforeStart = !today.isBefore(currentLicense.getStartDate());
                System.out.println("Today is not before start date: " + notBeforeStart);
                if (!notBeforeStart) {
                    System.out.println("  ⚠️  Today (" + today + ") is before start date (" + currentLicense.getStartDate() + ")");
                }
            }
            
            if (currentLicense.getEndDate() != null) {
                boolean notAfterEnd = !today.isAfter(currentLicense.getEndDate());
                System.out.println("Today is not after end date: " + notAfterEnd);
                if (!notAfterEnd) {
                    System.out.println("  ⚠️  Today (" + today + ") is after end date (" + currentLicense.getEndDate() + ")");
                }
            }
            
            // Overall validation
            boolean isValid = currentLicense.isValid();
            System.out.println("\n✅ Overall License Valid: " + isValid);
            
            // Service validation
            boolean serviceValid = licenseService.isLicenseValid();
            System.out.println("🔧 Service Validation: " + serviceValid);
            
            // Date comparison details
            if (currentLicense.getStartDate() != null && currentLicense.getEndDate() != null) {
                System.out.println("\n📊 Date Comparison Details:");
                System.out.println("Start Date: " + currentLicense.getStartDate() + " (" + currentLicense.getStartDate().toEpochDay() + " days since epoch)");
                System.out.println("Today:      " + today + " (" + today.toEpochDay() + " days since epoch)");
                System.out.println("End Date:   " + currentLicense.getEndDate() + " (" + currentLicense.getEndDate().toEpochDay() + " days since epoch)");
                
                long daysFromStart = today.toEpochDay() - currentLicense.getStartDate().toEpochDay();
                long daysToEnd = currentLicense.getEndDate().toEpochDay() - today.toEpochDay();
                
                System.out.println("Days since start: " + daysFromStart);
                System.out.println("Days until end: " + daysToEnd);
            }
            
            // Recommendations
            System.out.println("\n💡 Recommendations:");
            if (!isValid) {
                if (!"ACTIVE".equals(currentLicense.getStatus())) {
                    System.out.println("- License status is not ACTIVE. Current status: " + currentLicense.getStatus());
                    System.out.println("- Update license status to ACTIVE in database");
                }
                if (currentLicense.getStartDate() != null && today.isBefore(currentLicense.getStartDate())) {
                    System.out.println("- License has not started yet. Start date: " + currentLicense.getStartDate());
                }
                if (currentLicense.getEndDate() != null && today.isAfter(currentLicense.getEndDate())) {
                    System.out.println("- License has expired. End date: " + currentLicense.getEndDate());
                    System.out.println("- Renew the license or extend the end date");
                }
            } else {
                System.out.println("- License appears to be valid. Check application logs for other issues.");
            }
            
        } catch (DataAccessException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}