package com.thales.user_resource_service.controller;

import com.thales.user_resource_service.dto.PortalUserCreateRequest;
import com.thales.user_resource_service.dto.PortalUserResponse;
import com.thales.user_resource_service.dto.UserCreateOrGetRequest;
import com.thales.user_resource_service.dto.UserResponse;
import com.thales.user_resource_service.service.PortalUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portal/users")
public class UserPortalController {

    private final PortalUserService portalUserService;

    @Autowired
    public UserPortalController(PortalUserService portalUserService) {
        this.portalUserService = portalUserService;
    }

    @PostMapping
    public ResponseEntity<PortalUserResponse> createPortalUser(@Valid @RequestBody PortalUserCreateRequest request) {
        PortalUserResponse response = portalUserService.createPortalUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/create-or-get")
    @Operation(summary = "Create or get user", description = "Finds or creates a user based on email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found or created",
                    content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    public ResponseEntity<PortalUserResponse> createOrGetUser(@Valid @RequestBody
                                                        @Parameter(description = "User information", required = true)
                                                        UserCreateOrGetRequest request) {
        PortalUserResponse response = portalUserService.createOrGetUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate user", description = "Validates username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation result")
    })
    public ResponseEntity<Boolean> validate(
            @Valid @RequestParam("username") @Parameter(description = "Username", required = true) String username,
            @Valid @RequestParam("password") @Parameter(description = "Password", required = true) String password) {
        boolean isValid = portalUserService.validate(username, password);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PortalUserResponse> getPortalUserById(@PathVariable Long id) {
        PortalUserResponse response = portalUserService.getPortalUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PortalUserResponse> getPortalUserByUsername(@PathVariable String username) {
        PortalUserResponse response = portalUserService.getPortalUserByUsername(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PortalUserResponse> getPortalUserByEmail(@PathVariable String email) {
        PortalUserResponse response = portalUserService.getPortalUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PortalUserResponse>> getAllPortalUsers() {
        List<PortalUserResponse> responses = portalUserService.getAllPortalUsers();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/validate-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> validateEmailDomain(@RequestParam String email) {
        boolean isValid = portalUserService.isValidEmailDomain(email);
        return ResponseEntity.ok(isValid);
    }
}
