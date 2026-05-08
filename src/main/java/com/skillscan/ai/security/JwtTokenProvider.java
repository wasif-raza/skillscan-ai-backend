package com.skillscan.ai.security;

import com.skillscan.ai.exception.JwtValidationException;
import com.skillscan.ai.model.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessExp;

    @Value("${jwt.refresh.expiration}")
    private long refreshExp;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateAccessToken(UUID userId,String email, UserRole role) {
        return buildToken(userId,email, role, accessExp, "access");
    }

    public String generateRefreshToken(UUID userId,String email, UserRole role) {
        return buildToken(userId,email, role, refreshExp, "refresh");
    }

    private String buildToken(UUID userId,String email, UserRole role, long exp, String type) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userId.toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(new Date(now.getTime() + exp))
                .claim("type", type)
                .claim("role", role.name())
                .claim("email",email)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parse(token).getSubject()); 
    }

    public UserRole getRole(String token) {
        return UserRole.valueOf(parse(token).get("role", String.class));
    }

    public String getEmail(String token) {return parse(token).get("email", String.class);}

    public Date getExpiration(String token) {
        return parse(token).getExpiration();
    }

    public long getRemainingTime(String token) {
        Date expiration = getExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    public void validate(String token, String expectedType) {
        Claims c = parse(token);

        if (!expectedType.equals(c.get("type"))) {
            throw new JwtException("Invalid token type");
        }

        if (!issuer.equals(c.getIssuer())) {
            throw new JwtValidationException("Invalid issuer");
        }

        if (!c.getAudience().contains(audience)) {
            throw new JwtValidationException("Invalid audience");
        }
    }
}