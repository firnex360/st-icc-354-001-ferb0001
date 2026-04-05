package com.hospedaje.reservation.client;

import com.hospedaje.reservation.dto.PropertyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client for synchronous communication with the Property Catalog Service.
 * <p>
 * Uses the Eureka service name {@code CATALOG-SERVICE} so that Spring Cloud
 * LoadBalancer can resolve the actual host:port at runtime. This enables
 * transparent load balancing across multiple catalog instances.
 * <p>
 * Called before reservation creation to:
 * <ol>
 *   <li>Verify the requested property exists in the catalog</li>
 *   <li>Retrieve the nightly rate for total price calculation</li>
 * </ol>
 */
@FeignClient(name = "CATALOG-SERVICE")
public interface CatalogClient {

    /**
     * Fetch a property by its ID from the Catalog Service.
     *
     * @param id the MongoDB ObjectId of the property
     * @return the property details as a {@link PropertyDto}
     */
    @GetMapping("/api/properties/{id}")
    PropertyDto getPropertyById(@PathVariable("id") String id);
}
