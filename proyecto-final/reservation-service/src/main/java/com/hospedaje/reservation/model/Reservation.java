package com.hospedaje.reservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a reservation in the system.
 * <p>
 * Stores the booking details: which customer, which property,
 * check-in/out dates, assigned room, total price, and current status.
 * Audit timestamps are managed automatically via JPA lifecycle callbacks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the authenticated user who owns this reservation.
     * Maps to the user ID from the Auth Service.
     */
    @Column(nullable = false)
    private String customerId;

    /**
     * Reference to the property being booked.
     * This is the MongoDB ObjectId from the Catalog Service.
     */
    @Column(nullable = false)
    private String propertyId;

    /** Check-in date for the reservation. */
    @Column(nullable = false)
    private LocalDate checkInDate;

    /** Check-out date for the reservation. */
    @Column(nullable = false)
    private LocalDate checkOutDate;

    /** Room number or identifier assigned to this reservation. */
    private String assignedRoom;

    /** Number of guests for this reservation. */
    private Integer numberOfGuests;

    /**
     * Total price for the entire stay.
     * Calculated as: pricePerNight × number of nights.
     */
    @Column(nullable = false)
    private Double totalPrice;

    /** Current lifecycle status of the reservation. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    /** Timestamp when the reservation was created. */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the reservation was last modified. */
    private LocalDateTime updatedAt;

    // ───────────── JPA Lifecycle Callbacks ──────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
