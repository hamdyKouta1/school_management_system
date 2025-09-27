package com.canalprep.dao;

import com.canalprep.staticVariables.DBConst;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.canalprep.exception.DataAccessException;

public class MedicalHistoryDAO {
    private static final Logger logger = Logger.getLogger(MedicalHistoryDAO.class.getName());

    private static final String INSERT_STUDENT_MEDICAL = DBConst.DB_INSERT_STUDENT_MEDICAL;
    private static final String DELETE_STUDENT_MEDICAL = DBConst.DB_DELETE_STUDENT_MEDICAL;
    private static final String UPDATE_STUDENT_MEDICAL_STATUS = DBConst.DB_UPDATE_MEDICAL_STATUS;
    private static final String CHECK_REMAINING_MEDICAL_HISTORY = "SELECT 1 FROM medical_history WHERE student_id = ?";

    public boolean addMedicalHistory(int studentId, String description) throws DataAccessException {
        boolean result = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_STUDENT_MEDICAL)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, description);
            result = pstmt.executeUpdate() > 0;

            if (result) {
                updateMedicalStatus(studentId, true);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding medical history for studentId: " + studentId, e);
            throw new DataAccessException("Error adding medical history", e);
        }
        return result;
    }

    public boolean deleteMedicalHistory(int studentId, String description) throws DataAccessException {
        boolean result = false;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtDelete = conn.prepareStatement(DELETE_STUDENT_MEDICAL)) {
                pstmtDelete.setString(1, description);
                pstmtDelete.setInt(2, studentId);
                result = pstmtDelete.executeUpdate() > 0;
            }

            if (result) {
                boolean hasRemaining = false;
                try (PreparedStatement pstmtCheck = conn.prepareStatement(CHECK_REMAINING_MEDICAL_HISTORY)) {
                    pstmtCheck.setInt(1, studentId);
                    try (var rs = pstmtCheck.executeQuery()) {
                        hasRemaining = rs.next();
                    }
                }
                if (!hasRemaining) {
                    updateMedicalStatus(studentId, false);
                }
            }
            conn.commit();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting medical history for studentId: " + studentId, e);
            try (Connection conn = DBConnection.getConnection()) {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Error during rollback", rollbackEx);
            }
            throw new DataAccessException("Error deleting medical history", e);
        }
        return result;
    }

    private void updateMedicalStatus(int studentId, boolean status) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_STUDENT_MEDICAL_STATUS)) {
            pstmt.setBoolean(1, status);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating medical status for studentId: " + studentId, e);
            throw new DataAccessException("Error updating medical status", e);
        }
    }
}


