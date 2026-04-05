package com.hospedaje.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Inbound DTO for creating or updating a reservation.
 * <p>
 * Contains all the information the client must provide when
 * making a booking. The total price is calculated server-side
 * after fetching the property's nightly rate from the Catalog Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    /** Customer placing the reservation. */
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    /** Property to book (MongoDB ObjectId from the Catalog Service). */
    @NotBlank(message = "Property ID is required")
    private String propertyId;

    /** Desired check-in date. */
    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    /** Desired check-out date. */
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    /** Requested room assignment (optional — may be auto-assigned). */
    private String assignedRoom;

    /** Number of guests for this reservation. */
    @Min(value = 1, message = "At least one guest is required")
    private Integer numberOfGuests;
}
