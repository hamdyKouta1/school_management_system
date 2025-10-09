package com.canalprep.license.dao;

import com.canalprep.license.model.OTP;
import com.canalprep.dao.DBConnection;
import com.canalprep.utilities.LoggerUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OTPDAO {
    
    /**
     * Create a new OTP record
     * @param otp The OTP object to create
     * @throws SQLException if database operation fails
     */
    public void createOTP(OTP otp) throws SQLException {
        String sql = "INSERT INTO otps (otp_code, email, operation, created_at, expires_at, used, ip_address) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, otp.getOtpCode());
            stmt.setString(2, otp.getEmail());
            stmt.setString(3, otp.getOperation());
            stmt.setTimestamp(4, Timestamp.valueOf(otp.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(otp.getExpiresAt()));
            stmt.setBoolean(6, otp.isUsed());
            stmt.setString(7, otp.getIpAddress());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating OTP failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    otp.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating OTP failed, no ID obtained.");
                }
            }
            
            LoggerUtil.logInfo("OTPDAO", "OTP created successfully with ID: " + otp.getId());
            
        } catch (SQLException e) {
            LoggerUtil.logError("OTPDAO", "Failed to create OTP", e);
            throw e;
        }
    }
    
    /**
     * Get a valid OTP by code and operation
     * @param otpCode The OTP code
     * @param operation The operation type
     * @return OTP object if found and valid, null otherwise
     * @throws SQLException if database operation fails
     */
    public OTP getValidOTP(String otpCode, String operation) throws SQLException {
        String sql = "SELECT id, otp_code, email, operation, created_at, expires_at, used, ip_address " +
                    "FROM otps WHERE otp_code = ? AND operation = ? AND used = false AND expires_at > ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, otpCode);
            stmt.setString(2, operation);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOTP(rs);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            LoggerUtil.logError("OTPDAO", "Failed to get valid OTP", e);
            throw e;
        }
    }
    
    /**
     * Mark an OTP as used
     * @param otpId The OTP ID
     * @throws SQLException if database operation fails
     */
    public void markOTPAsUsed(int otpId) throws SQLException {
        String sql = "UPDATE otps SET used = true WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, otpId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Marking OTP as used failed, no rows affected.");
            }
            
            LoggerUtil.logInfo("OTPDAO", "OTP marked as used with ID: " + otpId);
            
        } catch (SQLException e) {
            LoggerUtil.logError("OTPDAO", "Failed to mark OTP as used", e);
            throw e;
        }
    }
    
    /**
     * Delete expired OTPs
     * @return Number of deleted records
     * @throws SQLException if database operation fails
     */
    public int deleteExpiredOTPs() throws SQLException {
        String sql = "DELETE FROM otps WHERE expires_at < ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            
            int deletedCount = stmt.executeUpdate();
            
            LoggerUtil.logInfo("OTPDAO", "Deleted " + deletedCount + " expired OTPs");
            
            return deletedCount;
            
        } catch (SQLException e) {
            LoggerUtil.logError("OTPDAO", "Failed to delete expired OTPs", e);
            throw e;
        }
    }
    
    /**
     * Get all OTPs for a specific operation
     * @param operation The operation type
     * @return List of OTPs
     * @throws SQLException if database operation fails
     */
    public List<OTP> getOTPsByOperation(String operation) throws SQLException {
        String sql = "SELECT id, otp_code, email, operation, created_at, expires_at, used, ip_address " +
                    "FROM otps WHERE operation = ? ORDER BY created_at DESC";
        
        List<OTP> otps = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, operation);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    otps.add(mapResultSetToOTP(rs));
                }
            }
            
            return otps;
            
        } catch (SQLException e) {
            LoggerUtil.logError("OTPDAO", "Failed to get OTPs by operation", e);
            throw e;
        }
    }
    
    /**
     * Map ResultSet to OTP object
     * @param rs The ResultSet
     * @return OTP object
     * @throws SQLException if mapping fails
     */
    private OTP mapResultSetToOTP(ResultSet rs) throws SQLException {
        OTP otp = new OTP();
        otp.setId(rs.getInt("id"));
        otp.setOtpCode(rs.getString("otp_code"));
        otp.setEmail(rs.getString("email"));
        otp.setOperation(rs.getString("operation"));
        otp.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        otp.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        otp.setUsed(rs.getBoolean("used"));
        otp.setIpAddress(rs.getString("ip_address"));
        return otp;
    }
}