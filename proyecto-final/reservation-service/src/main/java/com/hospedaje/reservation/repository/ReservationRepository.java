package com.hospedaje.reservation.repository;

import com.hospedaje.reservation.model.Reservation;
import com.hospedaje.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for {@link Reservation} entities.
 * <p>
 * Provides built-in CRUD operations plus custom query methods
 * for looking up reservations by customer and status.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find all reservations belonging to a specific customer.
     */
    List<Reservation> findByCustomerId(String customerId);

    /**
     * Find all reservations with a given status.
     */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Find reservations for a specific property.
     */
    List<Reservation> findByPropertyId(String propertyId);
}
