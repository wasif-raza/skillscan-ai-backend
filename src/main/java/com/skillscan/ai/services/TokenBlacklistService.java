package com.skillscan.ai.services;


public interface TokenBlacklistService {

    void blacklistToken(String token);

    boolean isBlacklisted(String token);
}
