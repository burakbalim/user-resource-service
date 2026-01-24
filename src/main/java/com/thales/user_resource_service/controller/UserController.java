package com.thales.user_resource_service.controller;

import com.thales.user_resource_service.dto.*;
import com.thales.user_resource_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "API endpoints for user management")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create new user", description = "Creates a new user based on user information without confirmation, " +
            "It should just use internally")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody
                                                 @Parameter(description = "User information to create", required = true)
                                                 UserCreateRequest request) {
        log.info("User creation request received");
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/create-or-get")
    @Operation(summary = "Create or get user", description = "Finds or creates a user based on email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found or created",
                    content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    public ResponseEntity<UserResponse> createOrGetUser(@Valid @RequestBody
                                                      @Parameter(description = "User information", required = true)
                                                      UserCreateOrGetRequest request) {
        log.info("CreateOrGet request received: {}", request.getEmail());
        UserResponse response = userService.createOrGetUser(request);
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
        boolean isValid = userService.validate(username, password);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/base/{username}")
    @Operation(summary = "Get user base information", description = "Retrieves basic user information based on username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User base information found",
                    content = @Content(schema = @Schema(implementation = UserBaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserBaseResponse> getUserBaseResponse(
            @PathVariable("username") @Parameter(description = "Username", required = true) String username) {
        UserBaseResponse userBaseResponse = userService.getUserBaseResponse(username);
        return ResponseEntity.ok(userBaseResponse);
    }

    @GetMapping("/id/{username}")
    @Operation(summary = "Get user ID", description = "Retrieves user ID based on username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User ID found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Long> getUserIdByUsername(
            @PathVariable("username") @Parameter(description = "Username", required = true) String username) {
        Long userId = userService.getUserIdByUsername(username);
        return ResponseEntity.ok(userId);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Searches users based on given prefix")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results",
                    content = @Content(schema = @Schema(implementation = UserBaseResponse.class)))
    })
    public ResponseEntity<List<UserBaseResponse>> searchUsersByUsernamePrefix(
            @RequestParam("prefix") @Parameter(description = "Username prefix", required = true) String prefix) {
        log.info("Searching users with prefix: {}", prefix);
        List<UserBaseResponse> users = userService.searchUsersByUsernamePrefix(prefix);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/bulk/username-id")
    @Operation(summary = "Get usernames and IDs in bulk", description = "Retrieves username and ID pairs for given user IDs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved username and ID pairs",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Map<Long, String>> getUsernameIdPairs(
            @RequestBody @Parameter(description = "List of user IDs", required = true) List<Long> userIds) {
        log.info("Bulk request received for {} user IDs", userIds.size());
        Map<Long, String> usernameIdPairs = userService.getUsernameIdPairs(userIds);
        return ResponseEntity.ok(usernameIdPairs);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot Password", description = "Sends password reset link to user's email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Boolean> forgotPassword(
            @Valid @RequestBody @Parameter(description = "Password reset request", required = true)
            ForgotPasswordRequest request) {
        log.info("Password reset request received: {}", request.getEmail());
        boolean success = userService.forgotPassword(request);

        if (success) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Resets password using the provided token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully reset"),
            @ApiResponse(responseCode = "400", description = "Invalid request or token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Boolean> resetPassword(
            @Valid @RequestBody @Parameter(description = "Password reset details", required = true)
            ResetPasswordRequest request) {
        log.info("Processing password reset request");
        boolean success = userService.resetPassword(request);

        if (success) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
