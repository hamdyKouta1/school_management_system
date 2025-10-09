package com.canalprep.dao;

import com.canalprep.model.SchoolClass;
import com.canalprep.exception.DataAccessException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchoolClassDAO {
    private static final Logger logger = Logger.getLogger(SchoolClassDAO.class.getName());
    
    private static final String SELECT_ALL = "SELECT class_id, class_name, grade_id FROM class ORDER BY class_id";
    private static final String SELECT_BY_ID = "SELECT class_id, class_name, grade_id FROM class WHERE class_id = ?";
    private static final String INSERT = "INSERT INTO class (class_name, grade_id) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE class SET class_name = ?, grade_id = ? WHERE class_id = ?";
    private static final String DELETE = "DELETE FROM class WHERE class_id = ?";
    private static final String SELECT_BY_GRADE = "SELECT class_id, class_name, grade_id FROM class WHERE grade_id = ? ORDER BY class_id";
    
    public List<SchoolClass> getAllClasses() throws DataAccessException {
        List<SchoolClass> classes = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                SchoolClass schoolClass = mapResultSetToSchoolClass(rs);
                classes.add(schoolClass);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all classes", e);
            throw new DataAccessException("Error retrieving all classes", e);
        }
        
        return classes;
    }
    
    public SchoolClass getClassById(int classId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            pstmt.setInt(1, classId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSchoolClass(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving class by ID: " + classId, e);
            throw new DataAccessException("Error retrieving class by ID: " + classId, e);
        }
        
        return null;
    }
    
    public List<SchoolClass> getClassesByGrade(int gradeId) throws DataAccessException {
        List<SchoolClass> classes = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_GRADE)) {
            
            pstmt.setInt(1, gradeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SchoolClass schoolClass = mapResultSetToSchoolClass(rs);
                    classes.add(schoolClass);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving classes by grade ID: " + gradeId, e);
            throw new DataAccessException("Error retrieving classes by grade ID: " + gradeId, e);
        }
        
        return classes;
    }
    
    public int createClass(SchoolClass schoolClass) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, schoolClass.getClassName());
            pstmt.setInt(2, schoolClass.getGradeId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DataAccessException("Creating class failed, no rows affected.", null);
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    schoolClass.setClassId(generatedId);
                    return generatedId;
                } else {
                    throw new DataAccessException("Creating class failed, no ID obtained.", null);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating class", e);
            throw new DataAccessException("Error creating class", e);
        }
    }
    
    public boolean updateClass(SchoolClass schoolClass) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
            
            pstmt.setString(1, schoolClass.getClassName());
            pstmt.setInt(2, schoolClass.getGradeId());
            pstmt.setInt(3, schoolClass.getClassId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating class with ID: " + schoolClass.getClassId(), e);
            throw new DataAccessException("Error updating class with ID: " + schoolClass.getClassId(), e);
        }
    }
    
    public boolean deleteClass(int classId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
            
            pstmt.setInt(1, classId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting class with ID: " + classId, e);
            throw new DataAccessException("Error deleting class with ID: " + classId, e);
        }
    }
    
    private SchoolClass mapResultSetToSchoolClass(ResultSet rs) throws SQLException {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setClassId(rs.getInt("class_id"));
        schoolClass.setClassName(rs.getString("class_name"));
        schoolClass.setGradeId(rs.getInt("grade_id"));
        return schoolClass;
    }
}