package com.canalprep.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.canalprep.staticVAr.DBConst;

public class DBConnection {
    private static final String JDBC_URL = DBConst.DB_URL;
    private static final String USERNAME = DBConst.DB_USER;
    private static final String PASSWORD = DBConst.DB_PASSWORD;
    
    static {
        try {
            // Explicitly load the JDBC driver
            Class.forName(DBConst.DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found. Include it in your library path", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    }
}