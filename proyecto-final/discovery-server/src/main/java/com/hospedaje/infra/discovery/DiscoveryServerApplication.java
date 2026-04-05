package com.hospedaje.infra.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Discovery Server — Eureka Service Registry.
 * <p>
 * This is the central service registry for the hotel accommodation platform.
 * Every microservice registers itself here, enabling dynamic service discovery
 * via logical names (e.g., {@code lb://CATALOG-SERVICE}) instead of hardcoded URLs.
 * <p>
 * Start this server FIRST before launching any other service in the ecosystem.
 * <p>
 * Dashboard: <a href="http://localhost:8761">http://localhost:8761</a>
 */
@SpringBootApplication
@EnableEurekaServer   // Activates the embedded Eureka Server
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
