package com.hospedaje.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Authentication & Security Microservice.
 * <p>
 * Provides user registration, login, and JWT generation for the
 * hotel accommodation platform. All other services delegate
 * authentication to this service via the API Gateway.
 * <p>
 * Runs on port 8081 and registers as {@code AUTH-SERVICE} in Eureka.
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
