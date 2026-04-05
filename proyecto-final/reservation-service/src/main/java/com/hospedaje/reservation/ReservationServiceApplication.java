package com.hospedaje.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Reservation Microservice.
 * <p>
 * Manages the full reservation lifecycle: creation, modification,
 * cancellation, and status tracking for the hotel accommodation platform.
 * Communicates with the Catalog Service via OpenFeign to verify property
 * existence and retrieve pricing before creating reservations.
 * <p>
 * Runs on port 8083 and registers as {@code RESERVATION-SERVICE} in Eureka.
 */
@SpringBootApplication
@EnableFeignClients   // Enables Feign client scanning for inter-service communication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}
