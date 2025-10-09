package com.canalprep.util;

import com.canalprep.dao.DBConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseInitializer {
    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());
    
    public static void initializeLicenseSchema() {
        try {
            // Read the schema file
            InputStream schemaStream = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("database/license_schema.sql");
            
            if (schemaStream == null) {
                logger.severe("Could not find license_schema.sql file");
                return;
            }
            
            StringBuilder sqlContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip comments and empty lines
                    if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                        sqlContent.append(line).append("\n");
                    }
                }
            }
            
            // Execute the SQL
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Split by semicolon and execute each statement
                String[] statements = sqlContent.toString().split(";");
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                            logger.info("Executed SQL: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                        } catch (SQLException e) {
                            // Log but continue - some statements might already exist
                            logger.warning("SQL execution warning: " + e.getMessage());
                        }
                    }
                }
                
                logger.info("License schema initialization completed");
                System.out.println("✅ License schema initialized successfully");
                
            }
            
        } catch (Exception e) {
            logger.severe("Failed to initialize license schema: " + e.getMessage());
            System.err.println("❌ Failed to initialize license schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        initializeLicenseSchema();
    }
}