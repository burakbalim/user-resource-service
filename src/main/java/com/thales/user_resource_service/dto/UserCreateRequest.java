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
 * Data Transfer Object for user creation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3-50 characters")
    @JsonProperty("username")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please enter a valid email address")
    @JsonProperty("email")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty("password")
    private String password;

    @Past(message = "Birth date must be in the past")
    @JsonProperty("birth_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
