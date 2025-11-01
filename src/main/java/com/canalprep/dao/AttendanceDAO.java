package com.canalprep.dao;

import com.canalprep.model.StudentAttendanceDetails;
import com.canalprep.exception.DataAccessException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AttendanceDAO {
    private static final Logger logger = Logger.getLogger(AttendanceDAO.class.getName());

    /**
     * Get student attendance details by student ID with attendance records
     */
    public Map<String, Object> getStudentAttendanceById(int studentId) {
        // Get all attendance records and filter by student ID
        List<Map<String, Object>> allAttendance = getAllAttendanceWithStudentDetails();
        
        Map<String, Object> studentData = null;
        List<Map<String, Object>> attendanceRecords = new ArrayList<>();
        
        for (Map<String, Object> record : allAttendance) {
            Integer recordStudentId = (Integer) record.get("student_id");
            if (recordStudentId != null && recordStudentId.equals(studentId)) {
                // Initialize student data if not done yet
                if (studentData == null) {
                    studentData = new HashMap<>();
                    studentData.put("student_id", record.get("student_id"));
                    studentData.put("student_name", record.get("student_name"));
                    studentData.put("current_address", record.get("current_address"));
                    studentData.put("medical_status", record.get("medical_status"));
                    studentData.put("grade", record.get("grade"));
                    studentData.put("class", record.get("class"));
                }
                
                // Add attendance record
                Map<String, Object> attendanceRecord = new HashMap<>();
                attendanceRecord.put("attendance_id", record.get("attendance_id"));
                attendanceRecord.put("attendance_date", record.get("attendance_date"));
                attendanceRecord.put("status_id", record.get("status_id"));
                attendanceRecord.put("status_name", record.get("status_name"));
                attendanceRecord.put("arrival_time", record.get("arrival_time"));
                attendanceRecords.add(attendanceRecord);
            }
        }
        
        if (studentData != null) {
            studentData.put("attendance_records", attendanceRecords);
            return studentData;
        }
        
        return null;
    }

    /**
     * Get student attendance details by student name with attendance records
     */
    public Map<String, Object> getStudentAttendanceByName(String studentName) {
        // Get all attendance records and filter by student name
        List<Map<String, Object>> allAttendance = getAllAttendanceWithStudentDetails();
        
        Map<String, Object> studentData = null;
        List<Map<String, Object>> attendanceRecords = new ArrayList<>();
        
        for (Map<String, Object> record : allAttendance) {
            String recordStudentName = (String) record.get("student_name");
            if (recordStudentName != null && recordStudentName.toLowerCase().contains(studentName.toLowerCase())) {
                // Initialize student data if not done yet
                if (studentData == null) {
                    studentData = new HashMap<>();
                    studentData.put("student_id", record.get("student_id"));
                    studentData.put("student_name", record.get("student_name"));
                    studentData.put("current_address", record.get("current_address"));
                    studentData.put("medical_status", record.get("medical_status"));
                    studentData.put("grade", record.get("grade"));
                    studentData.put("class", record.get("class"));
                }
                
                // Add attendance record
                Map<String, Object> attendanceRecord = new HashMap<>();
                attendanceRecord.put("attendance_id", record.get("attendance_id"));
                attendanceRecord.put("attendance_date", record.get("attendance_date"));
                attendanceRecord.put("status_id", record.get("status_id"));
                attendanceRecord.put("status_name", record.get("status_name"));
                attendanceRecord.put("arrival_time", record.get("arrival_time"));
                attendanceRecords.add(attendanceRecord);
            }
        }
        
        if (studentData != null) {
            studentData.put("attendance_records", attendanceRecords);
            return studentData;
        }
        
        return null;
    }

    /**
     * Get attendance records by date using get_attendance_with_student_details function
     */
    public List<Map<String, Object>> getAttendanceByDate(String date) {
        String sql = "SELECT * FROM get_attendance_with_student_details() WHERE attendance_date = ?";
        List<Map<String, Object>> attendanceList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendanceMap(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting attendance by date: " + date, e);
        }
        return attendanceList;
    }

    /**
     * Get all attendance records using get_attendance_with_student_details function
     */
    public List<Map<String, Object>> getAllAttendanceWithStudentDetails() {
        String sql = "SELECT * FROM get_attendance_with_student_details() ORDER BY attendance_date";
        List<Map<String, Object>> attendanceList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendanceMap(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all attendance records", e);
        }
        return attendanceList;
    }

    /**
     * Create new attendance record
     */
    public boolean createAttendance(Map<String, Object> attendanceData) {
        String sql = "INSERT INTO attendance (student_id, attendance_date, status_id, arrival_time) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, (Integer) attendanceData.get("student_id"));
            pstmt.setDate(2, java.sql.Date.valueOf((String) attendanceData.get("attendance_date")));
            pstmt.setInt(3, (Integer) attendanceData.get("status_id"));
            
            if (attendanceData.get("arrival_time") != null) {
                pstmt.setTime(4, Time.valueOf((String) attendanceData.get("arrival_time")));
            } else {
                pstmt.setNull(4, Types.TIME);
            }
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            throw new DataAccessException("Error creating attendance record", e);
        }
    }

    /**
     * Update attendance record by student ID and date
     */
    public boolean updateAttendance(Map<String, Object> updateData) {
        StringBuilder sql = new StringBuilder("UPDATE attendance SET ");
        List<Object> parameters = new ArrayList<>();
        
        // Build dynamic update query
        boolean first = true;
        if (updateData.containsKey("status_id")) {
            if (!first) sql.append(", ");
            sql.append("status_id = ?");
            parameters.add(updateData.get("status_id"));
            first = false;
        }
        
        if (updateData.containsKey("arrival_time")) {
            if (!first) sql.append(", ");
            sql.append("arrival_time = ?");
            parameters.add(updateData.get("arrival_time"));
            first = false;
        }
        
        if (first) {
            throw new IllegalArgumentException("No valid fields to update");
        }
        
        sql.append(" WHERE student_id = ? AND attendance_date = ?");
        parameters.add(updateData.get("student_id"));
        parameters.add(updateData.get("attendance_date"));
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                if (param instanceof String && (i == parameters.size() - 1 || i == parameters.size() - 2)) {
                    // Handle date and time parameters
                    if (i == parameters.size() - 1) {
                        pstmt.setDate(i + 1, java.sql.Date.valueOf((String) param));
                    } else if (param.toString().contains(":")) {
                        pstmt.setTime(i + 1, Time.valueOf((String) param));
                    } else {
                        pstmt.setObject(i + 1, param);
                    }
                } else {
                    pstmt.setObject(i + 1, param);
                }
            }
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            throw new DataAccessException("Error updating attendance record", e);
        }
    }

    /**
     * Delete attendance record by student ID and date
     */
    public boolean deleteAttendance(int studentId, String attendanceDate) {
        String sql = "DELETE FROM attendance WHERE student_id = ? AND attendance_date = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setDate(2, java.sql.Date.valueOf(attendanceDate));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting attendance record", e);
        }
    }

    /**
     * Get all students with their attendance records grouped by student
     * Returns array of students with all their attendance days, status, and arrival times
     */
    public List<Map<String, Object>> getAllStudentsAttendanceGrouped() {
        // First, get all attendance records using the existing working function
        List<Map<String, Object>> allAttendance = getAllAttendanceWithStudentDetails();
        
        // Group by student
        Map<Integer, Map<String, Object>> studentsMap = new LinkedHashMap<>();
        
        for (Map<String, Object> record : allAttendance) {
            Integer studentId = (Integer) record.get("student_id");
            
            // Get or create student entry
            Map<String, Object> student = studentsMap.computeIfAbsent(studentId, k -> {
                Map<String, Object> newStudent = new HashMap<>();
                newStudent.put("student_id", record.get("student_id"));
                newStudent.put("student_name", record.get("student_name"));
                newStudent.put("current_address", record.get("current_address"));
                newStudent.put("medical_status", record.get("medical_status"));
                newStudent.put("grade", record.get("grade"));
                newStudent.put("class", record.get("class"));
                newStudent.put("attendance_records", new ArrayList<Map<String, Object>>());
                return newStudent;
            });
            
            // Create attendance record
            Map<String, Object> attendanceRecord = new HashMap<>();
            attendanceRecord.put("attendance_id", record.get("attendance_id"));
            attendanceRecord.put("attendance_date", record.get("attendance_date"));
            attendanceRecord.put("status_id", record.get("status_id"));
            attendanceRecord.put("status_name", record.get("status_name"));
            attendanceRecord.put("arrival_time", record.get("arrival_time"));
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attendanceRecords = (List<Map<String, Object>>) student.get("attendance_records");
            attendanceRecords.add(attendanceRecord);
        }
        
        return new ArrayList<>(studentsMap.values());
    }

    /**
     * Map ResultSet to StudentAttendanceDetails object
     */
    private StudentAttendanceDetails mapResultSetToStudentAttendanceDetails(ResultSet rs) throws SQLException {
        StudentAttendanceDetails details = new StudentAttendanceDetails();
        
        details.setStudentId(rs.getInt("student_id"));
        details.setStudentName(rs.getString("student_name"));
        details.setCurrentAddress(rs.getString("current_address"));
        details.setMedicalStatus(rs.getString("medical_status"));
        details.setGrade(rs.getString("grade"));
        details.setClassName(rs.getString("class"));
        
        // Handle JSON arrays for phones and parents
        try {
            PGobject studentPhonesObj = (PGobject) rs.getObject("student_phones");
            if (studentPhonesObj != null) {
                JSONArray studentPhonesArray = new JSONArray(studentPhonesObj.getValue());
                List<String> phonesList = new ArrayList<>();
                for (int i = 0; i < studentPhonesArray.length(); i++) {
                    phonesList.add(studentPhonesArray.getString(i));
                }
                details.setStudentPhones(phonesList);
            }
            
            PGobject parentsInfoObj = (PGobject) rs.getObject("parents_info");
            if (parentsInfoObj != null) {
                JSONArray parentsArray = new JSONArray(parentsInfoObj.getValue());
                List<StudentAttendanceDetails.ParentInfo> parentsList = new ArrayList<>();
                
                for (int i = 0; i < parentsArray.length(); i++) {
                    JSONObject parentObj = parentsArray.getJSONObject(i);
                    StudentAttendanceDetails.ParentInfo parent = new StudentAttendanceDetails.ParentInfo();
                    
                    parent.setParentId(parentObj.getInt("parent_id"));
                    parent.setParentName(parentObj.getString("parent_name"));
                    parent.setRelationship(parentObj.getString("relationship"));
                    parent.setParentJob(parentObj.getString("parent_job"));
                    parent.setParentNid(parentObj.getString("parent_nid"));
                    parent.setParentAddress(parentObj.getString("parent_address"));
                    parent.setParentNationality(parentObj.getString("parent_nationality"));
                    parent.setParentSocialStatus(parentObj.getString("parent_social_status"));
                    parent.setSocialStatusId(parentObj.getInt("parent_social_status_id"));
                    
                    // Handle parent phones
                    if (parentObj.has("parent_phones")) {
                        JSONArray parentPhonesArray = parentObj.getJSONArray("parent_phones");
                        List<String> parentPhonesList = new ArrayList<>();
                        for (int j = 0; j < parentPhonesArray.length(); j++) {
                            parentPhonesList.add(parentPhonesArray.getString(j));
                        }
                        parent.setParentPhones(parentPhonesList);
                    }
                    
                    parentsList.add(parent);
                }
                details.setParentsInfo(parentsList);
            }
        } catch (Exception e) {
            logger.warning("Error parsing JSON data: " + e.getMessage());
        }
        
        return details;
    }

    /**
     * Map ResultSet to attendance Map for get_attendance_with_student_details results
     */
    private Map<String, Object> mapResultSetToAttendanceMap(ResultSet rs) throws SQLException {
        Map<String, Object> attendanceMap = new HashMap<>();
        
        attendanceMap.put("attendance_id", rs.getInt("attendance_id"));
        attendanceMap.put("attendance_date", rs.getDate("attendance_date").toString());
        attendanceMap.put("status_id", rs.getInt("status_id"));
        attendanceMap.put("status_name", rs.getString("status_name"));
        
        Time arrivalTime = rs.getTime("arrival_time");
        if (arrivalTime != null) {
            attendanceMap.put("arrival_time", arrivalTime.toString());
        }
        
        attendanceMap.put("student_id", rs.getInt("student_id"));
        attendanceMap.put("student_name", rs.getString("student_name"));
        attendanceMap.put("current_address", rs.getString("current_address"));
        attendanceMap.put("medical_status", rs.getString("medical_status"));
        attendanceMap.put("grade", rs.getString("grade"));
        attendanceMap.put("class", rs.getString("class"));
        
        // Handle JSON arrays
        try {
            PGobject studentPhonesObj = (PGobject) rs.getObject("student_phones");
            if (studentPhonesObj != null) {
                JSONArray studentPhonesArray = new JSONArray(studentPhonesObj.getValue());
                List<String> phonesList = new ArrayList<>();
                for (int i = 0; i < studentPhonesArray.length(); i++) {
                    phonesList.add(studentPhonesArray.getString(i));
                }
                attendanceMap.put("student_phones", phonesList);
            }
            
            PGobject parentsInfoObj = (PGobject) rs.getObject("parents_info");
            if (parentsInfoObj != null) {
                JSONArray parentsArray = new JSONArray(parentsInfoObj.getValue());
                List<Map<String, Object>> parentsList = new ArrayList<>();
                
                for (int i = 0; i < parentsArray.length(); i++) {
                    JSONObject parentObj = parentsArray.getJSONObject(i);
                    Map<String, Object> parent = new HashMap<>();
                    
                    parent.put("parent_id", parentObj.getInt("parent_id"));
                    parent.put("parent_name", parentObj.getString("parent_name"));
                    parent.put("relationship", parentObj.getString("relationship"));
                    parent.put("parent_job", parentObj.getString("parent_job"));
                    parent.put("parent_nid", parentObj.getString("parent_nid"));
                    parent.put("parent_address", parentObj.getString("parent_address"));
                    parent.put("parent_nationality", parentObj.getString("parent_nationality"));
                    parent.put("parent_social_status", parentObj.getString("parent_social_status"));
                    parent.put("parent_social_status_id", parentObj.getInt("parent_social_status_id"));
                    
                    // Handle parent phones
                    if (parentObj.has("parent_phones")) {
                        JSONArray parentPhonesArray = parentObj.getJSONArray("parent_phones");
                        List<String> parentPhonesList = new ArrayList<>();
                        for (int j = 0; j < parentPhonesArray.length(); j++) {
                            parentPhonesList.add(parentPhonesArray.getString(j));
                        }
                        parent.put("parent_phones", parentPhonesList);
                    }
                    
                    parentsList.add(parent);
                }
                attendanceMap.put("parents_info", parentsList);
            }
        } catch (Exception e) {
            logger.warning("Error parsing JSON data: " + e.getMessage());
        }
        
        return attendanceMap;
    }
    
    /**
     * Mark students as absent for today if they don't have attendance records
     * This method implements the automated daily attendance update logic
     * @return number of students marked as absent
     */
    public int markAbsentStudentsForToday() {
        String sql = "INSERT INTO attendance (student_id, attendance_date, status_id) " +
                    "SELECT s.student_id, CURRENT_DATE, 2 " +
                    "FROM students s " +
                    "LEFT JOIN attendance a " +
                    "ON s.student_id = a.student_id AND a.attendance_date = CURRENT_DATE " +
                    "WHERE a.student_id IS NULL";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info(String.format("Marked %d students as absent for today", rowsAffected));
            } else {
                logger.info("No students needed to be marked as absent for today");
            }
            
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.severe("Error marking absent students for today: " + e.getMessage());
            throw new DataAccessException("Error marking absent students for today", e);
        }
    }
    
    /**
     * Get count of students without attendance records for a specific date
     * Useful for testing and verification purposes
     * @param date the date to check (format: YYYY-MM-DD)
     * @return number of students without attendance records for the given date
     */
    public int getStudentsWithoutAttendanceCount(String date) {
        String sql = "SELECT COUNT(*) as count " +
                    "FROM students s " +
                    "LEFT JOIN attendance a " +
                    "ON s.student_id = a.student_id AND a.attendance_date = ? " +
                    "WHERE a.student_id IS NULL";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
                return 0;
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting students without attendance count: " + e.getMessage());
            throw new DataAccessException("Error getting students without attendance count", e);
        }
    }
}