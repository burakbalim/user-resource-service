package com.thales.user_resource_service.service.impl;

import com.thales.common.web.URLUtil;
import com.thales.user_resource_service.client.EmailRequest;
import com.thales.user_resource_service.client.EmailType;
import com.thales.user_resource_service.client.NotificationClient;
import com.thales.user_resource_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.thales.user_resource_service.cache.TempUserCacheUtil.getDefaultExpiryMinutes;
import static com.thales.user_resource_service.cache.TokenCacheUtil.DEFAULT_EXPIRY_HOURS;

/**
 * Implementation of the EmailService interface.
 * Handles sending various types of emails through the NotificationClient.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final NotificationClient notificationClient;

    private final URLUtil urlUtil;

    @Override
    public boolean sendPasswordResetEmail(String email, String username, String resetToken) {
        log.info("Sending password reset email to: {}", email);

        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("resetLink", urlUtil.buildUrl(String.format("resetPassword/%s?email=%s", resetToken, email)));
        templateParams.put("expiryHours", String.valueOf(DEFAULT_EXPIRY_HOURS));

        EmailRequest emailRequest = EmailRequest.builder()
                .emailType(EmailType.PASSWORD_RESET)
                .to(email)
                .subject("Password Reset Request")
                .templateFieldValues(templateParams)
                .build();

        return sendEmail(emailRequest);
    }

    @Override
    public boolean sendPasswordChangedEmail(String email, String username) {
        log.info("Sending password changed confirmation email to: {}", email);

        EmailRequest emailRequest = EmailRequest.builder()
                .emailType(EmailType.PASSWORD_CHANGED)
                .to(email)
                .subject("Your Password Has Been Changed")
                .templateFieldValues(new HashMap<>())
                .build();

        return sendEmail(emailRequest);
    }

    @Override
    public boolean sendCustomVerificationEmail(String email, String username, String verificationCode) {
        log.info("Sending custom verification email to: {}", email);

        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("verificationLink", urlUtil.buildUrl("/confirmation/" + verificationCode));
        templateParams.put("expiryMinutes", String.valueOf(getDefaultExpiryMinutes()));

        EmailRequest emailRequest = EmailRequest.builder()
                .emailType(EmailType.ACCOUNT_CONFIRMATION)
                .to(email)
                .subject("Your Account Verification Code")
                .templateFieldValues(templateParams)
                .build();

        return sendEmail(emailRequest);
    }

    /**
     * Helper method to send an email and handle the response.
     *
     * @param emailRequest The email request to send
     * @return true if the email was sent successfully, false otherwise
     */
    private boolean sendEmail(EmailRequest emailRequest) {
        try {
            ResponseEntity<String> response = notificationClient.sendEmail(emailRequest);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent successfully to: {}", emailRequest.getTo());
                return true;
            } else {
                log.error("Failed to send email. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("Error sending email to: {}", emailRequest.getTo(), e);
            return false;
        }
    }
}
