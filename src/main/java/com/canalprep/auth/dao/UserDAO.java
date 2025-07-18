package com.canalprep.auth.dao;

import com.canalprep.auth.model.User;
import com.canalprep.auth.utilities.PasswordUtils;
import com.canalprep.dao.DBConnection;
import com.canalprep.staticVAr.DBConst;

import java.sql.*;

public class UserDAO {
    private static final String INSERT_USER_SQL = DBConst.DB_INSERT_USER_SQL;
    private static final String SELECT_USER_BY_USERNAME = DBConst.DB_SELECT_USER_BY_USERNAME;
    private static final String UPDATE_LAST_LOGIN = DBConst.DB_UPDATE_LAST_LOGIN;
    
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
                throw new SQLException("Creating user failed, no rows affected.");
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
                    throw new SQLException("Creating user failed, no ID obtained.");
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
}