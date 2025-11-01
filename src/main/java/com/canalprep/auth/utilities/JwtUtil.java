package com.canalprep.auth.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.canalprep.config.ConfigLoader;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = getSecretKey();
    private static final long EXPIRATION_TIME = ConfigLoader.getLong("jwt.expiration_ms", 30 * 60 * 1000); // Default: 30 minutes

    private static SecretKey getSecretKey() {
        // Load JWT secret from configuration with fallback to environment variable
        String secret = ConfigLoader.getString("jwt.secret");
        if (secret == null || secret.isEmpty()) {
            // Fallback to environment variable for backward compatibility
            secret = System.getenv("JWT_SECRET");
        }
        if (secret == null || secret.isEmpty()) {
            // For development/testing, use a fixed key if not provided
            // In production, this should be a critical error or a pre-configured key
            secret = "development-jwt-secret-key-for-testing-only-not-for-production-use";
        }
        // Use a fixed key derived from the secret
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
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
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
}