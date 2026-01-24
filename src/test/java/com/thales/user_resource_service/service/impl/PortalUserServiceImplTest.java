package com.thales.user_resource_service.service.impl;

import com.thales.user_resource_service.dto.PortalUserCreateRequest;
import com.thales.user_resource_service.dto.PortalUserResponse;
import com.thales.user_resource_service.dto.UserCreateOrGetRequest;
import com.thales.user_resource_service.exception.ValidationException;
import com.thales.user_resource_service.mapper.PortalUserMapper;
import com.thales.user_resource_service.model.PortalUser;
import com.thales.user_resource_service.repository.PortalUserRepository;
import com.thales.user_resource_service.util.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PortalUserServiceImplTest {

    private PortalUserRepository portalUserRepository;
    private PortalUserMapper portalUserMapper;
    private PasswordEncoder passwordEncoder;
    private PortalUserServiceImpl portalUserService;

    @BeforeEach
    void setUp() {
        portalUserRepository = Mockito.mock(PortalUserRepository.class);
        portalUserMapper = Mockito.mock(PortalUserMapper.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        portalUserService = new PortalUserServiceImpl(portalUserRepository, portalUserMapper, passwordEncoder);
        ReflectionTestUtils.setField(portalUserService, "allowedDomain", "thales.com");
    }

    @Test
    @DisplayName("Should create portal user successfully with valid domain")
    void createPortalUser_WithValidDomain_Success() {
        // Arrange
        PortalUserCreateRequest request = TestDataFactory.createPortalUserCreateRequest();
        PortalUser portalUser = TestDataFactory.createPortalUser();
        PortalUserResponse expected = TestDataFactory.createPortalUserResponse();

        when(portalUserRepository.existsByUsername(anyString())).thenReturn(false);
        when(portalUserRepository.existsByEmail(anyString())).thenReturn(false);
        when(portalUserMapper.toEntity(any(PortalUserCreateRequest.class))).thenReturn(portalUser);
        when(portalUserRepository.save(any(PortalUser.class))).thenReturn(portalUser);
        when(portalUserMapper.toResponse(any(PortalUser.class))).thenReturn(expected);

        // Act
        PortalUserResponse result = portalUserService.createPortalUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());
        assertEquals(expected.getFullName(), result.getFullName());

        // Verify
        verify(portalUserRepository).existsByUsername(request.getUsername());
        verify(portalUserRepository).existsByEmail(request.getEmail());
        verify(portalUserMapper).toEntity(request);
        verify(portalUserRepository).save(portalUser);
        verify(portalUserMapper).toResponse(portalUser);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email domain is invalid")
    void createPortalUser_WithInvalidDomain_ThrowsException() {
        // Arrange
        PortalUserCreateRequest request = PortalUserCreateRequest.builder()
                .username("testuser")
                .email("test@invaliddomain.com")
                .password("password123")
                .fullName("Test User")
                .build();

        ReflectionTestUtils.setField(portalUserService, "allowedDomain", "thales.com");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portalUserService.createPortalUser(request)
        );

        assertTrue(exception.getMessage().contains("Email domain not allowed"));

        // Verify
        verify(portalUserRepository, never()).save(any(PortalUser.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when username already exists")
    void createPortalUser_WithExistingUsername_ThrowsException() {
        // Arrange
        PortalUserCreateRequest request = TestDataFactory.createPortalUserCreateRequest();

        ReflectionTestUtils.setField(portalUserService, "allowedDomain", "thales.com");

        when(portalUserRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portalUserService.createPortalUser(request)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));

        // Verify
        verify(portalUserRepository).existsByUsername(request.getUsername());
        verify(portalUserRepository, never()).save(any(PortalUser.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email already exists")
    void createPortalUser_WithExistingEmail_ThrowsException() {
        // Arrange
        PortalUserCreateRequest request = TestDataFactory.createPortalUserCreateRequest();

        ReflectionTestUtils.setField(portalUserService, "allowedDomain", "thales.com");

        when(portalUserRepository.existsByUsername(anyString())).thenReturn(false);
        when(portalUserRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portalUserService.createPortalUser(request)
        );

        assertTrue(exception.getMessage().contains("Email already exists"));

        // Verify
        verify(portalUserRepository).existsByUsername(request.getUsername());
        verify(portalUserRepository).existsByEmail(request.getEmail());
        verify(portalUserRepository, never()).save(any(PortalUser.class));
    }

    @Test
    @DisplayName("Should return portal user by ID successfully")
    void getPortalUserById_WithExistingId_Success() {
        // Arrange
        PortalUser portalUser = TestDataFactory.createPortalUser();
        PortalUserResponse expected = TestDataFactory.createPortalUserResponse();

        when(portalUserRepository.findById(anyLong())).thenReturn(Optional.of(portalUser));
        when(portalUserMapper.toResponse(any(PortalUser.class))).thenReturn(expected);

        // Act
        PortalUserResponse result = portalUserService.getPortalUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());

        // Verify
        verify(portalUserRepository).findById(1L);
        verify(portalUserMapper).toResponse(portalUser);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when ID does not exist")
    void getPortalUserById_WithNonExistingId_ThrowsException() {
        // Arrange
        when(portalUserRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> portalUserService.getPortalUserById(999L));

        // Verify
        verify(portalUserRepository).findById(999L);
        verify(portalUserMapper, never()).toResponse(any(PortalUser.class));
    }

    @Test
    @DisplayName("Should return portal user by username successfully")
    void getPortalUserByUsername_WithExistingUsername_Success() {
        // Arrange
        PortalUser portalUser = TestDataFactory.createPortalUser();
        PortalUserResponse expected = TestDataFactory.createPortalUserResponse();

        when(portalUserRepository.findByUsername(anyString())).thenReturn(Optional.of(portalUser));
        when(portalUserMapper.toResponse(any(PortalUser.class))).thenReturn(expected);

        // Act
        PortalUserResponse result = portalUserService.getPortalUserByUsername("portaltestuser");

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());

        // Verify
        verify(portalUserRepository).findByUsername("portaltestuser");
        verify(portalUserMapper).toResponse(portalUser);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when username does not exist")
    void getPortalUserByUsername_WithNonExistingUsername_ThrowsException() {
        // Arrange
        when(portalUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> portalUserService.getPortalUserByUsername("nonexistent"));

        // Verify
        verify(portalUserRepository).findByUsername("nonexistent");
        verify(portalUserMapper, never()).toResponse(any(PortalUser.class));
    }

    @Test
    @DisplayName("Should return portal user by email successfully")
    void getPortalUserByEmail_WithExistingEmail_Success() {
        // Arrange
        PortalUser portalUser = TestDataFactory.createPortalUser();
        PortalUserResponse expected = TestDataFactory.createPortalUserResponse();

        when(portalUserRepository.findByEmail(anyString())).thenReturn(Optional.of(portalUser));
        when(portalUserMapper.toResponse(any(PortalUser.class))).thenReturn(expected);

        // Act
        PortalUserResponse result = portalUserService.getPortalUserByEmail("portaltest@thales.com");

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());

        // Verify
        verify(portalUserRepository).findByEmail("portaltest@thales.com");
        verify(portalUserMapper).toResponse(portalUser);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when email does not exist")
    void getPortalUserByEmail_WithNonExistingEmail_ThrowsException() {
        // Arrange
        when(portalUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> portalUserService.getPortalUserByEmail("nonexistent@thales.com"));

        // Verify
        verify(portalUserRepository).findByEmail("nonexistent@thales.com");
        verify(portalUserMapper, never()).toResponse(any(PortalUser.class));
    }

    @Test
    @DisplayName("Should return all portal users successfully")
    void getAllPortalUsers_Success() {
        // Arrange
        PortalUser portalUser1 = TestDataFactory.createPortalUser(1L, "user1", "user1@thales.com");
        PortalUser portalUser2 = TestDataFactory.createPortalUser(2L, "user2", "user2@thales.com");
        PortalUserResponse response1 = TestDataFactory.createPortalUserResponse(1L, "user1", "user1@thales.com");
        PortalUserResponse response2 = TestDataFactory.createPortalUserResponse(2L, "user2", "user2@thales.com");

        when(portalUserRepository.findAll()).thenReturn(Arrays.asList(portalUser1, portalUser2));
        when(portalUserMapper.toResponse(portalUser1)).thenReturn(response1);
        when(portalUserMapper.toResponse(portalUser2)).thenReturn(response2);

        // Act
        List<PortalUserResponse> results = portalUserService.getAllPortalUsers();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("user1", results.get(0).getUsername());
        assertEquals("user2", results.get(1).getUsername());

        // Verify
        verify(portalUserRepository).findAll();
        verify(portalUserMapper, times(2)).toResponse(any(PortalUser.class));
    }

    @Test
    @DisplayName("Should return empty list when no portal users exist")
    void getAllPortalUsers_WithNoUsers_ReturnsEmptyList() {
        // Arrange
        when(portalUserRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PortalUserResponse> results = portalUserService.getAllPortalUsers();

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());

        // Verify
        verify(portalUserRepository).findAll();
        verify(portalUserMapper, never()).toResponse(any(PortalUser.class));
    }

    @Test
    @DisplayName("Should validate email domain correctly")
    void isValidEmailDomain_WithValidDomain_ReturnsTrue() {
        // Act & Assert
        assertTrue(portalUserService.isValidEmailDomain("test@thales.com"));
    }

    @Test
    @DisplayName("Should invalidate incorrect email domain")
    void isValidEmailDomain_WithInvalidDomain_ReturnsFalse() {
        // Act & Assert
        assertFalse(portalUserService.isValidEmailDomain("test@invalid.com"));
    }

    @Test
    @DisplayName("Should invalidate null or empty email")
    void isValidEmailDomain_WithNullOrEmptyEmail_ReturnsFalse() {
        // Act & Assert
        assertFalse(portalUserService.isValidEmailDomain(null));
        assertFalse(portalUserService.isValidEmailDomain(""));
    }

    @Test
    @DisplayName("Should invalidate invalid email format")
    void isValidEmailDomain_WithInvalidEmailFormat_ReturnsFalse() {
        // Act & Assert
        assertFalse(portalUserService.isValidEmailDomain("invalidemail"));
        assertFalse(portalUserService.isValidEmailDomain("invalid@"));
    }

    @Test
    @DisplayName("Should create new user when user does not exist")
    void createOrGetUser_WithNonExistingUser_CreatesNewUser() {
        // Arrange
        UserCreateOrGetRequest request = TestDataFactory.createUserCreateOrGetRequest(
                "newportaluser", "newportaluser@thales.com", "GOOGLE");
        PortalUser newPortalUser = TestDataFactory.createPortalUser(1L,
                "newportaluser", "newportaluser@thales.com");
        PortalUserResponse expected = TestDataFactory.createPortalUserResponse(1L,
                "newportaluser", "newportaluser@thales.com");

        when(portalUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(portalUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(portalUserMapper.toEntity(any(PortalUserCreateRequest.class))).thenReturn(newPortalUser);
        when(portalUserRepository.save(any(PortalUser.class))).thenReturn(newPortalUser);
        when(portalUserMapper.toResponse(any(PortalUser.class))).thenReturn(expected);

        // Act
        PortalUserResponse result = portalUserService.createOrGetUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());

        // Verify
        verify(portalUserRepository).findByEmail(request.getEmail());
        verify(portalUserMapper).toEntity(any(PortalUserCreateRequest.class));
        verify(portalUserRepository).save(any(PortalUser.class));
        verify(portalUserMapper).toResponse(any(PortalUser.class));
    }

    @Test
    @DisplayName("Should update existing user when user exists by email")
    void createOrGetUser_WithExistingUserByEmail_UpdatesUser() {
        // Arrange
        UserCreateOrGetRequest request = TestDataFactory.createUserCreateOrGetRequest(
                "existingportaluser", "existingportaluser@thales.com", "GOOGLE");
        PortalUser existingPortalUser = TestDataFactory.createPortalUser(1L,
                "existingportaluser", "existingportaluser@thales.com");
        PortalUserResponse expected = TestDataFactory.createPortalUserResponse(1L,
                "existingportaluser", "existingportaluser@thales.com");

        existingPortalUser.setAuthProvider("LOCAL"); // Önceden farklı bir provider ile kaydedilmiş

        when(portalUserRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingPortalUser));
        when(portalUserRepository.save(any(PortalUser.class))).thenReturn(existingPortalUser);
        when(portalUserMapper.toResponse(any(PortalUser.class))).thenReturn(expected);

        // Act
        PortalUserResponse result = portalUserService.createOrGetUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());

        // Verify
        verify(portalUserRepository).findByEmail(request.getEmail());
        verify(portalUserRepository).save(existingPortalUser);
        verify(portalUserMapper).toResponse(existingPortalUser);

        // Verify the user was updated with new auth provider
        assertEquals("GOOGLE", existingPortalUser.getAuthProvider());
        assertEquals("GOOGLE_123456", existingPortalUser.getExternalId());
    }

    @Test
    @DisplayName("Should update existing user when user exists by username")
    void createOrGetUser_WithExistingUserByUsername_UpdatesUser() {
        // Arrange
        UserCreateOrGetRequest request = TestDataFactory.createUserCreateOrGetRequest(
                "existingportaluser", null, "GOOGLE");
        PortalUser existingPortalUser = TestDataFactory.createPortalUser(1L,
                "existingportaluser", "existingportaluser@thales.com");
        PortalUserResponse expected = TestDataFactory.createPortalUserResponse(1L,
                "existingportaluser", "existingportaluser@thales.com");

        when(portalUserRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(existingPortalUser));
        when(portalUserRepository.save(any(PortalUser.class))).thenReturn(existingPortalUser);
        when(portalUserMapper.toResponse(any(PortalUser.class))).thenReturn(expected);

        // Act
        PortalUserResponse result = portalUserService.createOrGetUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getEmail(), result.getEmail());

        // Verify - Değiştirildi, email aramıyoruz çünkü PortalUserServiceImpl'da null email için findByEmail çağrılmıyor
        verify(portalUserRepository).findByUsername(request.getUsername());
        verify(portalUserRepository).save(existingPortalUser);
        verify(portalUserMapper).toResponse(existingPortalUser);
    }

    @Test
    @DisplayName("Should throw ValidationException when both email and username are null")
    void createOrGetUser_WithNullEmailAndUsername_ThrowsValidationException() {
        // Arrange
        UserCreateOrGetRequest request = new UserCreateOrGetRequest();
        request.setAuthProvider("GOOGLE");
        request.setExternalId("GOOGLE_123456");

        // Act & Assert
        assertThrows(ValidationException.class, () -> portalUserService.createOrGetUser(request));

        // Verify - verify metot çağrımlarını kaldır, çünkü ValidationException anında fırlatılıyor
        // ve repository çağrıları yapılmadan önce kontrol gerçekleşiyor
    }

    @Test
    @DisplayName("Should validate user credentials successfully")
    void validate_WithValidCredentials_ReturnsTrue() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        PortalUser portalUser = TestDataFactory.createPortalUser();
        portalUser.setUsername(username);
        portalUser.setPassword("encoded_password");
        portalUser.setActive(true);

        when(portalUserRepository.findByUsername(username)).thenReturn(Optional.of(portalUser));
        when(passwordEncoder.matches(password, portalUser.getPassword())).thenReturn(true);

        // Act
        boolean result = portalUserService.validate(username, password);

        // Assert
        assertTrue(result);

        // Verify
        verify(portalUserRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, portalUser.getPassword());
    }

    @Test
    @DisplayName("Should fail validation when user does not exist")
    void validate_WithNonExistingUser_ReturnsFalse() {
        // Arrange
        String username = "nonexistent";
        String password = "password123";

        when(portalUserRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean result = portalUserService.validate(username, password);

        // Assert
        assertFalse(result);

        // Verify
        verify(portalUserRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should fail validation when user is inactive")
    void validate_WithInactiveUser_ReturnsFalse() {
        // Arrange
        String username = "inactiveuser";
        String password = "password123";
        PortalUser portalUser = TestDataFactory.createPortalUser();
        portalUser.setUsername(username);
        portalUser.setActive(false);

        when(portalUserRepository.findByUsername(username)).thenReturn(Optional.of(portalUser));

        // Act
        boolean result = portalUserService.validate(username, password);

        // Assert
        assertFalse(result);

        // Verify
        verify(portalUserRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should fail validation when password is incorrect")
    void validate_WithIncorrectPassword_ReturnsFalse() {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        PortalUser portalUser = TestDataFactory.createPortalUser();
        portalUser.setUsername(username);
        portalUser.setPassword("encoded_password");
        portalUser.setActive(true);

        when(portalUserRepository.findByUsername(username)).thenReturn(Optional.of(portalUser));
        when(passwordEncoder.matches(password, portalUser.getPassword())).thenReturn(false);

        // Act
        boolean result = portalUserService.validate(username, password);

        // Assert
        assertFalse(result);

        // Verify
        verify(portalUserRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, portalUser.getPassword());
    }
}
