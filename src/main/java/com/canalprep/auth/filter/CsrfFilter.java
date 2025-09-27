package com.canalprep.auth.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebFilter(urlPatterns = "/api/*")
public class CsrfFilter implements Filter {

    private static final Logger logger = Logger.getLogger(CsrfFilter.class.getName());
    private static final String ALLOWED_ORIGIN = "*";//"http://localhost:5173";//System.getenv("ALLOWED_ORIGIN"); // e.g., "http://localhost:3000"

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Only apply CSRF checks to non-GET requests
        if (!httpRequest.getMethod().equalsIgnoreCase("GET")) {
            String origin = httpRequest.getHeader("Origin");
            String referer = httpRequest.getHeader("Referer");

            // For simplicity, we'll check Origin header. For production, more robust checks are needed.
            // This assumes a single allowed origin for the frontend.
            if (ALLOWED_ORIGIN == null || ALLOWED_ORIGIN.isEmpty()) {
                logger.log(Level.WARNING, "ALLOWED_ORIGIN environment variable is not set. CSRF protection may be incomplete.");
                // For development, might allow if not set, but in production, this should be an error.
            } else if (origin == null || !origin.equals(ALLOWED_ORIGIN)) {
                logger.log(Level.WARNING, "CSRF attack detected: Invalid Origin header. Origin: " + origin);
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Origin header");
                return;
            }

            // Optionally, check Referer header as a fallback or additional layer
            // Referer can be more easily spoofed or absent, so Origin is preferred.
            if (referer != null && !referer.startsWith(ALLOWED_ORIGIN)) {
                logger.log(Level.WARNING, "CSRF attack detected: Invalid Referer header. Referer: " + referer);
                // Depending on strictness, you might block here or just log.
                // For now, we'll rely primarily on Origin if set.
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}


