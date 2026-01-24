package com.thales.user_resource_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user verification request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyUserRequest {

    @NotBlank(message = "Verification token cannot be empty")
    @JsonProperty("token")
    private String token;
    
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Please enter a valid email address")
    @JsonProperty("email")
    private String email;
} 