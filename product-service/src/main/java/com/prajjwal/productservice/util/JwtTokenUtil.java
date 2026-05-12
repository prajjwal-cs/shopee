package com.prajjwal.productservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extraClaim(token, Claims::getSubject);
    }

    public List<SimpleGrantedAuthority> extractRoles(String token) {
        Claims claims = getAllClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof String roleStr && !roleStr.isBlank()) {
            return Stream.of(roleStr.split(","))
                    .map(String::trim)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }
        return List.of();
    }

    private boolean isTokenExpired(String token) {
        return extraClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(getAllClaims(token));
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}