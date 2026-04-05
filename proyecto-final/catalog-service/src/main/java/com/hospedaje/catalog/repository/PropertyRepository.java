package com.hospedaje.catalog.repository;

import com.hospedaje.catalog.model.Property;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * MongoDB repository for {@link Property} documents.
 * <p>
 * Provides built-in CRUD operations plus custom query methods
 * for searching properties by location, room type, price range,
 * and date availability.
 */
@Repository
public interface PropertyRepository extends MongoRepository<Property, String> {

    /**
     * Find properties by location (case-insensitive partial match).
     */
    List<Property> findByLocationContainingIgnoreCase(String location);

    /**
     * Find properties by room type (case-insensitive exact match).
     */
    List<Property> findByRoomTypeIgnoreCase(String roomType);

    /**
     * Find properties with a nightly rate at or below the given maximum.
     */
    List<Property> findByPricePerNightLessThanEqual(Double maxPrice);

    /**
     * Find active properties whose availability window covers the
     * requested check-in and check-out dates.
     * <p>
     * A property is available if:
     * <ul>
     *   <li>{@code availableFrom <= checkIn}</li>
     *   <li>{@code availableTo >= checkOut}</li>
     *   <li>{@code active == true}</li>
     * </ul>
     */
    List<Property> findByAvailableFromLessThanEqualAndAvailableToGreaterThanEqualAndActiveTrue(
            LocalDate checkIn, LocalDate checkOut);

    /**
     * Find all active properties.
     */
    List<Property> findByActiveTrue();
}
