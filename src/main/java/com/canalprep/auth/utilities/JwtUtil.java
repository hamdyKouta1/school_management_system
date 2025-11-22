package com.canalprep.auth.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.canalprep.config.ConfigLoader;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = getSecretKey();
    private static final long EXPIRATION_TIME = ConfigLoader.getLong("jwt.expiration_ms", 30 * 60 * 1000); // Default: 30 minutes
    private static final long REFRESH_EXPIRATION_TIME = ConfigLoader.getLong("jwt.refresh_expiration_ms", 7L * 24 * 60 * 60 * 1000); // Default: 7 days

    private static SecretKey getSecretKey() {
        // Load JWT secret from configuration with fallback to environment variable
        String secret = ConfigLoader.getString("jwt.secret");
        if (secret == null || secret.isEmpty()) {
            // Fallback to environment variable for backward compatibility
            secret = System.getenv("JWT_SECRET");
        }
       if (secret == null || secret.isEmpty()) {
        throw new IllegalStateException("JWT secret must be configured in production");
    }
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public static String generateToken(String userId, String username, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("email", email);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId) // Set the subject to userId for proper validation
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Generate a refresh token with distinct token type and JWT ID (jti)
     */
    public static String generateRefreshToken(String userId, String username, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "refresh");
        if (deviceId != null && !deviceId.isEmpty()) {
            claims.put("deviceId", deviceId);
        }

        String jti = java.util.UUID.randomUUID().toString();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Validates a JWT token and returns detailed validation result
     * @param token The JWT token to validate
     * @return TokenValidationResult containing validation status and details
     */
    public static TokenValidationResult validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return new TokenValidationResult(false, "Token is null or empty", null);
            }
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Check if token is expired
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                return new TokenValidationResult(false, "Token has expired", null);
            }
            
            // Validate required claims
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            
            if (userId == null || userId.trim().isEmpty()) {
                return new TokenValidationResult(false, "Token missing user ID", null);
            }
            
            if (username == null || username.trim().isEmpty()) {
                return new TokenValidationResult(false, "Token missing username", null);
            }
            
            return new TokenValidationResult(true, "Token is valid", claims);
            
        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(false, "Token has expired", null);
        } catch (MalformedJwtException e) {
            return new TokenValidationResult(false, "Token is malformed", null);
        } catch (SignatureException e) {
            return new TokenValidationResult(false, "Token signature is invalid", null);
        } catch (UnsupportedJwtException e) {
            return new TokenValidationResult(false, "Token is unsupported", null);
        } catch (IllegalArgumentException e) {
            return new TokenValidationResult(false, "Token is invalid", null);
        } catch (JwtException e) {
            return new TokenValidationResult(false, "Token validation failed: " + e.getMessage(), null);
        } catch (Exception e) {
            return new TokenValidationResult(false, "Unexpected error during token validation", null);
        }
    }

    /**
     * Validates a refresh JWT token (tokenType == refresh) and returns detailed validation result
     */
    public static TokenValidationResult validateRefreshToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return new TokenValidationResult(false, "Token is null or empty", null);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Ensure token type is refresh
            String tokenType = claims.get("tokenType", String.class);
            if (tokenType == null || !"refresh".equals(tokenType)) {
                return new TokenValidationResult(false, "Invalid token type for refresh", null);
            }

            // Check expiration
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                return new TokenValidationResult(false, "Token has expired", null);
            }

            // Basic claim checks
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            if (userId == null || userId.trim().isEmpty()) {
                return new TokenValidationResult(false, "Token missing user ID", null);
            }
            if (username == null || username.trim().isEmpty()) {
                return new TokenValidationResult(false, "Token missing username", null);
            }

            return new TokenValidationResult(true, "Token is valid", claims);

        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(false, "Token has expired", null);
        } catch (MalformedJwtException e) {
            return new TokenValidationResult(false, "Token is malformed", null);
        } catch (SignatureException e) {
            return new TokenValidationResult(false, "Token signature is invalid", null);
        } catch (UnsupportedJwtException e) {
            return new TokenValidationResult(false, "Token is unsupported", null);
        } catch (IllegalArgumentException e) {
            return new TokenValidationResult(false, "Token is invalid", null);
        } catch (JwtException e) {
            return new TokenValidationResult(false, "Token validation failed: " + e.getMessage(), null);
        } catch (Exception e) {
            return new TokenValidationResult(false, "Unexpected error during token validation", null);
        }
    }

    /**
     * Extract JWT ID (jti) from a token
     */
    public static String getTokenId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getId();
    }
    
    /**
     * Result class for token validation operations
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final Claims claims;
        
        public TokenValidationResult(boolean valid, String errorMessage, Claims claims) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.claims = claims;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public Claims getClaims() {
            return claims;
        }
    }
}