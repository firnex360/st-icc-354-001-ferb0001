package com.hospedaje.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object that mirrors the essential fields
 * returned by the Catalog Service's {@code GET /api/properties/{id}} endpoint.
 * <p>
 * Used by the Feign Client to deserialize the catalog response
 * when verifying a property's existence and retrieving its price.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDto {

    private String id;
    private String name;
    private String description;
    private String location;
    private String roomType;
    private List<String> amenities;
    private List<String> imageUrls;
    private Double pricePerNight;
    private LocalDate availableFrom;
    private LocalDate availableTo;
    private Integer maxGuests;
    private Boolean active;
}
