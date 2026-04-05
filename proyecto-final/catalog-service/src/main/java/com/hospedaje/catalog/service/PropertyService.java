package com.hospedaje.catalog.service;

import com.hospedaje.catalog.model.Property;
import com.hospedaje.catalog.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business-logic layer for managing the property catalog.
 * <p>
 * Provides search, CRUD, and filtering operations over the
 * {@link Property} collection stored in MongoDB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;

    // ───────────────────────── Queries ──────────────────────────

    /**
     * Retrieve every property in the catalog.
     */
    public List<Property> findAll() {
        log.debug("Fetching all properties from catalog");
        return propertyRepository.findAll();
    }

    /**
     * Retrieve a single property by its ID.
     */
    public Optional<Property> findById(String id) {
        log.debug("Looking up property with id={}", id);
        return propertyRepository.findById(id);
    }

    /**
     * Search properties that match any combination of the given filters.
     * <p>
     * Filters are applied independently and combined by intersection.
     * Any filter parameter that is {@code null} is simply ignored.
     *
     * @param location  partial location name (case-insensitive)
     * @param roomType  exact room type (case-insensitive)
     * @param maxPrice  maximum nightly rate
     * @param checkIn   desired check-in date
     * @param checkOut  desired check-out date
     * @return list of matching properties
     */
    public List<Property> search(String location, String roomType,
                                 Double maxPrice, LocalDate checkIn,
                                 LocalDate checkOut) {

        log.debug("Searching properties — location={}, roomType={}, maxPrice={}, " +
                  "checkIn={}, checkOut={}", location, roomType, maxPrice, checkIn, checkOut);

        // Start with all properties
        List<Property> results = propertyRepository.findAll();

        // Apply each filter if the parameter was provided
        if (location != null && !location.isBlank()) {
            List<Property> byLocation =
                    propertyRepository.findByLocationContainingIgnoreCase(location);
            results.retainAll(byLocation);
        }

        if (roomType != null && !roomType.isBlank()) {
            List<Property> byRoom =
                    propertyRepository.findByRoomTypeIgnoreCase(roomType);
            results.retainAll(byRoom);
        }

        if (maxPrice != null) {
            List<Property> byPrice =
                    propertyRepository.findByPricePerNightLessThanEqual(maxPrice);
            results.retainAll(byPrice);
        }

        if (checkIn != null && checkOut != null) {
            List<Property> byAvailability =
                    propertyRepository
                            .findByAvailableFromLessThanEqualAndAvailableToGreaterThanEqualAndActiveTrue(
                                    checkIn, checkOut);
            results.retainAll(byAvailability);
        }

        log.debug("Search returned {} results", results.size());
        return results;
    }

    // ───────────────────────── Commands ─────────────────────────

    /**
     * Persist a new property to the catalog.
     */
    public Property create(Property property) {
        property.setId(null); // Ensure MongoDB generates a fresh ObjectId
        log.info("Creating new property: {}", property.getName());
        return propertyRepository.save(property);
    }

    /**
     * Update an existing property. Returns empty if not found.
     */
    public Optional<Property> update(String id, Property updated) {
        return propertyRepository.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setDescription(updated.getDescription());
                    existing.setLocation(updated.getLocation());
                    existing.setRoomType(updated.getRoomType());
                    existing.setAmenities(updated.getAmenities());
                    existing.setImageUrls(updated.getImageUrls());
                    existing.setPricePerNight(updated.getPricePerNight());
                    existing.setAvailableFrom(updated.getAvailableFrom());
                    existing.setAvailableTo(updated.getAvailableTo());
                    existing.setMaxGuests(updated.getMaxGuests());
                    existing.setActive(updated.getActive());
                    log.info("Updating property id={}", id);
                    return propertyRepository.save(existing);
                });
    }

    /**
     * Delete a property by its ID.
     */
    public void delete(String id) {
        log.info("Deleting property id={}", id);
        propertyRepository.deleteById(id);
    }
}
