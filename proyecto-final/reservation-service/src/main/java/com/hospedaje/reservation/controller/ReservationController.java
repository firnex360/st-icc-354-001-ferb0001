package com.hospedaje.reservation.controller;

import com.hospedaje.reservation.dto.ReservationRequest;
import com.hospedaje.reservation.dto.ReservationResponse;
import com.hospedaje.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the Reservation Service.
 * <p>
 * Exposes endpoints for CRUD operations on reservations.
 * All endpoints are prefixed with {@code /api/reservations}.
 */
@Slf4j
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // ───────────────────────── LIST ALL ─────────────────────────

    /**
     * GET /api/reservations
     * Retrieve every reservation in the system.
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        log.debug("GET /api/reservations — fetching all");
        return ResponseEntity.ok(reservationService.findAll());
    }

    // ───────────────────────── DASHBOARD STATS ─────────────────────────

    /**
     * GET /api/reservations/stats
     * Retrieve statistics for the admin dashboard.
     */
    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getDashboardStats() {
        log.debug("GET /api/reservations/stats — fetching dashboard stats");
        return ResponseEntity.ok(reservationService.getDashboardStats());
    }

    // ───────────────────────── GET BY ID ────────────────────────

    /**
     * GET /api/reservations/{id}
     * Retrieve a single reservation by its primary key.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        log.debug("GET /api/reservations/{}", id);
        return reservationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ───────────────────── GET BY CUSTOMER ──────────────────────

    /**
     * GET /api/reservations/customer/{customerId}
     * Retrieve all reservations belonging to a specific customer.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByCustomer(
            @PathVariable String customerId) {
        log.debug("GET /api/reservations/customer/{}", customerId);
        return ResponseEntity.ok(reservationService.findByCustomerId(customerId));
    }

    // ───────────────────────── CREATE ──────────────────────────

    /**
     * POST /api/reservations
     * Create a new reservation.
     * <p>
     * The service validates the property via the Catalog Service (Feign)
     * and calculates the total price automatically.
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        log.debug("POST /api/reservations — customer={}, property={}",
                request.getCustomerId(), request.getPropertyId());
        ReservationResponse created = reservationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ───────────────────────── UPDATE ──────────────────────────

    /**
     * PUT /api/reservations/{id}
     * Update an existing reservation's details.
     * Returns 404 if the reservation ID doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request) {
        log.debug("PUT /api/reservations/{}", id);
        return reservationService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ───────────────────────── CANCEL ──────────────────────────

    /**
     * PUT /api/reservations/{id}/cancel
     * Cancel a reservation. Only PENDING and CONFIRMED reservations
     * can be cancelled.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        log.debug("PUT /api/reservations/{}/cancel", id);
        return reservationService.cancel(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ───────────────────────── PAY ─────────────────────────

    /**
     * PUT /api/reservations/{id}/pay
     * Confirm payment for a reservation: sets status to PAID and
     * triggers the invoice email via the Notifications Service.
     */
    @PutMapping("/{id}/pay")
    public ResponseEntity<ReservationResponse> payReservation(@PathVariable Long id) {
        log.debug("PUT /api/reservations/{}/pay", id);
        return reservationService.pay(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ───────────────────────── DELETE ──────────────────────────

    /**
     * DELETE /api/reservations/{id}
     * Permanently remove a reservation from the system.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        log.debug("DELETE /api/reservations/{}", id);
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
