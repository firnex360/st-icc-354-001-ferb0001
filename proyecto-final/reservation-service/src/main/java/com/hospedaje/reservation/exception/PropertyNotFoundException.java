package com.hospedaje.reservation.exception;

/**
 * Thrown when the Catalog Service reports that a property
 * does not exist for the given ID.
 */
public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String propertyId) {
        super("Property not found in catalog with id: " + propertyId);
    }
}
