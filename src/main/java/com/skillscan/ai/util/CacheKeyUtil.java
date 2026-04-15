package com.skillscan.ai.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CacheKeyUtil {

    public static String hashKey(String... parts) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (String part : parts) {
                if (part != null) {
                    md.update(part.getBytes(StandardCharsets.UTF_8));
                }
            }
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
