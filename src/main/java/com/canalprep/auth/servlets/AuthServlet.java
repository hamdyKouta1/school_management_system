package com.canalprep.auth.servlets;

import com.canalprep.auth.utilities.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.canalprep.auth.dao.UserDAO;
import com.canalprep.auth.model.User;
import com.canalprep.auth.utilities.PasswordUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private final UserDAO userDao = new UserDAO();
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
            case "/logout":
                handleLogout(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }
    
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> requestData = objectMapper.readValue(req.getInputStream(), Map.class);
            String username = requestData.get("username");
            String password = requestData.get("password");
            
            if (username == null || password == null) {
                sendErrorResponse(resp, "Username and password are required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            User user = userDao.getUserByUsername(username);
           System.out.println(user.getPasswordHash());
             System.out.println("at login servlet    "+PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash()));
            System.out.println(user.getEmail()+" "+user.getPasswordHash()+" "+user.getSalt()+" "+user.getUsername());
            if (user == null || !PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
                sendErrorResponse(resp, "Invalid username or password", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // Update last login
            userDao.updateLastLogin(user.getId());
            
            // Generate JWT token instead of using session
            String token = JwtUtil.generateToken(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
            );
            
            // Prepare response with token
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("message", "Login successful");
            responseData.put("token", token);
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
            sendErrorResponse(resp, "Login failed: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> requestData = objectMapper.readValue(req.getInputStream(), Map.class);
            String username = requestData.get("username");
            String email = requestData.get("email");
            String password = requestData.get("password");
            String role = "USER";
            
            if (username == null || email == null || password == null) {
                sendErrorResponse(resp, "Username, email, and password are required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            if (userDao.getUserByUsername(username) != null) {
                sendErrorResponse(resp, "Username already exists", HttpServletResponse.SC_CONFLICT);
                return;
            }
            
            String secretCode = requestData.get("secretCode");
            if ("ADMIN_SECRET_123".equals(secretCode)) {
                role = "ADMIN";
            }
            
           User newUser = userDao.createUser(username, email, password, role);
            
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
            
        } catch (SQLException e) {
            sendErrorResponse(resp, "Registration failed: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // With JWT, logout is handled client-side by discarding the token
        Map<String, String> responseData = new HashMap<>();
        responseData.put("status", "success");
        responseData.put("message", "Logout successful");
        
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), responseData);
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