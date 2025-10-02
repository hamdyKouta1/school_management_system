package com.canalprep.utilities;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * Centralized logging utility for the School Management System.
 * Provides different logging methods for various types of events.
 */
public class LoggerUtil {
    
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Different loggers for different purposes
    private static final Logger APPLICATION_LOGGER = Logger.getLogger("APPLICATION");
    private static final Logger ERROR_LOGGER = Logger.getLogger("ERROR");
    private static final Logger ACCESS_LOGGER = Logger.getLogger("ACCESS");
    private static final Logger DEBUG_LOGGER = Logger.getLogger("DEBUG");
    private static final Logger SECURITY_LOGGER = Logger.getLogger("SECURITY");
    
    private static boolean initialized = false;
    
    /**
     * Initialize the logging system with custom handlers
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Ensure log directories exist
            createLogDirectories();
            
            // Configure application logger
            setupApplicationLogger();
            
            // Configure error logger
            setupErrorLogger();
            
            // Configure access logger
            setupAccessLogger();
            
            // Configure debug logger
            setupDebugLogger();
            
            // Configure security logger
            setupSecurityLogger();
            
            // Initialize log rotation manager
            LogRotationManager.initialize();
            
            initialized = true;
            logInfo("LoggerUtil", "Logging system initialized successfully with log rotation");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize logging system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create log directories if they don't exist
     */
    private static void createLogDirectories() throws IOException {
        String[] directories = {"application", "error", "access", "debug", "archive"};
        
        for (String dir : directories) {
            Path path = Paths.get(LOG_DIR, dir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }
    }
    
    /**
     * Setup application logger for general application events
     */
    private static void setupApplicationLogger() throws IOException {
        FileHandler fileHandler = new FileHandler(
            LOG_DIR + "/application/app-%g.log", 
            10 * 1024 * 1024, // 10MB
            10, // 10 files
            true // append
        );
        fileHandler.setFormatter(new CustomFormatter());
        APPLICATION_LOGGER.addHandler(fileHandler);
        APPLICATION_LOGGER.setLevel(Level.INFO);
        APPLICATION_LOGGER.setUseParentHandlers(false);
    }
    
    /**
     * Setup error logger for error events
     */
    private static void setupErrorLogger() throws IOException {
        FileHandler fileHandler = new FileHandler(
            LOG_DIR + "/error/error-%g.log", 
            5 * 1024 * 1024, // 5MB
            5, // 5 files
            true // append
        );
        fileHandler.setFormatter(new CustomFormatter());
        ERROR_LOGGER.addHandler(fileHandler);
        ERROR_LOGGER.setLevel(Level.WARNING);
        ERROR_LOGGER.setUseParentHandlers(false);
    }
    
    /**
     * Setup access logger for HTTP requests
     */
    private static void setupAccessLogger() throws IOException {
        FileHandler fileHandler = new FileHandler(
            LOG_DIR + "/access/access-%g.log", 
            20 * 1024 * 1024, // 20MB
            10, // 10 files
            true // append
        );
        fileHandler.setFormatter(new AccessLogFormatter());
        ACCESS_LOGGER.addHandler(fileHandler);
        ACCESS_LOGGER.setLevel(Level.INFO);
        ACCESS_LOGGER.setUseParentHandlers(false);
    }
    
    /**
     * Setup debug logger for detailed debugging
     */
    private static void setupDebugLogger() throws IOException {
        FileHandler fileHandler = new FileHandler(
            LOG_DIR + "/debug/debug-%g.log", 
            50 * 1024 * 1024, // 50MB
            5, // 5 files
            true // append
        );
        fileHandler.setFormatter(new CustomFormatter());
        DEBUG_LOGGER.addHandler(fileHandler);
        DEBUG_LOGGER.setLevel(Level.FINE);
        DEBUG_LOGGER.setUseParentHandlers(false);
    }
    
    /**
     * Setup security logger for authentication and authorization events
     */
    private static void setupSecurityLogger() throws IOException {
        FileHandler fileHandler = new FileHandler(
            LOG_DIR + "/application/security-%g.log", 
            10 * 1024 * 1024, // 10MB
            10, // 10 files
            true // append
        );
        fileHandler.setFormatter(new SecurityLogFormatter());
        SECURITY_LOGGER.addHandler(fileHandler);
        SECURITY_LOGGER.setLevel(Level.INFO);
        SECURITY_LOGGER.setUseParentHandlers(false);
    }
    
    // Public logging methods
    
    /**
     * Log general application information
     */
    public static void logInfo(String source, String message) {
        if (!initialized) initialize();
        APPLICATION_LOGGER.info(String.format("[%s] %s", source, message));
    }
    
    /**
     * Log warning messages
     */
    public static void logWarning(String source, String message) {
        if (!initialized) initialize();
        APPLICATION_LOGGER.warning(String.format("[%s] %s", source, message));
        ERROR_LOGGER.warning(String.format("[%s] %s", source, message));
    }
    
    /**
     * Log error messages
     */
    public static void logError(String source, String message, Throwable throwable) {
        if (!initialized) initialize();
        String errorMsg = String.format("[%s] %s", source, message);
        APPLICATION_LOGGER.severe(errorMsg);
        ERROR_LOGGER.severe(errorMsg);
        if (throwable != null) {
            ERROR_LOGGER.log(Level.SEVERE, "Exception details:", throwable);
        }
    }
    
    /**
     * Log HTTP access events
     */
    public static void logAccess(String method, String uri, String userAgent, String clientIP, int responseCode, long responseTime) {
        if (!initialized) initialize();
        String accessMsg = String.format("%s %s - %s - %s - %d - %dms", 
            method, uri, clientIP, userAgent, responseCode, responseTime);
        ACCESS_LOGGER.info(accessMsg);
    }
    
    /**
     * Log debug information
     */
    public static void logDebug(String source, String message) {
        if (!initialized) initialize();
        DEBUG_LOGGER.fine(String.format("[%s] %s", source, message));
    }
    
    /**
     * Log security events (authentication, authorization)
     */
    public static void logSecurity(String event, String username, String details) {
        if (!initialized) initialize();
        String securityMsg = String.format("EVENT: %s | USER: %s | DETAILS: %s", 
            event, username != null ? username : "ANONYMOUS", details);
        SECURITY_LOGGER.info(securityMsg);
    }
    
    /**
     * Log database operations
     */
    public static void logDatabase(String operation, String table, String details) {
        if (!initialized) initialize();
        String dbMsg = String.format("DB_OPERATION: %s | TABLE: %s | DETAILS: %s", 
            operation, table, details);
        APPLICATION_LOGGER.info(dbMsg);
    }
    
    /**
     * Custom formatter for general logs
     */
    private static class CustomFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("%s [%s] %s: %s%n",
                LocalDateTime.now().format(DATE_FORMAT),
                record.getLevel(),
                record.getLoggerName(),
                record.getMessage());
        }
    }
    
    /**
     * Custom formatter for access logs
     */
    private static class AccessLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("%s [ACCESS] %s%n",
                LocalDateTime.now().format(DATE_FORMAT),
                record.getMessage());
        }
    }
    
    /**
     * Custom formatter for security logs
     */
    private static class SecurityLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("%s [SECURITY] %s%n",
                LocalDateTime.now().format(DATE_FORMAT),
                record.getMessage());
        }
    }
    
    /**
     * Get current log directory path
     */
    public static String getLogDirectory() {
        return LOG_DIR;
    }
    
    /**
     * Get current logging status including rotation information
     */
    public static String getLoggingStatus() {
        return LogRotationManager.getLogStatus();
    }
    
    /**
     * Manually trigger log rotation
     */
    public static void rotateLogsNow() {
        LogRotationManager.forceRotation();
        logInfo("LoggerUtil", "Manual log rotation completed");
    }
    
    /**
     * Shutdown the logging system and rotation manager
     */
    public static void shutdown() {
        if (initialized) {
            LogRotationManager.shutdown();
            logInfo("LoggerUtil", "LoggerUtil shutdown completed");
            initialized = false;
        }
    }
    
    /**
     * Check if logging system is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}