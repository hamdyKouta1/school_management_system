package com.canalprep.dao;

import com.canalprep.model.Nationality;
import com.canalprep.exception.DataAccessException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NationalityDAO {
    private static final Logger logger = Logger.getLogger(NationalityDAO.class.getName());
    
    private static final String SELECT_ALL = "SELECT nationality_id, national_name FROM nationality ORDER BY national_name";
    private static final String SELECT_BY_ID = "SELECT nationality_id, national_name FROM nationality WHERE nationality_id = ?";
    private static final String INSERT = "INSERT INTO nationality (national_name) VALUES (?)";
    private static final String UPDATE = "UPDATE nationality SET national_name = ? WHERE nationality_id = ?";
    private static final String DELETE = "DELETE FROM nationality WHERE nationality_id = ?";
    
    public List<Nationality> getAllNationalities() throws DataAccessException {
        List<Nationality> nationalities = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Nationality nationality = mapResultSetToNationality(rs);
                nationalities.add(nationality);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all nationalities", e);
            throw new DataAccessException("Error retrieving all nationalities", e);
        }
        
        return nationalities;
    }
    
    public Nationality getNationalityById(int nationalityId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            pstmt.setInt(1, nationalityId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNationality(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving nationality by ID: " + nationalityId, e);
            throw new DataAccessException("Error retrieving nationality by ID: " + nationalityId, e);
        }
        
        return null;
    }
    
    public int createNationality(Nationality nationality) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, nationality.getNationalName());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DataAccessException("Creating nationality failed, no rows affected.", null);
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    nationality.setNationalityId(generatedId);
                    return generatedId;
                } else {
                    throw new DataAccessException("Creating nationality failed, no ID obtained.", null);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating nationality", e);
            throw new DataAccessException("Error creating nationality", e);
        }
    }
    
    public boolean updateNationality(Nationality nationality) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
            
            pstmt.setString(1, nationality.getNationalName());
            pstmt.setInt(2, nationality.getNationalityId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating nationality with ID: " + nationality.getNationalityId(), e);
            throw new DataAccessException("Error updating nationality with ID: " + nationality.getNationalityId(), e);
        }
    }
    
    public boolean deleteNationality(int nationalityId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
            
            pstmt.setInt(1, nationalityId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting nationality with ID: " + nationalityId, e);
            throw new DataAccessException("Error deleting nationality with ID: " + nationalityId, e);
        }
    }
    
    private Nationality mapResultSetToNationality(ResultSet rs) throws SQLException {
        Nationality nationality = new Nationality();
        nationality.setNationalityId(rs.getInt("nationality_id"));
        nationality.setNationalName(rs.getString("national_name"));
        return nationality;
    }
}