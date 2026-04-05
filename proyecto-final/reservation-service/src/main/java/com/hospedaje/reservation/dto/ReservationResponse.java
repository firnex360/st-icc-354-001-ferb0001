package com.hospedaje.reservation.dto;

import com.hospedaje.reservation.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Outbound DTO returned to the client after a reservation operation.
 * <p>
 * Includes the calculated total price and the property name
 * (fetched from the Catalog Service) for a richer response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long id;
    private String customerId;
    private String propertyId;
    private String propertyName;        // Enriched from Catalog Service
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String assignedRoom;
    private Integer numberOfGuests;
    private Double totalPrice;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
