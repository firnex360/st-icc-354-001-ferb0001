package com.hospedaje.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notifications & Reports Microservice.
 * <p>
 * Handles transactional emails (welcome, booking confirmations)
 * and generates PDF invoices using JasperReports for the
 * hotel accommodation platform.
 * <p>
 * Runs on port 8085 and registers as {@code NOTIFICATIONS-SERVICE} in Eureka.
 */
@SpringBootApplication
public class NotificationsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationsServiceApplication.class, args);
    }
}
