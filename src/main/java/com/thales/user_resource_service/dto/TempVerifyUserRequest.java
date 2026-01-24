package com.thales.user_resource_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for temporary user verification request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempVerifyUserRequest {

    @NotBlank(message = "Verification code cannot be empty")
    @JsonProperty("validation_code")
    private String validationCode;
} 