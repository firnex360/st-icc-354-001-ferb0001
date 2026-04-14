package com.hospedaje.reservation.service;

import com.hospedaje.reservation.client.CatalogClient;
import com.hospedaje.reservation.client.NotificationClient;
import com.hospedaje.reservation.dto.PropertyDto;
import com.hospedaje.reservation.dto.ReservationRequest;
import com.hospedaje.reservation.dto.ReservationResponse;
import com.hospedaje.reservation.exception.PropertyNotFoundException;
import com.hospedaje.reservation.model.Reservation;
import com.hospedaje.reservation.model.ReservationStatus;
import com.hospedaje.reservation.repository.ReservationRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business-logic layer for managing reservations.
 * <p>
 * Coordinates with the Catalog Service (via Feign) to validate
 * property existence and retrieve pricing before persisting
 * reservations to PostgreSQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CatalogClient catalogClient;
    private final NotificationClient notificationClient;

    // ───────────────────────── Queries ──────────────────────────

    /**
     * Retrieve all reservations in the system.
     */
    public List<ReservationResponse> findAll() {
        log.debug("Fetching all reservations");
        return reservationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieve a single reservation by its ID.
     */
    public Optional<ReservationResponse> findById(Long id) {
        log.debug("Looking up reservation id={}", id);
        return reservationRepository.findById(id)
                .map(this::toResponse);
    }

    /**
     * Retrieve all reservations for a specific customer.
     */
    public List<ReservationResponse> findByCustomerId(String customerId) {
        log.debug("Fetching reservations for customerId={}", customerId);
        return reservationRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieve statistics for the admin dashboard.
     */
    public Map<String, Object> getDashboardStats() {
        log.debug("Calculating dashboard stats");
        List<Reservation> all = reservationRepository.findAll();
        long total = all.size();

        long pendingCount = all.stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .count();
        long completedCount = all.stream()
                .filter(r -> r.getStatus() == ReservationStatus.PAID || r.getStatus() == ReservationStatus.CONFIRMED)
                .count();
        long cancelledCount = all.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", pendingCount);
        stats.put("completedCount", completedCount);
        stats.put("cancelledCount", cancelledCount);

        stats.put("pendingPercentage", total == 0 ? 0 : Math.round((pendingCount * 100.0) / total));
        stats.put("completedPercentage", total == 0 ? 0 : Math.round((completedCount * 100.0) / total));
        stats.put("cancelledPercentage", total == 0 ? 0 : Math.round((cancelledCount * 100.0) / total));

        return stats;
    }

    // ───────────────────────── Commands ─────────────────────────

    /**
     * Create a new reservation.
     * <p>
     * Steps:
     * <ol>
     *   <li>Call Catalog Service to verify the property exists</li>
     *   <li>Retrieve the nightly rate from the property</li>
     *   <li>Calculate the total price (nights × rate)</li>
     *   <li>Persist the reservation with status PENDING</li>
     * </ol>
     *
     * @param request the reservation details from the client
     * @return the created reservation with calculated total price
     * @throws PropertyNotFoundException if the property doesn't exist in the catalog
     */
    public ReservationResponse create(ReservationRequest request) {
        log.info("Creating reservation — customer={}, property={}",
                request.getCustomerId(), request.getPropertyId());

        // Validate dates
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException(
                    "Check-out date must be after check-in date");
        }

        // ── Step 1: Verify property exists via Feign ──
        PropertyDto property = fetchProperty(request.getPropertyId());

        // ── Step 2: Calculate total price ──
        long numberOfNights = ChronoUnit.DAYS.between(
                request.getCheckInDate(), request.getCheckOutDate());
        double totalPrice = property.getPricePerNight() * numberOfNights;

        log.debug("Calculated {} nights × ${}/night = ${}",
                numberOfNights, property.getPricePerNight(), totalPrice);

        // ── Step 3: Build and save the reservation ──
        Reservation reservation = Reservation.builder()
                .customerId(request.getCustomerId())
                .propertyId(request.getPropertyId())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .assignedRoom(request.getAssignedRoom())
                .numberOfGuests(request.getNumberOfGuests())
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created with id={}, totalPrice={}",
                saved.getId(), saved.getTotalPrice());

        return toResponseWithProperty(saved, property);
    }

    /**
     * Update an existing reservation's details.
     * Returns empty if the reservation ID doesn't exist.
     */
    public Optional<ReservationResponse> update(Long id, ReservationRequest request) {
        log.info("Updating reservation id={}", id);

        return reservationRepository.findById(id)
                .map(existing -> {
                    // Verify the (possibly new) property exists
                    PropertyDto property = fetchProperty(request.getPropertyId());

                    // Recalculate price
                    long nights = ChronoUnit.DAYS.between(
                            request.getCheckInDate(), request.getCheckOutDate());
                    double totalPrice = property.getPricePerNight() * nights;

                    existing.setCustomerId(request.getCustomerId());
                    existing.setPropertyId(request.getPropertyId());
                    existing.setCheckInDate(request.getCheckInDate());
                    existing.setCheckOutDate(request.getCheckOutDate());
                    existing.setAssignedRoom(request.getAssignedRoom());
                    existing.setNumberOfGuests(request.getNumberOfGuests());
                    existing.setTotalPrice(totalPrice);

                    Reservation saved = reservationRepository.save(existing);
                    return toResponseWithProperty(saved, property);
                });
    }

    /**
     * Cancel a reservation by setting its status to CANCELLED.
     * Only reservations in PENDING or CONFIRMED status can be cancelled.
     */
    public Optional<ReservationResponse> cancel(Long id) {
        log.info("Cancelling reservation id={}", id);

        return reservationRepository.findById(id)
                .map(reservation -> {
                    if (reservation.getStatus() == ReservationStatus.PAID) {
                        throw new IllegalArgumentException(
                                "Cannot cancel a reservation that has already been paid. " +
                                "Contact support for refunds.");
                    }
                    if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                        throw new IllegalArgumentException(
                                "Reservation is already cancelled.");
                    }

                    reservation.setStatus(ReservationStatus.CANCELLED);
                    Reservation saved = reservationRepository.save(reservation);
                    return toResponse(saved);
                });
    }

    /**
     * Confirm payment for a reservation.
     * Sets status to PAID, then sends a JasperReports PDF invoice
     * to the customer's email via the Notifications Service.
     * The email call is non-fatal — a notification failure never rolls back the payment.
     */
    public Optional<ReservationResponse> pay(Long id) {
        log.info("Confirming payment for reservation id={}", id);

        return reservationRepository.findById(id).map(reservation -> {
            if (reservation.getStatus() == ReservationStatus.PAID) {
                throw new IllegalArgumentException("Reservation is already paid.");
            }
            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                throw new IllegalArgumentException("Cannot pay a cancelled reservation.");
            }

            reservation.setStatus(ReservationStatus.PAID);
            Reservation saved = reservationRepository.save(reservation);
            log.info("Reservation id={} marked as PAID", saved.getId());

            // Send invoice email via Notifications Service
            try {
                PropertyDto property = catalogClient.getPropertyById(saved.getPropertyId());
                long nights = ChronoUnit.DAYS.between(saved.getCheckInDate(), saved.getCheckOutDate());
                double pricePerNight = property.getPricePerNight();
                double subtotal  = pricePerNight * nights;
                double taxRate   = 18.0;
                double taxAmount = subtotal * taxRate / 100.0;
                double total     = subtotal + taxAmount;

                // customerId is stored as the customer's email
                String customerEmail = saved.getCustomerId();
                String customerName  = customerEmail.contains("@")
                        ? customerEmail.substring(0, customerEmail.indexOf('@'))
                        : customerEmail;

                Map<String, Object> invoice = new HashMap<>();
                invoice.put("customerName",    customerName);
                invoice.put("customerEmail",   customerEmail);
                invoice.put("propertyName",    property.getName());
                invoice.put("checkInDate",     saved.getCheckInDate().toString());
                invoice.put("checkOutDate",    saved.getCheckOutDate().toString());
                invoice.put("numberOfNights",  (int) nights);
                invoice.put("pricePerNight",   pricePerNight);
                invoice.put("subtotal",        subtotal);
                invoice.put("taxRate",         taxRate);
                invoice.put("taxAmount",       taxAmount);
                invoice.put("total",           total);

                notificationClient.sendInvoice(invoice);
                log.info("Invoice email sent to {}", customerEmail);
            } catch (Exception e) {
                log.warn("Could not send invoice for reservation id={}: {}", saved.getId(), e.getMessage());
            }

            return toResponse(saved);
        });
    }

    /**
     * Permanently delete a reservation by its ID.
     */
    public void delete(Long id) {
        log.info("Deleting reservation id={}", id);
        reservationRepository.deleteById(id);
    }

    // ───────────────────── Internal Helpers ─────────────────────

    /**
     * Calls the Catalog Service to fetch a property.
     * Throws {@link PropertyNotFoundException} if the property doesn't exist.
     */
    private PropertyDto fetchProperty(String propertyId) {
        try {
            PropertyDto property = catalogClient.getPropertyById(propertyId);
            if (property == null) {
                throw new PropertyNotFoundException(propertyId);
            }
            return property;
        } catch (FeignException.NotFound ex) {
            throw new PropertyNotFoundException(propertyId);
        }
    }

    /**
     * Maps a Reservation entity to a ReservationResponse DTO.
     * Property name is left null (lightweight response for listing).
     */
    private ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .customerId(reservation.getCustomerId())
                .propertyId(reservation.getPropertyId())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .assignedRoom(reservation.getAssignedRoom())
                .numberOfGuests(reservation.getNumberOfGuests())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    /**
     * Maps a Reservation entity to a ReservationResponse DTO,
     * enriched with the property name from the Catalog Service.
     */
    private ReservationResponse toResponseWithProperty(Reservation reservation,
                                                        PropertyDto property) {
        ReservationResponse response = toResponse(reservation);
        response.setPropertyName(property.getName());
        return response;
    }
}
