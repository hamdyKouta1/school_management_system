package com.canalprep.license.scheduler;

import com.canalprep.license.service.LicenseService;
import com.canalprep.license.service.OTPService;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.exception.DataAccessException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class LicenseScheduler {
    
    private static LicenseScheduler instance;
    private final ScheduledExecutorService scheduler;
    private final LicenseService licenseService;
    private final OTPService otpService;
    private boolean isRunning = false;
    
    private LicenseScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.licenseService = new LicenseService();
        this.otpService = new OTPService();
    }
    
    /**
     * Get singleton instance of LicenseScheduler
     * @return LicenseScheduler instance
     */
    public static synchronized LicenseScheduler getInstance() {
        if (instance == null) {
            instance = new LicenseScheduler();
        }
        return instance;
    }
    
    /**
     * Start the license scheduler
     */
    public synchronized void start() {
        if (isRunning) {
            LoggerUtil.logInfo("LicenseScheduler", "License scheduler is already running");
            return;
        }
        
        try {
            // Calculate initial delay to run at midnight
            long initialDelay = calculateInitialDelay();
            
            // Schedule daily license check at midnight
            scheduler.scheduleAtFixedRate(
                this::performDailyLicenseCheck,
                initialDelay,
                TimeUnit.DAYS.toSeconds(1), // 24 hours
                TimeUnit.SECONDS
            );
            
            // Schedule OTP cleanup every hour
            scheduler.scheduleAtFixedRate(
                this::performOTPCleanup,
                0, // Start immediately
                TimeUnit.HOURS.toSeconds(1), // 1 hour
                TimeUnit.SECONDS
            );
            
            isRunning = true;
            
            LoggerUtil.logInfo("LicenseScheduler", 
                "License scheduler started. Daily check will run in " + initialDelay + " seconds");
            
        } catch (Exception e) {
            LoggerUtil.logError("LicenseScheduler", "Failed to start license scheduler", e);
        }
    }
    
    /**
     * Stop the license scheduler
     */
    public synchronized void stop() {
        if (!isRunning) {
            LoggerUtil.logInfo("LicenseScheduler", "License scheduler is not running");
            return;
        }
        
        try {
            scheduler.shutdown();
            
            // Wait for existing tasks to complete
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            
            isRunning = false;
            
            LoggerUtil.logInfo("LicenseScheduler", "License scheduler stopped successfully");
            
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            LoggerUtil.logError("LicenseScheduler", "License scheduler shutdown interrupted", e);
        }
    }
    
    /**
     * Perform daily license check
     */
    private void performDailyLicenseCheck() {
        try {
            LoggerUtil.logInfo("LicenseScheduler", "Starting daily license check");
            
            // Get license status before check
            LicenseService.LicenseStatus statusBefore = licenseService.getLicenseStatus();
            
            licenseService.performDailyLicenseCheck();
            
            // Get license status after check
            LicenseService.LicenseStatus statusAfter = licenseService.getLicenseStatus();
            
            // Log status changes
            if (!statusBefore.getStatus().equals(statusAfter.getStatus())) {
                LoggerUtil.logSecurity("LICENSE_STATUS_CHANGED_DAILY_CHECK", "system", 
                    String.format("License status changed during daily check: %s -> %s, Valid: %s -> %s", 
                        statusBefore.getStatus(), statusAfter.getStatus(), 
                        statusBefore.isValid(), statusAfter.isValid()));
                        
                // Log endpoint access implications
                if (statusAfter.isValid() && !statusBefore.isValid()) {
                    LoggerUtil.logSecurity("ENDPOINTS_REACTIVATED_DAILY_CHECK", "system", 
                        "All endpoints reactivated due to license becoming valid during daily check");
                } else if (!statusAfter.isValid() && statusBefore.isValid()) {
                    LoggerUtil.logSecurity("ENDPOINTS_RESTRICTED_DAILY_CHECK", "system", 
                        "Endpoints restricted to license-only access due to license expiration during daily check");
                }
            }
            
            LoggerUtil.logInfo("LicenseScheduler", 
                String.format("Daily license check completed successfully. Status: %s, Valid: %s, Remaining Days: %d", 
                    statusAfter.getStatus(), statusAfter.isValid(), statusAfter.getRemainingDays()));
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("LicenseScheduler", "Failed to perform daily license check", e);
        } catch (Exception e) {
            LoggerUtil.logError("LicenseScheduler", "Unexpected error during daily license check", e);
        }
    }
    
    /**
     * Perform OTP cleanup
     */
    private void performOTPCleanup() {
        try {
            LoggerUtil.logInfo("LicenseScheduler", "Starting OTP cleanup");
            
            otpService.cleanupExpiredOTPs();
            
            LoggerUtil.logInfo("LicenseScheduler", "OTP cleanup completed successfully");
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("LicenseScheduler", "Failed to perform OTP cleanup", e);
        } catch (Exception e) {
            LoggerUtil.logError("LicenseScheduler", "Unexpected error during OTP cleanup", e);
        }
    }
    
    /**
     * Calculate initial delay to run at midnight
     * @return Initial delay in seconds
     */
    private long calculateInitialDelay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT);
        
        return ChronoUnit.SECONDS.between(now, nextMidnight);
    }
    
    /**
     * Manually trigger license check (for testing or immediate execution)
     */
    public void triggerLicenseCheck() {
        scheduler.execute(this::performDailyLicenseCheck);
        LoggerUtil.logInfo("LicenseScheduler", "Manual license check triggered");
    }
    
    /**
     * Manually trigger OTP cleanup (for testing or immediate execution)
     */
    public void triggerOTPCleanup() {
        scheduler.execute(this::performOTPCleanup);
        LoggerUtil.logInfo("LicenseScheduler", "Manual OTP cleanup triggered");
    }
    
    /**
     * Check if scheduler is running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning && !scheduler.isShutdown();
    }
    
    /**
     * Get scheduler status information
     * @return Status information as string
     */
    public String getStatus() {
        StringBuilder status = new StringBuilder();
        status.append("License Scheduler Status:\n");
        status.append("Running: ").append(isRunning()).append("\n");
        status.append("Shutdown: ").append(scheduler.isShutdown()).append("\n");
        status.append("Terminated: ").append(scheduler.isTerminated()).append("\n");
        
        if (isRunning()) {
            LocalDateTime nextMidnight = LocalDateTime.now().toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT);
            status.append("Next daily check: ").append(nextMidnight).append("\n");
        }
        
        return status.toString();
    }
}