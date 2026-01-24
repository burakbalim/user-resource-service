package com.thales.user_resource_service.controller;

import com.thales.user_resource_service.dto.CreateTempUserResponse;
import com.thales.user_resource_service.dto.TempUserCreateRequest;
import com.thales.user_resource_service.dto.TempVerifyUserRequest;
import com.thales.user_resource_service.dto.UserResponse;
import com.thales.user_resource_service.service.TempUserService;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for temporary user operations.
 */
@RestController
@RequestMapping("/api/v1/temp-users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Temporary User Management", description = "API endpoints for temporary user management")
public class TempUserController {

    private final TempUserService tempUserService;

    @PostMapping
    @Operation(summary = "Create temporary user", description = "Creates a temporary user and sends verification code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Temporary user created",
                    content = @Content(schema = @Schema(implementation = CreateTempUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<CreateTempUserResponse> createTempUser(@Valid @RequestBody
                                                              @Parameter(description = "User information", required = true)
                                                              TempUserCreateRequest request) {
        log.info("Temporary user creation request received");

        CreateTempUserResponse response = tempUserService.createTempUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify temporary user", description = "Verifies a temporary user and creates a permanent user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Verification code not found")
    })
    public ResponseEntity<UserResponse> verifyTempUser(@Valid @RequestBody
                                                     @Parameter(description = "Verification details", required = true)
                                                     TempVerifyUserRequest request) {
        log.info("Temporary user verification request received");
        UserResponse response = tempUserService.verifyAndCreateUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
