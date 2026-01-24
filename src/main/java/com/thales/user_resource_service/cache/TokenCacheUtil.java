package com.thales.user_resource_service.cache;

import com.thales.common.cache.StaticCacheUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for handling user verification and reset tokens in Redis cache.
 */
@Slf4j
public class TokenCacheUtil {

    private static final String VERIFICATION_TOKENS_CACHE = "verificationTokens";
    private static final String RESET_TOKENS_CACHE = "resetTokens";
    public static final int DEFAULT_EXPIRY_HOURS = 24;

    private TokenCacheUtil() {
        // Utility class, private constructor
    }

    /**
     * Removes a verification token for the given email address.
     *
     * @param email The user's email address
     */
    public static void removeVerificationToken(String email) {
        String cacheKey = buildVerificationCacheKey(email);
        StaticCacheUtil.evict(VERIFICATION_TOKENS_CACHE, cacheKey);
        log.debug("Verification token removed for: {}", email);
    }

    /**
     * Creates a password reset token for the given email address.
     *
     * @param email The user's email address
     * @return The generated reset token
     */
    public static String createResetToken(String email) {
        String token = generateRandomToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(DEFAULT_EXPIRY_HOURS);

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("token", token);
        tokenInfo.put("expiry", expiryDate);
        tokenInfo.put("email", email);

        String cacheKey = buildResetCacheKey(email);
        StaticCacheUtil.put(RESET_TOKENS_CACHE, cacheKey, tokenInfo);
        log.debug("Reset token created for: {}", email);

        return token;
    }

    /**
     * Retrieves and validates a reset token for the given email.
     *
     * @param email The user's email address
     * @param token The reset token to validate
     * @return true if the token is valid, false otherwise
     */
    @SuppressWarnings("unchecked")
    public static boolean validateResetToken(String email, String token) {
        String cacheKey = buildResetCacheKey(email);

        Optional<Map> tokenInfoOpt = StaticCacheUtil.get(
                RESET_TOKENS_CACHE,
                cacheKey,
                Map.class);

        if (tokenInfoOpt.isEmpty()) {
            log.warn("No reset token found for email: {}", email);
            return false;
        }

        Map<String, Object> tokenInfo = (Map<String, Object>) tokenInfoOpt.get();
        String storedToken = (String) tokenInfo.get("token");
        LocalDateTime expiryDate = (LocalDateTime) tokenInfo.get("expiry");

        if (!token.equals(storedToken)) {
            log.warn("Invalid reset token for email: {}", email);
            return false;
        }

        if (expiryDate.isBefore(LocalDateTime.now())) {
            log.warn("Expired reset token for email: {}", email);
            return false;
        }

        return true;
    }

    /**
     * Removes a reset token for the given email address.
     *
     * @param email The user's email address
     */
    public static void removeResetToken(String email) {
        String cacheKey = buildResetCacheKey(email);
        StaticCacheUtil.evict(RESET_TOKENS_CACHE, cacheKey);
        log.debug("Reset token removed for: {}", email);
    }

    /**
     * Generates a random token.
     *
     * @return A random UUID as string
     */
    private static String generateRandomToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Builds a cache key for verification tokens.
     *
     * @param email The user's email address
     * @return The cache key
     */
    private static String buildVerificationCacheKey(String email) {
        return "verify:" + email;
    }

    /**
     * Builds a cache key for reset tokens.
     *
     * @param email The user's email address
     * @return The cache key
     */
    private static String buildResetCacheKey(String email) {
        return "reset:" + email;
    }
}
