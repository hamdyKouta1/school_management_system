package com.canalprep.dao;

import com.canalprep.model.GradeLevel;
import com.canalprep.exception.DataAccessException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GradeLevelDAO {
    private static final Logger logger = Logger.getLogger(GradeLevelDAO.class.getName());
    
    private static final String SELECT_ALL = "SELECT grade_id, grade_name_ar, grade_name_en, grade_order FROM grade_level ORDER BY grade_order";
    private static final String SELECT_BY_ID = "SELECT grade_id, grade_name_ar, grade_name_en, grade_order FROM grade_level WHERE grade_id = ?";
    private static final String INSERT = "INSERT INTO grade_level (grade_name_ar, grade_name_en, grade_order) VALUES (?, ?, ?)";
    private static final String UPDATE = "UPDATE grade_level SET grade_name_ar = ?, grade_name_en = ?, grade_order = ? WHERE grade_id = ?";
    private static final String DELETE = "DELETE FROM grade_level WHERE grade_id = ?";
    
    public List<GradeLevel> getAllGradeLevels() throws DataAccessException {
        List<GradeLevel> gradeLevels = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                GradeLevel gradeLevel = mapResultSetToGradeLevel(rs);
                gradeLevels.add(gradeLevel);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all grade levels", e);
            throw new DataAccessException("Error retrieving all grade levels", e);
        }
        
        return gradeLevels;
    }
    
    public GradeLevel getGradeLevelById(int gradeId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            pstmt.setInt(1, gradeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGradeLevel(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving grade level by ID: " + gradeId, e);
            throw new DataAccessException("Error retrieving grade level by ID: " + gradeId, e);
        }
        
        return null;
    }
    
    public int createGradeLevel(GradeLevel gradeLevel) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, gradeLevel.getGradeNameAr());
            pstmt.setString(2, gradeLevel.getGradeNameEn());
            pstmt.setInt(3, gradeLevel.getGradeOrder());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DataAccessException("Creating grade level failed, no rows affected.", null);
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    gradeLevel.setGradeId(generatedId);
                    return generatedId;
                } else {
                    throw new DataAccessException("Creating grade level failed, no ID obtained.", null);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating grade level", e);
            throw new DataAccessException("Error creating grade level", e);
        }
    }
    
    public boolean updateGradeLevel(GradeLevel gradeLevel) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
            
            pstmt.setString(1, gradeLevel.getGradeNameAr());
            pstmt.setString(2, gradeLevel.getGradeNameEn());
            pstmt.setInt(3, gradeLevel.getGradeOrder());
            pstmt.setInt(4, gradeLevel.getGradeId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating grade level with ID: " + gradeLevel.getGradeId(), e);
            throw new DataAccessException("Error updating grade level with ID: " + gradeLevel.getGradeId(), e);
        }
    }
    
    public boolean deleteGradeLevel(int gradeId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
            
            pstmt.setInt(1, gradeId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting grade level with ID: " + gradeId, e);
            throw new DataAccessException("Error deleting grade level with ID: " + gradeId, e);
        }
    }
    
    private GradeLevel mapResultSetToGradeLevel(ResultSet rs) throws SQLException {
        GradeLevel gradeLevel = new GradeLevel();
        gradeLevel.setGradeId(rs.getInt("grade_id"));
        gradeLevel.setGradeNameAr(rs.getString("grade_name_ar"));
        gradeLevel.setGradeNameEn(rs.getString("grade_name_en"));
        gradeLevel.setGradeOrder(rs.getInt("grade_order"));
        return gradeLevel;
    }
}