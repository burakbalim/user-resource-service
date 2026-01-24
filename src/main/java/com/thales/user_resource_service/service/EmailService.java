package com.thales.user_resource_service.service;

import java.util.Map;

/**
 * Service interface for email operations.
 */
public interface EmailService {

    /**
     * Sends a password reset email with the reset token.
     *
     * @param email The recipient's email address
     * @param username The recipient's username
     * @param resetToken The password reset token
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendPasswordResetEmail(String email, String username, String resetToken);

    /**
     * Sends a password change confirmation email.
     *
     * @param email The recipient's email address
     * @param username The recipient's username
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendPasswordChangedEmail(String email, String username);

    /**
     * Sends a custom verification email with additional parameters.
     *
     * @param email The recipient's email address
     * @param username The recipient's username
     * @param verificationCode The verification code
     * @param additionalParams Additional parameters for the email template
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendCustomVerificationEmail(String email, String username, String verificationCode);
}
