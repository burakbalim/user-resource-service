package com.thales.user_resource_service.service;

import com.thales.user_resource_service.dto.*;

import java.util.List;
import java.util.Map;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse createOrGetUser(UserCreateOrGetRequest request);

    boolean validate(String username, String password);

    UserBaseResponse getUserBaseResponse(String username);

    Long getUserIdByUsername(String username);

    /**
     * Searches for users whose username starts with the given prefix.
     * Used for autocomplete or search functionality.
     *
     * @param prefix The prefix to search for (at least 3 characters)
     * @return A list of user responses matching the prefix
     */
    List<UserBaseResponse> searchUsersByUsernamePrefix(String prefix);

    Map<Long, String> getUsernameIdPairs(List<Long> userIds);

    IAMResponse getIAMResponse();

    /**
     * Checks if a user exists with the given email.
     *
     * @param email The email to check
     * @return true if a user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     *
     * @param username The username to check
     * @return true if a user exists with this username, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists with the given email or username.
     *
     * @param email The email to check
     * @param username The username to check
     * @return true if a user exists with this email or username, false otherwise
     */
    boolean existsByEmailOrUsername(String email, String username);

    /**
     * Process a forgot password request, generating a password reset token
     * and sending reset instructions to the user's email.
     *
     * @param request Forgot password request containing the user's email
     * @return true if the request was processed successfully
     */
    boolean forgotPassword(ForgotPasswordRequest request);

    /**
     * Resets a user's password using the provided reset token.
     *
     * @param request Reset password request containing token and new password
     * @return true if the password was reset successfully
     */
    boolean resetPassword(ResetPasswordRequest request);

}
