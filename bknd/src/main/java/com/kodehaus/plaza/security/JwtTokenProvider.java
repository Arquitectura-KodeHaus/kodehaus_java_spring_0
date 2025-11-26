package com.kodehaus.plaza.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
// Lombok annotations removed for compatibility
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.kodehaus.plaza.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * Generate JWT token from authentication
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        // try to cast to our User entity to include plaza information
        Long plazaId = null;
        String plazaName = null;
        java.util.UUID plazaUuid = null;
        List<String> roles = Collections.emptyList();
        Long userId = null;
        if (userPrincipal instanceof User) {
            User u = (User) userPrincipal;
            userId = u.getId();
            if (u.getPlaza() != null) {
                plazaId = u.getPlaza().getId();
                plazaName = u.getPlaza().getName();
                plazaUuid = u.getPlaza().getUuid();
            }
            if (u.getRoles() != null && !u.getRoles().isEmpty()) {
                roles = u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList());
            }
        }
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        JwtBuilder builder = Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey());

        builder.claim("roles", roles);
        if (userId != null) builder.claim("userId", userId);
        if (plazaId != null) builder.claim("plazaId", plazaId);
        if (plazaName != null) builder.claim("plazaName", plazaName);
        if (plazaUuid != null) builder.claim("plazaUuid", plazaUuid.toString());

        return builder.compact();
    }
    
    /**
     * Generate JWT token from username
     */
    public String generateTokenFromUsername(String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
        
    return claims.getSubject();
    }

    public Long getPlazaIdFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    Object val = claims.get("plazaId");
    if (val instanceof Number) return ((Number) val).longValue();
    if (val instanceof String) try { return Long.parseLong((String) val); } catch (Exception e) { return null; }
    return null;
    }

    public String getPlazaNameFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
    return claims.get("plazaName", String.class);
    }

    public java.util.UUID getPlazaUuidFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
    String s = claims.get("plazaUuid", String.class);
    if (s == null) return null;
    try { return java.util.UUID.fromString(s); } catch (Exception e) { return null; }
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException ex) {
            System.out.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            System.out.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.out.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.out.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.out.println("JWT claims string is empty");
        }
        return false;
    }
    
    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getExpiration();
    }
    
    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Verify Google ID Token and return email
     */
    public String getEmailFromGoogleToken(String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .build();
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                return idToken.getPayload().getEmail();
            }
        } catch (Exception e) {
            // Token invalid or verification failed
        }
        return null;
    }
}
