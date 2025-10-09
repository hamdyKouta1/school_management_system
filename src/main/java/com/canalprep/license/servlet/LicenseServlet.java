package com.canalprep.license.servlet;

import com.canalprep.license.model.License;
import com.canalprep.license.service.LicenseService;
import com.canalprep.license.service.OTPService;
import com.canalprep.auth.model.User;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.auth.utilities.JwtUtil;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet(urlPatterns = {"/api/protected/licence", "/api/protected/licence/renew", "/api/protected/licence/remove"})
public class LicenseServlet extends HttpServlet {
    
    private final LicenseService licenseService;
    private final OTPService otpService;
    private final Gson gson;
    
    public LicenseServlet() {
        this.licenseService = new LicenseService();
        this.otpService = new OTPService();
        this.gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        
        // Handle GET /api/protected/licence
        if ("/api/protected/licence".equals(servletPath)) {
            handleGetLicense(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Endpoint not found");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String servletPath = request.getServletPath();
        
        // Handle POST /api/protected/licence/renew
        if ("/api/protected/licence/renew".equals(servletPath)) {
            handleRenewLicense(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Endpoint not found");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String servletPath = request.getServletPath();
        
        // Handle DELETE /api/protected/licence/remove
        if ("/api/protected/licence/remove".equals(servletPath)) {
            handleRemoveLicense(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Endpoint not found");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    /**
     * Handle GET /api/protected/licence - Get current license information
     */
    private void handleGetLicense(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        
        try {
            License currentLicense = licenseService.getCurrentLicense();
            
            JsonObject responseJson = new JsonObject();
            
            if (currentLicense != null) {
                responseJson.addProperty("success", true);
                responseJson.addProperty("licenseKey", currentLicense.getLicenseKey());
                responseJson.addProperty("startDate", currentLicense.getStartDate().toString());
                responseJson.addProperty("endDate", currentLicense.getEndDate().toString());
                responseJson.addProperty("remainingDays", currentLicense.getRemainingDays());
                responseJson.addProperty("status", currentLicense.getStatus());
                responseJson.addProperty("isValid", currentLicense.isValid());
                responseJson.addProperty("createdAt", currentLicense.getCreatedAt().toString());
                responseJson.addProperty("updatedAt", currentLicense.getUpdatedAt().toString());
            } else {
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "No active license found");
                responseJson.addProperty("remainingDays", 0);
                responseJson.addProperty("status", "NO_LICENSE");
                responseJson.addProperty("isValid", false);
            }
            
            response.getWriter().write(gson.toJson(responseJson));
            
            LoggerUtil.logInfo("LicenseServlet", "License information retrieved successfully");
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("LicenseServlet", "Failed to get license information", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to retrieve license information");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    /**
     * Handle POST /api/protected/licence/renew - Renew license with OTP validation
     */
    private void handleRenewLicense(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        
        // Check if user is developer
        User currentUser = getCurrentUser(request);
        if (currentUser == null || !"DEVELOPER".equals(currentUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Access denied. Only developer users can renew licenses.");
            response.getWriter().write(gson.toJson(errorResponse));
            
            LoggerUtil.logSecurity("LICENSE_RENEW_DENIED", currentUser != null ? currentUser.getUsername() : "unknown", 
                "Unauthorized license renewal attempt");
            return;
        }
        
        try {
            // Parse request body
            JsonObject requestJson = JsonParser.parseString(request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual)).getAsJsonObject();
            
            String otpCode = requestJson.get("otp").getAsString();
            int durationDays = requestJson.get("duration").getAsInt();
            String startDateStr = requestJson.has("startDate") && !requestJson.get("startDate").isJsonNull() 
                ? requestJson.get("startDate").getAsString() : null;
            
            // Validate input
            if (otpCode == null || otpCode.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "OTP is required");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            // Validate license duration
            licenseService.validateLicenseDuration(durationDays);
            
            // Parse start date if provided
            LocalDate startDate = null;
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                try {
                    startDate = LocalDate.parse(startDateStr);
                    licenseService.validateStartDate(startDate);
                } catch (DateTimeParseException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("success", false);
                    errorResponse.addProperty("error", "Invalid start date format. Use YYYY-MM-DD");
                    response.getWriter().write(gson.toJson(errorResponse));
                    return;
                }
            }
            
            // Validate OTP
            String clientIP = getClientIP(request);
            boolean otpValid = otpService.validateOTP(otpCode, "RENEW", clientIP);
            
            if (!otpValid) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Invalid or expired OTP");
                response.getWriter().write(gson.toJson(errorResponse));
                
                LoggerUtil.logSecurity("LICENSE_RENEW_INVALID_OTP", currentUser.getUsername(), 
                    "Invalid OTP provided for license renewal");
                return;
            }
            
            // Renew license
            License newLicense = licenseService.renewLicense(durationDays, startDate, currentUser.getUsername());
            
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "License renewed successfully");
            responseJson.addProperty("licenseKey", newLicense.getLicenseKey());
            responseJson.addProperty("startDate", newLicense.getStartDate().toString());
            responseJson.addProperty("endDate", newLicense.getEndDate().toString());
            responseJson.addProperty("remainingDays", newLicense.getRemainingDays());
            responseJson.addProperty("status", newLicense.getStatus());
            
            response.getWriter().write(gson.toJson(responseJson));
            
            LoggerUtil.logSecurity("LICENSE_RENEWED", currentUser.getUsername(), 
                "License renewed successfully for " + durationDays + " days");
            
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", e.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("LicenseServlet", "Failed to renew license", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to renew license");
            response.getWriter().write(gson.toJson(errorResponse));
            
        } catch (Exception e) {
            LoggerUtil.logError("LicenseServlet", "Unexpected error during license renewal", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "An unexpected error occurred");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    /**
     * Handle DELETE /api/protected/licence/remove - Remove license with OTP validation
     */
    private void handleRemoveLicense(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        
        // Check if user is developer
        User currentUser = getCurrentUser(request);
        if (currentUser == null || !"DEVELOPER".equals(currentUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Access denied. Only developer users can remove licenses.");
            response.getWriter().write(gson.toJson(errorResponse));
            
            LoggerUtil.logSecurity("LICENSE_REMOVE_DENIED", currentUser != null ? currentUser.getUsername() : "unknown", 
                "Unauthorized license removal attempt");
            return;
        }
        
        try {
            // Parse request body
            JsonObject requestJson = JsonParser.parseString(request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual)).getAsJsonObject();
            
            String otpCode = requestJson.get("otp").getAsString();
            
            // Validate input
            if (otpCode == null || otpCode.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "OTP is required");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            // Validate OTP
            String clientIP = getClientIP(request);
            boolean otpValid = otpService.validateOTP(otpCode, "REMOVE", clientIP);
            
            if (!otpValid) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Invalid or expired OTP");
                response.getWriter().write(gson.toJson(errorResponse));
                
                LoggerUtil.logSecurity("LICENSE_REMOVE_INVALID_OTP", currentUser.getUsername(), 
                    "Invalid OTP provided for license removal");
                return;
            }
            
            // Remove license
            licenseService.removeLicense(currentUser.getUsername());
            
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "License removed successfully");
            
            response.getWriter().write(gson.toJson(responseJson));
            
            LoggerUtil.logSecurity("LICENSE_REMOVED", currentUser.getUsername(), 
                "License removed successfully");
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("LicenseServlet", "Failed to remove license", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", e.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
            
        } catch (Exception e) {
            LoggerUtil.logError("LicenseServlet", "Unexpected error during license removal", e);
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
     * Get client IP address
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