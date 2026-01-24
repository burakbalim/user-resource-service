package com.thales.user_resource_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for user creation or retrieval requests from external authentication providers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateOrGetRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3-50 characters")
    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Auth provider could not be empty")
    @JsonProperty("auth_provider")
    private String authProvider;

    @JsonProperty("external_id")
    private String externalId;

    @Past(message = "Birth date must be in the past")
    @JsonProperty("birth_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonProperty("password")
    private String password;
}
