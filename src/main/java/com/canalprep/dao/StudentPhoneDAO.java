package com.canalprep.dao;

import com.canalprep.staticVariables.DBConst;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.canalprep.exception.DataAccessException;

public class StudentPhoneDAO {
    private static final Logger logger = Logger.getLogger(StudentPhoneDAO.class.getName());

    private static final String INSERT_STUDENT_PHONE = DBConst.DB_INSERT_STUDENT_PHONE;
    private static final String DELETE_STUDENT_PHONE = DBConst.DB_DELETE_STUDENT_PHONE;

    public boolean addStudentPhone(int studentId, String phoneNumber) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_STUDENT_PHONE)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, phoneNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding student phone for studentId: " + studentId, e);
            throw new DataAccessException("Error adding student phone", e);
        }
    }

    public boolean deleteStudentPhone(int studentId, String phoneNumber) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_STUDENT_PHONE)) {
            pstmt.setString(1, phoneNumber);
            pstmt.setInt(2, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting student phone for studentId: " + studentId, e);
            throw new DataAccessException("Error deleting student phone", e);
        }
    }
}


