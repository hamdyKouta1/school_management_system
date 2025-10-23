package com.canalprep.auth.dao;

import com.canalprep.auth.model.User;
import com.canalprep.auth.utilities.PasswordUtils;
import com.canalprep.dao.DBConnection;
import com.canalprep.staticVariables.DBConst;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.canalprep.exception.DataAccessException;

public class UserDAO {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());
    private static final String INSERT_USER_SQL = DBConst.DB_INSERT_USER_SQL;
    private static final String SELECT_USER_BY_USERNAME = DBConst.DB_SELECT_USER_BY_USERNAME;
    private static final String UPDATE_LAST_LOGIN = DBConst.DB_UPDATE_LAST_LOGIN;
    
    // Additional SQL queries for user management
    private static final String SELECT_ALL_USERS = "SELECT id, username, email, role, created_at, last_login FROM users ORDER BY id";
    private static final String SELECT_USER_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String UPDATE_USER = "UPDATE users SET username = ?, email = ?, role = ? WHERE id = ?";
    private static final String DELETE_USER_BY_ID = "DELETE FROM users WHERE id = ?";
    private static final String DELETE_USER_BY_USERNAME = "DELETE FROM users WHERE username = ?";
    private static final String UPDATE_PASSWORD = "UPDATE users SET password_hash = ?, salt = ? WHERE id = ?";
    private static final String CHECK_USER_EXISTS_BY_ID = "SELECT COUNT(*) FROM users WHERE id = ?";
    private static final String CHECK_USER_EXISTS_BY_USERNAME = "SELECT COUNT(*) FROM users WHERE username = ?";
    
    public User createUser(String username, String email, String password, String role) throws SQLException {
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(password, salt);
        
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, salt);
            statement.setString(5, role);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                logger.log(Level.SEVERE, "Creating user failed, no rows affected.");
                throw new DataAccessException("Creating user failed, no rows affected.", null);
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    User user = new User();
                    user.setId(generatedKeys.getInt(1));
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPasswordHash(passwordHash);
                    user.setSalt(salt);
                    user.setRole(role);

                    return user;
                } else {
                    logger.log(Level.SEVERE, "Creating user failed, no ID obtained.");
                    throw new DataAccessException("Creating user failed, no ID obtained.", null);
                }
            }
        }
    }
    
    public User getUserByUsername(String username) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_USERNAME)) {
            
            statement.setString(1, username);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPasswordHash(resultSet.getString("password_hash"));
                    user.setSalt(resultSet.getString("salt"));
                    user.setCreatedAt(resultSet.getTimestamp("created_at"));
                    user.setLastLogin(resultSet.getTimestamp("last_login"));
                    user.setRole(resultSet.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by username: " + username, e);
            throw new DataAccessException("Error getting user by username: " + username, e);
        }
        return null;
    }
    
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPasswordHash(resultSet.getString("password_hash"));
                    user.setSalt(resultSet.getString("salt"));
                    user.setCreatedAt(resultSet.getTimestamp("created_at"));
                    user.setLastLogin(resultSet.getTimestamp("last_login"));
                    user.setRole(resultSet.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by email: " + email, e);
            throw new DataAccessException("Error getting user by email: " + email, e);
        }
        return null;
    }
    
    public void updateLastLogin(int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_LAST_LOGIN)) {
            
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }
    
    /**
     * Get all users with limited fields (id, username, email, role, last_login)
     * @return List of users
     * @throws SQLException if database error occurs
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_USERS);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setRole(resultSet.getString("role"));
                user.setCreatedAt(resultSet.getTimestamp("created_at"));
                user.setLastLogin(resultSet.getTimestamp("last_login"));
                users.add(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all users", e);
            throw new DataAccessException("Error getting all users", e);
        }
        
        return users;
    }
    
    /**
     * Get user by ID
     * @param userId the user ID
     * @return User object or null if not found
     * @throws SQLException if database error occurs
     */
    public User getUserById(int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_ID)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPasswordHash(resultSet.getString("password_hash"));
                    user.setSalt(resultSet.getString("salt"));
                    user.setCreatedAt(resultSet.getTimestamp("created_at"));
                    user.setLastLogin(resultSet.getTimestamp("last_login"));
                    user.setRole(resultSet.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by ID: " + userId, e);
            throw new DataAccessException("Error getting user by ID: " + userId, e);
        }
        return null;
    }
    
    /**
     * Update user information (username, email, role)
     * @param userId the user ID
     * @param username new username
     * @param email new email
     * @param role new role
     * @return true if update successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updateUser(int userId, String username, String email, String role) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USER)) {
            
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, role);
            statement.setInt(4, userId);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating user with ID: " + userId, e);
            throw new DataAccessException("Error updating user with ID: " + userId, e);
        }
    }
    
    /**
     * Delete user by ID
     * @param userId the user ID
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteUserById(int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USER_BY_ID)) {
            
            statement.setInt(1, userId);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting user with ID: " + userId, e);
            throw new DataAccessException("Error deleting user with ID: " + userId, e);
        }
    }
    
    /**
     * Delete user by username
     * @param username the username
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteUserByUsername(String username) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USER_BY_USERNAME)) {
            
            statement.setString(1, username);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting user with username: " + username, e);
            throw new DataAccessException("Error deleting user with username: " + username, e);
        }
    }
    
    /**
     * Update user password
     * @param userId the user ID
     * @param newPassword the new password
     * @return true if update successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(newPassword, salt);
        
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD)) {
            
            statement.setString(1, passwordHash);
            statement.setString(2, salt);
            statement.setInt(3, userId);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating password for user ID: " + userId, e);
            throw new DataAccessException("Error updating password for user ID: " + userId, e);
        }
    }
    
    /**
     * Check if user exists by ID
     * @param userId the user ID
     * @return true if user exists, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean userExistsById(int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_USER_EXISTS_BY_ID)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if user exists by ID: " + userId, e);
            throw new DataAccessException("Error checking if user exists by ID: " + userId, e);
        }
        return false;
    }
    
    /**
     * Check if user exists by username
     * @param username the username
     * @return true if user exists, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean userExistsByUsername(String username) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_USER_EXISTS_BY_USERNAME)) {
            
            statement.setString(1, username);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if user exists by username: " + username, e);
            throw new DataAccessException("Error checking if user exists by username: " + username, e);
        }
        return false;
    }
}