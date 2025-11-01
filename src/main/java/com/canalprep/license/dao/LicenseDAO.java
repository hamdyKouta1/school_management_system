package com.canalprep.license.dao;

import com.canalprep.license.model.License;
import com.canalprep.dao.DBConnection;
import com.canalprep.utilities.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LicenseDAO {
    
    /**
     * Create a new license record
     * @param license The license object to create
     * @throws SQLException if database operation fails
     */
    public void createLicense(License license) throws SQLException {
        String sql = "INSERT INTO licenses (license_key, start_date, end_date, remaining_days, status, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, license.getLicenseKey());
            stmt.setDate(2, Date.valueOf(license.getStartDate()));
            stmt.setDate(3, Date.valueOf(license.getEndDate()));
            stmt.setInt(4, license.getRemainingDays());
            stmt.setString(5, license.getStatus());
            stmt.setString(6, license.getCreatedBy());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating license failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    license.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating license failed, no ID obtained.");
                }
            }
            
            LoggerUtil.logInfo("LicenseDAO", "License created successfully with ID: " + license.getId());
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseDAO", "Failed to create license: " + e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Get the current active license
     * @return License object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public License getCurrentLicense() throws SQLException {
        String sql = "SELECT id, license_key, start_date, end_date, remaining_days, status, created_at, updated_at, created_by " +
                    "FROM licenses WHERE status = 'ACTIVE' ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return mapResultSetToLicense(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseDAO", "Failed to get current license", e);
            throw e;
        }
    }
    
    /**
     * Update license remaining days and status
     * @param licenseId The license ID
     * @param remainingDays The remaining days
     * @param status The license status
     * @throws SQLException if database operation fails
     */
    public void updateLicense(int licenseId, int remainingDays, String status) throws SQLException {
        String sql = "UPDATE licenses SET remaining_days = ?, status = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, remainingDays);
            stmt.setString(2, status);
            stmt.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.setInt(4, licenseId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating license failed, no rows affected.");
            }
            
            LoggerUtil.logInfo("LicenseDAO", "License updated successfully with ID: " + licenseId);
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseDAO", "Failed to update license", e);
            throw e;
        }
    }
    
    /**
     * Deactivate all existing licenses
     * @throws SQLException if database operation fails
     */
    public void deactivateAllLicenses() throws SQLException {
        String sql = "UPDATE licenses SET status = 'INACTIVE', updated_at = ? WHERE status = 'ACTIVE'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
            
            int affectedRows = stmt.executeUpdate();
            
            LoggerUtil.logInfo("LicenseDAO", "Deactivated " + affectedRows + " licenses");
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseDAO", "Failed to deactivate licenses", e);
            throw e;
        }
    }
    
    /**
     * Delete a license by ID
     * @param licenseId The license ID
     * @throws SQLException if database operation fails
     */
    public void deleteLicense(int licenseId) throws SQLException {
        String sql = "DELETE FROM licenses WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, licenseId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Deleting license failed, no rows affected.");
            }
            
            LoggerUtil.logInfo("LicenseDAO", "License deleted successfully with ID: " + licenseId);
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseDAO", "Failed to delete license", e);
            throw e;
        }
    }
    
    /**
     * Get all licenses
     * @return List of all licenses
     * @throws SQLException if database operation fails
     */
    public List<License> getAllLicenses() throws SQLException {
        String sql = "SELECT id, license_key, start_date, end_date, remaining_days, status, created_at, updated_at, created_by " +
                    "FROM licenses ORDER BY created_at DESC";
        
        List<License> licenses = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                licenses.add(mapResultSetToLicense(rs));
            }
            
            return licenses;
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseDAO", "Failed to get all licenses", e);
            throw e;
        }
    }
    
    /**
     * Get license by ID
     * @param licenseId The license ID
     * @return License object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public License getLicenseById(int licenseId) throws SQLException {
        String sql = "SELECT id, license_key, start_date, end_date, remaining_days, status, created_at, updated_at, created_by " +
                    "FROM licenses WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, licenseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLicense(rs);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            LoggerUtil.logError("LicenseDAO", "Failed to get license by ID", e);
            throw e;
        }
    }
    
    /**
     * Map ResultSet to License object
     * @param rs The ResultSet
     * @return License object
     * @throws SQLException if mapping fails
     */
    private License mapResultSetToLicense(ResultSet rs) throws SQLException {
        License license = new License();
        license.setId(rs.getInt("id"));
        license.setLicenseKey(rs.getString("license_key"));
        license.setStartDate(rs.getDate("start_date").toLocalDate());
        license.setEndDate(rs.getDate("end_date").toLocalDate());
        license.setRemainingDays(rs.getInt("remaining_days"));
        license.setStatus(rs.getString("status"));
        license.setCreatedAt(rs.getTimestamp("created_at"));
        license.setUpdatedAt(rs.getTimestamp("updated_at"));
        license.setCreatedBy(rs.getString("created_by"));
        return license;
    }
}