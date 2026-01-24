package com.thales.user_resource_service.service.impl;

import com.thales.user_resource_service.dto.UserBaseResponse;
import com.thales.user_resource_service.dto.UserCreateOrGetRequest;
import com.thales.user_resource_service.dto.UserCreateRequest;
import com.thales.user_resource_service.dto.UserResponse;
import com.thales.user_resource_service.exception.ResourceAlreadyExistsException;
import com.thales.user_resource_service.exception.ValidationException;
import com.thales.user_resource_service.mapper.UserMapper;
import com.thales.user_resource_service.model.User;
import com.thales.user_resource_service.repository.UserRepository;
import com.thales.user_resource_service.util.PasswordUtil;
import com.thales.user_resource_service.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should return correct UserResponse when user is successfully created")
    void createUser_Success() {
        // Arrange
        UserCreateRequest request = TestDataFactory.createUserCreateRequest();
        User user = TestDataFactory.createUser();
        UserResponse expected = TestDataFactory.createUserResponse();
        
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(false);
        when(userMapper.toUser(any(UserCreateRequest.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(expected);

        try (MockedStatic<PasswordUtil> passwordUtil = mockStatic(PasswordUtil.class)) {
            passwordUtil.when(() -> PasswordUtil.encode(anyString())).thenReturn("encoded_password");

            // Act
            UserResponse result = userService.createUser(request);

            // Assert
            assertNotNull(result);
            assertEquals(expected.getUsername(), result.getUsername());
            assertEquals(expected.getEmail(), result.getEmail());
            
            // Verify
            verify(userRepository).existsByEmailOrUsername(request.getEmail(), request.getUsername());
            verify(userMapper).toUser(request);
            verify(userRepository).save(user);
            verify(userMapper).toUserResponse(user);
        }
    }

    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when email is already in use")
    void createUser_WithExistingEmail_ThrowsException() {
        // Arrange
        UserCreateRequest request = TestDataFactory.createUserCreateRequest();
        
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        ResourceAlreadyExistsException exception = assertThrows(
            ResourceAlreadyExistsException.class, 
            () -> userService.createUser(request)
        );
        
        assertTrue(exception.getMessage().contains("email"));
        
        // Verify
        verify(userRepository).existsByEmailOrUsername(request.getEmail(), request.getUsername());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when username is already in use")
    void createUser_WithExistingUsername_ThrowsException() {
        // Arrange
        UserCreateRequest request = TestDataFactory.createUserCreateRequest();
        
        when(userRepository.existsByEmailOrUsername(anyString(), anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        ResourceAlreadyExistsException exception = assertThrows(
            ResourceAlreadyExistsException.class, 
            () -> userService.createUser(request)
        );
        
        assertTrue(exception.getMessage().contains("username"));
        
        // Verify
        verify(userRepository).existsByEmailOrUsername(request.getEmail(), request.getUsername());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update and return existing user")
    void createOrGetUser_WithExistingUser_ReturnsUpdatedUser() {
        // Arrange
        UserCreateOrGetRequest request = TestDataFactory.createUserCreateOrGetRequest();
        User existingUser = TestDataFactory.createUser();
        UserResponse expected = TestDataFactory.createUserResponse();
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(expected);

        // Act
        UserResponse result = userService.createOrGetUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());
        
        // Verify
        verify(userRepository).findByEmail(request.getEmail());
        verify(userMapper).toUserResponse(existingUser);
        
        // Verify changes
        assertEquals(request.getAuthProvider(), existingUser.getAuthProvider());
        assertEquals(request.getExternalId(), existingUser.getExternalId());
    }

    @Test
    @DisplayName("Should create and return new user when user is not found")
    void createOrGetUser_WithNonExistingUser_CreatesAndReturnsUser() {
        // Arrange
        UserCreateOrGetRequest request = TestDataFactory.createUserCreateOrGetRequest();
        User newUser = TestDataFactory.createUser();
        UserResponse expected = TestDataFactory.createUserResponse();
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userMapper.toUser(any(UserCreateOrGetRequest.class))).thenReturn(newUser);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(expected);

        // Act
        UserResponse result = userService.createOrGetUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());
        
        // Verify
        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository).existsByUsername(request.getUsername());
        verify(userMapper).toUser(request);
        verify(userRepository).save(newUser);
        verify(userMapper).toUserResponse(newUser);
    }

    @Test
    @DisplayName("Should generate new username when username already exists")
    void createOrGetUser_WithExistingUsername_GeneratesNewUsername() {
        // Arrange
        UserCreateOrGetRequest request = TestDataFactory.createUserCreateOrGetRequest();
        User newUser = TestDataFactory.createUser();
        UserResponse expected = TestDataFactory.createUserResponse();
        
        // Using lenient mocking for this test to handle the dynamic username generation
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        lenient().when(userRepository.existsByUsername("testuser")).thenReturn(true);
        lenient().when(userRepository.existsByUsername(startsWith("testuser_"))).thenReturn(false);
        lenient().when(userMapper.toUser(any(UserCreateOrGetRequest.class))).thenReturn(newUser);
        lenient().when(userRepository.save(any(User.class))).thenReturn(newUser);
        lenient().when(userMapper.toUserResponse(any(User.class))).thenReturn(expected);
        
        // Act
        UserResponse result = userService.createOrGetUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());
        
        // Verify
        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository, atLeastOnce()).existsByUsername(anyString());
        verify(userMapper).toUser(any(UserCreateOrGetRequest.class));
        verify(userRepository).save(newUser);
        verify(userMapper).toUserResponse(newUser);
    }

    @Test
    @DisplayName("Should return true when user credentials are valid")
    void validate_WithValidCredentials_ReturnsTrue() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String encodedPassword = "encoded_password";
        
        try (MockedStatic<PasswordUtil> passwordUtil = mockStatic(PasswordUtil.class)) {
            passwordUtil.when(() -> PasswordUtil.encode(password)).thenReturn(encodedPassword);
            when(userRepository.existsByUsernameAndPassword(username, encodedPassword)).thenReturn(true);
            
            // Act
            boolean result = userService.validate(username, password);
            
            // Assert
            assertTrue(result);
            
            // Verify
            verify(userRepository).existsByUsernameAndPassword(username, encodedPassword);
        }
    }

    @Test
    @DisplayName("Should return false when user credentials are invalid")
    void validate_WithInvalidCredentials_ReturnsFalse() {
        // Arrange
        String username = "testuser";
        String password = "wrong_password";
        String encodedPassword = "encoded_wrong_password";
        
        try (MockedStatic<PasswordUtil> passwordUtil = mockStatic(PasswordUtil.class)) {
            passwordUtil.when(() -> PasswordUtil.encode(password)).thenReturn(encodedPassword);
            when(userRepository.existsByUsernameAndPassword(username, encodedPassword)).thenReturn(false);
            
            // Act
            boolean result = userService.validate(username, password);
            
            // Assert
            assertFalse(result);
            
            // Verify
            verify(userRepository).existsByUsernameAndPassword(username, encodedPassword);
        }
    }

    @Test
    @DisplayName("Should return UserBaseResponse for existing user")
    void getUserBaseResponse_WithExistingUser_ReturnsUserBaseResponse() {
        // Arrange
        String username = "testuser";
        User user = TestDataFactory.createUser();
        UserBaseResponse expected = TestDataFactory.createUserBaseResponse();
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userMapper.toUserBaseResponse(user)).thenReturn(expected);
        
        // Act
        UserBaseResponse result = userService.getUserBaseResponse(username);
        
        // Assert
        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getUsername(), result.getUsername());
        
        // Verify
        verify(userRepository).findByUsername(username);
        verify(userMapper).toUserBaseResponse(user);
    }

    @Test
    @DisplayName("Should return null when user is not found")
    void getUserBaseResponse_WithNonExistingUser_ReturnsNull() {
        // Arrange
        String username = "nonexistentuser";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        
        // Act
        UserBaseResponse result = userService.getUserBaseResponse(username);
        
        // Assert
        assertNull(result);
        
        // Verify
        verify(userRepository).findByUsername(username);
        verify(userMapper, never()).toUserBaseResponse(any(User.class));
    }

    @Test
    @DisplayName("Should return user ID for existing user")
    void getUserIdByUsername_WithExistingUser_ReturnsUserId() {
        // Arrange
        String username = "testuser";
        User user = TestDataFactory.createUser();
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        
        // Act
        Long userId = userService.getUserIdByUsername(username);
        
        // Assert
        assertNotNull(userId);
        assertEquals(user.getId(), userId);
        
        // Verify
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Should return null when user is not found")
    void getUserIdByUsername_WithNonExistingUser_ReturnsNull() {
        // Arrange
        String username = "nonexistentuser";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        
        // Act
        Long userId = userService.getUserIdByUsername(username);
        
        // Assert
        assertNull(userId);
        
        // Verify
        verify(userRepository).findByUsername(username);
    }

    @Test
    void searchUsersByUsernamePrefix_WithValidPrefix_ReturnsMatchingUsers() {
        // Given
        String prefix = "tes";
        User user1 = TestDataFactory.createUser("testUser", "test@example.com");
        User user2 = TestDataFactory.createUser("testing", "testing@example.com");
        List<User> users = Arrays.asList(user1, user2);
        
        when(userRepository.findByUsernameStartingWith(prefix)).thenReturn(users);
        when(userMapper.toUserBaseResponse(any(User.class)))
                .thenReturn(new UserBaseResponse(1L, "testuser"));
        
        // When
        List<UserBaseResponse> result = userService.searchUsersByUsernamePrefix(prefix);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findByUsernameStartingWith(prefix);
        verify(userMapper, times(2)).toUserBaseResponse(any(User.class));
    }
    
    @Test
    void searchUsersByUsernamePrefix_WithPrefixTooShort_ThrowsValidationException() {
        // Given
        String prefix = "te";
        
        // When & Then
        assertThrows(ValidationException.class, () -> userService.searchUsersByUsernamePrefix(prefix));
        verify(userRepository, never()).findByUsernameStartingWith(anyString());
    }
    
    @Test
    void searchUsersByUsernamePrefix_WithNullPrefix_ThrowsValidationException() {
        // Given
        String prefix = null;
        
        // When & Then
        assertThrows(ValidationException.class, () -> userService.searchUsersByUsernamePrefix(prefix));
        verify(userRepository, never()).findByUsernameStartingWith(anyString());
    }
    
    @Test
    void searchUsersByUsernamePrefix_WithNoMatchingUsers_ReturnsEmptyList() {
        // Given
        String prefix = "xyz";
        when(userRepository.findByUsernameStartingWith(prefix)).thenReturn(Collections.emptyList());
        
        // When
        List<UserBaseResponse> result = userService.searchUsersByUsernamePrefix(prefix);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findByUsernameStartingWith(prefix);
        verify(userMapper, never()).toUserBaseResponse(any(User.class));
    }
} 