package com.canalprep.auth.servlets;

import com.canalprep.auth.utilities.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.canalprep.auth.dao.UserDAO;
import com.canalprep.auth.dao.RefreshTokenDAO;
import com.canalprep.auth.model.User;
import com.canalprep.auth.utilities.PasswordUtils;
import com.canalprep.auth.service.AdminOTPService;
import com.canalprep.service.PasswordRecoveryService;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.utilities.RateLimiter;
import com.canalprep.config.ConfigLoader;
import io.jsonwebtoken.Claims;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.canalprep.exception.DataAccessException;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private final UserDAO userDao = new UserDAO();
    private final RefreshTokenDAO refreshTokenDao = new RefreshTokenDAO();
    private final AdminOTPService adminOTPService = new AdminOTPService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            return;
        }
        
        switch (pathInfo) {
            case "/login":
                handleLogin(req, resp);
                break;
            case "/register":
                handleRegister(req, resp);
                break;
            case "/check-otp":
                handleCheckOTP(req, resp);
                break;
            case "/refresh":
                handleRefreshToken(req, resp);
                break;
            case "/logout":
                handleLogout(req, resp);
                break;
            case "/forget_password":
                handleForgetPassword(req, resp);
                break;
            case "/verify_reset_otp":
                handleVerifyResetOTP(req, resp);
                break;
            case "/validate":
                handleTokenValidation(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }
    
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> requestData = objectMapper.readValue(req.getInputStream(), new TypeReference<Map<String, String>>() {});
            String username = requestData.get("username");
            String password = requestData.get("password");
            
            if (username == null || password == null) {
                LoggerUtil.logSecurity("LOGIN_MISSING_CREDENTIALS", "UNKNOWN", "Login attempt with missing credentials from IP: " + req.getRemoteAddr());
                sendErrorResponse(resp, "Username and password are required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            User user = userDao.getUserByUsername(username);
            if (user == null || !PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
                LoggerUtil.logSecurity("LOGIN_FAILED", username, "Failed login attempt from IP: " + req.getRemoteAddr());
                sendErrorResponse(resp, "Invalid username or password", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // Update last login
            userDao.updateLastLogin(user.getId());
            
            // Log successful login
            LoggerUtil.logSecurity("LOGIN_SUCCESS", username, "Successful login (ID: " + user.getId() + ") from IP: " + req.getRemoteAddr());
            
            // Generate JWT token instead of using session
            String token = JwtUtil.generateToken(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
            );
            
            // Generate refresh token and persist (for session continuity)
            String deviceId = requestData.getOrDefault("deviceId", "");
            String refreshToken = JwtUtil.generateRefreshToken(
                String.valueOf(user.getId()),
                user.getUsername(),
                deviceId
            );
            Claims refreshClaims = JwtUtil.parseToken(refreshToken);
            String refreshJti = refreshClaims.getId();
            long refreshExpMillis = refreshClaims.getExpiration() != null ? refreshClaims.getExpiration().getTime() : System.currentTimeMillis();
            try {
                refreshTokenDao.saveRefreshToken(refreshJti, user.getId(), refreshJti, refreshExpMillis,
                        req.getRemoteAddr(), req.getHeader("User-Agent"), deviceId);
            } catch (Exception e) {
                LoggerUtil.logWarning("AuthServlet", "Failed to persist refresh token for user: " + user.getUsername());
            }

            // Prepare response with tokens
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("message", "Login successful");
            responseData.put("token", token);
            responseData.put("refreshToken", refreshToken);
            responseData.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));
            
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), responseData);
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("AuthServlet", "Login failed due to data access issue: " + e.getMessage(), e);
            sendErrorResponse(resp, "Login failed: Invalid username or password", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            LoggerUtil.logError("AuthServlet", "An unexpected error occurred during login: " + e.getMessage(), e);
            sendErrorResponse(resp, "Login failed: An unexpected error occurred", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isHttpsRequest(HttpServletRequest req) {
        if (req.isSecure()) return true;
        String xfProto = req.getHeader("X-Forwarded-Proto");
        return xfProto != null && xfProto.equalsIgnoreCase("https");
    }

    private void handleRefreshToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Rate limiting per IP
            int maxReq = ConfigLoader.getInt("rate.refresh.max_requests", 10);
            long windowMs = ConfigLoader.getLong("rate.refresh.window_ms", 5 * 60 * 1000);
            String ip = req.getRemoteAddr();
            String rlKey = "refresh:" + ip;
            if (!RateLimiter.checkAndConsume(rlKey, maxReq, windowMs)) {
                LoggerUtil.logSecurity("REFRESH_RATE_LIMITED", "UNKNOWN", "Too many refresh requests from IP: " + ip);
                resp.setStatus(429);
                objectMapper.writeValue(resp.getWriter(), Map.of(
                        "status", "error",
                        "message", "Too many requests"
                ));
                return;
            }

            boolean enforceHttps = ConfigLoader.getBoolean("security.enforce_https", false);
            if (enforceHttps && !isHttpsRequest(req)) {
                LoggerUtil.logSecurity("REFRESH_HTTP_BLOCKED", "UNKNOWN", "HTTPS required for token refresh from IP: " + ip);
                sendErrorResponse(resp, "HTTPS is required for this endpoint", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Map<String, Object> requestData = objectMapper.readValue(req.getInputStream(), new TypeReference<Map<String, Object>>() {});
            Object rtObj = requestData.get("refreshToken");
            if (rtObj == null) {
                sendErrorResponse(resp, "Refresh token is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String refreshToken = rtObj.toString();

            // Optional verification data
            Map<String, Object> verification = null;
            if (requestData.get("verification") instanceof Map) {
                verification = (Map<String, Object>) requestData.get("verification");
            }
            String deviceId = verification != null && verification.get("deviceId") != null ? verification.get("deviceId").toString() : null;
            String userAgent = req.getHeader("User-Agent");

            // Validate refresh token
            JwtUtil.TokenValidationResult validation = JwtUtil.validateRefreshToken(refreshToken);
            if (!validation.isValid()) {
                LoggerUtil.logSecurity("REFRESH_TOKEN_INVALID", "UNKNOWN", "Invalid refresh token: " + validation.getErrorMessage() + " from IP: " + ip);
                sendErrorResponse(resp, validation.getErrorMessage(), HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Claims claims = validation.getClaims();
            String userIdStr = claims.getSubject();
            String username = claims.get("username", String.class);
            String jti = claims.getId();
            String claimDeviceId = claims.get("deviceId", String.class);

            // Check DAO status
            RefreshTokenDAO.TokenStatus status = refreshTokenDao.getTokenStatus(jti);
            if (status != RefreshTokenDAO.TokenStatus.VALID) {
                LoggerUtil.logSecurity("REFRESH_TOKEN_REJECTED", username != null ? username : "UNKNOWN",
                        "Refresh rejected due to status: " + status + " from IP: " + ip);
                String msg;
                switch (status) {
                    case EXPIRED:
                        msg = "Refresh token has expired";
                        break;
                    case REVOKED:
                        msg = "Refresh token is revoked";
                        break;
                    case NOT_FOUND:
                        msg = "Refresh token not found";
                        break;
                    default:
                        msg = "Invalid refresh token";
                        break;
                }
                sendErrorResponse(resp, msg, HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Optional security verification
            if (deviceId != null && claimDeviceId != null && !deviceId.equals(claimDeviceId)) {
                LoggerUtil.logSecurity("REFRESH_SECURITY_MISMATCH", username != null ? username : "UNKNOWN",
                        "Device verification mismatch from IP: " + ip);
                sendErrorResponse(resp, "Security verification failed", HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // Generate new refresh token and revoke the old one
            String newRefreshToken = JwtUtil.generateRefreshToken(userIdStr, username, claimDeviceId);
            Claims newClaims = JwtUtil.parseToken(newRefreshToken);
            String newJti = newClaims.getId();
            long newExpMillis = newClaims.getExpiration() != null ? newClaims.getExpiration().getTime() : System.currentTimeMillis();
            try {
                refreshTokenDao.revokeByJti(jti);
            } catch (Exception e) {
                LoggerUtil.logWarning("AuthServlet", "Failed to revoke old refresh token jti=" + jti);
            }
            try {
                int userId = Integer.parseInt(userIdStr);
                refreshTokenDao.saveRefreshToken(newJti, userId, newJti, newExpMillis, ip, userAgent, claimDeviceId);
            } catch (Exception e) {
                LoggerUtil.logWarning("AuthServlet", "Failed to persist new refresh token for userId=" + userIdStr);
            }

            LoggerUtil.logSecurity("REFRESH_TOKEN_SUCCESS", username != null ? username : "UNKNOWN",
                    "Refresh token rotated successfully from IP: " + ip);

            Map<String, Object> respBody = new HashMap<>();
            respBody.put("status", "success");
            respBody.put("message", "Refresh token issued");
            respBody.put("refreshToken", newRefreshToken);
            respBody.put("expiresAt", newExpMillis);

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), respBody);
        } catch (Exception e) {
            LoggerUtil.logError("AuthServlet", "Error in handleRefreshToken: " + e.getMessage(), e);
            sendErrorResponse(resp, "An error occurred while refreshing the token", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> requestData = objectMapper.readValue(req.getInputStream(), new TypeReference<Map<String, String>>() {});
            String username = requestData.get("username");
            String email = requestData.get("email");
            String password = requestData.get("password");
            String requestedRole = requestData.get("role");
            String secretCode = requestData.get("secretCode");
            
            if (username == null || email == null || password == null) {
                LoggerUtil.logInfo("AuthServlet", "Registration attempt with missing fields from IP: " + req.getRemoteAddr());
                sendErrorResponse(resp, "Username, email, and password are required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            if (userDao.getUserByUsername(username) != null) {
                LoggerUtil.logInfo("AuthServlet", "Registration attempt with existing username: " + username + " from IP: " + req.getRemoteAddr());
                sendErrorResponse(resp, "Username already exists", HttpServletResponse.SC_CONFLICT);
                return;
            }
            
            // Check if this is an ADMIN role request
            if ("ADMIN".equals(requestedRole)) {
                // Validate requester has proper authorization
                String authHeader = req.getHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    sendErrorResponse(resp, "Authorization required for admin user creation", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                
                String token = authHeader.substring(7);
    
                try {
                    Claims claims = JwtUtil.parseToken(token);
                    String requesterRole = claims.get("role", String.class);
                    String requesterUsername = claims.get("username", String.class);
    
                    
                    // Only ADMIN or DEVELOPER can create admin users
                    if (!"ADMIN".equals(requesterRole) && !"DEVELOPER".equals(requesterRole)) {
                        LoggerUtil.logSecurity("UNAUTHORIZED_ADMIN_CREATION", requesterUsername, 
                            "Unauthorized attempt to create admin user from role: " + requesterRole + " IP: " + req.getRemoteAddr());
                        sendErrorResponse(resp, "Only ADMIN or DEVELOPER roles can create admin users", HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    
                    // Validate secret code
                    // if (secretCode == null) {
                    //     LoggerUtil.logSecurity("MISSING_SECRET_CODE", requesterUsername, 
                    //         "Missing secret code for admin creation from IP: " + req.getRemoteAddr());
                    //     sendErrorResponse(resp, "Secret code required for admin creation", HttpServletResponse.SC_BAD_REQUEST);
                    //     return;
                    // }
                    
                    String adminSecret = ConfigLoader.getString("admin.secret_code");
                    if (!adminSecret.equals(secretCode)) {
                        LoggerUtil.logSecurity("INVALID_SECRET_CODE", requesterUsername, 
                            "Invalid secret code for admin creation from IP: " + req.getRemoteAddr());
                        sendErrorResponse(resp, "Invalid secret code", HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    
                    // Generate OTP and send to admin email
                    String otpCode = adminOTPService.generateAndSendAdminOTP(
                        username, email, password, "ADMIN", requesterRole, req.getRemoteAddr()
                    );
                    
                    // Return pending response
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("status", "pending");
                    responseData.put("message", "OTP sent to admin email. Please verify to complete registration.");
                    responseData.put("otpRequired", true);
                    responseData.put("nextStep", "/api/auth/check-otp");
                    
                    resp.setContentType("application/json");
                    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                    objectMapper.writeValue(resp.getWriter(), responseData);
                    return;
                } catch (Exception e) {
        
                    LoggerUtil.logError("AuthServlet", "JWT token validation failed: " + e.getMessage(), e);
                    sendErrorResponse(resp, "Invalid authorization token", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
            
            // Handle regular user registration
            String role = "USER";
            String adminSecret = ConfigLoader.getString("admin.secret_code", "123456");
            if (adminSecret != null && adminSecret.equals(secretCode)) {
                role = "ADMIN";
            }
            
            User newUser = userDao.createUser(username, email, password, role);
            
            // Log successful registration
            LoggerUtil.logInfo("AuthServlet", "New user registered: " + username + " (" + email + ") with role: " + role + " from IP: " + req.getRemoteAddr());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("message", "Registration successful");
            responseData.put("user", Map.of(
                    "id", newUser.getId(),
                    "username", newUser.getUsername(),
                    "email", newUser.getEmail(),
                    "role", newUser.getRole()
            ));
            
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), responseData);
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("AuthServlet", "Registration failed due to data access issue: " + e.getMessage(), e);
            sendErrorResponse(resp, "Registration failed: Database error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LoggerUtil.logError("AuthServlet", "An unexpected error occurred during registration: " + e.getMessage(), e);
            sendErrorResponse(resp, "Registration failed: An unexpected error occurred", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // With JWT, logout is handled client-side by discarding the token
        LoggerUtil.logInfo("AuthServlet", "User logout from IP: " + req.getRemoteAddr());
        
        Map<String, String> responseData = new HashMap<>();
        responseData.put("status", "success");
        responseData.put("message", "Logout successful");
        
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), responseData);
    }
    
    private void handleCheckOTP(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> requestData = objectMapper.readValue(req.getInputStream(), new TypeReference<Map<String, String>>() {});
            String otpCode = requestData.get("otpCode");
            
            if (otpCode == null || otpCode.trim().isEmpty()) {
                sendErrorResponse(resp, "OTP code is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Validate OTP and get pending registration data
            AdminOTPService.PendingAdminRegistration pendingReg = 
                adminOTPService.validateAdminOTP(otpCode, req.getRemoteAddr());
            
            if (pendingReg == null) {
                sendErrorResponse(resp, "Invalid or expired OTP", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // Create the admin user
            User newUser = userDao.createUser(
                pendingReg.getUsername(), 
                pendingReg.getEmail(), 
                pendingReg.getPassword(), 
                pendingReg.getRole()
            );
            
            // Log successful admin creation
            LoggerUtil.logSecurity("ADMIN_USER_CREATED", pendingReg.getRequesterRole(), 
                "Admin user created: " + newUser.getUsername() + " (" + newUser.getEmail() + ") from IP: " + req.getRemoteAddr());
            
            // Return success response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("message", "Admin user created successfully");
            responseData.put("user", Map.of(
                    "id", newUser.getId(),
                    "username", newUser.getUsername(),
                    "email", newUser.getEmail(),
                    "role", newUser.getRole()
            ));
            
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), responseData);
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("AuthServlet", "OTP validation failed due to data access issue: " + e.getMessage(), e);
            sendErrorResponse(resp, "OTP validation failed: Database error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LoggerUtil.logError("AuthServlet", "An unexpected error occurred during OTP validation: " + e.getMessage(), e);
            sendErrorResponse(resp, "OTP validation failed: An unexpected error occurred", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleForgetPassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> requestData = objectMapper.readValue(req.getInputStream(), new TypeReference<Map<String, String>>() {});
            String username = requestData.get("username");
            String email = requestData.get("email");
            
            if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty()) {
                sendErrorResponse(resp, "Username and email are required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Verify user exists and email matches
            User user = userDao.getUserByUsername(username);
            if (user == null || !user.getEmail().equals(email)) {
                // Don't reveal if user exists or not for security
                LoggerUtil.logSecurity("PASSWORD_RESET_ATTEMPT", username, "Password reset attempt for non-existent user or email mismatch from IP: " + req.getRemoteAddr());
                sendSuccessResponse(resp, "If the username and email match our records, a password reset OTP has been sent.");
                return;
            }
            
            // Initialize password recovery service and send OTP
             PasswordRecoveryService recoveryService = new PasswordRecoveryService();
             String otpResult = recoveryService.initiatePasswordRecovery(username, email, req.getRemoteAddr());
             
             if (otpResult != null) {
                 LoggerUtil.logSecurity("PASSWORD_RESET_OTP_SENT", username, "Password reset OTP sent to: " + email + " from IP: " + req.getRemoteAddr());
                 sendSuccessResponse(resp, "Password reset OTP has been sent to your email.");
             } else {
                 LoggerUtil.logError("AuthServlet", "Failed to send password reset OTP for user: " + username, null);
                 sendErrorResponse(resp, "Failed to send password reset OTP. Please try again later.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             }
            
        } catch (Exception e) {
            LoggerUtil.logError("AuthServlet", "Error in handleForgetPassword: " + e.getMessage(), e);
            sendErrorResponse(resp, "An error occurred while processing your request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleVerifyResetOTP(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> requestData = null;
        try {
            requestData = objectMapper.readValue(req.getInputStream(), new TypeReference<Map<String, String>>() {});
            String otpCode = requestData.get("otpCode");
            String newPassword = requestData.get("newPassword");
            
            if (otpCode == null || otpCode.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
                sendErrorResponse(resp, "OTP code and new password are required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Verify OTP and reset password
             PasswordRecoveryService recoveryService = new PasswordRecoveryService();
             boolean resetSuccessful = recoveryService.verifyOTPAndResetPassword(otpCode, newPassword, req.getRemoteAddr());
            
            if (resetSuccessful) {
                LoggerUtil.logSecurity("PASSWORD_RESET_SUCCESS", "UNKNOWN", "Password reset completed successfully from IP: " + req.getRemoteAddr());
                sendSuccessResponse(resp, "Password has been reset successfully.");
            } else {
                LoggerUtil.logSecurity("PASSWORD_RESET_FAILED", "UNKNOWN", "Invalid or expired OTP for password reset from IP: " + req.getRemoteAddr());
                sendErrorResponse(resp, "Invalid or expired OTP. Please request a new password reset.", HttpServletResponse.SC_UNAUTHORIZED);
            }
            
        } catch (Exception e) {
            LoggerUtil.logError("AuthServlet", "Error in handleVerifyResetOTP: " + e.getMessage(), e);
            sendErrorResponse(resp, "An error occurred while processing your request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleTokenValidation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Extract token from Authorization header
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                LoggerUtil.logSecurity("TOKEN_VALIDATION_FAILED", "UNKNOWN", "Missing or invalid Authorization header from IP: " + req.getRemoteAddr());
                sendErrorResponse(resp, "Authorization header is required", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Validate token using JwtUtil
            JwtUtil.TokenValidationResult validationResult = JwtUtil.validateToken(token);
            
            if (!validationResult.isValid()) {
                LoggerUtil.logSecurity("TOKEN_VALIDATION_FAILED", "UNKNOWN", "Invalid token validation: " + validationResult.getErrorMessage() + " from IP: " + req.getRemoteAddr());
                sendErrorResponse(resp, validationResult.getErrorMessage(), HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            Claims claims = validationResult.getClaims();
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            
            // Verify user still exists in database
            UserDAO userDao = new UserDAO();
            User user = userDao.getUserById(Integer.parseInt(userId));
            if (user == null) {
                LoggerUtil.logSecurity("TOKEN_VALIDATION_FAILED", username, "Token validation failed - user not found (ID: " + userId + ") from IP: " + req.getRemoteAddr());
                sendErrorResponse(resp, "User not found", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // Log successful validation
            LoggerUtil.logSecurity("TOKEN_VALIDATION_SUCCESS", username, "Token validation successful (ID: " + userId + ") from IP: " + req.getRemoteAddr());
            
            // Return success response with user info
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("message", "Token is valid");
            responseData.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole()
            ));
            
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), responseData);
            
        } catch (Exception e) {
            LoggerUtil.logError("AuthServlet", "Error in handleTokenValidation: " + e.getMessage(), e);
            LoggerUtil.logSecurity("TOKEN_VALIDATION_ERROR", "UNKNOWN", "Token validation error: " + e.getMessage() + " from IP: " + req.getRemoteAddr());
            sendErrorResponse(resp, "An error occurred while validating the token", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void sendSuccessResponse(HttpServletResponse resp, String message) throws IOException {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), response);
    }
    
    private void sendErrorResponse(HttpServletResponse resp, String message, int statusCode) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        
        resp.setContentType("application/json");
        resp.setStatus(statusCode);
        objectMapper.writeValue(resp.getWriter(), errorResponse);
    }
}