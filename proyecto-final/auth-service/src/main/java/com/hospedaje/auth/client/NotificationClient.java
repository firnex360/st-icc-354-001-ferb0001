package com.hospedaje.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for the Notifications Service.
 * Calls the service directly via Eureka (bypasses the API Gateway,
 * so no JWT is required for this internal call).
 */
@FeignClient(name = "notifications-service")
public interface NotificationClient {

    /**
     * POST /api/notifications/email
     * Sends a plain-text email. Payload keys: "to", "subject", "body".
     */
    @PostMapping("/api/notifications/email")
    Map<String, String> sendEmail(@RequestBody Map<String, String> request);
}
