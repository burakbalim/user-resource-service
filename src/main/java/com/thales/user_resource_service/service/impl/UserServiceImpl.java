package com.thales.user_resource_service.service.impl;

import com.thales.security.context.JwtSecurityContext;
import com.thales.security.model.JwtUserClaims;
import com.thales.user_resource_service.cache.TokenCacheUtil;
import com.thales.user_resource_service.dto.*;
import com.thales.user_resource_service.exception.ResourceAlreadyExistsException;
import com.thales.user_resource_service.exception.ValidationException;
import com.thales.user_resource_service.mapper.UserMapper;
import com.thales.user_resource_service.model.User;
import com.thales.user_resource_service.repository.UserRepository;
import com.thales.user_resource_service.service.EmailService;
import com.thales.user_resource_service.service.UserService;
import com.thales.user_resource_service.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface that handles user-related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;

    /**
     * Creates a new user with the provided information.
     *
     * @param request User creation request containing user details
     * @return UserResponse with the created user's information
     * @throws ResourceAlreadyExistsException if a user with the same email already exists
     */
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("User creation request received: {}", request.getEmail());
        checkEmailAndUsernameAvailability(request.getEmail(), request.getUsername());
        User user = createUserFromRequest(request);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createOrGetUser(UserCreateOrGetRequest request) {
        log.info("CreateOrGet request received: {}, provider: {}", request.getEmail(), request.getAuthProvider());

        Optional<User> existingUser = getExistingUser(request);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setAuthProvider(request.getAuthProvider());
            user.setExternalId(request.getExternalId());
            return userMapper.toUserResponse(user);
        } else {
            checkUserName(request);
            return createExternalUser(request);
        }
    }

    private Optional<User> getExistingUser(UserCreateOrGetRequest request) {
        if (request.getEmail() != null) {
            return userRepository.findByEmail(request.getEmail());
        } else if (request.getUsername() != null) {
            return userRepository.findByUsername(request.getUsername());
        } else {
            throw new ValidationException("User or email cannot be empty");
        }
    }

    private void checkUserName(UserCreateOrGetRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            request.setUsername(request.getUsername() + "_" + new Random().nextInt());
            checkUserName(request);
        }
    }

    @Override
    public boolean validate(String username, String password) {
        String encodedPassword = PasswordUtil.encode(password);
        return userRepository.existsByUsernameAndPassword(username, encodedPassword);
    }

    @Override
    public UserBaseResponse getUserBaseResponse(String username) {
        log.info("Getting user base response for username: {}", username);
        return userRepository.findByUsername(username)
                .map(userMapper::toUserBaseResponse)
                .orElse(null);
    }

    @Override
    public Long getUserIdByUsername(String username) {
        log.info("Getting user ID for username: {}", username);
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }

    @Override
    public List<UserBaseResponse> searchUsersByUsernamePrefix(String prefix) {
        if (prefix == null || prefix.length() < 3) {
            log.warn("Username prefix must be at least 3 characters, provided: {}", prefix);
            throw new ValidationException("Username prefix must be at least 3 characters");
        }

        log.info("Searching for users with username prefix: {}", prefix);
        List<User> users = userRepository.findByUsernameStartingWith(prefix);

        if (users.isEmpty()) {
            log.info("No users found with username prefix: {}", prefix);
            return Collections.emptyList();
        }

        log.info("Found {} users with username prefix: {}", users.size(), prefix);
        return users.stream()
                .map(userMapper::toUserBaseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Checks if both email and username are available for registration.
     *
     * @param email Email to check
     * @param username Username to check
     * @throws ResourceAlreadyExistsException if either the email or username is already in use
     */
    private void checkEmailAndUsernameAvailability(String email, String username) {
        if (userRepository.existsByEmailOrUsername(email, username)) {
            if (userRepository.existsByEmail(email)) {
                log.error("Email is already in use: {}", email);
                throw new ResourceAlreadyExistsException("This email is already in use: " + email);
            } else {
                log.error("Username is already in use: {}", username);
                throw new ResourceAlreadyExistsException("This username is already in use: " + username);
            }
        }
    }


    /**
     * Creates a new user from a standard registration request.
     *
     * @param request User creation request
     * @return Saved user entity
     */
    private User createUserFromRequest(UserCreateRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setAuthProvider("LOCAL");

        User savedUser = userRepository.save(user);
        log.info("User successfully created: {}", savedUser.getEmail());

        return savedUser;
    }

    /**
     * Creates a new user from an external authentication provider.
     *
     * @param request User creation or retrieval request
     * @return UserResponse with the created user's information
     */
    private UserResponse createExternalUser(UserCreateOrGetRequest request) {
        log.info("User not found, creating new user: {}", request.getEmail());
        User newUser = userMapper.toUser(request);
        User savedUser = userRepository.save(newUser);
        log.info("New user successfully created: {}", savedUser.getEmail());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public Map<Long, String> getUsernameIdPairs(List<Long> userIds) {
        log.info("Fetching username and ID pairs for {} user IDs", userIds.size());
        return userRepository.findByIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        User::getUsername
                ));
    }

    @Override
    public IAMResponse getIAMResponse() {
        JwtUserClaims currentUser = JwtSecurityContext.getCurrentUser();
        return userMapper.toIAMResponse(currentUser);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.info("Checking if user exists with email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        log.info("Checking if user exists with username: {}", username);
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmailOrUsername(String email, String username) {
        log.info("Checking if user exists with email: {} or username: {}", email, username);
        return userRepository.existsByEmailOrUsername(email, username);
    }

    @Override
    public boolean forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset request received: {}", request.getEmail());
        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    String resetToken = TokenCacheUtil.createResetToken(user.getEmail());
                    return emailService.sendPasswordResetEmail(
                        user.getEmail(),
                        user.getUsername(),
                        resetToken
                    );
                })
                .orElse(false);
    }

    @Override
    public boolean resetPassword(ResetPasswordRequest request) {
        log.info("Password reset process initiated: {}", request.getEmail());

        if (!TokenCacheUtil.validateResetToken(request.getEmail(), request.getToken())) {
            log.warn("Invalid or expired reset token for: {}", request.getEmail());
            return false;
        }

        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    user.setPassword(PasswordUtil.encode(request.getPassword()));
                    userRepository.save(user);
                    TokenCacheUtil.removeResetToken(request.getEmail());
                    emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());
                    return true;
                })
                .orElse(false);
    }
}
