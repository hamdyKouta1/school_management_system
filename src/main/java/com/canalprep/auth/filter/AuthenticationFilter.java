package com.canalprep.auth.filter;

import com.canalprep.auth.utilities.JwtUtil;
import com.canalprep.license.service.LicenseService;
import com.canalprep.license.model.License;
import com.canalprep.exception.DataAccessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebFilter({"/api/protected/*", "/api/admin/*"})
public class AuthenticationFilter implements Filter {
    private static final Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());
    
    // License-allowed endpoints when license is expired
    private static final String[] LICENSE_ALLOWED_ENDPOINTS = {
        "/api/auth",
        "/api/protected/login",
        "/api/protected/licence",
        "/api/protected/licence/renew", 
        "/api/protected/licence/remove",
        "/api/protected/otp"
    };
    
    // Endpoints that don't require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/check-otp",
        "/api/auth/logout"
    };
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Get the requested path
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        
        // Skip authentication for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check license status first - if expired, only allow license-related endpoints
        if (!isLicenseEndpointAllowed(path)) {
            try {
                LicenseService licenseService = new LicenseService();
                License currentLicense = licenseService.getCurrentLicense();
                
                if (currentLicense == null || !currentLicense.isValid()) {
                    // Log endpoint restriction due to expired license
                    String clientIP = getClientIP(httpRequest);
                    String userAgent = httpRequest.getHeader("User-Agent");
                    logger.warning(String.format("ENDPOINT_RESTRICTED_LICENSE_EXPIRED - Access to endpoint '%s' restricted due to expired license. Client IP: %s, User-Agent: %s", 
                        path, clientIP, userAgent != null ? userAgent : "unknown"));
                    
                    sendLicenseExpiredRedirect(httpResponse);
                    return;
                }
            } catch (DataAccessException e) {
                logger.log(Level.SEVERE, "Failed to validate license for endpoint access", e);
                sendLicenseExpiredRedirect(httpResponse);
                return;
            }
        }
        
        // Get token from Authorization header
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(httpResponse, "Authorization header missing", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            Claims claims = JwtUtil.parseToken(token);
            
            // Debug logging
            logger.log(Level.INFO, "Token parsed successfully for user: " + claims.get("username") + ", role: " + claims.get("role") + ", path: " + path);
            
            // License validation is now handled earlier in the filter
            // Role-based access control for license endpoints
            String userRole = claims.get("role", String.class);
            
            // Check role-based access for OTP endpoints
            if (path.startsWith("/api/protected/otp/")) {
                if (path.startsWith("/api/protected/otp/developer") && !"DEVELOPER".equals(userRole)) {
                    sendErrorResponse(httpResponse, "Developer role required for developer OTP endpoints", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                if (path.startsWith("/api/protected/otp/admin") && !"ADMIN".equals(userRole)) {
                    sendErrorResponse(httpResponse, "Admin role required for admin OTP endpoints", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
            // Check role-based access for license management endpoints
            if (path.startsWith("/api/protected/licence/") && !"DEVELOPER".equals(userRole) && !"ADMIN".equals(userRole)) {
                sendErrorResponse(httpResponse, "Developer or Admin role required for license management", HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // Set user attributes from token claims
            httpRequest.setAttribute("userId", claims.get("userId", String.class));
            httpRequest.setAttribute("username", claims.get("username", String.class));
            httpRequest.setAttribute("email", claims.get("email", String.class));
            httpRequest.setAttribute("role", claims.get("role", String.class));
            
            // Check admin privileges for admin endpoints (allow both ADMIN and DEVELOPER)
            if (path.startsWith("/api/admin/") && !"ADMIN".equals(claims.get("role")) && !"DEVELOPER".equals(claims.get("role"))) {
                sendErrorResponse(httpResponse, "Admin or Developer privileges required", HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // Add security headers
            addSecurityHeaders(httpResponse);
            
            // Debug logging before passing to servlet
            logger.log(Level.INFO, "Passing request to servlet for path: " + path + ", user: " + claims.get("username"));
            
            chain.doFilter(request, response);
            
        } catch (ExpiredJwtException e) {
            logger.log(Level.WARNING, "Token expired for user: " + e.getClaims().getSubject());
            sendErrorResponse(httpResponse, "Token expired", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (SignatureException e) {
            logger.log(Level.WARNING, "Invalid JWT signature: " + e.getMessage());
            sendErrorResponse(httpResponse, "Invalid token", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Authentication failed: " + e.getMessage(), e);
            sendErrorResponse(httpResponse, "Authentication failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write("{\"status\":\"error\",\"message\":\"" + message + "\"}");
    }
    
    /**
     * Check if the requested endpoint is allowed when license is expired
     */
    private boolean isLicenseEndpointAllowed(String path) {
        for (String allowedEndpoint : LICENSE_ALLOWED_ENDPOINTS) {
            if (path.startsWith(allowedEndpoint)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if the requested endpoint is public (doesn't require authentication)
     */
    private boolean isPublicEndpoint(String path) {
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (path.equals(publicEndpoint)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Send redirect response to license endpoint when license is expired
     */
    private void sendLicenseExpiredRedirect(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(
            "{\"status\":\"error\"," +
            "\"message\":\"License expired. Access restricted to license management endpoints only.\"," +
            "\"redirect\":\"/api/protected/licence\"," +
            "\"allowedEndpoints\":[\"/api/auth\",\"/api/protected/login\",\"/api/protected/licence\",\"/api/protected/licence/renew\",\"/api/protected/licence/remove\",\"/api/protected/otp\"]}"
        );
    }
    
    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.setHeader("Content-Security-Policy", "default-src 'self'");
        
    }
    
    /**
     * Extract client IP address from request
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
    
    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}