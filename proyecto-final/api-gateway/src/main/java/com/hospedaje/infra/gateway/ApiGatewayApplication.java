package com.hospedaje.infra.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — Spring Cloud Gateway.
 * <p>
 * Acts as the single entry point for all client requests to the hotel
 * accommodation platform. Incoming requests are routed to the appropriate
 * downstream microservice using Eureka-based load balancing ({@code lb://}).
 * <p>
 * Features enabled:
 * <ul>
 *   <li>Path-based routing to all business microservices</li>
 *   <li>Service discovery via Eureka (automatic + explicit routes)</li>
 *   <li>Distributed tracing via Zipkin (Micrometer + Brave bridge)</li>
 *   <li>Actuator health and gateway route introspection endpoints</li>
 * </ul>
 * <p>
 * Start this server AFTER the Discovery Server and Config Server.
 * <p>
 * Gateway runs on: <a href="http://localhost:8080">http://localhost:8080</a>
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
