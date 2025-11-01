package com.canalprep.debug;

import com.canalprep.license.service.LicenseService;
import com.canalprep.license.model.License;
import com.canalprep.exception.DataAccessException;
import java.time.LocalDate;


public class LicenseDebugger {
    
    public static void main(String[] args) {
        try {

            
            LicenseService licenseService = new LicenseService();
            
            // Get current license
            License currentLicense = licenseService.getCurrentLicense();
            
            if (currentLicense == null) {

                return;
            }
            
            // Display license information

            
            // Current date information
            LocalDate today = LocalDate.now();

            
            // Validation checks

            
            if (currentLicense.getStartDate() != null) {
                boolean notBeforeStart = !today.isBefore(currentLicense.getStartDate());
    
            }
            
            if (currentLicense.getEndDate() != null) {
                boolean notAfterEnd = !today.isAfter(currentLicense.getEndDate());
            }
            
            // Overall validation
            boolean isValid = currentLicense.isValid();

            
            // Service validation
            boolean serviceValid = licenseService.isLicenseValid();

            
            // Date comparison details
            if (currentLicense.getStartDate() != null && currentLicense.getEndDate() != null) {

                
                long daysFromStart = today.toEpochDay() - currentLicense.getStartDate().toEpochDay();
                long daysToEnd = currentLicense.getEndDate().toEpochDay() - today.toEpochDay();
                

            }
            

            
        } catch (DataAccessException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}