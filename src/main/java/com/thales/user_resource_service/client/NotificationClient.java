package com.thales.user_resource_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the notification service.
 * This interface abstracts API calls to the notification service.
 */
@FeignClient(name = "notification-service", url = "${services.notification-service.url}")
public interface NotificationClient {

    /**
     * Sends an email using the notification service.
     *
     * @param request The email request containing recipient, template and parameters
     * @return Response from the notification service
     */
    @PostMapping("/v1/emails/send")
    ResponseEntity<String> sendEmail(@RequestBody EmailRequest request);
} 