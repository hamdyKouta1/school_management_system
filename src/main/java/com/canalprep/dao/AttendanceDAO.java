package com.canalprep.dao;

import com.canalprep.model.Attendance;
import com.canalprep.staticVariables.DBConst;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttendanceDAO {
    private static final Logger logger = Logger.getLogger(AttendanceDAO.class.getName());
    
    // SQL Queries
    private static final String SELECT_ALL = DBConst.DB_ATTENDANCE_SELECT_ALL;//"SELECT * FROM attendance";
    private static final String SELECT_BY_ID = DBConst.DB_ATTENDANCE_SELECT_BY_ID;//"SELECT * FROM attendance WHERE student_id = ?";
    private static final String INSERT_ATTENDANCE = DBConst.DB_ATTENDANCE_INSERT_ATTENDANCE;//"INSERT INTO attendance (student_id, attendance_date, status_id,class_id,grade_id,today_date) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = DBConst.DB_ATTENDANCE_UPDATE;//"UPDATE attendance SET student_id = ?, attendance_date = ?, status_id = ? WHERE attendance_id = ?";
    private static final String DELETE = DBConst.DB_ATTENDANCE_DELETE;//"DELETE FROM attendance WHERE attendance_id = ?";
    private static final String SELECT_BY_STUDENT_AND_DATE = DBConst.DB_ATTENDANCE_SELECT_BY_STUDENT_AND_DATE;//"SELECT * FROM attendance WHERE student_id = ? AND attendance_date = ?";
    private static final String SELECT_BY_DATE_STRING = DBConst.DB_ATTENDANCE_SELECT_BY_DATE_STRING;//"SELECT * FROM attendance WHERE today_date = ?";

    public List<Attendance> getTodayAttendances(Date dayDate){
        List<Attendance> attendanceList = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_DATE_STRING)) {
            pstmt.setDate(1, dayDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(extractAttendanceFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error getting today's attendance records", e);
        }
        return attendanceList;
    }

    public List<Attendance> getAllAttendance() {
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            handleSQLException("Error getting all attendance records", e);
        }
        return attendanceList;
    }
    
    public List<Attendance> getAttendanceById(int attendanceId) {
    List<Attendance> attendanceList = new ArrayList<>();
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
        
        pstmt.setInt(1, attendanceId);
        
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Attendance attendance = extractAttendanceFromResultSet(rs);
                attendanceList.add(attendance);
            }
        }
    } catch (SQLException e) {
        handleSQLException("Error getting attendance by ID: " + attendanceId, e);
    }
    
    return attendanceList;
}

    
    public Attendance getAttendanceByStudentAndDate(int studentId, Date date) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_STUDENT_AND_DATE)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setDate(2, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractAttendanceFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error getting attendance for student: " + studentId + " on date: " + date, e);
        }
        return null;
    }
    
    public boolean addAttendance(Attendance attendance) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_ATTENDANCE, Statement.RETURN_GENERATED_KEYS)) {
            

            setAttendanceParameters(pstmt, attendance);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        attendance.setAttendanceId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error adding attendance", e);
        }
        return false;
    }
    
    public boolean updateAttendance(Attendance attendance) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
            
            updateAttendanceParameters(pstmt, attendance);
            pstmt.setInt(4, attendance.getAttendanceId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error updating attendance: " + attendance.getAttendanceId(), e);
        }
        return false;
    }
    
    public boolean deleteAttendance(int attendanceId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
            
            pstmt.setInt(1, attendanceId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error deleting attendance: " + attendanceId, e);
        }
        return false;
    }
    
    private Attendance extractAttendanceFromResultSet(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setAttendanceDate(rs.getDate("attendance_date"));
        attendance.setStatusId(rs.getInt("status_id"));
        attendance.setStdClass(rs.getString("class_id"));
        attendance.setGradeName(rs.getString("grade_id"));
        attendance.setTodayDate(rs.getDate("today_date"));
        
        return attendance;
    }
    
    private void setAttendanceParameters(PreparedStatement pstmt, Attendance attendance) throws SQLException {
        pstmt.setInt(1, attendance.getStudentId());
        pstmt.setDate(2, attendance.getAttendanceDate());
        pstmt.setInt(3, attendance.getStatusId());
        pstmt.setString(4, attendance.getStdClass());
        pstmt.setInt(5, attendance.getGrade());
        pstmt.setDate(6, java.sql.Date.valueOf(LocalDate.now()));    }
   
        

    private void updateAttendanceParameters(PreparedStatement pstmt, Attendance attendance) throws SQLException {
        pstmt.setInt(1, attendance.getStudentId());
        pstmt.setDate(2, attendance.getAttendanceDate());
        pstmt.setInt(3, attendance.getStatusId());
      
    }

    private void handleSQLException(String message, SQLException e) {
        logger.log(Level.SEVERE, message, e);
        // You could throw a custom application exception here
        // throw new DataAccessException(message, e);
    }
}