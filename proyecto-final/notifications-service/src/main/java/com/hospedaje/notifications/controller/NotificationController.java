package com.hospedaje.notifications.controller;

import com.hospedaje.notifications.dto.EmailRequest;
import com.hospedaje.notifications.dto.InvoiceRequest;
import com.hospedaje.notifications.service.EmailService;
import com.hospedaje.notifications.service.InvoiceReportService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for the Notifications & Reports Service.
 * <p>
 * Provides endpoints that other microservices (Auth, Reservation)
 * can call to trigger emails and generate PDF invoices.
 * All endpoints are prefixed with {@code /api/notifications}.
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;
    private final InvoiceReportService invoiceReportService;

    // ─────────────── SEND PLAIN TEXT EMAIL ──────────────────

    /**
     * POST /api/notifications/email
     * Send a plain-text email (e.g., registration confirmation).
     * <p>
     * Called by the Auth Service after user registration, or by
     * the Reservation Service for booking status updates.
     */
    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> sendEmail(
            @Valid @RequestBody EmailRequest request) {
        log.debug("POST /api/notifications/email — to={}", request.getTo());

        emailService.sendPlainTextEmail(request);

        Map<String, String> response = new HashMap<>();
        response.put("status", "sent");
        response.put("message", "Email sent successfully to " + request.getTo());
        return ResponseEntity.ok(response);
    }

    // ─────────────── GENERATE PDF INVOICE ──────────────────

    /**
     * POST /api/notifications/invoice/pdf
     * Generate a PDF invoice and return it as a downloadable file.
     * <p>
     * The caller provides all invoice parameters (customer, property,
     * dates, cost breakdown) and receives the PDF as a byte stream.
     */
    @PostMapping("/invoice/pdf")
    public ResponseEntity<byte[]> generateInvoicePdf(
            @Valid @RequestBody InvoiceRequest request) throws JRException {
        log.debug("POST /api/notifications/invoice/pdf — customer='{}'",
                request.getCustomerName());

        byte[] pdfBytes = invoiceReportService.generateInvoicePdf(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice.pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // ───────── GENERATE AND SEND INVOICE VIA EMAIL ─────────

    /**
     * POST /api/notifications/invoice/send
     * Generate a PDF invoice and send it as an email attachment.
     * <p>
     * Combines invoice generation and email sending in a single call.
     * Requires the {@code customerEmail} field in the request.
     */
    @PostMapping("/invoice/send")
    public ResponseEntity<Map<String, String>> generateAndSendInvoice(
            @Valid @RequestBody InvoiceRequest request)
            throws JRException, MessagingException {

        log.debug("POST /api/notifications/invoice/send — customer='{}', email='{}'",
                request.getCustomerName(), request.getCustomerEmail());

        if (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "customerEmail is required to send the invoice");
            return ResponseEntity.badRequest().body(error);
        }

        // Generate the PDF
        byte[] pdfBytes = invoiceReportService.generateInvoicePdf(request);

        // Send it as an email attachment
        String subject = "Invoice — " + request.getPropertyName() +
                          " (" + request.getCheckInDate() + " to " +
                          request.getCheckOutDate() + ")";

        String body = "Dear " + request.getCustomerName() + ",\n\n" +
                       "Thank you for your reservation at " + request.getPropertyName() + ".\n" +
                       "Please find your invoice attached.\n\n" +
                       "Check-in:  " + request.getCheckInDate() + "\n" +
                       "Check-out: " + request.getCheckOutDate() + "\n" +
                       "Total:     $" + String.format("%.2f", request.getTotal()) + "\n\n" +
                       "Best regards,\n" +
                       "Hotel Accommodation Platform";

        emailService.sendEmailWithPdfAttachment(
                request.getCustomerEmail(), subject, body,
                pdfBytes, "invoice.pdf");

        Map<String, String> response = new HashMap<>();
        response.put("status", "sent");
        response.put("message", "Invoice generated and emailed to " +
                     request.getCustomerEmail());
        return ResponseEntity.ok(response);
    }

    // ───────────────── EXCEPTION HANDLERS ──────────────────

    @ExceptionHandler(JRException.class)
    public ResponseEntity<Map<String, String>> handleJasperException(JRException ex) {
        log.error("JasperReports error: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", "Failed to generate invoice: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<Map<String, String>> handleMailException(MessagingException ex) {
        log.error("Email sending error: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", "Failed to send email: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
