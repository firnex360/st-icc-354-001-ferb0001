package com.hospedaje.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound DTO for generating a PDF invoice via JasperReports.
 * <p>
 * Contains all the parameters needed to populate the invoice
 * template: customer info, property info, dates, and costs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {

    /** Customer's full name for the invoice header. */
    @NotBlank(message = "Customer name is required")
    private String customerName;

    /** Customer's email (for sending the invoice as attachment). */
    private String customerEmail;

    /** Name of the booked property. */
    @NotBlank(message = "Property name is required")
    private String propertyName;

    /** Check-in date as a formatted string (e.g., "2026-05-01"). */
    @NotBlank(message = "Check-in date is required")
    private String checkInDate;

    /** Check-out date as a formatted string (e.g., "2026-05-04"). */
    @NotBlank(message = "Check-out date is required")
    private String checkOutDate;

    /** Number of nights for the stay. */
    @NotNull(message = "Number of nights is required")
    @Positive(message = "Number of nights must be positive")
    private Integer numberOfNights;

    /** Nightly rate charged for the property. */
    @NotNull(message = "Price per night is required")
    @Positive(message = "Price per night must be positive")
    private Double pricePerNight;

    /** Subtotal before taxes (nights × rate). */
    @NotNull(message = "Subtotal is required")
    private Double subtotal;

    /** Tax rate as a percentage (e.g., 18.0 for 18%). */
    @NotNull(message = "Tax rate is required")
    private Double taxRate;

    /** Tax amount in currency. */
    @NotNull(message = "Tax amount is required")
    private Double taxAmount;

    /** Grand total including taxes. */
    @NotNull(message = "Total is required")
    private Double total;
}
