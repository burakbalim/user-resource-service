package com.thales.user_resource_service.service;

import com.thales.user_resource_service.dto.CreateTempUserResponse;
import com.thales.user_resource_service.dto.TempUserCreateRequest;
import com.thales.user_resource_service.dto.TempVerifyUserRequest;
import com.thales.user_resource_service.dto.UserResponse;

/**
 * Service interface for temporary user operations.
 */
public interface TempUserService {

    /**
     * Creates a temporary user and stores it in Redis cache with a validation code.
     * The validation code is sent to the user's email instead of returning it directly.
     *
     * @param request The temporary user creation request
     * @return Response with success message
     */
    CreateTempUserResponse createTempUser(TempUserCreateRequest request);

    /**
     * Verifies a user using the validation code and creates permanent user in the database.
     *
     * @param request The verification request containing validation code
     * @return The created user response
     */
    UserResponse verifyAndCreateUser(TempVerifyUserRequest request);
} 