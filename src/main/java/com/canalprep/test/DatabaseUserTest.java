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

            } else {

                
                // Create admin user
                User newUser = userDAO.createUser("admin", "admin@example.com", "admin123", "ADMIN");
                

            }
            
            // Also check for a regular test user
            User testUser = userDAO.getUserByUsername("testuser");
            
            if (testUser == null) {

                User newTestUser = userDAO.createUser("testuser", "test@example.com", "test123", "USER");
                

            } else {

            }
            
        } catch (SQLException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}