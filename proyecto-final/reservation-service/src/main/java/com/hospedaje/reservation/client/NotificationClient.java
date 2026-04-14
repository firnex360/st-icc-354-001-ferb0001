package com.hospedaje.reservation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for the Notifications Service.
 * Calls the service directly via Eureka, bypassing the API Gateway.
 * Used to send invoice emails after a reservation is marked PAID.
 */
@FeignClient(name = "notifications-service")
public interface NotificationClient {

    /**
     * POST /api/notifications/invoice/send
     * Generates a JasperReports PDF invoice and emails it to the customer.
     */
    @PostMapping("/api/notifications/invoice/send")
    Map<String, String> sendInvoice(@RequestBody Map<String, Object> invoiceRequest);
}
