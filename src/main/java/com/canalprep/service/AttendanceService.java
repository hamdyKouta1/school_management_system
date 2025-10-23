package com.canalprep.service;

import com.canalprep.dao.AttendanceDAO;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.exception.DataAccessException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service class for attendance-related business logic
 * Handles automated attendance management and business rules
 */
public class AttendanceService {
    private final AttendanceDAO attendanceDAO;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }
    
    /**
     * Perform automated daily attendance update
     * Marks students as absent if they don't have attendance records for today
     * @return AttendanceUpdateResult containing the results of the operation
     */
    public AttendanceUpdateResult performDailyAttendanceUpdate() {
        try {
            LoggerUtil.logInfo("AttendanceService", "Starting automated daily attendance update");
            
            String today = LocalDate.now().format(DATE_FORMATTER);
            
            // Get count of students without attendance before update
            int studentsWithoutAttendance = attendanceDAO.getStudentsWithoutAttendanceCount(today);
            
            if (studentsWithoutAttendance == 0) {
                LoggerUtil.logInfo("AttendanceService", "All students already have attendance records for today");
                return new AttendanceUpdateResult(true, 0, "All students already have attendance records");
            }
            
            // Mark absent students
            int studentsMarkedAbsent = attendanceDAO.markAbsentStudentsForToday();
            
            String message = String.format("Successfully marked %d students as absent for %s", 
                studentsMarkedAbsent, today);
            
            LoggerUtil.logInfo("AttendanceService", message);
            
            return new AttendanceUpdateResult(true, studentsMarkedAbsent, message);
            
        } catch (DataAccessException e) {
            String errorMessage = "Failed to perform daily attendance update: " + e.getMessage();
            LoggerUtil.logError("AttendanceService", errorMessage, e);
            return new AttendanceUpdateResult(false, 0, errorMessage);
        } catch (Exception e) {
            String errorMessage = "Unexpected error during daily attendance update: " + e.getMessage();
            LoggerUtil.logError("AttendanceService", errorMessage, e);
            return new AttendanceUpdateResult(false, 0, errorMessage);
        }
    }
    
    /**
     * Get attendance statistics for a specific date
     * @param date the date to check (format: YYYY-MM-DD)
     * @return AttendanceStatistics object with attendance data
     */
    public AttendanceStatistics getAttendanceStatistics(String date) {
        try {
            List<Map<String, Object>> attendanceRecords = attendanceDAO.getAttendanceByDate(date);
            int studentsWithoutAttendance = attendanceDAO.getStudentsWithoutAttendanceCount(date);
            
            int totalPresent = 0;
            int totalAbsent = 0;
            int totalLate = 0;
            
            for (Map<String, Object> record : attendanceRecords) {
                Integer status = (Integer) record.get("status_id");
                if (status != null) {
                    switch (status) {
                        case 1: // Present
                            totalPresent++;
                            break;
                        case 2: // Absent
                            totalAbsent++;
                            break;
                        case 3: // Late (if exists)
                            totalLate++;
                            break;
                    }
                }
            }
            
            return new AttendanceStatistics(date, totalPresent, totalAbsent, totalLate, studentsWithoutAttendance);
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("AttendanceService", "Error getting attendance statistics for date: " + date, e);
            return new AttendanceStatistics(date, 0, 0, 0, 0);
        }
    }
    
    /**
     * Validate attendance data before processing
     * @param studentId the student ID
     * @param date the attendance date
     * @param status the attendance status
     * @return true if valid, false otherwise
     */
    public boolean validateAttendanceData(int studentId, String date, int status) {
        // Basic validation
        if (studentId <= 0) {
            LoggerUtil.logWarning("AttendanceService", "Invalid student ID: " + studentId);
            return false;
        }
        
        if (date == null || date.trim().isEmpty()) {
            LoggerUtil.logWarning("AttendanceService", "Invalid date: " + date);
            return false;
        }
        
        // Validate status (1=Present, 2=Absent, 3=Late if applicable)
        if (status < 1 || status > 3) {
            LoggerUtil.logWarning("AttendanceService", "Invalid status: " + status);
            return false;
        }
        
        // Validate date format
        try {
            LocalDate.parse(date, DATE_FORMATTER);
        } catch (Exception e) {
            LoggerUtil.logWarning("AttendanceService", "Invalid date format: " + date);
            return false;
        }
        
        return true;
    }
    
    /**
     * Result class for attendance update operations
     */
    public static class AttendanceUpdateResult {
        private final boolean success;
        private final int studentsAffected;
        private final String message;
        
        public AttendanceUpdateResult(boolean success, int studentsAffected, String message) {
            this.success = success;
            this.studentsAffected = studentsAffected;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public int getStudentsAffected() { return studentsAffected; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return String.format("AttendanceUpdateResult{success=%s, studentsAffected=%d, message='%s'}", 
                success, studentsAffected, message);
        }
    }
    
    /**
     * Statistics class for attendance data
     */
    public static class AttendanceStatistics {
        private final String date;
        private final int totalPresent;
        private final int totalAbsent;
        private final int totalLate;
        private final int studentsWithoutRecords;
        
        public AttendanceStatistics(String date, int totalPresent, int totalAbsent, 
                                  int totalLate, int studentsWithoutRecords) {
            this.date = date;
            this.totalPresent = totalPresent;
            this.totalAbsent = totalAbsent;
            this.totalLate = totalLate;
            this.studentsWithoutRecords = studentsWithoutRecords;
        }
        
        public String getDate() { return date; }
        public int getTotalPresent() { return totalPresent; }
        public int getTotalAbsent() { return totalAbsent; }
        public int getTotalLate() { return totalLate; }
        public int getStudentsWithoutRecords() { return studentsWithoutRecords; }
        public int getTotalStudentsWithRecords() { return totalPresent + totalAbsent + totalLate; }
        
        @Override
        public String toString() {
            return String.format("AttendanceStatistics{date='%s', present=%d, absent=%d, late=%d, withoutRecords=%d}", 
                date, totalPresent, totalAbsent, totalLate, studentsWithoutRecords);
        }
    }
}