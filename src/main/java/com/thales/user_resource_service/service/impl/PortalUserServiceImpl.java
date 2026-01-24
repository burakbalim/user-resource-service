package com.thales.user_resource_service.service.impl;

import com.thales.user_resource_service.dto.PortalUserCreateRequest;
import com.thales.user_resource_service.dto.PortalUserResponse;
import com.thales.user_resource_service.dto.UserCreateOrGetRequest;
import com.thales.user_resource_service.exception.ValidationException;
import com.thales.user_resource_service.mapper.PortalUserMapper;
import com.thales.user_resource_service.model.PortalUser;
import com.thales.user_resource_service.repository.PortalUserRepository;
import com.thales.user_resource_service.service.PortalUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PortalUserServiceImpl implements PortalUserService {

    private final PortalUserRepository portalUserRepository;
    private final PortalUserMapper portalUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${portal.user.allowed-domain:thales.com}")
    private String allowedDomain;

    @Autowired
    public PortalUserServiceImpl(PortalUserRepository portalUserRepository,
                                PortalUserMapper portalUserMapper,
                                PasswordEncoder passwordEncoder) {
        this.portalUserRepository = portalUserRepository;
        this.portalUserMapper = portalUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PortalUserResponse createPortalUser(PortalUserCreateRequest request) {
        if (!isValidEmailDomain(request.getEmail())) {
            throw new IllegalArgumentException("Email domain not allowed. Only " + allowedDomain + " domain is permitted.");
        }
        if (portalUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (portalUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        PortalUser portalUser = portalUserMapper.toEntity(request);
        PortalUser savedPortalUser = portalUserRepository.save(portalUser);
        return portalUserMapper.toResponse(savedPortalUser);
    }

    @Override
    public PortalUserResponse getPortalUserById(Long id) {
        PortalUser portalUser = portalUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Portal user not found with id: " + id));
        return portalUserMapper.toResponse(portalUser);
    }

    @Override
    public PortalUserResponse getPortalUserByUsername(String username) {
        PortalUser portalUser = portalUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Portal user not found with username: " + username));
        return portalUserMapper.toResponse(portalUser);
    }

    @Override
    public PortalUserResponse getPortalUserByEmail(String email) {
        PortalUser portalUser = portalUserRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Portal user not found with email: " + email));
        return portalUserMapper.toResponse(portalUser);
    }

    @Override
    public List<PortalUserResponse> getAllPortalUsers() {
        return portalUserRepository.findAll().stream()
                .map(portalUserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PortalUserResponse createOrGetUser(UserCreateOrGetRequest request) {
        log.info("CreateOrGet request received for portal user: {}, provider: {}",
                request.getEmail() != null ? request.getEmail() : request.getUsername(),
                request.getAuthProvider());

        Optional<PortalUser> existingUser = getExistingUser(request);
        if (existingUser.isPresent()) {
            PortalUser user = existingUser.get();
            // Update existing user with new provider information
            user.setAuthProvider(request.getAuthProvider());
            user.setExternalId(request.getExternalId());
            PortalUser updatedUser = portalUserRepository.save(user);
            log.info("Existing portal user updated with provider: {}", request.getAuthProvider());
            return portalUserMapper.toResponse(updatedUser);
        } else {
            return createExternalUser(request);
        }
    }

    @Override
    public boolean validate(String username, String password) {
        log.info("Validating portal user credentials for username: {}", username);

        Optional<PortalUser> userOptional = portalUserRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.warn("User not found with username: {}", username);
            return false;
        }

        PortalUser user = userOptional.get();
        if (!user.isActive()) {
            log.warn("User account is not active: {}", username);
            return false;
        }

        boolean isValid = passwordEncoder.matches(password, user.getPassword());
        log.info("Password validation result for user {}: {}", username, isValid ? "success" : "failed");
        return isValid;
    }

    @Override
    public boolean isValidEmailDomain(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        int atIndex = email.lastIndexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return false;
        }

        String domain = email.substring(atIndex + 1);
        return domain.equalsIgnoreCase(allowedDomain);
    }

    private Optional<PortalUser> getExistingUser(UserCreateOrGetRequest request) {
        if (request.getEmail() != null) {
            return portalUserRepository.findByEmail(request.getEmail());
        } else if (request.getUsername() != null) {
            return portalUserRepository.findByUsername(request.getUsername());
        } else {
            throw new ValidationException("User or email cannot be empty");
        }
    }

    private PortalUserResponse createExternalUser(UserCreateOrGetRequest request) {
        log.info("Creating new portal user from external provider: {}",
                request.getEmail() != null ? request.getEmail() : request.getUsername());

        // Validate email domain if it's not null
        if (request.getEmail() != null && !isValidEmailDomain(request.getEmail())) {
            throw new ValidationException("Email domain not allowed. Only " + allowedDomain + " domain is permitted.");
        }

        // Create new user from request
        PortalUserCreateRequest portalUserRequest = PortalUserCreateRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) // This might be null for OAuth users
                .authProvider(request.getAuthProvider())
                .externalId(request.getExternalId())
                .birthDate(request.getBirthDate())
                .fullName(request.getUsername()) // Default to username if full name is not provided
                .department("") // Default empty values
                .role("USER") // Default role
                .build();

        PortalUser portalUser = portalUserMapper.toEntity(portalUserRequest);
        PortalUser savedUser = portalUserRepository.save(portalUser);
        log.info("New portal user successfully created: {}",
                savedUser.getEmail() != null ? savedUser.getEmail() : savedUser.getUsername());

        return portalUserMapper.toResponse(savedUser);
    }
}
