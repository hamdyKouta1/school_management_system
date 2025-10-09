package com.canalprep.otp.servlet;

import com.canalprep.license.service.OTPService;
import com.canalprep.auth.model.User;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.exception.DataAccessException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/api/protected/otp/developer", "/api/protected/otp/admin"})
public class StandaloneOTPServlet extends HttpServlet {
    
    private final OTPService otpService;
    private final Gson gson;
    
    public StandaloneOTPServlet() {
        this.otpService = new OTPService();
        this.gson = new Gson();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        
        String path = request.getRequestURI();
        User currentUser = getCurrentUser(request);
        
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Authentication required");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // Handle developer OTP endpoint
        if (path.endsWith("/api/protected/otp/developer")) {
            handleDeveloperOTP(request, response, currentUser);
        }
        // Handle admin OTP endpoint
        else if (path.endsWith("/api/protected/otp/admin")) {
            handleAdminOTP(request, response, currentUser);
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Endpoint not found");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    /**
     * Handle OTP generation for developer operations
     * Accessible by DEVELOPER role only
     */
    private void handleDeveloperOTP(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws IOException {
        
        // Check if user is developer
        if (!"DEVELOPER".equals(currentUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Access denied. Only developer users can request developer OTPs.");
            response.getWriter().write(gson.toJson(errorResponse));
            
            LoggerUtil.logSecurity("DEVELOPER_OTP_REQUEST_DENIED", currentUser.getUsername(), 
                "Unauthorized developer OTP request attempt");
            return;
        }
        
        try {
            // Parse request body
            JsonObject requestJson = JsonParser.parseString(request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual)).getAsJsonObject();
            
            String operation = requestJson.get("operation").getAsString();
            
            // Validate operation for developer context
            if (!"RENEW".equals(operation) && !"REMOVE".equals(operation) && !"SYSTEM_CONFIG".equals(operation)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Invalid operation. Allowed: RENEW, REMOVE, SYSTEM_CONFIG");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            // Generate OTP
            String clientIP = getClientIP(request);
            String otpCode = otpService.generateAndSendOTP(operation, clientIP);
            
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Developer OTP generated successfully");
            responseJson.addProperty("otp", otpCode);
            responseJson.addProperty("operation", operation);
            responseJson.addProperty("expiresInMinutes", 10);
            
            response.getWriter().write(gson.toJson(responseJson));
            
            LoggerUtil.logSecurity("DEVELOPER_OTP_GENERATED", currentUser.getUsername(), 
                "Developer OTP generated for operation: " + operation);
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("StandaloneOTPServlet", "Failed to generate developer OTP", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", e.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
            
        } catch (Exception e) {
            LoggerUtil.logError("StandaloneOTPServlet", "Unexpected error during developer OTP generation", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "An unexpected error occurred");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    /**
     * Handle OTP generation for admin operations
     * Accessible by ADMIN and DEVELOPER roles
     */
    private void handleAdminOTP(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws IOException {
        
        // Check if user is admin or developer
        if (!"ADMIN".equals(currentUser.getRole()) && !"DEVELOPER".equals(currentUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Access denied. Only admin or developer users can request admin OTPs.");
            response.getWriter().write(gson.toJson(errorResponse));
            
            LoggerUtil.logSecurity("ADMIN_OTP_REQUEST_DENIED", currentUser.getUsername(), 
                "Unauthorized admin OTP request attempt");
            return;
        }
        
        try {
            // Parse request body
            JsonObject requestJson = JsonParser.parseString(request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual)).getAsJsonObject();
            
            String operation = requestJson.get("operation").getAsString();
            
            // Validate operation for admin context
            if (!"USER_MANAGEMENT".equals(operation) && !"SYSTEM_RESET".equals(operation) && !"DATABASE_BACKUP".equals(operation)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Invalid operation. Allowed: USER_MANAGEMENT, SYSTEM_RESET, DATABASE_BACKUP");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            // Generate OTP
            String clientIP = getClientIP(request);
            String otpCode = otpService.generateAndSendOTP(operation, clientIP);
            
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Admin OTP generated successfully");
            responseJson.addProperty("otp", otpCode);
            responseJson.addProperty("operation", operation);
            responseJson.addProperty("expiresInMinutes", 10);
            
            response.getWriter().write(gson.toJson(responseJson));
            
            LoggerUtil.logSecurity("ADMIN_OTP_GENERATED", currentUser.getUsername(), 
                "Admin OTP generated for operation: " + operation);
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("StandaloneOTPServlet", "Failed to generate admin OTP", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", e.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
            
        } catch (Exception e) {
            LoggerUtil.logError("StandaloneOTPServlet", "Unexpected error during admin OTP generation", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "An unexpected error occurred");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    /**
     * Get current user from JWT token properties
     * @param request The HTTP request
     * @return User object created from token data or null if not authenticated
     */
    private User getCurrentUser(HttpServletRequest request) {
        // Extract user data from JWT token properties set by AuthenticationFilter
        String userId = (String) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String email = (String) request.getAttribute("email");
        String role = (String) request.getAttribute("role");
        
        // Return null if any required property is missing
        if (userId == null || username == null || email == null || role == null) {
            return null;
        }
        
        // Create User object from token properties
        User user = new User();
        try {
            user.setId(Integer.parseInt(userId));
        } catch (NumberFormatException e) {
            return null; // Invalid userId format
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        
        return user;
    }
    
    /**
     * Get client IP address from request
     * @param request The HTTP request
     * @return Client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}