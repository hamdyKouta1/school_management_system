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
           
            
            User newUser = userDAO.createUser(testUsername, testEmail, "password123", "USER");
            

            
        } catch (SQLException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }
        
        // Also test checking existing users
        try {

            User adminUser = userDAO.getUserByUsername("admin");
            if (adminUser != null) {
                 
            } else {
    
            }
            
            User testUser = userDAO.getUserByUsername("testuser");
            if (testUser != null) {
                 
            } else {
    
            }
            
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}