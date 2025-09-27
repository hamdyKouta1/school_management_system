package com.canalprep.dao;


import com.canalprep.staticVariables.DBConst;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.canalprep.exception.DataAccessException;

public class StudentNotesDAO {
    private static final Logger logger = Logger.getLogger(StudentNotesDAO.class.getName());

    private static final String INSERT_STUDENT_NOTE = DBConst.DB_INSERT_STUDENT_NOTE;
    private static final String DELETE_STUDENT_NOTE = DBConst.DB_DELETE_STUDENT_NOTE;

    public boolean addStudentNote(int studentId, String noteText, String createdBy) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_STUDENT_NOTE)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, noteText);
            pstmt.setString(3, createdBy);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding student note for studentId: " + studentId, e);
            throw new DataAccessException("Error adding student note", e);
        }
    }

    public boolean deleteStudentNote(int noteId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_STUDENT_NOTE)) {
            pstmt.setInt(1, noteId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting student note with ID: " + noteId, e);
            throw new DataAccessException("Error deleting student note", e);
        }
    }
}


