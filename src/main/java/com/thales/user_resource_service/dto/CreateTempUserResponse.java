package com.thales.user_resource_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for temporary user creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTempUserResponse {
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("expiry_seconds")
    private Long expirySeconds;
} 