package com.canalprep.dao;

import com.canalprep.model.Religion;
import com.canalprep.exception.DataAccessException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReligionDAO {
    private static final Logger logger = Logger.getLogger(ReligionDAO.class.getName());
    
    private static final String SELECT_ALL = "SELECT religion_id, religion_name FROM religion ORDER BY religion_name";
    private static final String SELECT_BY_ID = "SELECT religion_id, religion_name FROM religion WHERE religion_id = ?";
    private static final String INSERT = "INSERT INTO religion (religion_name) VALUES (?)";
    private static final String UPDATE = "UPDATE religion SET religion_name = ? WHERE religion_id = ?";
    private static final String DELETE = "DELETE FROM religion WHERE religion_id = ?";
    
    public List<Religion> getAllReligions() throws DataAccessException {
        List<Religion> religions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Religion religion = mapResultSetToReligion(rs);
                religions.add(religion);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all religions", e);
            throw new DataAccessException("Error retrieving all religions", e);
        }
        
        return religions;
    }
    
    public Religion getReligionById(int religionId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            pstmt.setInt(1, religionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReligion(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving religion by ID: " + religionId, e);
            throw new DataAccessException("Error retrieving religion by ID: " + religionId, e);
        }
        
        return null;
    }
    
    public int createReligion(Religion religion) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, religion.getReligionName());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DataAccessException("Creating religion failed, no rows affected.", null);
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    religion.setReligionId(generatedId);
                    return generatedId;
                } else {
                    throw new DataAccessException("Creating religion failed, no ID obtained.", null);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating religion", e);
            throw new DataAccessException("Error creating religion", e);
        }
    }
    
    public boolean updateReligion(Religion religion) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
            
            pstmt.setString(1, religion.getReligionName());
            pstmt.setInt(2, religion.getReligionId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating religion with ID: " + religion.getReligionId(), e);
            throw new DataAccessException("Error updating religion with ID: " + religion.getReligionId(), e);
        }
    }
    
    public boolean deleteReligion(int religionId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
            
            pstmt.setInt(1, religionId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting religion with ID: " + religionId, e);
            throw new DataAccessException("Error deleting religion with ID: " + religionId, e);
        }
    }
    
    private Religion mapResultSetToReligion(ResultSet rs) throws SQLException {
        Religion religion = new Religion();
        religion.setReligionId(rs.getInt("religion_id"));
        religion.setReligionName(rs.getString("religion_name"));
        return religion;
    }
}