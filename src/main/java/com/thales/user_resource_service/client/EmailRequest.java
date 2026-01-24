package com.thales.user_resource_service.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO class for email sending requests to notification service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotNull(message = "Email type must be specified")
    @JsonProperty("emailType")
    private EmailType emailType;

    @NotBlank(message = "Recipient email address cannot be empty")
    @Email(message = "A valid email address must be provided")
    @JsonProperty("to")
    private String to;

    @JsonProperty("subject")
    private String subject;

    @NotNull(message = "Template field values must be specified")
    @JsonProperty("templateFieldValues")
    private Map<String, String> templateFieldValues;

    @JsonProperty("cc")
    private String[] cc;

    @JsonProperty("bcc")
    private String[] bcc;
} 