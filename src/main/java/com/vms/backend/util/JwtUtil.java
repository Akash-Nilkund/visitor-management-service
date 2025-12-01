package com.vms.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

@Component
public class JwtUtil {

    // --- Use strong stable signing key (Do NOT recreate on each restart) ---
    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ---------- Extract Username ----------
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ---------- Extract Expiration ----------
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ---------- Generic Claim Extract ----------
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    // ---------- Parse & Validate Token ----------
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ---------- Check Expiry ----------
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ---------- Generate Token ----------
    public String generateToken(UserDetails userDetails) {
        return createToken(new HashMap<>(), userDetails.getUsername());
    }

    // ---------- Token Builder ----------
    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (10 * 60 * 60 * 1000))) // 10 hours
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ---------- Validate Token ----------
    public boolean validateToken(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractUsername(token))
                && !isTokenExpired(token);
    }
}
