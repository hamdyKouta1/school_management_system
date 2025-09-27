package com.canalprep.test;

import com.canalprep.auth.dao.UserDAO;
import com.canalprep.auth.model.User;
import java.sql.SQLException;

public class RegistrationTest {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        
        try {
            // Test registration with a unique username and email
            String testUsername = "testuser_" + System.currentTimeMillis();
            String testEmail = "test_" + System.currentTimeMillis() + "@example.com";
            
            System.out.println("Attempting to register user:");
            System.out.println("Username: " + testUsername);
            System.out.println("Email: " + testEmail);
            
            User newUser = userDAO.createUser(testUsername, testEmail, "password123", "USER");
            
            System.out.println("\nRegistration successful!");
            System.out.println("User ID: " + newUser.getId());
            System.out.println("Username: " + newUser.getUsername());
            System.out.println("Email: " + newUser.getEmail());
            System.out.println("Role: " + newUser.getRole());
            
        } catch (SQLException e) {
            System.err.println("SQL Error during registration:");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error during registration:");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Also test checking existing users
        try {
            System.out.println("\n=== Checking existing users ===");
            User adminUser = userDAO.getUserByUsername("admin");
            if (adminUser != null) {
                System.out.println("Admin user exists:");
                System.out.println("ID: " + adminUser.getId());
                System.out.println("Username: " + adminUser.getUsername());
                System.out.println("Email: " + adminUser.getEmail());
                System.out.println("Role: " + adminUser.getRole());
            } else {
                System.out.println("No admin user found");
            }
            
            User testUser = userDAO.getUserByUsername("testuser");
            if (testUser != null) {
                System.out.println("\nTest user exists:");
                System.out.println("ID: " + testUser.getId());
                System.out.println("Username: " + testUser.getUsername());
                System.out.println("Email: " + testUser.getEmail());
                System.out.println("Role: " + testUser.getRole());
            } else {
                System.out.println("\nNo test user found");
            }
            
        } catch (Exception e) {
            System.err.println("Error checking existing users: " + e.getMessage());
            e.printStackTrace();
        }
    }
}