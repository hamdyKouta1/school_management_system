package com.canalprep.test;

import com.canalprep.auth.dao.UserDAO;
import com.canalprep.auth.model.User;
import java.util.HashMap;
import java.util.Map;

public class AuthServletTest {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        
        try {
            // Simulate the exact same process as AuthServlet
            System.out.println("=== Testing AuthServlet Registration Logic ===");
            
            // Create request data like AuthServlet does
            Map<String, String> requestData = new HashMap<>();
            requestData.put("username", "servlettest123");
            requestData.put("email", "servlettest123@example.com");
            requestData.put("password", "password123");
            
            String username = requestData.get("username");
            String email = requestData.get("email");
            String password = requestData.get("password");
            String role = "USER";
            
            System.out.println("Request data:");
            System.out.println("Username: " + username);
            System.out.println("Email: " + email);
            System.out.println("Password: " + password);
            System.out.println("Role: " + role);
            
            // Check validation
            if (username == null || email == null || password == null) {
                System.out.println("ERROR: Username, email, and password are required");
                return;
            }
            
            // Check if user exists
            System.out.println("\nChecking if user exists...");
            User existingUser = userDAO.getUserByUsername(username);
            if (existingUser != null) {
                System.out.println("ERROR: Username already exists");
                return;
            }
            System.out.println("User does not exist, proceeding...");
            
            // Check secret code (simulate no secret code)
            String secretCode = requestData.get("secretCode");
            System.out.println("Secret code: " + secretCode);
            if (System.getenv("ADMIN_SECRET") != null && System.getenv("ADMIN_SECRET").equals(secretCode)) {
                role = "ADMIN";
                System.out.println("Admin role assigned");
            } else {
                System.out.println("Regular user role assigned");
            }
            
            // Create user
            System.out.println("\nCreating user...");
            User newUser = userDAO.createUser(username, email, password, role);
            
            System.out.println("\nUser created successfully!");
            System.out.println("ID: " + newUser.getId());
            System.out.println("Username: " + newUser.getUsername());
            System.out.println("Email: " + newUser.getEmail());
            System.out.println("Role: " + newUser.getRole());
            
            System.out.println("\nRegistration would be successful!");
            System.out.println("Response would be: {\"status\":\"success\",\"message\":\"Registration successful\"}");
            
        } catch (Exception e) {
            System.err.println("\nException occurred (this is what AuthServlet would catch):");
            System.err.println("Exception type: " + e.getClass().getSimpleName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            
            // This is what AuthServlet does in the catch block
            System.err.println("\nAuthServlet would return: Registration failed: An unexpected error occurred");
        }
    }
}