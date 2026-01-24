package com.thales.user_resource_service.repository;

import com.thales.user_resource_service.model.PortalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing PortalUser entities.
 */
@Repository
public interface PortalUserRepository extends JpaRepository<PortalUser, Long> {

    /**
     * Finds a portal user by their username.
     *
     * @param username Username to search for
     * @return Optional containing the portal user if found
     */
    Optional<PortalUser> findByUsername(String username);

    /**
     * Finds a portal user by their email address.
     *
     * @param email Email to search for
     * @return Optional containing the portal user if found
     */
    Optional<PortalUser> findByEmail(String email);

    /**
     * Checks if a portal user exists with the given email.
     *
     * @param email Email to check
     * @return true if a portal user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a portal user exists with the given username.
     *
     * @param username Username to check
     * @return true if a portal user exists with this username, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Finds portal users whose email domain matches the given domain.
     *
     * @param domain The email domain to match
     * @return List of portal users with matching email domain
     */
    List<PortalUser> findByEmailEndingWith(String domain);
} 