package com.hospedaje.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Property Catalog Microservice.
 * <p>
 * Manages the inventory of available properties (hotels, apartments, rooms)
 * for the hotel accommodation platform. Uses MongoDB as the backing store
 * and seeds mock data on first startup using Datafaker.
 * <p>
 * Runs on port 8082 and registers as {@code CATALOG-SERVICE} in Eureka.
 */
@SpringBootApplication
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
