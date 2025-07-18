package com.canalprep.auth.filter;

import com.canalprep.auth.utilities.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter({"/api/protected/*", "/api/admin/*"})
public class AuthenticationFilter implements Filter {
    
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
        
        // Get token from Authorization header
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(httpResponse, "Authorization header missing", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            Claims claims = JwtUtil.parseToken(token);
            
            // Set user attributes from token claims
            httpRequest.setAttribute("userId", claims.get("userId", String.class));
            httpRequest.setAttribute("username", claims.get("username", String.class));
            httpRequest.setAttribute("email", claims.get("email", String.class));
            httpRequest.setAttribute("role", claims.get("role", String.class));
            
            // Check admin privileges for admin endpoints
            if (path.startsWith("/api/admin/") && !"ADMIN".equals(claims.get("role"))) {
                sendErrorResponse(httpResponse, "Admin privileges required", HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // Add security headers
            addSecurityHeaders(httpResponse);
            
            chain.doFilter(request, response);
            
        } catch (ExpiredJwtException e) {
            sendErrorResponse(httpResponse, "Token expired", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (SignatureException e) {
            sendErrorResponse(httpResponse, "Invalid token", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            sendErrorResponse(httpResponse, "Authentication failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write("{\"status\":\"error\",\"message\":\"" + message + "\"}");
    }
    
    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.setHeader("Content-Security-Policy", "default-src 'self'");
    }
    
    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}