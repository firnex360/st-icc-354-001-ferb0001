package com.hotel.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/api/properties")
    List<Map<String, Object>> getProperties();

    @GetMapping("/api/properties/{id}")
    Map<String, Object> getPropertyById(@PathVariable("id") String id);
}
