package com.hospedaje.catalog.controller;

import com.hospedaje.catalog.model.Property;
import com.hospedaje.catalog.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for the Property Catalog.
 * <p>
 * Exposes endpoints for CRUD operations and search/filtering.
 * All endpoints are prefixed with {@code /api/properties}.
 */
@Slf4j
@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    // ───────────────────────── LIST ALL ─────────────────────────

    /**
     * GET /api/properties
     * Retrieve every property in the catalog.
     */
    @GetMapping
    public ResponseEntity<List<Property>> getAllProperties() {
        log.debug("GET /api/properties — fetching all");
        return ResponseEntity.ok(propertyService.findAll());
    }

    // ───────────────────────── GET BY ID ────────────────────────

    /**
     * GET /api/properties/{id}
     * Retrieve a single property by its MongoDB ObjectId.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Property> getPropertyById(@PathVariable String id) {
        log.debug("GET /api/properties/{}", id);
        return propertyService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ───────────────────────── SEARCH ──────────────────────────

    /**
     * GET /api/properties/search?location=...&roomType=...&maxPrice=...&checkIn=...&checkOut=...
     * <p>
     * Searches properties by any combination of filters.
     * All parameters are optional; omitted parameters are ignored.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Property>> searchProperties(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        log.debug("GET /api/properties/search — location={}, roomType={}, " +
                  "maxPrice={}, checkIn={}, checkOut={}",
                  location, roomType, maxPrice, checkIn, checkOut);

        List<Property> results =
                propertyService.search(location, roomType, maxPrice, checkIn, checkOut);
        return ResponseEntity.ok(results);
    }

    // ───────────────────────── CREATE ──────────────────────────

    /**
     * POST /api/properties
     * Create a new property listing in the catalog.
     */
    @PostMapping
    public ResponseEntity<Property> createProperty(@RequestBody Property property) {
        log.debug("POST /api/properties — creating {}", property.getName());
        Property created = propertyService.create(property);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ───────────────────────── UPDATE ──────────────────────────

    /**
     * PUT /api/properties/{id}
     * Update an existing property. Returns 404 if the ID doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(@PathVariable String id,
                                                   @RequestBody Property property) {
        log.debug("PUT /api/properties/{}", id);
        return propertyService.update(id, property)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ───────────────────────── DELETE ──────────────────────────

    /**
     * DELETE /api/properties/{id}
     * Remove a property from the catalog.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable String id) {
        log.debug("DELETE /api/properties/{}", id);
        propertyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
