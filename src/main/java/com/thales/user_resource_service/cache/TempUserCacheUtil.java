package com.thales.user_resource_service.cache;

import com.thales.common.cache.StaticCacheUtil;
import com.thales.user_resource_service.dto.TempUserCreateRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Utility class for handling temporary user data in cache.
 */
@Slf4j
public class TempUserCacheUtil {

    private static final String TEMP_USERS_CACHE = "tempUsers";
    private static final int DEFAULT_EXPIRY_MINUTES = 30;

    private TempUserCacheUtil() {
        // Utility class, private constructor
    }

    /**
     * Stores a temporary user in cache with a generated validation code.
     *
     * @param request The temporary user creation request
     * @return The generated validation code
     */
    public static String storeTempUser(TempUserCreateRequest request) {
        String validationCode = generateValidationCode();

        Map<String, Object> userData = new HashMap<>();
        userData.put("user", request);
        userData.put("createdAt", LocalDateTime.now());
        userData.put("expiryMinutes", DEFAULT_EXPIRY_MINUTES);

        StaticCacheUtil.put(TEMP_USERS_CACHE, validationCode, userData);
        log.debug("Temporary user stored in cache for: {}", request.getEmail());

        return validationCode;
    }

    /**
     * Retrieves a temporary user by validation code.
     *
     * @param validationCode The validation code
     * @return The temporary user request if found and not expired, otherwise empty
     */
    @SuppressWarnings("unchecked")
    public static Optional<TempUserCreateRequest> getTempUser(String validationCode) {
        Optional<Map> userDataOpt = StaticCacheUtil.get(
                TEMP_USERS_CACHE,
                validationCode,
                Map.class);

        if (userDataOpt.isEmpty()) {
            log.warn("No temporary user found with validation code: {}", validationCode);
            return Optional.empty();
        }

        Map<String, Object> userData = (Map<String, Object>) userDataOpt.get();
        LocalDateTime createdAt = (LocalDateTime) userData.get("createdAt");
        int expiryMinutes = (int) userData.get("expiryMinutes");

        if (createdAt.plusMinutes(expiryMinutes).isBefore(LocalDateTime.now())) {
            log.warn("Temporary user with validation code {} has expired", validationCode);
            removeTempUser(validationCode);
            return Optional.empty();
        }

        return Optional.ofNullable((TempUserCreateRequest) userData.get("user"));
    }

    /**
     * Removes a temporary user from cache.
     *
     * @param validationCode The validation code
     */
    public static void removeTempUser(String validationCode) {
        StaticCacheUtil.evict(TEMP_USERS_CACHE, validationCode);
        log.debug("Temporary user removed with validation code: {}", validationCode);
    }

    /**
     * Generates a random validation code.
     *
     * @return A random validation code (6 digits)
     */
    public static String generateValidationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Gets the default expiry time in minutes.
     *
     * @return The default expiry time
     */
    public static int getDefaultExpiryMinutes() {
        return DEFAULT_EXPIRY_MINUTES;
    }
}
