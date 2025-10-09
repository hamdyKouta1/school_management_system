package com.canalprep.dao;

import com.canalprep.model.RelationshipType;
import com.canalprep.exception.DataAccessException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RelationshipTypeDAO {
    private static final Logger logger = Logger.getLogger(RelationshipTypeDAO.class.getName());
    
    private static final String SELECT_ALL = "SELECT type_id, type_name FROM relationship_type ORDER BY type_name";
    private static final String SELECT_BY_ID = "SELECT type_id, type_name FROM relationship_type WHERE type_id = ?";
    private static final String INSERT = "INSERT INTO relationship_type (type_name) VALUES (?)";
    private static final String UPDATE = "UPDATE relationship_type SET type_name = ? WHERE type_id = ?";
    private static final String DELETE = "DELETE FROM relationship_type WHERE type_id = ?";
    
    public List<RelationshipType> getAllRelationshipTypes() throws DataAccessException {
        List<RelationshipType> relationshipTypes = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                RelationshipType relationshipType = mapResultSetToRelationshipType(rs);
                relationshipTypes.add(relationshipType);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all relationship types", e);
            throw new DataAccessException("Error retrieving all relationship types", e);
        }
        
        return relationshipTypes;
    }
    
    public RelationshipType getRelationshipTypeById(int typeId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            pstmt.setInt(1, typeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRelationshipType(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving relationship type by ID: " + typeId, e);
            throw new DataAccessException("Error retrieving relationship type by ID: " + typeId, e);
        }
        
        return null;
    }
    
    public int createRelationshipType(RelationshipType relationshipType) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, relationshipType.getTypeName());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DataAccessException("Creating relationship type failed, no rows affected.", null);
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    relationshipType.setTypeId(generatedId);
                    return generatedId;
                } else {
                    throw new DataAccessException("Creating relationship type failed, no ID obtained.", null);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating relationship type", e);
            throw new DataAccessException("Error creating relationship type", e);
        }
    }
    
    public boolean updateRelationshipType(RelationshipType relationshipType) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
            
            pstmt.setString(1, relationshipType.getTypeName());
            pstmt.setInt(2, relationshipType.getTypeId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating relationship type with ID: " + relationshipType.getTypeId(), e);
            throw new DataAccessException("Error updating relationship type with ID: " + relationshipType.getTypeId(), e);
        }
    }
    
    public boolean deleteRelationshipType(int typeId) throws DataAccessException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
            
            pstmt.setInt(1, typeId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting relationship type with ID: " + typeId, e);
            throw new DataAccessException("Error deleting relationship type with ID: " + typeId, e);
        }
    }
    
    private RelationshipType mapResultSetToRelationshipType(ResultSet rs) throws SQLException {
        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setTypeId(rs.getInt("type_id"));
        relationshipType.setTypeName(rs.getString("type_name"));
        return relationshipType;
    }
}