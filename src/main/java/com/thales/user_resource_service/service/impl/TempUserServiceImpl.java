package com.thales.user_resource_service.service.impl;

import com.thales.user_resource_service.cache.TempUserCacheUtil;
import com.thales.user_resource_service.dto.*;
import com.thales.user_resource_service.exception.ResourceAlreadyExistsException;
import com.thales.user_resource_service.exception.ResourceNotFoundException;
import com.thales.user_resource_service.service.EmailService;
import com.thales.user_resource_service.service.TempUserService;
import com.thales.user_resource_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of TempUserService for managing temporary users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TempUserServiceImpl implements TempUserService {

    private final UserService userService;
    private final EmailService emailService;

    /**
     * Creates a temporary user and stores it in Redis cache with a validation code.
     * The validation code is sent to the user's email instead of returning it directly.
     *
     * @param request The temporary user creation request
     * @return Response with success message
     */
    @Override
    public CreateTempUserResponse createTempUser(TempUserCreateRequest request) {
        log.info("Creating temporary user for: {}", request.getEmail());

        validateUser(request);

        String validationCode = TempUserCacheUtil.storeTempUser(request);

        sendVerificationEmail(request.getEmail(), request.getUsername(), validationCode);

        log.info("Temporary user created and verification email sent for: {}", request.getEmail());

        return CreateTempUserResponse.builder()
                .message("Verification code has been sent to your email address. Please check your inbox.")
                .expirySeconds(TempUserCacheUtil.getDefaultExpiryMinutes() * 60L)
                .build();
    }

    private void validateUser(TempUserCreateRequest request) {
        if (userService.existsByEmailOrUsername(request.getEmail(), request.getUsername())) {
            if (userService.existsByEmail(request.getEmail())) {
                log.error("Email is already in use: {}", request.getEmail());
                throw new ResourceAlreadyExistsException("This email is already in use: " + request.getEmail());
            } else {
                log.error("Username is already in use: {}", request.getUsername());
                throw new ResourceAlreadyExistsException("This username is already in use: " + request.getUsername());
            }
        }
    }

    /**
     * Verifies a user using the validation code and creates permanent user in the database.
     *
     * @param request The verification request containing validation code
     * @return The created user response
     */
    @Override
    public UserResponse verifyAndCreateUser(TempVerifyUserRequest request) {
        log.info("Verifying and creating user with code: {}", request.getValidationCode());

        Optional<TempUserCreateRequest> tempUserOpt = TempUserCacheUtil.getTempUser(request.getValidationCode());

        if (tempUserOpt.isEmpty()) {
            log.error("No temporary user found for validation code: {}", request.getValidationCode());
            throw new ResourceNotFoundException("No temporary user found for this validation code. It may have expired.");
        }

        TempUserCreateRequest tempUser = tempUserOpt.get();
        TempUserCacheUtil.removeTempUser(request.getValidationCode());

        UserCreateRequest createRequest = new UserCreateRequest();
        BeanUtils.copyProperties(tempUser, createRequest);

        UserResponse response = userService.createUser(createRequest);
        log.info("User successfully verified and created: {}", tempUser.getEmail());
        return response;
    }

    /**
     * Sends a verification email with the validation code.
     *
     * @param email User's email address
     * @param username User's username
     * @param validationCode The verification code to send
     */
    private void sendVerificationEmail(String email, String username, String validationCode) {
        try {
            emailService.sendCustomVerificationEmail(
                    email,
                    username,
                    validationCode);
        } catch (Exception e) {
            log.error("Error sending verification email to: {}", email, e);
        }
    }
}
