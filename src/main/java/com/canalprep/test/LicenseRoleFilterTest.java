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

            } else {

                
                // Create developer user
                User newDeveloper = userDAO.createUser("developer", "developer@example.com", "dev123", "DEVELOPER");
                

            }
            
            // Also check for a regular test user to demonstrate access restriction
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