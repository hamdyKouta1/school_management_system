package com.canalprep.servlet;

import com.canalprep.auth.dao.UserDAO;
import com.canalprep.auth.model.User;
import com.canalprep.service.UserOTPService;
import com.canalprep.exception.DataAccessException;
import com.canalprep.utilities.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet for handling user management operations
 * Endpoints:
 * - GET /api/protected/users/ - Get all users (admin/developer only)
 * - GET /api/protected/users/{id} - Get user by ID (admin/developer only)
 * - PUT /api/protected/users/{id} - Update user (admin/developer only)
 * - DELETE /api/protected/users/{id} - Delete user by ID (admin/developer only)
 * - DELETE /api/protected/users/username/{username} - Delete user by username (admin/developer only)
 * - PUT /api/protected/users/{id}/resetpassword - Reset password with OTP (self/admin/developer)
 */
@WebServlet(urlPatterns = {"/api/protected/users/*"})
public class UserServlet extends HttpServlet {
    
    private final UserDAO userDAO;
    private final UserOTPService userOTPService;
    private final Gson gson;
    
    public UserServlet() {
        this.userDAO = new UserDAO();
        this.userOTPService = new UserOTPService();
        this.gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Debug logging
        LoggerUtil.logInfo("UserServlet", "doGet called for path: " + request.getPathInfo());
        
        // Get current user from request attributes (set by AuthenticationFilter)
        String userId = (String) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String email = (String) request.getAttribute("email");
        String userRole = (String) request.getAttribute("role");
        
        LoggerUtil.logInfo("UserServlet", "Attributes - userId: " + userId + ", username: " + username + ", role: " + userRole);
        
        if (userId == null || username == null || userRole == null) {
            sendErrorResponse(response, "Authentication required", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Create a User object from the attributes
        User currentUser = new User();
        currentUser.setId(Integer.parseInt(userId));
        currentUser.setUsername(username);
        currentUser.setEmail(email);
        currentUser.setRole(userRole);
        
        // Check role-based access (only admin and developer can access user management)
        if (!"ADMIN".equals(userRole) && !"DEVELOPER".equals(userRole)) {
            sendErrorResponse(response, "Access denied. Admin or Developer role required.", HttpServletResponse.SC_FORBIDDEN);
            LoggerUtil.logSecurity("USER_MANAGEMENT_ACCESS_DENIED", currentUser.getUsername(),
                "Unauthorized user management access attempt by role: " + userRole);
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/protected/users/ - Get all users
                handleGetAllUsers(request, response, currentUser);
            } else {
                // Parse path to extract user ID
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    try {
                        int targetUserId = Integer.parseInt(pathParts[1]);
                        // GET /api/protected/users/{id} - Get user by ID
                        handleGetUserById(request, response, currentUser, targetUserId);
                    } catch (NumberFormatException e) {
                        sendErrorResponse(response, "Invalid user ID format", HttpServletResponse.SC_BAD_REQUEST);
                    }
                } else {
                    sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError("UserServlet", "Error in doGet", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get current user from request attributes
        String userId = (String) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String email = (String) request.getAttribute("email");
        String userRole = (String) request.getAttribute("role");
        
        if (userId == null || username == null || userRole == null) {
            sendErrorResponse(response, "Authentication required", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Create a User object from the attributes
        User currentUser = new User();
        currentUser.setId(Integer.parseInt(userId));
        currentUser.setUsername(username);
        currentUser.setEmail(email);
        currentUser.setRole(userRole);
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String[] pathParts = pathInfo.split("/");
        
        try {
            if (pathParts.length == 2) {
                // PUT /api/protected/users/{id} - Update user
                if (!"ADMIN".equals(userRole) && !"DEVELOPER".equals(userRole)) {
                    sendErrorResponse(response, "Access denied. Admin or Developer role required.", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                
                try {
                    int targetUserId = Integer.parseInt(pathParts[1]);
                    handleUpdateUser(request, response, currentUser, targetUserId);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, "Invalid user ID format", HttpServletResponse.SC_BAD_REQUEST);
                }
            } else if (pathParts.length == 3 && "resetpassword".equals(pathParts[2])) {
                // PUT /api/protected/users/{id}/resetpassword - Reset password
                try {
                    int targetUserId = Integer.parseInt(pathParts[1]);
                    handleResetPassword(request, response, currentUser, targetUserId, userRole);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, "Invalid user ID format", HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LoggerUtil.logError("UserServlet", "Error in doPut", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get current user from request attributes
        String userId = (String) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String email = (String) request.getAttribute("email");
        String userRole = (String) request.getAttribute("role");
        
        if (userId == null || username == null || userRole == null) {
            sendErrorResponse(response, "Authentication required", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Create a User object from the attributes
        User currentUser = new User();
        currentUser.setId(Integer.parseInt(userId));
        currentUser.setUsername(username);
        currentUser.setEmail(email);
        currentUser.setRole(userRole);
        
        // Check role-based access (only admin and developer can delete users)
        if (!"ADMIN".equals(userRole) && !"DEVELOPER".equals(userRole)) {
            sendErrorResponse(response, "Access denied. Admin or Developer role required.", HttpServletResponse.SC_FORBIDDEN);
            LoggerUtil.logSecurity("USER_DELETE_ACCESS_DENIED", currentUser.getUsername(),
                "Unauthorized user deletion attempt by role: " + userRole);
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String[] pathParts = pathInfo.split("/");
        
        try {
            if (pathParts.length == 2) {
                // DELETE /api/protected/users/{id} - Delete user by ID
                try {
                    int targetUserId = Integer.parseInt(pathParts[1]);
                    handleDeleteUserById(request, response, currentUser, targetUserId);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, "Invalid user ID format", HttpServletResponse.SC_BAD_REQUEST);
                }
            } else if (pathParts.length == 3 && "username".equals(pathParts[1])) {
                // DELETE /api/protected/users/username/{username} - Delete user by username
                String targetUsername = pathParts[2];
                handleDeleteUserByUsername(request, response, currentUser, targetUsername);
            } else {
                sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LoggerUtil.logError("UserServlet", "Error in doDelete", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle GET all users request
     */
    private void handleGetAllUsers(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws IOException, SQLException {
        
        List<User> users = userDAO.getAllUsers();
        
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", true);
        responseJson.addProperty("message", "Users retrieved successfully");
        responseJson.add("data", gson.toJsonTree(users));
        responseJson.addProperty("count", users.size());
        
        sendJsonResponse(response, responseJson, HttpServletResponse.SC_OK);
        
        LoggerUtil.logInfo("UserServlet", "All users retrieved by: " + currentUser.getUsername());
    }
    
    /**
     * Handle GET user by ID request
     */
    private void handleGetUserById(HttpServletRequest request, HttpServletResponse response, 
                                 User currentUser, int userId) throws IOException, SQLException {
        
        User user = userDAO.getUserById(userId);
        
        if (user == null) {
            sendErrorResponse(response, "User not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Create response with limited fields for security
        JsonObject userJson = new JsonObject();
        userJson.addProperty("id", user.getId());
        userJson.addProperty("username", user.getUsername());
        userJson.addProperty("email", user.getEmail());
        userJson.addProperty("role", user.getRole());
        userJson.addProperty("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        userJson.addProperty("lastLogin", user.getLastLogin() != null ? user.getLastLogin().toString() : null);
        
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", true);
        responseJson.addProperty("message", "User retrieved successfully");
        responseJson.add("data", userJson);
        
        sendJsonResponse(response, responseJson, HttpServletResponse.SC_OK);
        
        LoggerUtil.logInfo("UserServlet", "User " + userId + " retrieved by: " + currentUser.getUsername());
    }
    
    /**
     * Handle PUT update user request
     */
    private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response, 
                                User currentUser, int userId) throws IOException, SQLException {
        
        // Parse request body
        JsonObject requestJson;
        try {
            requestJson = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, "Invalid JSON format", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Validate required fields
        if (!requestJson.has("username") || !requestJson.has("email") || !requestJson.has("role")) {
            sendErrorResponse(response, "Missing required fields: username, email, role", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        String username = requestJson.get("username").getAsString().trim();
        String email = requestJson.get("email").getAsString().trim();
        String role = requestJson.get("role").getAsString().trim();
        
        // Validate input
        if (username.isEmpty() || email.isEmpty() || role.isEmpty()) {
            sendErrorResponse(response, "Username, email, and role cannot be empty", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Validate role
        if (!"ADMIN".equals(role) && !"DEVELOPER".equals(role) && !"USER".equals(role)) {
            sendErrorResponse(response, "Invalid role. Must be ADMIN, DEVELOPER, or USER", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Check if user exists
        if (!userDAO.userExistsById(userId)) {
            sendErrorResponse(response, "User not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Update user
        boolean updated = userDAO.updateUser(userId, username, email, role);
        
        if (updated) {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "User updated successfully");
            
            sendJsonResponse(response, responseJson, HttpServletResponse.SC_OK);
            
            LoggerUtil.logSecurity("USER_UPDATED", currentUser.getUsername(),
                "User " + userId + " updated by: " + currentUser.getUsername());
        } else {
            sendErrorResponse(response, "Failed to update user", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle DELETE user by ID request
     */
    private void handleDeleteUserById(HttpServletRequest request, HttpServletResponse response, 
                                    User currentUser, int userId) throws IOException, SQLException {
        
        // Prevent self-deletion
        if (userId == currentUser.getId()) {
            sendErrorResponse(response, "Cannot delete your own account", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Check if user exists
        if (!userDAO.userExistsById(userId)) {
            sendErrorResponse(response, "User not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Delete user
        boolean deleted = userDAO.deleteUserById(userId);
        
        if (deleted) {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "User deleted successfully");
            
            sendJsonResponse(response, responseJson, HttpServletResponse.SC_OK);
            
            LoggerUtil.logSecurity("USER_DELETED", currentUser.getUsername(),
                "User " + userId + " deleted by: " + currentUser.getUsername());
        } else {
            sendErrorResponse(response, "Failed to delete user", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle DELETE user by username request
     */
    private void handleDeleteUserByUsername(HttpServletRequest request, HttpServletResponse response, 
                                          User currentUser, String username) throws IOException, SQLException {
        
        // Prevent self-deletion
        if (username.equals(currentUser.getUsername())) {
            sendErrorResponse(response, "Cannot delete your own account", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Check if user exists
        if (!userDAO.userExistsByUsername(username)) {
            sendErrorResponse(response, "User not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Delete user
        boolean deleted = userDAO.deleteUserByUsername(username);
        
        if (deleted) {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "User deleted successfully");
            
            sendJsonResponse(response, responseJson, HttpServletResponse.SC_OK);
            
            LoggerUtil.logSecurity("USER_DELETED", currentUser.getUsername(),
                "User " + username + " deleted by: " + currentUser.getUsername());
        } else {
            sendErrorResponse(response, "Failed to delete user", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle PUT reset password request
     */
    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response, 
                                   User currentUser, int userId, String userRole) throws IOException {
        
        // Check authorization for password reset
        boolean isAuthorized = (userId == currentUser.getId()) || // Self-service
                              "ADMIN".equals(userRole) || "DEVELOPER".equals(userRole); // Admin/Developer
        
        if (!isAuthorized) {
            sendErrorResponse(response, "Access denied. You can only reset your own password unless you are an admin or developer.", 
                            HttpServletResponse.SC_FORBIDDEN);
            LoggerUtil.logSecurity("PASSWORD_RESET_ACCESS_DENIED", currentUser.getUsername(),
                "Unauthorized password reset attempt for user " + userId + " by: " + currentUser.getUsername());
            return;
        }
        
        // Parse request body
        JsonObject requestJson;
        try {
            requestJson = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, "Invalid JSON format", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Check if this is OTP generation or password reset
        if (requestJson.has("generateOTP") && requestJson.get("generateOTP").getAsBoolean()) {
            // Generate OTP for password reset
            try {
                String otpCode = userOTPService.generatePasswordResetOTP(
                    userId, currentUser.getId(), userRole, request.getRemoteAddr()
                );
                
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "OTP sent to user email successfully");
                responseJson.addProperty("otpRequired", true);
                responseJson.addProperty("nextStep", "Provide OTP and new password to complete reset");
                
                sendJsonResponse(response, responseJson, HttpServletResponse.SC_OK);
                
            } catch (DataAccessException e) {
                LoggerUtil.logError("UserServlet", "Failed to generate password reset OTP", e);
                sendErrorResponse(response, "Failed to generate OTP: " + e.getMessage(), 
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            // Validate OTP and reset password
            if (!requestJson.has("otp") || !requestJson.has("newPassword")) {
                sendErrorResponse(response, "Missing required fields: otp, newPassword", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            String otpCode = requestJson.get("otp").getAsString().trim();
            String newPassword = requestJson.get("newPassword").getAsString();
            
            if (otpCode.isEmpty() || newPassword.isEmpty()) {
                sendErrorResponse(response, "OTP and new password cannot be empty", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Validate password strength (basic validation)
            if (newPassword.length() < 6) {
                sendErrorResponse(response, "Password must be at least 6 characters long", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            try {
                boolean resetSuccessful = userOTPService.validateOTPAndResetPassword(
                    otpCode, newPassword, request.getRemoteAddr()
                );
                
                if (resetSuccessful) {
                    JsonObject responseJson = new JsonObject();
                    responseJson.addProperty("success", true);
                    responseJson.addProperty("message", "Password reset successfully");
                    
                    sendJsonResponse(response, responseJson, HttpServletResponse.SC_OK);
                } else {
                    sendErrorResponse(response, "Invalid or expired OTP", HttpServletResponse.SC_UNAUTHORIZED);
                }
                
            } catch (DataAccessException e) {
                LoggerUtil.logError("UserServlet", "Failed to reset password", e);
                sendErrorResponse(response, "Failed to reset password: " + e.getMessage(), 
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        // Special endpoint to fix OTP constraint
        if ("/fix-otp-constraint".equals(pathInfo)) {
            try {
                fixOTPConstraint();
                JsonObject successResponse = new JsonObject();
                successResponse.addProperty("success", true);
                successResponse.addProperty("message", "OTP constraint updated successfully");
                sendJsonResponse(response, successResponse, 200);
            } catch (Exception e) {
                LoggerUtil.logError("UserServlet", "Failed to fix OTP constraint: " + e.getMessage(), e);
                sendErrorResponse(response, "Failed to fix OTP constraint: " + e.getMessage(), 500);
            }
            return;
        }
        
        sendErrorResponse(response, "Method not allowed", 405);
    }
    
    /**
     * Fix OTP constraint to include PASSWORD_RESET operation
     */
    private void fixOTPConstraint() throws SQLException {
        try (java.sql.Connection conn = com.canalprep.dao.DBConnection.getConnection()) {
            // Drop existing constraint
            String dropConstraint = "ALTER TABLE otps DROP CONSTRAINT IF EXISTS chk_otp_operation";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(dropConstraint)) {
                stmt.executeUpdate();
                LoggerUtil.logInfo("UserServlet", "Dropped existing OTP constraint");
            }
            
            // Add new constraint with PASSWORD_RESET
            String addConstraint = "ALTER TABLE otps ADD CONSTRAINT chk_otp_operation CHECK (operation IN ('RENEW', 'REMOVE', 'ADMIN_CREATION', 'PASSWORD_RESET'))";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(addConstraint)) {
                stmt.executeUpdate();
                LoggerUtil.logInfo("UserServlet", "Added new OTP constraint with PASSWORD_RESET");
            }
        }
    }

    /**
     * Send JSON response
     */
    private void sendJsonResponse(HttpServletResponse response, JsonObject jsonResponse, int statusCode) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    /**
     * Send error response
     */
    private void sendErrorResponse(HttpServletResponse response, String errorMessage, int statusCode) 
            throws IOException {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("success", false);
        errorResponse.addProperty("error", errorMessage);
        
        sendJsonResponse(response, errorResponse, statusCode);
    }
}