package com.thales.user_resource_service.repository;

import com.thales.user_resource_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username Username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email Email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given username and password.
     *
     * @param username Username to check
     * @param password Password to check
     * @return true if a user exists with this username and password, false otherwise
     */
    boolean existsByUsernameAndPassword(String username, String password);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email Email to check
     * @return true if a user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     *
     * @param username Username to check
     * @return true if a user exists with this username, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists with the given email or username.
     *
     * @param email Email to check
     * @param username Username to check
     * @return true if a user exists with this email or username, false otherwise
     */
    boolean existsByEmailOrUsername(String email, String username);

    /**
     * Finds users whose username starts with the given prefix.
     *
     * @param prefix The prefix to search for
     * @return List of users matching the prefix
     */
    List<User> findByUsernameStartingWith(String prefix);

    /**
     * Finds users by their IDs.
     *
     * @param userIds List of user IDs to find
     * @return List of users matching the IDs
     */
    List<User> findByIdIn(List<Long> userIds);
}
