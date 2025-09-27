package com.canalprep.test;

import com.canalprep.auth.dao.UserDAO;
import com.canalprep.auth.model.User;
import java.sql.SQLException;

public class DatabaseUserTest {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        
        try {
            // Check if admin user exists
            User existingUser = userDAO.getUserByUsername("admin");
            
            if (existingUser != null) {
                System.out.println("Admin user already exists:");
                System.out.println("Username: " + existingUser.getUsername());
                System.out.println("Email: " + existingUser.getEmail());
                System.out.println("Role: " + existingUser.getRole());
            } else {
                System.out.println("No admin user found. Creating admin user...");
                
                // Create admin user
                User newUser = userDAO.createUser("admin", "admin@example.com", "admin123", "ADMIN");
                
                System.out.println("Admin user created successfully:");
                System.out.println("Username: " + newUser.getUsername());
                System.out.println("Email: " + newUser.getEmail());
                System.out.println("Role: " + newUser.getRole());
                System.out.println("Password: admin123");
            }
            
            // Also check for a regular test user
            User testUser = userDAO.getUserByUsername("testuser");
            
            if (testUser == null) {
                System.out.println("\nCreating test user...");
                User newTestUser = userDAO.createUser("testuser", "test@example.com", "test123", "USER");
                
                System.out.println("Test user created successfully:");
                System.out.println("Username: " + newTestUser.getUsername());
                System.out.println("Email: " + newTestUser.getEmail());
                System.out.println("Role: " + newTestUser.getRole());
                System.out.println("Password: test123");
            } else {
                System.out.println("\nTest user already exists:");
                System.out.println("Username: " + testUser.getUsername());
                System.out.println("Email: " + testUser.getEmail());
                System.out.println("Role: " + testUser.getRole());
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}