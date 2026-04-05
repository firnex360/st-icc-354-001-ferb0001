package com.hospedaje.reservation.model;

/**
 * Possible states of a reservation throughout its lifecycle.
 * <p>
 * Typical flow: PENDING → CONFIRMED → PAID → (completed)
 * A reservation can be CANCELLED at any point before PAID.
 */
public enum ReservationStatus {

    /** Reservation created but not yet confirmed by the system. */
    PENDING,

    /** Reservation confirmed — property and dates validated. */
    CONFIRMED,

    /** Payment completed — reservation is fully active. */
    PAID,

    /** Reservation cancelled by the customer or administrator. */
    CANCELLED
}
