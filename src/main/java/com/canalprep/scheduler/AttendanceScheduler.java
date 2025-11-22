package com.canalprep.scheduler;
import com.canalprep.service.AttendanceService;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.config.ConfigLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Scheduler for automated attendance management
 * Runs daily at configurable time to mark absent students
 */
public class AttendanceScheduler {
    private static final Logger logger = Logger.getLogger(AttendanceScheduler.class.getName());
    private final ScheduledExecutorService scheduler;
    private final AttendanceService attendanceService;
    private final LocalTime dailyRunTime;
    private final boolean schedulerEnabled;
    
    public AttendanceScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.attendanceService = new AttendanceService();
        
        // Load configuration settings
        this.schedulerEnabled = ConfigLoader.getBoolean("attendance.schedule.enabled", true);
        String timeConfig = ConfigLoader.getString("attendance.schedule.time", "16:30");
        this.dailyRunTime = parseTimeConfig(timeConfig);
        
        LoggerUtil.logInfo("AttendanceScheduler", 
            String.format("Attendance scheduler initialized - Enabled: %s, Daily run time: %s", 
                schedulerEnabled, dailyRunTime));
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public AttendanceScheduler(AttendanceService attendanceService) {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.attendanceService = attendanceService;
        
        // Load configuration settings
        this.schedulerEnabled = ConfigLoader.getBoolean("attendance.schedule.enabled", true);
        String timeConfig = ConfigLoader.getString("attendance.schedule.time", "16:30");
        this.dailyRunTime = parseTimeConfig(timeConfig);
        
        LoggerUtil.logInfo("AttendanceScheduler", 
            String.format("Attendance scheduler initialized with injected service - Enabled: %s, Daily run time: %s", 
                schedulerEnabled, dailyRunTime));
    }
    
    /**
     * Parse time configuration string (HH:mm format) to LocalTime
     */
    private LocalTime parseTimeConfig(String timeConfig) {
        try {
            String[] parts = timeConfig.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid time format: " + timeConfig);
            }
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            LoggerUtil.logWarning("AttendanceScheduler", 
                String.format("Invalid time configuration '%s', using default 16:30. Error: %s", 
                    timeConfig, e.getMessage()));
            return LocalTime.of(16, 30); // Default fallback
        }
    }
    
    /**
     * Start the attendance scheduler
     */
    public void start() {
        if (!schedulerEnabled) {
            LoggerUtil.logInfo("AttendanceScheduler", "Attendance scheduler is disabled in configuration");
            return;
        }
        
        LoggerUtil.logInfo("AttendanceScheduler", 
            String.format("Starting attendance scheduler to run daily at %s...", dailyRunTime));
        
        // Calculate initial delay until next configured time
        long initialDelay = calculateInitialDelay();
        
        // Schedule the task to run daily at configured time
        scheduler.scheduleAtFixedRate(
            this::runDailyAttendanceUpdate,
            initialDelay,
            TimeUnit.DAYS.toSeconds(1), // Run every 24 hours
            TimeUnit.SECONDS
        );
        
        LoggerUtil.logInfo("AttendanceScheduler", 
            String.format("Attendance scheduler started. Next run in %d seconds at %s", initialDelay, dailyRunTime));
    }
    
    /**
     * Stop the attendance scheduler
     */
    public void stop() {
        LoggerUtil.logInfo("AttendanceScheduler", "Stopping attendance scheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LoggerUtil.logInfo("AttendanceScheduler", "Attendance scheduler stopped");
    }
    
    /**
     * Calculate the initial delay until the next configured run time
     */
    private long calculateInitialDelay() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextRun = now.with(dailyRunTime);
        
        // If it's already past the configured time today, schedule for tomorrow
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }
        
        return Duration.between(now, nextRun).getSeconds();
    }
    
    /**
     * Main method that runs the daily attendance update
     */
    private void runDailyAttendanceUpdate() {
        try {
            LoggerUtil.logInfo("AttendanceScheduler", "Starting automated daily attendance update job...");
            
            // Use AttendanceService for business logic and enhanced error handling
            AttendanceService.AttendanceUpdateResult result = attendanceService.performDailyAttendanceUpdate();
            
            if (result.isSuccess()) {
                LoggerUtil.logInfo("AttendanceScheduler", 
                    String.format("Daily attendance update completed successfully. %s", result.getMessage()));
                
                // Log security event for successful automation
                LoggerUtil.logSecurity("AUTOMATED_ATTENDANCE_UPDATE_SUCCESS", "SYSTEM", 
                    String.format("Automated attendance update completed. %d students marked absent for %s", 
                        result.getStudentsAffected(), LocalDateTime.now().toLocalDate()));
                        
                // Log database event for audit trail
                LoggerUtil.logDatabase("ATTENDANCE_AUTO_UPDATE", "SYSTEM", 
                    String.format("Automated attendance marking: %d students affected", result.getStudentsAffected()));
                        
            } else {
                LoggerUtil.logError("AttendanceScheduler", "Daily attendance update failed: " + result.getMessage(), null);
                
                // Log security event for failed automation
                LoggerUtil.logSecurity("AUTOMATED_ATTENDANCE_UPDATE_FAILED", "SYSTEM", 
                    "Automated attendance update failed: " + result.getMessage());
            }
                    
        } catch (Exception e) {
            LoggerUtil.logError("AttendanceScheduler", "Unexpected error during daily attendance update", e);
            
            // Log security event for failed automation with exception details
            LoggerUtil.logSecurity("AUTOMATED_ATTENDANCE_UPDATE_ERROR", "SYSTEM", 
                "Automated attendance update encountered unexpected error: " + e.getMessage());
                
            // Log error event for monitoring
            LoggerUtil.logError("AttendanceScheduler", 
                "Daily attendance job failed with exception: " + e.getClass().getSimpleName(), e);
        }
    }
    
    /**
     * Manual trigger for testing purposes
     * @return AttendanceUpdateResult with the results of the manual update
     */
    public AttendanceService.AttendanceUpdateResult runManualAttendanceUpdate() {
        LoggerUtil.logInfo("AttendanceScheduler", "Manual attendance update triggered");
        LoggerUtil.logSecurity("MANUAL_ATTENDANCE_UPDATE", "ADMIN", "Manual attendance update initiated");
        
        try {
            AttendanceService.AttendanceUpdateResult result = attendanceService.performDailyAttendanceUpdate();
            
            if (result.isSuccess()) {
                LoggerUtil.logInfo("AttendanceScheduler", "Manual attendance update completed: " + result.getMessage());
            } else {
                LoggerUtil.logWarning("AttendanceScheduler", "Manual attendance update failed: " + result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            LoggerUtil.logError("AttendanceScheduler", "Error during manual attendance update", e);
            return new AttendanceService.AttendanceUpdateResult(false, 0, "Manual update failed: " + e.getMessage());
        }
    }
    
    /**
     * Check if scheduler is running
     */
    public boolean isRunning() {
        return !scheduler.isShutdown() && !scheduler.isTerminated();
    }
    
    /**
     * Get the configured daily run time
     */
    public LocalTime getDailyRunTime() {
        return dailyRunTime;
    }
    
    /**
     * Check if scheduler is enabled in configuration
     */
    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }
}