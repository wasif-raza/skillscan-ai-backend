package com.skillscan.ai.services.impl;

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

        try {
            long expiry = jwt.getRemainingTime(token);

            //  fallback to avoid failure on expired token
            if (expiry <= 0) {
                expiry = 60_000; // 1 minute
            }

            String key = PREFIX + hashToken(token);

            redisTemplate.opsForValue().set(
                    key,
                    "1",
                    expiry,
                    TimeUnit.MILLISECONDS
            );

        } catch (Exception e) {

            log.warn("Failed to blacklist token: {}", e.getMessage());
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
            log.warn("Blacklist check failed: {}", e.getMessage());
            return false;
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
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}