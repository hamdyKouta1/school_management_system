package com.canalprep.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.canalprep.staticVariables.DBConst;
import com.canalprep.config.ConfigLoader;
import com.canalprep.exception.DataAccessException;

public class DBConnection {
    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());
    
    // Load database configuration from application.properties with environment overrides
    private static final String JDBC_URL = ConfigLoader.getString("db.url", "jdbc:postgresql://localhost:5432/canal_prep_school_clone");
    private static final String USERNAME = ConfigLoader.getString("db.username", "postgres");
    private static final String PASSWORD = ConfigLoader.getString("db.password", "123");

    
    static {
        try {
            // Explicitly load the JDBC driver
            Class.forName(DBConst.DB_DRIVER);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "PostgreSQL JDBC Driver not found.", e);
            throw new DataAccessException("PostgreSQL JDBC Driver not found.", e);
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting database connection", e);
            throw new DataAccessException("Error getting database connection", e);
        }
    }
}