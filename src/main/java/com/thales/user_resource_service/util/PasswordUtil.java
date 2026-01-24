package com.thales.user_resource_service.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Helper class for password operations.
 * Used for simple password encoding operations without Spring Security.
 */
public class PasswordUtil {

    /**
     * Encodes the given password with SHA-256 algorithm.
     *
     * @param password Password to encode
     * @return Encoded password
     */
    public static String encode(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password encoding error", e);
        }
    }

    /**
     * Checks if the raw password matches the encoded password.
     *
     * @param rawPassword Raw password
     * @param encodedPassword Encoded password
     * @return Match status
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        String encodedRawPassword = encode(rawPassword);
        return encodedRawPassword.equals(encodedPassword);
    }
} 