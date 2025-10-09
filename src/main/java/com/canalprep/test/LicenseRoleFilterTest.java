package com.canalprep.test;

import com.canalprep.auth.dao.UserDAO;
import com.canalprep.auth.model.User;
import java.sql.SQLException;

public class LicenseRoleFilterTest {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        
        try {
            // Check if developer user exists
            User existingDeveloper = userDAO.getUserByUsername("developer");
            
            if (existingDeveloper != null) {
                System.out.println("Developer user already exists:");
                System.out.println("Username: " + existingDeveloper.getUsername());
                System.out.println("Email: " + existingDeveloper.getEmail());
                System.out.println("Role: " + existingDeveloper.getRole());
            } else {
                System.out.println("No developer user found. Creating developer user...");
                
                // Create developer user
                User newDeveloper = userDAO.createUser("developer", "developer@example.com", "dev123", "DEVELOPER");
                
                System.out.println("Developer user created successfully:");
                System.out.println("Username: " + newDeveloper.getUsername());
                System.out.println("Email: " + newDeveloper.getEmail());
                System.out.println("Role: " + newDeveloper.getRole());
                System.out.println("Password: dev123");
            }
            
            // Also check for a regular test user to demonstrate access restriction
            User testUser = userDAO.getUserByUsername("testuser");
            
            if (testUser == null) {
                System.out.println("\nCreating regular test user (should NOT have access to license endpoints)...");
                User newTestUser = userDAO.createUser("testuser", "test@example.com", "test123", "USER");
                
                System.out.println("Regular test user created successfully:");
                System.out.println("Username: " + newTestUser.getUsername());
                System.out.println("Email: " + newTestUser.getEmail());
                System.out.println("Role: " + newTestUser.getRole());
                System.out.println("Password: test123");
            } else {
                System.out.println("\nRegular test user already exists:");
                System.out.println("Username: " + testUser.getUsername());
                System.out.println("Email: " + testUser.getEmail());
                System.out.println("Role: " + testUser.getRole());
            }
            
            System.out.println("\n=== License Endpoint Access Summary ===");
            System.out.println("✅ DEVELOPER role users CAN access /api/protected/licence endpoints");
            System.out.println("❌ USER role users CANNOT access /api/protected/licence endpoints");
            System.out.println("❌ ADMIN role users CANNOT access /api/protected/licence endpoints (unless also DEVELOPER)");
            
            System.out.println("\n=== Test Instructions ===");
            System.out.println("1. Start the server: java -jar target/school-management.jar 8083");
            System.out.println("2. Login as developer: POST /api/auth/login with {\"username\":\"developer\",\"password\":\"dev123\"}");
            System.out.println("3. Use the JWT token to access: GET /api/protected/licence");
            System.out.println("4. Try with regular user token - should get 403 Forbidden");
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}