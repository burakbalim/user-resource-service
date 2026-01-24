package com.thales.user_resource_service.service.impl;

import com.thales.user_resource_service.UserResourceServiceApplication;
import com.thales.user_resource_service.dto.PortalUserCreateRequest;
import com.thales.user_resource_service.dto.PortalUserResponse;
import com.thales.user_resource_service.dto.UserCreateOrGetRequest;
import com.thales.user_resource_service.exception.ValidationException;
import com.thales.user_resource_service.model.PortalUser;
import com.thales.user_resource_service.repository.PortalUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = UserResourceServiceApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "portal.user.allowed-domain=thales.com"
})
@Transactional
public class PortalUserServiceImplIntegrationTest {

    @Autowired
    private PortalUserServiceImpl portalUserService;

    @Autowired
    private PortalUserRepository portalUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${portal.user.allowed-domain}")
    private String allowedDomain;

    @MockBean
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        portalUserRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        portalUserRepository.deleteAll();
    }

    @Test
    void createPortalUser_Success() {
        // Arrange
        PortalUserCreateRequest request = new PortalUserCreateRequest();
        request.setUsername("portaluser");
        request.setEmail("portaluser@thales.com");
        request.setPassword("password123");
        request.setFullName("Portal User");
        request.setDepartment("IT");
        request.setRole("DEVELOPER");

        // Act
        PortalUserResponse response = portalUserService.createPortalUser(request);

        // Assert
        assertNotNull(response);
        assertEquals("portaluser", response.getUsername());
        assertEquals("portaluser@thales.com", response.getEmail());
        assertEquals("Portal User", response.getFullName());
        assertEquals("IT", response.getDepartment());
        assertEquals("DEVELOPER", response.getRole());
        assertTrue(response.isActive());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        // Verify in database
        assertTrue(portalUserRepository.existsByUsername("portaluser"));
        assertTrue(portalUserRepository.existsByEmail("portaluser@thales.com"));
    }

    @Test
    void createPortalUser_WithInvalidDomain_ThrowsException() {
        // Arrange
        PortalUserCreateRequest request = new PortalUserCreateRequest();
        request.setUsername("portaluser");
        request.setEmail("portaluser@invalid.com");
        request.setPassword("password123");
        request.setFullName("Portal User");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portalUserService.createPortalUser(request)
        );

        assertTrue(exception.getMessage().contains("Email domain not allowed"));
        assertEquals("Email domain not allowed. Only " + allowedDomain + " domain is permitted.", exception.getMessage());

        // Verify in database
        assertFalse(portalUserRepository.existsByUsername("portaluser"));
    }

    @Test
    void createPortalUser_WithExistingUsername_ThrowsException() {
        // Arrange - Create initial user
        PortalUser existingUser = new PortalUser();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@thales.com");
        existingUser.setPassword("password");
        existingUser.setFullName("Existing User");
        existingUser.setActive(true);
        portalUserRepository.save(existingUser);

        // Create request with same username
        PortalUserCreateRequest request = new PortalUserCreateRequest();
        request.setUsername("existinguser");
        request.setEmail("new@thales.com");
        request.setPassword("password123");
        request.setFullName("New User");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portalUserService.createPortalUser(request)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));

        // Verify in database - only original user exists
        assertEquals(1, portalUserRepository.count());
        assertTrue(portalUserRepository.existsByEmail("existing@thales.com"));
        assertFalse(portalUserRepository.existsByEmail("new@thales.com"));
    }

    @Test
    void createPortalUser_WithExistingEmail_ThrowsException() {
        // Arrange - Create initial user
        PortalUser existingUser = new PortalUser();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("same@thales.com");
        existingUser.setPassword("password");
        existingUser.setFullName("Existing User");
        existingUser.setActive(true);
        portalUserRepository.save(existingUser);

        // Create request with same email
        PortalUserCreateRequest request = new PortalUserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("same@thales.com");
        request.setPassword("password123");
        request.setFullName("New User");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portalUserService.createPortalUser(request)
        );

        assertTrue(exception.getMessage().contains("Email already exists"));

        // Verify in database - only original user exists
        assertEquals(1, portalUserRepository.count());
        assertTrue(portalUserRepository.existsByUsername("existinguser"));
        assertFalse(portalUserRepository.existsByUsername("newuser"));
    }

    @Test
    void getPortalUserById_WithExistingId_Success() {
        // Arrange - Create user
        PortalUser user = new PortalUser();
        user.setUsername("testuser");
        user.setEmail("test@thales.com");
        user.setPassword("password");
        user.setFullName("Test User");
        user.setActive(true);
        user = portalUserRepository.save(user);

        // Act
        PortalUserResponse response = portalUserService.getPortalUserById(user.getId());

        // Assert
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@thales.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    void getPortalUserById_WithNonExistingId_ThrowsException() {
        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> portalUserService.getPortalUserById(999L)
        );
    }

    @Test
    void getPortalUserByUsername_WithExistingUsername_Success() {
        // Arrange - Create user
        PortalUser user = new PortalUser();
        user.setUsername("testuser");
        user.setEmail("test@thales.com");
        user.setPassword("password");
        user.setFullName("Test User");
        user.setActive(true);
        portalUserRepository.save(user);

        // Act
        PortalUserResponse response = portalUserService.getPortalUserByUsername("testuser");

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@thales.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    void getPortalUserByUsername_WithNonExistingUsername_ThrowsException() {
        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> portalUserService.getPortalUserByUsername("nonexistent")
        );
    }

    @Test
    void getPortalUserByEmail_WithExistingEmail_Success() {
        // Arrange - Create user
        PortalUser user = new PortalUser();
        user.setUsername("testuser");
        user.setEmail("test@thales.com");
        user.setPassword("password");
        user.setFullName("Test User");
        user.setActive(true);
        portalUserRepository.save(user);

        // Act
        PortalUserResponse response = portalUserService.getPortalUserByEmail("test@thales.com");

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@thales.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    void getPortalUserByEmail_WithNonExistingEmail_ThrowsException() {
        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> portalUserService.getPortalUserByEmail("nonexistent@thales.com")
        );
    }

    @Test
    void getAllPortalUsers_Success() {
        // Arrange - Create multiple users
        PortalUser user1 = new PortalUser();
        user1.setUsername("user1");
        user1.setEmail("user1@thales.com");
        user1.setPassword("password");
        user1.setFullName("User One");
        user1.setActive(true);
        portalUserRepository.save(user1);

        PortalUser user2 = new PortalUser();
        user2.setUsername("user2");
        user2.setEmail("user2@thales.com");
        user2.setPassword("password");
        user2.setFullName("User Two");
        user2.setActive(true);
        portalUserRepository.save(user2);

        // Act
        List<PortalUserResponse> responses = portalUserService.getAllPortalUsers();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());

        // Verify both users are in the result (order might vary)
        boolean foundUser1 = false;
        boolean foundUser2 = false;

        for (PortalUserResponse response : responses) {
            if ("user1".equals(response.getUsername())) {
                foundUser1 = true;
                assertEquals("user1@thales.com", response.getEmail());
                assertEquals("User One", response.getFullName());
            } else if ("user2".equals(response.getUsername())) {
                foundUser2 = true;
                assertEquals("user2@thales.com", response.getEmail());
                assertEquals("User Two", response.getFullName());
            }
        }

        assertTrue(foundUser1);
        assertTrue(foundUser2);
    }

    @Test
    void getAllPortalUsers_WithNoUsers_ReturnsEmptyList() {
        // Act
        List<PortalUserResponse> responses = portalUserService.getAllPortalUsers();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void isValidEmailDomain_WithValidDomain_ReturnsTrue() {
        // Act & Assert
        assertTrue(portalUserService.isValidEmailDomain("test@thales.com"));
    }

    @Test
    void isValidEmailDomain_WithInvalidDomain_ReturnsFalse() {
        // Act & Assert
        assertFalse(portalUserService.isValidEmailDomain("test@invalid.com"));
    }

    @Test
    void isValidEmailDomain_WithInvalidEmailFormat_ReturnsFalse() {
        // Act & Assert
        assertFalse(portalUserService.isValidEmailDomain("invalidemail"));
        assertFalse(portalUserService.isValidEmailDomain("invalid@"));
    }

    @Test
    void createOrGetUser_WithNewUser_Success() {
        // Arrange
        UserCreateOrGetRequest request = new UserCreateOrGetRequest();
        request.setUsername("newportaluser");
        request.setEmail("newportaluser@thales.com");
        request.setAuthProvider("GOOGLE");
        request.setExternalId("google_id_123");
        request.setPassword("defaultpassword");
        
        // Act
        PortalUserResponse response = portalUserService.createOrGetUser(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("newportaluser", response.getUsername());
        assertEquals("newportaluser@thales.com", response.getEmail());
        assertEquals("GOOGLE", response.getAuthProvider());
        assertEquals("google_id_123", response.getExternalId());
        assertTrue(response.isActive());
        
        // Verify in database
        assertTrue(portalUserRepository.existsByUsername("newportaluser"));
        assertTrue(portalUserRepository.existsByEmail("newportaluser@thales.com"));
    }
    
    @Test
    void createOrGetUser_WithExistingUserByEmail_UpdatesUser() {
        // Arrange - Create existing user
        PortalUser existingUser = new PortalUser();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existinguser@thales.com");
        existingUser.setPassword("password");
        existingUser.setAuthProvider("LOCAL");
        existingUser.setActive(true);
        portalUserRepository.save(existingUser);
        
        // Create request with same email but different provider
        UserCreateOrGetRequest request = new UserCreateOrGetRequest();
        request.setEmail("existinguser@thales.com");
        request.setAuthProvider("FACEBOOK");
        request.setExternalId("facebook_id_123");
        
        // Act
        PortalUserResponse response = portalUserService.createOrGetUser(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("existinguser", response.getUsername());
        assertEquals("existinguser@thales.com", response.getEmail());
        assertEquals("FACEBOOK", response.getAuthProvider());
        assertEquals("facebook_id_123", response.getExternalId());
        
        // Verify in database
        Optional<PortalUser> updatedUserOpt = portalUserRepository.findByEmail("existinguser@thales.com");
        assertTrue(updatedUserOpt.isPresent());
        
        PortalUser updatedUser = updatedUserOpt.get();
        assertEquals("FACEBOOK", updatedUser.getAuthProvider());
        assertEquals("facebook_id_123", updatedUser.getExternalId());
    }
    
    @Test
    void createOrGetUser_WithExistingUserByUsername_UpdatesUser() {
        // Arrange - Create existing user
        PortalUser existingUser = new PortalUser();
        existingUser.setUsername("existinguser2");
        existingUser.setEmail("existinguser2@thales.com");
        existingUser.setPassword("password");
        existingUser.setAuthProvider("LOCAL");
        existingUser.setActive(true);
        portalUserRepository.save(existingUser);
        
        // Create request with same username but different provider
        UserCreateOrGetRequest request = new UserCreateOrGetRequest();
        request.setUsername("existinguser2");
        request.setAuthProvider("TWITTER");
        request.setExternalId("twitter_id_123");
        
        // Act
        PortalUserResponse response = portalUserService.createOrGetUser(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("existinguser2", response.getUsername());
        assertEquals("existinguser2@thales.com", response.getEmail());
        assertEquals("TWITTER", response.getAuthProvider());
        assertEquals("twitter_id_123", response.getExternalId());
        
        // Verify in database
        Optional<PortalUser> updatedUserOpt = portalUserRepository.findByUsername("existinguser2");
        assertTrue(updatedUserOpt.isPresent());
        
        PortalUser updatedUser = updatedUserOpt.get();
        assertEquals("TWITTER", updatedUser.getAuthProvider());
        assertEquals("twitter_id_123", updatedUser.getExternalId());
    }
    
    @Test
    void createOrGetUser_WithInvalidEmailDomain_ThrowsException() {
        // Arrange
        UserCreateOrGetRequest request = new UserCreateOrGetRequest();
        request.setUsername("invaliduser");
        request.setEmail("invaliduser@invalid.com");
        request.setAuthProvider("GOOGLE");
        
        // Act & Assert
        Exception exception = assertThrows(
            ValidationException.class,
            () -> portalUserService.createOrGetUser(request)
        );
        
        assertTrue(exception.getMessage().contains("Email domain not allowed"));
        
        // Verify user was not created
        assertFalse(portalUserRepository.existsByUsername("invaliduser"));
    }
    
    @Test
    void validate_WithValidCredentials_ReturnsTrue() {
        // Arrange - Create user with encoded password
        String plainPassword = "password123";
        
        PortalUser user = new PortalUser();
        user.setUsername("validuser");
        user.setEmail("validuser@thales.com");
        
        // Kullanılan gerçek şifre kodlayıcısını kullanarak şifreyi kodla
        String encodedPassword = passwordEncoder.encode(plainPassword);
        user.setPassword(encodedPassword);
        user.setActive(true);
        portalUserRepository.save(user);
        
        // Act
        boolean result = portalUserService.validate("validuser", plainPassword);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void validate_WithNonExistingUser_ReturnsFalse() {
        // Act
        boolean result = portalUserService.validate("nonexistentuser", "password123");
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void validate_WithInactiveUser_ReturnsFalse() {
        // Arrange - Create inactive user
        PortalUser user = new PortalUser();
        user.setUsername("inactiveuser");
        user.setEmail("inactiveuser@thales.com");
        user.setPassword("$2a$10$tGr7DT5D.R3Fo.n0/9jkYOFwMI2UcSHYfKabGI9UFW9ZIAowQPXWK");
        user.setActive(false);
        portalUserRepository.save(user);
        
        // Act
        boolean result = portalUserService.validate("inactiveuser", "password123");
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void validate_WithIncorrectPassword_ReturnsFalse() {
        // Arrange - Create user
        PortalUser user = new PortalUser();
        user.setUsername("user");
        user.setEmail("user@thales.com");
        user.setPassword("$2a$10$tGr7DT5D.R3Fo.n0/9jkYOFwMI2UcSHYfKabGI9UFW9ZIAowQPXWK"); // "password123" encoded
        user.setActive(true);
        portalUserRepository.save(user);
        
        // Act
        boolean result = portalUserService.validate("user", "wrongpassword");
        
        // Assert
        assertFalse(result);
    }
}
 