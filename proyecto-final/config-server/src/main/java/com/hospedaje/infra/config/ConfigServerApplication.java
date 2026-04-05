package com.hospedaje.infra.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Configuration Server — Spring Cloud Config.
 * <p>
 * Centralizes externalized configuration for every microservice in the
 * hotel accommodation platform. In this development phase it uses the
 * {@code native} profile to read {@code .yml} files from the local file system.
 * <p>
 * Start this server AFTER the Discovery Server and BEFORE any business service.
 * <p>
 * Example: to fetch the Catalog Service config for the "default" profile, visit
 * <a href="http://localhost:8888/catalog-service/default">
 * http://localhost:8888/catalog-service/default</a>
 */
@SpringBootApplication
@EnableConfigServer   // Activates the embedded Config Server
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
