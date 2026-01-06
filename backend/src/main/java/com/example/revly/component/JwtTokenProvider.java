package com.example.revly.component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Secure key for signing
    private final long accessTokenValidity = 3600000; // 1 hour
    private final long refreshTokenValidity = 604800000; // 7 days

    // Generate access token
    public String createAccessToken(String email, int userId) {
        return createToken(email, userId, accessTokenValidity);
    }

    // Generate refresh token
    public String createRefreshToken(String email, int userId) {
        return createToken(email, userId, refreshTokenValidity);
    }

    // Common token creation logic
    private String createToken(String email, int userId, long validity) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Get email from token
    public String getEmail(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // Get userId from token
    public Integer getUserId(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody().get("userId", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }
}