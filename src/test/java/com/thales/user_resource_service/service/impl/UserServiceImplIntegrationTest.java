package com.thales.user_resource_service.service.impl;

import com.thales.user_resource_service.UserResourceServiceApplication;
import com.thales.user_resource_service.client.EmailRequest;
import com.thales.user_resource_service.client.NotificationClient;
import com.thales.user_resource_service.dto.UserBaseResponse;
import com.thales.user_resource_service.dto.UserCreateRequest;
import com.thales.user_resource_service.dto.UserResponse;
import com.thales.user_resource_service.exception.ResourceAlreadyExistsException;
import com.thales.user_resource_service.exception.ValidationException;
import com.thales.user_resource_service.model.User;
import com.thales.user_resource_service.repository.UserRepository;
import com.thales.user_resource_service.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = UserResourceServiceApplication.class)
@ActiveProfiles("test")
@Transactional
public class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CacheManager cacheManager;

    @Test
    public void createUser_Success() {
        // Setup - Mock email sending
        when(notificationClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));
        
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("testpassword");

        // Execute
        UserResponse createdUser = userService.createUser(request);

        // Verify
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
        assertEquals("test@example.com", createdUser.getEmail());
        assertNotNull(createdUser.getId());
    }

    @Test
    public void createUser_WithExistingEmail_ThrowsException() {
        // Setup - Mock email sending
        when(notificationClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));
        
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("testpassword");
        userRepository.save(existingUser);

        // Create new user with same email
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("test@example.com");
        request.setPassword("newpassword");

        // Execute & Verify
        assertThrows(ResourceAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    public void createUser_WithExistingUsername_ThrowsException() {
        // Setup - Mock email sending
        when(notificationClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));
        
        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("testpassword");
        userRepository.save(existingUser);

        // Create new user with same username
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setEmail("new@example.com");
        request.setPassword("newpassword");

        // Execute & Verify
        assertThrows(ResourceAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    public void validate_WithValidCredentials_ReturnsTrue() {
        // Setup
        when(notificationClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));
        
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(PasswordUtil.encode("testpassword"));
        userRepository.save(user);

        // Execute & Verify
        assertTrue(userService.validate("testuser", "testpassword"));
    }

    @Test
    public void validate_WithInvalidPassword_ReturnsFalse() {
        // Setup
        when(notificationClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));
        
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(PasswordUtil.encode("testpassword"));
        userRepository.save(user);

        // Execute & Verify
        assertFalse(userService.validate("testuser", "wrongpassword"));
    }

    @Test
    public void validate_WithNonExistingUser_ReturnsFalse() {
        // Execute & Verify
        assertFalse(userService.validate("nonexistentuser", "anypassword"));
    }

    @Test
    public void getUserIdByUsername_WithExistingUser_ReturnsUserId() {
        // Setup
        when(notificationClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));
        
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        // Execute
        Long userId = userService.getUserIdByUsername("testuser");

        // Verify
        assertEquals(user.getId(), userId);
    }

    @Test
    public void getUserIdByUsername_WithNonExistingUser_ReturnsNull() {
        // Execute & Verify
        assertNull(userService.getUserIdByUsername("nonexistentuser"));
    }

    @Test
    public void getUserBaseResponse_WithExistingUser_ReturnsUserBaseResponse() {
        // Setup
        when(notificationClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));
        
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        // Execute
        UserBaseResponse response = userService.getUserBaseResponse("testuser");

        // Verify
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    public void getUserBaseResponse_WithNonExistingUser_ReturnsNull() {
        // Execute & Verify
        assertNull(userService.getUserBaseResponse("nonexistentuser"));
    }

    @Test
    public void searchUsersByUsernamePrefix_WithValidPrefix_ReturnsMatchingUsers() {
        // Setup
        when(notificationClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));
        
        User user1 = new User();
        user1.setUsername("testuser1");
        user1.setEmail("test1@example.com");
        user1.setPassword("password");
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword("password");
        userRepository.save(user2);

        User user3 = new User();
        user3.setUsername("otheruser");
        user3.setEmail("other@example.com");
        user3.setPassword("password");
        userRepository.save(user3);

        // Execute
        List<UserBaseResponse> results = userService.searchUsersByUsernamePrefix("tes");

        // Verify
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("testuser1")));
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("testuser2")));
    }

    @Test
    public void searchUsersByUsernamePrefix_WithNoMatchingUsers_ReturnsEmptyList() {
        // Setup
        when(notificationClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));
        
        User user1 = new User();
        user1.setUsername("testuser1");
        user1.setEmail("test1@example.com");
        user1.setPassword("password");
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword("password");
        userRepository.save(user2);

        // Execute
        List<UserBaseResponse> results = userService.searchUsersByUsernamePrefix("xyz");

        // Verify
        assertTrue(results.isEmpty());
    }

    @Test
    public void searchUsersByUsernamePrefix_WithPrefixTooShort_ThrowsValidationException() {
        // Execute & Verify
        assertThrows(ValidationException.class, () -> userService.searchUsersByUsernamePrefix("te"));
    }
}
