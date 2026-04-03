package com.prajjwal.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:84600000}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        return createToken(Map.of(), userDetails);
    }

    public String createToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extraClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
            return extractUsername(token).equals(userDetails.getUsername())
                    && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extraClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}