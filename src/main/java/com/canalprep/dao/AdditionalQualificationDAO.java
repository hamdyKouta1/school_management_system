package com.canalprep.dao;

import com.canalprep.staticVariables.DBConst;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.canalprep.exception.DataAccessException;

public class AdditionalQualificationDAO {
    private static final Logger logger = Logger.getLogger(AdditionalQualificationDAO.class.getName());

    private static final String INSERT_STUDENT_QUALIFICATIONS = DBConst.DB_INSERT_STUDENT_QUALIFICATION;
    private static final String DELETE_STUDENT_QUALIFICATIONS = DBConst.DB_DELETE_STUDENT_QUALIFICATION;

    public boolean addAdditionalQualification(int studentId, String description) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_STUDENT_QUALIFICATIONS)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, description);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding additional qualification for studentId: " + studentId, e);
            throw new DataAccessException("Error adding additional qualification", e);
        }
    }

    public boolean deleteAdditionalQualification(int qualificationId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_STUDENT_QUALIFICATIONS)) {
            pstmt.setInt(1, qualificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting additional qualification with ID: " + qualificationId, e);
            throw new DataAccessException("Error deleting additional qualification", e);
        }
    }
}


