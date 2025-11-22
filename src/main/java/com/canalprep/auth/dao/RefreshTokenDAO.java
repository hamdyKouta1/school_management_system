package com.canalprep.auth.dao;

import com.canalprep.dao.DBConnection;
import com.canalprep.exception.DataAccessException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO for managing refresh tokens persistence and revocation.
 * Schema is created lazily if it does not exist.
 */
public class RefreshTokenDAO {
    private static final Logger logger = Logger.getLogger(RefreshTokenDAO.class.getName());

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS refresh_tokens (" +
            " id UUID PRIMARY KEY," +
            " user_id INTEGER NOT NULL," +
            " token_jti TEXT UNIQUE NOT NULL," +
            " expires_at TIMESTAMP NOT NULL," +
            " revoked_at TIMESTAMP NULL," +
            " created_at TIMESTAMP NOT NULL DEFAULT NOW()," +
            " ip_address TEXT NULL," +
            " user_agent TEXT NULL," +
            " device_id TEXT NULL" +
            ")";

    private static final String INSERT_SQL =
            "INSERT INTO refresh_tokens (id, user_id, token_jti, expires_at, revoked_at, created_at, ip_address, user_agent, device_id) " +
            "VALUES (?::uuid, ?, ?, ?, NULL, NOW(), ?, ?, ?)";

    private static final String SELECT_BY_JTI_SQL =
            "SELECT user_id, token_jti, expires_at, revoked_at, device_id FROM refresh_tokens WHERE token_jti = ?";

    private static final String REVOKE_BY_JTI_SQL =
            "UPDATE refresh_tokens SET revoked_at = NOW() WHERE token_jti = ? AND revoked_at IS NULL";

    public RefreshTokenDAO() {
        ensureTable();
    }

    private void ensureTable() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to ensure refresh_tokens table", e);
            // Do not throw to avoid startup failure; operations will fail later instead
        }
    }

    /**
     * Save a new refresh token record.
     */
    public void saveRefreshToken(String tokenId, int userId, String jti, long expiresAtMillis,
                                 String ipAddress, String userAgent, String deviceId) {
        Timestamp expiresAt = new Timestamp(expiresAtMillis);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, tokenId);
            ps.setInt(2, userId);
            ps.setString(3, jti);
            ps.setTimestamp(4, expiresAt);
            ps.setString(5, ipAddress);
            ps.setString(6, userAgent);
            ps.setString(7, deviceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save refresh token", e);
        }
    }

    /**
     * Check token status by jti and return whether it is valid (exists, not revoked, not expired).
     */
    public TokenStatus getTokenStatus(String jti) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_JTI_SQL)) {
            ps.setString(1, jti);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return TokenStatus.NOT_FOUND;
                }
                Timestamp expiresAt = rs.getTimestamp("expires_at");
                Timestamp revokedAt = rs.getTimestamp("revoked_at");
                if (revokedAt != null) {
                    return TokenStatus.REVOKED;
                }
                if (expiresAt != null && expiresAt.getTime() < System.currentTimeMillis()) {
                    return TokenStatus.EXPIRED;
                }
                return TokenStatus.VALID;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check refresh token status", e);
        }
    }

    /**
     * Revoke a refresh token by its jti.
     */
    public boolean revokeByJti(String jti) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(REVOKE_BY_JTI_SQL)) {
            ps.setString(1, jti);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to revoke refresh token", e);
        }
    }

    public enum TokenStatus {
        VALID,
        EXPIRED,
        REVOKED,
        NOT_FOUND
    }
}