package com.canalprep.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Manages log file rotation and archival to prevent log files from growing too large
 * and to maintain a clean logging directory structure.
 */
public class LogRotationManager {
    private static final Logger logger = Logger.getLogger(LogRotationManager.class.getName());
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_ARCHIVE_FILES = 30; // Keep 30 days of archives
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    private static ScheduledExecutorService scheduler;
    private static boolean isInitialized = false;
    
    /**
     * Initialize the log rotation manager with automatic rotation every 24 hours
     */
    public static synchronized void initialize() {
        if (isInitialized) {
            return;
        }
        
        scheduler = Executors.newScheduledThreadPool(1);
        
        // Schedule rotation check every hour
        scheduler.scheduleAtFixedRate(() -> {
            try {
                rotateLogsIfNeeded();
                cleanupOldArchives();
            } catch (Exception e) {
                logger.severe("Error during log rotation: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.HOURS);
        
        isInitialized = true;
        logger.info("Log rotation manager initialized");
    }
    
    /**
     * Check all log files and rotate them if they exceed the maximum size
     */
    public static void rotateLogsIfNeeded() {
        String logDir = LoggerUtil.getLogDirectory();
        File logDirectory = new File(logDir);
        
        if (!logDirectory.exists()) {
            return;
        }
        
        // Check each log subdirectory
        String[] subDirs = {"application", "error", "access", "debug"};
        
        for (String subDir : subDirs) {
            File subDirectory = new File(logDirectory, subDir);
            if (subDirectory.exists() && subDirectory.isDirectory()) {
                rotateDirectoryLogs(subDirectory);
            }
        }
    }
    
    /**
     * Rotate log files in a specific directory if they exceed the maximum size
     */
    private static void rotateDirectoryLogs(File directory) {
        File[] logFiles = directory.listFiles((dir, name) -> name.endsWith(".log"));
        
        if (logFiles == null) {
            return;
        }
        
        for (File logFile : logFiles) {
            if (logFile.length() > MAX_FILE_SIZE) {
                rotateLogFile(logFile);
            }
        }
    }
    
    /**
     * Rotate a specific log file by moving it to the archive directory
     */
    private static void rotateLogFile(File logFile) {
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            String fileName = logFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            
            // Create archive directory
            File archiveDir = new File(logFile.getParent(), "../archive");
            archiveDir.mkdirs();
            
            // Create archived file name
            String archivedFileName = baseName + "_" + timestamp + extension;
            File archivedFile = new File(archiveDir, archivedFileName);
            
            // Move the current log file to archive
            Files.move(logFile.toPath(), archivedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Create a new empty log file
            logFile.createNewFile();
            
            logger.info("Rotated log file: " + fileName + " -> " + archivedFileName);
            
        } catch (IOException e) {
            logger.severe("Failed to rotate log file " + logFile.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Clean up old archived log files to prevent disk space issues
     */
    public static void cleanupOldArchives() {
        String logDir = LoggerUtil.getLogDirectory();
        File archiveDirectory = new File(logDir, "archive");
        
        if (!archiveDirectory.exists()) {
            return;
        }
        
        File[] archiveFiles = archiveDirectory.listFiles((dir, name) -> name.endsWith(".log"));
        
        if (archiveFiles == null || archiveFiles.length <= MAX_ARCHIVE_FILES) {
            return;
        }
        
        // Sort files by last modified date (oldest first)
        java.util.Arrays.sort(archiveFiles, (f1, f2) -> 
            Long.compare(f1.lastModified(), f2.lastModified()));
        
        // Delete oldest files beyond the limit
        int filesToDelete = archiveFiles.length - MAX_ARCHIVE_FILES;
        for (int i = 0; i < filesToDelete; i++) {
            if (archiveFiles[i].delete()) {
                logger.info("Deleted old archive file: " + archiveFiles[i].getName());
            } else {
                logger.warning("Failed to delete old archive file: " + archiveFiles[i].getName());
            }
        }
    }
    
    /**
     * Manually trigger log rotation (useful for testing or manual maintenance)
     */
    public static void forceRotation() {
        logger.info("Manual log rotation triggered");
        rotateLogsIfNeeded();
        cleanupOldArchives();
    }
    
    /**
     * Shutdown the log rotation manager
     */
    public static synchronized void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("Log rotation manager shutdown");
        }
        isInitialized = false;
    }
    
    /**
     * Get the current status of log files
     */
    public static String getLogStatus() {
        StringBuilder status = new StringBuilder();
        String logDir = LoggerUtil.getLogDirectory();
        File logDirectory = new File(logDir);
        
        if (!logDirectory.exists()) {
            return "Log directory does not exist";
        }
        
        status.append("Log Directory: ").append(logDir).append("\n");
        
        String[] subDirs = {"application", "error", "access", "debug", "archive"};
        
        for (String subDir : subDirs) {
            File subDirectory = new File(logDirectory, subDir);
            if (subDirectory.exists() && subDirectory.isDirectory()) {
                File[] files = subDirectory.listFiles((dir, name) -> name.endsWith(".log"));
                if (files != null) {
                    status.append(subDir).append(": ").append(files.length).append(" files\n");
                    for (File file : files) {
                        long sizeKB = file.length() / 1024;
                        status.append("  - ").append(file.getName())
                               .append(" (").append(sizeKB).append(" KB)\n");
                    }
                }
            }
        }
        
        return status.toString();
    }
}