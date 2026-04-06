package com.hospedaje.notifications.service;

import com.hospedaje.notifications.dto.InvoiceRequest;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating PDF invoices using JasperReports.
 * <p>
 * Compiles the {@code invoice.jrxml} template located at
 * {@code src/main/resources/reports/invoice.jrxml}, fills it
 * with the provided parameters, and exports the result as a
 * PDF byte array.
 * <p>
 * The generated PDF can be:
 * <ul>
 *   <li>Returned directly as a downloadable HTTP response</li>
 *   <li>Sent as an email attachment via {@link EmailService}</li>
 * </ul>
 */
@Slf4j
@Service
public class InvoiceReportService {

    /**
     * Path to the JRXML template inside the classpath.
     * <p>
     * ⚠️ The file must be placed at:
     * {@code src/main/resources/reports/invoice.jrxml}
     */
    private static final String TEMPLATE_PATH = "/reports/invoice.jrxml";

    /**
     * Generate a PDF invoice from the given request parameters.
     * <p>
     * Steps:
     * <ol>
     *   <li>Load the JRXML template from the classpath</li>
     *   <li>Compile the template into a JasperReport object</li>
     *   <li>Map the request fields to JasperReports parameters</li>
     *   <li>Fill the report (no datasource — parameter-only report)</li>
     *   <li>Export the filled report to a PDF byte array</li>
     * </ol>
     *
     * @param request the invoice parameters (customer, property, costs)
     * @return the generated PDF as a byte array
     * @throws JRException if template compilation or export fails
     */
    public byte[] generateInvoicePdf(InvoiceRequest request) throws JRException {
        log.info("Generating invoice PDF — customer='{}', property='{}'",
                request.getCustomerName(), request.getPropertyName());

        // ── Step 1: Load the JRXML template from classpath ──
        InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH);
        if (templateStream == null) {
            throw new IllegalStateException(
                    "Invoice template not found at classpath:" + TEMPLATE_PATH +
                    ". Ensure the file exists at src/main/resources/reports/invoice.jrxml");
        }

        // ── Step 2: Compile the JRXML into a JasperReport ──
        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
        log.debug("JRXML template compiled successfully");

        // ── Step 3: Map request fields to report parameters ──
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("customerName",  request.getCustomerName());
        parameters.put("propertyName",  request.getPropertyName());
        parameters.put("checkInDate",   request.getCheckInDate());
        parameters.put("checkOutDate",  request.getCheckOutDate());
        parameters.put("numberOfNights", request.getNumberOfNights());
        parameters.put("pricePerNight", request.getPricePerNight());
        parameters.put("subtotal",      request.getSubtotal());
        parameters.put("taxRate",       request.getTaxRate());
        parameters.put("taxAmount",     request.getTaxAmount());
        parameters.put("total",         request.getTotal());

        // ── Step 4: Fill the report (no data source — params only) ──
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, new JREmptyDataSource());
        log.debug("Report filled with parameters");

        // ── Step 5: Export to PDF byte array ──
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
        log.info("Invoice PDF generated — {} bytes", pdfBytes.length);

        return pdfBytes;
    }
}
