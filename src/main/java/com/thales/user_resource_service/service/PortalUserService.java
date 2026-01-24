package com.thales.user_resource_service.service;

import com.thales.user_resource_service.dto.PortalUserCreateRequest;
import com.thales.user_resource_service.dto.PortalUserResponse;
import com.thales.user_resource_service.dto.UserCreateOrGetRequest;

import java.util.List;

public interface PortalUserService {

    /**
     * Creates a new portal user with the provided details.
     * The email domain must match the allowed domain from configuration.
     *
     * @param request Portal user creation request
     * @return The created portal user response
     */
    PortalUserResponse createPortalUser(PortalUserCreateRequest request);

    /**
     * Retrieves a portal user by their ID.
     *
     * @param id Portal user ID
     * @return The portal user response
     */
    PortalUserResponse getPortalUserById(Long id);

    /**
     * Retrieves a portal user by their username.
     *
     * @param username Portal user username
     * @return The portal user response
     */
    PortalUserResponse getPortalUserByUsername(String username);

    /**
     * Retrieves a portal user by their email.
     *
     * @param email Portal user email
     * @return The portal user response
     */
    PortalUserResponse getPortalUserByEmail(String email);

    /**
     * Retrieves all portal users.
     *
     * @return List of portal user responses
     */
    List<PortalUserResponse> getAllPortalUsers();

    /**
     * Checks if the email domain is valid according to configuration.
     *
     * @param email Email to validate
     * @return true if the email domain is valid, false otherwise
     */
    boolean isValidEmailDomain(String email);

    PortalUserResponse createOrGetUser(UserCreateOrGetRequest userCreateOrGetRequest);

    boolean validate(String username, String password);
}
