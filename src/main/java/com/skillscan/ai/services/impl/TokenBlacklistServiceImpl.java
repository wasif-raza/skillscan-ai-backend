package com.skillscan.ai.services.impl;

import com.skillscan.ai.exception.TokenBlacklistException;
import com.skillscan.ai.exception.TokenHashingException;
import com.skillscan.ai.security.JwtTokenProvider;
import com.skillscan.ai.services.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwt;

    private static final String PREFIX = "auth:blacklist:";

    @Override
    public void blacklistToken(String token) {

        if (token == null || token.isBlank()) {
            return;
        }

        long expiry;


        try {
            expiry = jwt.getRemainingTime(token);

            // Token already expired → nothing to blacklist
            if (expiry <= 0) {
                log.debug("Token already expired, skipping blacklist");
                return;
            }

        } catch (Exception e) {
            // Invalid / expired token → not a failure case
            log.warn("Invalid or expired token during logout", e);
            return;
        }


        try {
            String key = PREFIX + hashToken(token);

            redisTemplate.opsForValue().set(
                    key,
                    "1",
                    expiry,
                    TimeUnit.MILLISECONDS
            );

        } catch (Exception e) {
            log.error("Redis failure while blacklisting token", e);
            throw new TokenBlacklistException(
                    "Token could not be invalidated. Please try again."
            );
        }
    }

    @Override
    public boolean isBlacklisted(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            String key = PREFIX + hashToken(token);
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));

        } catch (Exception e) {

            log.warn("Blacklist check failed", e);
            throw new TokenBlacklistException(
                    "Authentication service unavailable"
            );
        }
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {
            log.error("Token hashing failed", e);
            throw new TokenHashingException(
                    "Failed to process authentication token"
            );
        }
    }
}