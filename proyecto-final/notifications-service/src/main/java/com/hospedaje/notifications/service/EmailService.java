package com.hospedaje.notifications.service;

import com.hospedaje.notifications.dto.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service for sending transactional emails.
 * <p>
 * Supports two modes:
 * <ul>
 *   <li><strong>Plain text</strong>: for registration confirmations, notifications</li>
 *   <li><strong>With attachment</strong>: for sending PDF invoices</li>
 * </ul>
 *
 * ⚠️ SMTP credentials must be configured in {@code application.yml}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /** Default "from" address, configured in application.yml */
    @Value("${app.mail.from}")
    private String fromAddress;

    // ───────────────── Plain Text Email ─────────────────────

    /**
     * Send a plain-text email.
     * <p>
     * Use cases: user registration confirmation, booking status updates,
     * password reset notifications.
     *
     * @param request the email details (to, subject, body)
     */
    public void sendPlainTextEmail(EmailRequest request) {
        log.info("Sending plain-text email to={}, subject='{}'",
                request.getTo(), request.getSubject());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(request.getBody());

        mailSender.send(message);
        log.info("Email sent successfully to {}", request.getTo());
    }

    // ───────────────── Email with PDF Attachment ────────────

    /**
     * Send an email with a PDF file attached.
     * <p>
     * Primarily used for sending generated invoices to customers
     * after a booking is confirmed or paid.
     *
     * @param to              recipient email address
     * @param subject         email subject line
     * @param body            email body text
     * @param pdfBytes        the PDF content as a byte array
     * @param attachmentName  filename for the attachment (e.g., "invoice.pdf")
     * @throws MessagingException if the email cannot be composed
     */
    public void sendEmailWithPdfAttachment(String to, String subject,
                                            String body, byte[] pdfBytes,
                                            String attachmentName)
            throws MessagingException {

        log.info("Sending email with PDF attachment to={}, subject='{}', file='{}'",
                to, subject, attachmentName);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);

        // Attach the PDF
        helper.addAttachment(attachmentName,
                new ByteArrayResource(pdfBytes),
                "application/pdf");

        mailSender.send(mimeMessage);
        log.info("Email with attachment sent successfully to {}", to);
    }
}
