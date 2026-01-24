package com.thales.user_resource_service.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for email types supported by the notification service.
 */
@Getter
@RequiredArgsConstructor
public enum EmailType {

    // Standard emails
    WELCOME("welcome-template", "Welcome"),
    PASSWORD_RESET("password-reset-template", "Password Reset"),
    ACCOUNT_VERIFICATION("account-verification-template", "Account Verification"),
    ACCOUNT_CONFIRMATION("account-confirmation-template", "Account Confirmation"),
    PASSWORD_CHANGED("password-changed-template", "Account Password Changed");

    private final String templateName;
    private final String defaultSubject;
}
